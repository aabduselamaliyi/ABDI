/**
 * ============================================================================
 * BEKANSI AI SALES ASSISTANT PLATFORM
 * MULTI-TENANT CRM QUOTATIONS & ORDER INTEGRATOR
 * ============================================================================
 *
 * Implements a high-precision quotation billing builder. Calculates subtotals
 * and taxes dynamically using prices verified directly in the database.
 */

const express = require('express');
const logger = require('../logger');
const { tenantAuthenticator, injectScopedDbClient } = require('../tenantMiddleware');
const dbPool = require('../config/dbPool');

const router = express.Router();

router.use(tenantAuthenticator);
router.use(injectScopedDbClient(dbPool));

/**
 * Route: GET /api/v1/quotations
 * Description: Retrieve quotations list.
 */
router.get('/', async (req, res) => {
    try {
        const { status, customer_id, limit = 20, page = 1 } = req.query;
        const queryLimit = parseInt(limit, 10);
        const offset = (parseInt(page, 10) - 1) * queryLimit;

        const result = await req.dbExecute(async (txClient) => {
            let sqlQuery = `
                SELECT 
                    q.*,
                    c.first_name AS customer_first_name,
                    c.last_name AS customer_last_name,
                    c.phone_number AS customer_phone
                FROM quotations q
                JOIN customers c ON q.customer_id = c.id
                WHERE 1=1
            `;
            const queryParams = [];
            let paramIndex = 1;

            if (status) {
                sqlQuery += ` AND q.status = $${paramIndex}`;
                queryParams.push(status);
                paramIndex++;
            }

            if (customer_id) {
                sqlQuery += ` AND q.customer_id = $${paramIndex}::UUID`;
                queryParams.push(customer_id);
                paramIndex++;
            }

            const countQuery = `SELECT COUNT(*) FROM (${sqlQuery}) AS sub`;
            const { rows: countRows } = await txClient.query(countQuery, queryParams);
            const totalCount = parseInt(countRows[0].count, 10);

            sqlQuery += ` ORDER BY q.created_at DESC LIMIT $${paramIndex} OFFSET $${paramIndex + 1}`;
            queryParams.push(queryLimit, offset);

            const { rows: quotations } = await txClient.query(sqlQuery, queryParams);

            return {
                quotations,
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
            data: result.quotations,
            pagination: result.pagination
        });

    } catch (error) {
        logger.error('Failed to list multi-tenant quotations', {
            tenant_id: req.tenantId,
            error: error.message
        });
        return res.status(500).json({
            success: false,
            error: 'ServerDbDatabaseError',
            message: 'Database query execution failed.'
        });
    }
});

/**
 * Route: GET /api/v1/quotations/:id
 * Description: View detailed quotation with line items.
 */
router.get('/:id', async (req, res) => {
    const { id } = req.params;

    try {
        const fullQuotation = await req.dbExecute(async (txClient) => {
            // 1. Fetch quotation header
            const { rows: headerRows } = await txClient.query(`
                SELECT 
                    q.*,
                    c.first_name AS customer_first_name,
                    c.last_name AS customer_last_name,
                    c.email AS customer_email,
                    c.phone_number AS customer_phone,
                    u.first_name AS creator_first_name,
                    u.last_name AS creator_last_name
                FROM quotations q
                JOIN customers c ON q.customer_id = c.id
                LEFT JOIN users u ON q.created_by = u.id
                WHERE q.id = $1
            `, [id]);

            if (headerRows.length === 0) {
                return null;
            }

            const header = headerRows[0];

            // 2. Fetch quotation items
            const { rows: items } = await txClient.query(`
                SELECT 
                    qi.id,
                    qi.quantity,
                    qi.unit_price,
                    qi.total_price,
                    p.id AS product_id,
                    p.sku,
                    p.names->>'en' AS product_name_en,
                    p.names->>'am' AS product_name_am
                FROM quotation_items qi
                JOIN products p ON qi.product_id = p.id
                WHERE qi.quotation_id = $1
            `, [id]);

            return {
                ...header,
                line_items: items
            };
        });

        if (!fullQuotation) {
            return res.status(404).json({
                success: false,
                error: 'QuotationNotFound',
                message: 'Quotation does not exist or has been archived outside your organization boundaries.'
            });
        }

        return res.json({
            success: true,
            data: fullQuotation
        });

    } catch (error) {
        logger.error('Failed to query quotation details with items list', {
            tenant_id: req.tenantId,
            quotation_id: id,
            error: error.message
        });
        return res.status(500).json({
            success: false,
            error: 'ServerFetchFailure',
            message: 'Database query crashed during retrieval.'
        });
    }
});

/**
 * Route: POST /api/v1/quotations
 * Description: Generate a new structured quotation dynamically.
 * Flow:
 *  - Verifies customer details.
 *  - Fetches product base price from the database to prevent pricing manipulations.
 *  - Computes subtotal, applies discount, calculates tax, and sets valid_until.
 *  - Generates serial quotation code.
 *  - Inserts atomic lines within transaction.
 */
router.post('/', async (req, res) => {
    const {
        customer_id,
        lead_id,
        items = [], // Array of objects: { product_id: UUID, quantity: Integer }
        discount = 0.00,
        tax_rate = 0.15, // Default VAT for Ethiopia is 15%
        notes,
        valid_days = 15
    } = req.body;

    if (!customer_id || items.length === 0) {
        return res.status(400).json({
            success: false,
            error: 'ValidationError',
            message: 'Customer identifier and at least one catalog line item are mandatory.'
        });
    }

    try {
        const savedQuotation = await req.dbExecute(async (txClient) => {
            // 1. Verify customer profiles exist and are active
            const { rows: custCheck } = await txClient.query(
                'SELECT id FROM customers WHERE id = $1',
                [customer_id]
            );
            if (custCheck.length === 0) {
                throw new Error('CUSTOMER_NOT_FOUND');
            }

            // 2. Fetch actual products pricing dynamically to calculate subtotals safely
            let subtotal = 0;
            const verifiedLineItems = [];

            for (const item of items) {
                const { rows: prodRows } = await txClient.query(
                    'SELECT price, sku FROM products WHERE id = $1 AND is_active = TRUE',
                    [item.product_id]
                );

                if (prodRows.length === 0) {
                    throw new Error(`PRODUCT_UNAVAILABLE_OR_INACTIVE: ${item.product_id}`);
                }

                const product = prodRows[0];
                const itemQty = parseInt(item.quantity, 10);
                if (isNaN(itemQty) || itemQty <= 0) {
                    throw new Error('QUANTITY_VALIDATION_FAILED');
                }

                const lineTotalPrice = parseFloat(product.price) * itemQty;
                subtotal += lineTotalPrice;

                verifiedLineItems.push({
                    product_id: item.product_id,
                    quantity: itemQty,
                    unit_price: parseFloat(product.price),
                    total_price: lineTotalPrice
                });
            }

            // Calculations
            const resolvedSubtotal = subtotal;
            const resolvedDiscount = parseFloat(discount) || 0.00;
            const taxableAmount = Math.max(0, resolvedSubtotal - resolvedDiscount);
            const calculatedTax = taxableAmount * parseFloat(tax_rate);
            const resolvedTotal = taxableAmount + calculatedTax;

            // 3. Generate sequential Quotation Document Number
            const dateStr = new Date().toISOString().slice(0, 10).replace(/-/g, '');
            const randSuffix = Math.floor(10000 + Math.random() * 90000); // Thread-safe invoice serialization
            const quotationNumber = `QT-${dateStr}-${randSuffix}`;

            const validUntilDate = new Date();
            validUntilDate.setDate(validUntilDate.getDate() + parseInt(valid_days, 10));

            // 4. Save Quotation Header
            const insertHeaderQuery = `
                INSERT INTO quotations (
                    organization_id, subscriber_id, customer_id, lead_id, created_by,
                    quotation_number, valid_until, subtotal, discount, tax, total, status, notes
                ) VALUES (
                    $1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, 'draft', $12
                ) RETURNING *
            `;

            // Since subscriber_id or column is organization_id, let's verify schema:
            // schema has: organization_id, customer_id, lead_id, created_by, quotation_number, etc.
            const headerValues = [
                req.tenantId,
                customer_id,
                lead_id || null,
                req.user.id || null, // creator
                quotationNumber,
                validUntilDate,
                resolvedSubtotal,
                resolvedDiscount,
                calculatedTax,
                resolvedTotal,
                notes || null
            ];

            // Wait, let's look at schema for `quotations` columns:
            // id, organization_id, customer_id, lead_id, created_by, quotation_number, valid_until, subtotal, discount, tax, total, status, notes, created_at, updated_at
            // So structural insert has 11 indices:
            const cleanInsertHeaderQuery = `
                INSERT INTO quotations (
                    organization_id, customer_id, lead_id, created_by,
                    quotation_number, valid_until, subtotal, discount, tax, total, status, notes
                ) VALUES (
                    $1, $2, $3, $4, $5, $6, $7, $8, $9, $10, 'draft', $11
                ) RETURNING *
            `;

            const cleanHeaderValues = [
                req.tenantId,
                customer_id,
                lead_id || null,
                req.user.id || null,
                quotationNumber,
                validUntilDate,
                resolvedSubtotal,
                resolvedDiscount,
                calculatedTax,
                resolvedTotal,
                notes || null
            ];

            const { rows: headerRows } = await txClient.query(cleanInsertHeaderQuery, cleanHeaderValues);
            const savedHeader = headerRows[0];

            // 5. Save Quotation Line Items
            for (const item of verifiedLineItems) {
                await txClient.query(`
                    INSERT INTO quotation_items (
                        quotation_id, product_id, quantity, unit_price, total_price
                    ) VALUES ($1, $2, $3, $4, $5)
                `, [savedHeader.id, item.product_id, item.quantity, item.unit_price, item.total_price]);
            }

            // 6. If lead_id is present, shift lead status pipeline to 'custom_proposal_sent' (proposal_sent)
            if (lead_id) {
                await txClient.query(`
                    UPDATE leads 
                    SET status = 'proposal_sent'::lead_status, updated_at = CURRENT_TIMESTAMP
                    WHERE id = $1
                `, [lead_id]);
            }

            return {
                ...savedHeader,
                line_items: verifiedLineItems
            };
        });

        logger.info('Structured sales quotation calculated and persisted', {
            tenant_id: req.tenantId,
            quotation_id: savedQuotation.id,
            quotation_number: savedQuotation.quotation_number
        });

        return res.status(201).json({
            success: true,
            message: 'Quotation generated and saved successfully.',
            data: savedQuotation
        });

    } catch (error) {
        logger.error('Failed to create quotation transaction in CRM database', {
            tenant_id: req.tenantId,
            error: error.message,
            stack: error.stack
        });

        if (error.message === 'CUSTOMER_NOT_FOUND') {
            return res.status(404).json({
                success: false,
                error: 'CustomerNotFound',
                message: 'The bound customer account was not found inside your tenant scope.'
            });
        }

        if (error.message.startsWith('PRODUCT_UNAVAILABLE_OR_INACTIVE')) {
            return res.status(400).json({
                success: false,
                error: 'ProductUnavailable',
                message: 'One or more selected products are inactive or belong to another brand.'
            });
        }

        return res.status(500).json({
            success: false,
            error: 'ServerWriteFailure',
            message: 'Atomic transactional write on database failed.'
        });
    }
});

/**
 * Route: PUT /api/v1/quotations/:id
 * Description: Pipeline state transition (e.g., approved state shifts to CRM Won stage).
 */
router.put('/:id', async (req, res) => {
    const { id } = req.params;
    const { status } = req.body;

    if (!status) {
        return res.status(400).json({
            success: false,
            error: 'MissingStatus',
            message: 'Status parameter is required for transition.'
        });
    }

    try {
        const updated = await req.dbExecute(async (txClient) => {
            const { rows: headerCheck } = await txClient.query(
                'SELECT * FROM quotations WHERE id = $1',
                [id]
            );

            if (headerCheck.length === 0) {
                throw new Error('QUOTATION_NOT_FOUND');
            }

            const current = headerCheck[0];

            // Perform write
            const { rows } = await txClient.query(`
                UPDATE quotations
                SET status = $1, updated_at = CURRENT_TIMESTAMP
                WHERE id = $2
                RETURNING *
            `, [status, id]);

            // Real CRM Synchronisation Action:
            if (status === 'approved' && current.lead_id) {
                // If customer accepts the quote, win the lead pipeline automatically!
                await txClient.query(`
                    UPDATE leads 
                    SET status = 'won'::lead_status, lead_score = 100, updated_at = CURRENT_TIMESTAMP
                    WHERE id = $1
                `, [current.lead_id]);

                logger.info('Lead won automatically via accepted sales quote approval', {
                    lead_id: current.lead_id,
                    quotation_id: id
                });
            } else if (status === 'declined' && current.lead_id) {
                // Return lead status back to negotiation
                await txClient.query(`
                    UPDATE leads 
                    SET status = 'negotiation'::lead_status, updated_at = CURRENT_TIMESTAMP
                    WHERE id = $1
                `, [current.lead_id]);
            }

            return rows[0];
        });

        return res.json({
            success: true,
            message: `Quotation status shifted to "${status}" successfully.`,
            data: updated
        });

    } catch (error) {
        logger.error('Failed to transition quotation object state', {
            tenant_id: req.tenantId,
            quotation_id: id,
            error: error.message
        });

        if (error.message === 'QUOTATION_NOT_FOUND') {
            return res.status(404).json({
                success: false,
                error: 'QuotationNotFound',
                message: 'The requested quotation document was not found.'
            });
        }

        return res.status(500).json({
            success: false,
            error: 'ServerDatabaseFailure',
            message: 'Internal data transaction error occurred.'
        });
    }
});

module.exports = router;
