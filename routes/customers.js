/**
 * ============================================================================
 * BEKANSI AI SALES ASSISTANT PLATFORM
 * MULTI-TENANT CUSTOMER PROFILE ROUTER
 * ============================================================================
 *
 * Exposes API endpoints for managing customer cards. Integrates with RLS
 * policies to prevent cross-tenant exposure.
 */

const express = require('express');
const logger = require('../logger');
const { tenantAuthenticator, injectScopedDbClient } = require('../tenantMiddleware');
const dbPool = require('../config/dbPool');

const router = express.Router();

// Register global middlewares
router.use(tenantAuthenticator);
router.use(injectScopedDbClient(dbPool));

/**
 * Route: GET /api/v1/customers
 * Description: List and search customers by name, phone numbers, or segment tags.
 */
router.get('/', async (req, res) => {
    try {
        const { search, tag, limit = 50, page = 1 } = req.query;
        const queryLimit = parseInt(limit, 10);
        const offset = (parseInt(page, 10) - 1) * queryLimit;

        const result = await req.dbExecute(async (txClient) => {
            let sqlQuery = `
                SELECT * FROM customers WHERE 1=1
            `;
            const queryParams = [];
            let paramIndex = 1;

            if (search) {
                sqlQuery += ` AND (phone_number ILIKE $${paramIndex} OR first_name ILIKE $${paramIndex} OR last_name ILIKE $${paramIndex} OR email ILIKE $${paramIndex})`;
                queryParams.push(`%${search}%`);
                paramIndex++;
            }

            if (tag) {
                sqlQuery += ` AND $${paramIndex} = ANY(segment_tags)`;
                queryParams.push(tag);
                paramIndex++;
            }

            const countQuery = `SELECT COUNT(*) FROM (${sqlQuery}) AS cnt`;
            const { rows: countRows } = await txClient.query(countQuery, queryParams);
            const totalCount = parseInt(countRows[0].count, 10);

            // Add sorting and pagination limits
            sqlQuery += ` ORDER BY created_at DESC LIMIT $${paramIndex} OFFSET $${paramIndex + 1}`;
            queryParams.push(queryLimit, offset);

            const { rows: customers } = await txClient.query(sqlQuery, queryParams);

            return {
                customers,
                pagination: {
                    total_items: totalCount,
                    current_page: parseInt(page, 10),
                    total_pages: Math.ceil(totalCount / queryLimit),
                    limit: queryLimit
                }
            };
        });

        return res.json({
            success: true,
            data: result.customers,
            pagination: result.pagination
        });

    } catch (error) {
        logger.error('Failed to query multi-tenant customer collection', {
            tenant_id: req.tenantId,
            error: error.message
        });
        return res.status(500).json({
            success: false,
            error: 'ServerDBError',
            message: 'Failed to query database collections.'
        });
    }
});

/**
 * Route: GET /api/v1/customers/:id
 * Description: View comprehensive customer dashboard with relative historical sheets.
 */
router.get('/:id', async (req, res) => {
    const { id } = req.params;

    try {
        const customerProfile = await req.dbExecute(async (txClient) => {
            // 1. Fetch core customer card
            const { rows: profileRows } = await txClient.query(
                'SELECT * FROM customers WHERE id = $1',
                [id]
            );

            if (profileRows.length === 0) {
                return null;
            }

            const customer = profileRows[0];

            // 2. Aggregate leads history
            const { rows: leads } = await txClient.query(
                'SELECT id, status, source, requirements, estimated_budget, created_at FROM leads WHERE customer_id = $1 ORDER BY created_at DESC',
                [id]
            );

            // 3. Aggregate favorite design albums
            const { rows: favorites } = await txClient.query(`
                SELECT cf.id AS favorite_id, da.id AS album_id, da.name, da.album_code, da.price_range_lower, da.price_range_upper
                FROM customer_favorites cf
                JOIN design_albums da ON cf.album_id = da.id
                WHERE cf.customer_id = $1
            `, [id]);

            // 4. Aggregate custom design selections
            const { rows: selections } = await txClient.query(`
                SELECT ds.id, ds.budget, ds.status, ds.requirements, da.name AS album_name
                FROM design_selections ds
                JOIN design_albums da ON ds.album_id = da.id
                WHERE ds.customer_id = $1
                ORDER BY ds.created_at DESC
            `, [id]);

            // 5. Aggregate interactive quotations history
            const { rows: quotations } = await txClient.query(
                'SELECT id, quotation_number, total, status, valid_until, created_at FROM quotations WHERE customer_id = $1 ORDER BY created_at DESC',
                [id]
            );

            return {
                ...customer,
                history: {
                    leads,
                    favorites,
                    selections,
                    quotations
                }
            };
        });

        if (!customerProfile) {
            return res.status(404).json({
                success: false,
                error: 'CustomerNotFound',
                message: 'Customer does not exist inside your active workspace.'
            });
        }

        return res.json({
            success: true,
            data: customerProfile
        });

    } catch (error) {
        logger.error('Failed to query detailed customer timeline profiles', {
            tenant_id: req.tenantId,
            customer_id: id,
            error: error.message,
            stack: error.stack
        });
        return res.status(500).json({
            success: false,
            error: 'ServerFetchFailure',
            message: 'Database query execution crashed.'
        });
    }
});

/**
 * Route: POST /api/v1/customers
 * Description: Register custom profile manually.
 */
router.post('/', async (req, res) => {
    const { first_name, last_name, email, phone_number, preferred_language = 'am', telegram_id, facebook_psid, segment_tags = [] } = req.body;

    if (!first_name || !phone_number) {
        return res.status(400).json({
            success: false,
            error: 'ValidationError',
            message: 'First name and a verified phone number are required attributes.'
        });
    }

    try {
        const createdCustomer = await req.dbExecute(async (txClient) => {
            // Check for uniqueness of phone number within tenant workspace boundary (managed by unique index)
            const { rows: duplicateCheck } = await txClient.query(
                'SELECT id FROM customers WHERE phone_number = $1',
                [phone_number.trim()]
            );

            if (duplicateCheck.length > 0) {
                throw new Error('DUPLICATE_PHONE');
            }

            const insertQuery = `
                INSERT INTO customers (
                    organization_id, first_name, last_name, email, 
                    phone_number, preferred_language, telegram_id, facebook_psid, segment_tags
                ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
                RETURNING *
            `;

            const values = [
                req.tenantId,
                first_name.trim(),
                last_name ? last_name.trim() : null,
                email ? email.toLowerCase().trim() : null,
                phone_number.trim(),
                preferred_language,
                telegram_id || null,
                facebook_psid || null,
                segment_tags
            ];

            const { rows } = await txClient.query(insertQuery, values);
            return rows[0];
        });

        return res.status(201).json({
            success: true,
            message: 'Customer registered successfully.',
            data: createdCustomer
        });

    } catch (error) {
        logger.error('Failed to manually insert customer profile record', {
            tenant_id: req.tenantId,
            phone_number,
            error: error.message
        });

        if (error.message === 'DUPLICATE_PHONE') {
            return res.status(409).json({
                success: false,
                error: 'ConflictMatch',
                message: 'A customer profile is already registered under this phone number.'
            });
        }

        return res.status(500).json({
            success: false,
            error: 'ServerInsertFailure',
            message: 'Failed to commit customer record.'
        });
    }
});

/**
 * Route: PUT /api/v1/customers/:id
 * Description: Update profiles, append segment tags etc.
 */
router.put('/:id', async (req, res) => {
    const { id } = req.params;
    const { first_name, last_name, email, phone_number, preferred_language, telegram_id, facebook_psid, segment_tags } = req.body;

    try {
        const updated = await req.dbExecute(async (txClient) => {
            // Confirm existence inside organization bounds
            const { rows: check } = await txClient.query('SELECT * FROM customers WHERE id = $1', [id]);
            if (check.length === 0) {
                throw new Error('CUSTOMER_NOT_FOUND');
            }

            const current = check[0];

            // If phone number updates, verify duplicate constraints
            if (phone_number && phone_number.trim() !== current.phone_number) {
                const { rows: doubleCheck } = await txClient.query(
                    'SELECT id FROM customers WHERE phone_number = $1 AND id <> $2',
                    [phone_number.trim(), id]
                );
                if (doubleCheck.length > 0) {
                    throw new Error('DUPLICATE_PHONE');
                }
            }

            const updateQuery = `
                UPDATE customers
                SET
                    first_name = COALESCE($1, first_name),
                    last_name = COALESCE($2, last_name),
                    email = COALESCE($3, email),
                    phone_number = COALESCE($4, phone_number),
                    preferred_language = COALESCE($5, preferred_language),
                    telegram_id = COALESCE($6, telegram_id),
                    facebook_psid = COALESCE($7, facebook_psid),
                    segment_tags = COALESCE($8, segment_tags),
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = $9
                RETURNING *
            `;

            const values = [
                first_name ? first_name.trim() : null,
                last_name ? last_name.trim() : null,
                email ? email.toLowerCase().trim() : null,
                phone_number ? phone_number.trim() : null,
                preferred_language || null,
                telegram_id !== undefined ? telegram_id : null,
                facebook_psid !== undefined ? facebook_psid : null,
                segment_tags || null,
                id
            ];

            const { rows } = await txClient.query(updateQuery, values);
            return rows[0];
        });

        return res.json({
            success: true,
            message: 'Customer records synchronized successfully.',
            data: updated
        });

    } catch (error) {
        logger.error('Failed to update customer profile data', {
            tenant_id: req.tenantId,
            customer_id: id,
            error: error.message
        });

        if (error.message === 'CUSTOMER_NOT_FOUND') {
            return res.status(404).json({
                success: false,
                error: 'CustomerNotFound',
                message: 'Customer record does not exist or belongs to another tenant organization.'
            });
        }

        if (error.message === 'DUPLICATE_PHONE') {
            return res.status(409).json({
                success: false,
                error: 'PhoneCollision',
                message: 'This phone number matches another existing customer card.'
            });
        }

        return res.status(500).json({
            success: false,
            error: 'DatabaseFailure',
            message: 'Failed to update record.'
        });
    }
});

module.exports = router;
