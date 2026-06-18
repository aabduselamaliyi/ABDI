/**
 * ============================================================================
 * BEKANSI AI SALES ASSISTANT PLATFORM
 * MULTI-TENANT CRM LEADS PIPELINE ROUTER
 * ============================================================================
 *
 * Implements a secure CRM routing container for lead pipeline management.
 * Leverages row-level security scopes to protect organization domains.
 */

const express = require('express');
const logger = require('../logger');
const { tenantAuthenticator, injectScopedDbClient } = require('../tenantMiddleware');
const dbPool = require('../config/dbPool');

const router = express.Router();

// Apply security and connection context middlewares
router.use(tenantAuthenticator);
router.use(injectScopedDbClient(dbPool));

/**
 * Route: GET /api/v1/leads
 * Description: List leads using advanced filtration and tenant isolation.
 */
router.get('/', async (req, res) => {
    try {
        const { status, source, agent_id, limit = 50, page = 1 } = req.query;
        const queryLimit = parseInt(limit, 10);
        const offset = (parseInt(page, 10) - 1) * queryLimit;

        const result = await req.dbExecute(async (txClient) => {
            let sqlQuery = `
                SELECT 
                    l.id,
                    l.status,
                    l.source,
                    l.lead_score,
                    l.requirements,
                    l.estimated_budget,
                    l.notes,
                    l.follow_up_at,
                    l.created_at,
                    c.id AS customer_id,
                    c.first_name AS customer_first_name,
                    c.last_name AS customer_last_name,
                    c.phone_number AS customer_phone,
                    c.preferred_language AS customer_lang,
                    u.id AS agent_id,
                    u.first_name AS agent_first_name,
                    u.last_name AS agent_last_name
                FROM leads l
                JOIN customers c ON l.customer_id = c.id
                LEFT JOIN users u ON l.assigned_agent_id = u.id
                WHERE 1=1
            `;
            const queryParams = [];
            let paramIndex = 1;

            if (status) {
                sqlQuery += ` AND l.status = $${paramIndex}::lead_status`;
                queryParams.push(status);
                paramIndex++;
            }

            if (source) {
                sqlQuery += ` AND l.source = $${paramIndex}`;
                queryParams.push(source);
                paramIndex++;
            }

            if (agent_id) {
                sqlQuery += ` AND l.assigned_agent_id = $${paramIndex}::UUID`;
                queryParams.push(agent_id);
                paramIndex++;
            }

            // Estimate total record count
            const countQuery = `SELECT COUNT(*) FROM (${sqlQuery}) AS count_sub`;
            const { rows: countRows } = await txClient.query(countQuery, queryParams);
            const totalCount = parseInt(countRows[0].count, 10);

            // Add pagination and ordering
            sqlQuery += ` ORDER BY l.created_at DESC LIMIT $${paramIndex} OFFSET $${paramIndex + 1}`;
            queryParams.push(queryLimit, offset);

            const { rows: leads } = await txClient.query(sqlQuery, queryParams);

            return {
                leads,
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
            data: result.leads,
            pagination: result.pagination
        });

    } catch (error) {
        logger.error('Failed to query multi-tenant CRM leads pipeline', {
            tenant_id: req.tenantId,
            error: error.message,
            stack: error.stack
        });
        return res.status(500).json({
            success: false,
            error: 'ServerDatabaseError',
            message: 'Query failed during leads listing.'
        });
    }
});

/**
 * Route: GET /api/v1/leads/:id
 * Description: View specific lead dashboard card details.
 */
router.get('/:id', async (req, res) => {
    try {
        const { id } = req.params;

        const lead = await req.dbExecute(async (txClient) => {
            const { rows } = await txClient.query(`
                SELECT 
                    l.*,
                    c.first_name AS customer_first_name,
                    c.last_name AS customer_last_name,
                    c.phone_number AS customer_phone,
                    c.email AS customer_email,
                    c.telegram_id,
                    c.facebook_psid,
                    c.preferred_language,
                    u.first_name AS agent_first_name,
                    u.last_name AS agent_last_name
                FROM leads l
                JOIN customers c ON l.customer_id = c.id
                LEFT JOIN users u ON l.assigned_agent_id = u.id
                WHERE l.id = $1
            `, [id]);
            return rows[0] || null;
        });

        if (!lead) {
            return res.status(404).json({
                success: false,
                error: 'LeadNotFound',
                message: 'Lead does not exist or has been archived outside your organization bounds.'
            });
        }

        return res.json({
            success: true,
            data: lead
        });

    } catch (error) {
        logger.error('Failed to retrieve specific CRM lead detail', {
            tenant_id: req.tenantId,
            lead_id: req.params.id,
            error: error.message
        });
        return res.status(500).json({
            success: false,
            error: 'DatabaseError',
            message: 'DB execution failed during query.'
        });
    }
});

/**
 * Route: POST /api/v1/leads
 * Description: Creates a new lead.
 * Smart Flow: Auto-associates or inserts Customer by phone_number to guarantee single CRM Customer profiles.
 */
router.post('/', async (req, res) => {
    const {
        phone_number,
        first_name,
        last_name,
        email,
        source = 'websocket',
        requirements,
        estimated_budget,
        notes,
        preferred_language = 'am',
        assigned_agent_id
    } = req.body;

    if (!phone_number || !first_name) {
        return res.status(400).json({
            success: false,
            error: 'ValidationError',
            message: 'Customer phone number and starting first name are mandatory attributes for lead generation.'
        });
    }

    try {
        const newLead = await req.dbExecute(async (txClient) => {
            // 1. Locate or upsert customer with identical phone within organization bounds
            const { rows: matchedCust } = await txClient.query(
                'SELECT id FROM customers WHERE phone_number = $1',
                [phone_number.trim()]
            );

            let customerId;

            if (matchedCust.length > 0) {
                customerId = matchedCust[0].id;
                // Update optional parameters if supplied inside lead card
                await txClient.query(`
                    UPDATE customers 
                    SET 
                        first_name = COALESCE($1, first_name),
                        last_name = COALESCE($2, last_name),
                        email = COALESCE($3, email),
                        updated_at = CURRENT_TIMESTAMP
                    WHERE id = $4
                `, [first_name.trim(), last_name ? last_name.trim() : null, email ? email.toLowerCase().trim() : null, customerId]);
            } else {
                // Register a new customer context card
                const { rows: insertedCust } = await txClient.query(`
                    INSERT INTO customers (organization_id, first_name, last_name, email, phone_number, preferred_language)
                    VALUES ($1, $2, $3, $4, $5, $6)
                    RETURNING id
                `, [
                    req.tenantId,
                    first_name.trim(),
                    last_name ? last_name.trim() : null,
                    email ? email.toLowerCase().trim() : null,
                    phone_number.trim(),
                    preferred_language
                ]);
                customerId = insertedCust[0].id;
            }

            // 2. Classify / calculate starting Lead Score contextually
            let computedScore = 50;
            if (estimated_budget && parseFloat(estimated_budget) > 100000) {
                computedScore = 80; // High budget equals high score prioritisation
            }
            if (requirements && requirements.length > 50) {
                computedScore += 10; // Detailed specs indicators hot status
            }

            // 3. Create the Lead card in CRM
            const { rows: leadRows } = await txClient.query(`
                INSERT INTO leads (
                    organization_id, customer_id, source, requirements, 
                    estimated_budget, notes, lead_score, assigned_agent_id, status
                ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, 'new'::lead_status)
                RETURNING *
            `, [
                req.tenantId,
                customerId,
                source,
                requirements || null,
                estimated_budget ? parseFloat(estimated_budget) : null,
                notes || null,
                computedScore,
                assigned_agent_id || null
            ]);

            return leadRows[0];
        });

        logger.info('CRM Lead registered and synchronized successfully', {
            tenant_id: req.tenantId,
            lead_id: newLead.id,
            customer_id: newLead.customer_id
        });

        return res.status(201).json({
            success: true,
            message: 'CRM Lead created successfully.',
            data: newLead
        });

    } catch (error) {
        logger.error('Failed to create new lead in CRM database transactional block', {
            tenant_id: req.tenantId,
            body: req.body,
            error: error.message,
            stack: error.stack
        });
        return res.status(500).json({
            success: false,
            error: 'ServerWriteFailure',
            message: 'Database insertion aborted due to transaction error.'
        });
    }
});

/**
 * Route: PUT /api/v1/leads/:id
 * Description: Modify single lead attributes or shift status pipeline.
 */
router.put('/:id', async (req, res) => {
    const { id } = req.params;
    const { status, requirements, estimated_budget, notes, lead_score, assigned_agent_id, follow_up_at } = req.body;

    try {
        const updatedLead = await req.dbExecute(async (txClient) => {
            // Confirm existence inside organization block
            const { rows: existing } = await txClient.query(
                'SELECT * FROM leads WHERE id = $1',
                [id]
            );

            if (existing.length === 0) {
                throw new Error('LEAD_NOT_FOUND');
            }

            const { rows } = await txClient.query(`
                UPDATE leads
                SET
                    status = COALESCE($1, status),
                    requirements = COALESCE($2, requirements),
                    estimated_budget = COALESCE($3, estimated_budget),
                    notes = COALESCE($4, notes),
                    lead_score = COALESCE($5, lead_score),
                    assigned_agent_id = COALESCE($6, assigned_agent_id),
                    follow_up_at = COALESCE($7, follow_up_at),
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = $8
                RETURNING *
            `, [
                status || null,
                requirements !== undefined ? requirements : null,
                estimated_budget !== undefined ? parseFloat(estimated_budget) : null,
                notes !== undefined ? notes : null,
                lead_score !== undefined ? parseInt(lead_score, 10) : null,
                assigned_agent_id !== undefined ? assigned_agent_id : null,
                follow_up_at !== undefined ? follow_up_at : null,
                id
            ]);

            return rows[0];
        });

        return res.json({
            success: true,
            message: 'Lead updated successfully in CRM.',
            data: updatedLead
        });

    } catch (error) {
        logger.error('Failed to perform update action on CRM Lead card', {
            tenant_id: req.tenantId,
            lead_id: id,
            error: error.message
        });

        if (error.message === 'LEAD_NOT_FOUND') {
            return res.status(404).json({
                success: false,
                error: 'LeadNotFound',
                message: 'The requested lead card was not found inside your authorized organization workspace.'
            });
        }

        return res.status(500).json({
            success: false,
            error: 'ServerDatabaseFailure',
            message: 'Query aborted during update execution.'
        });
    }
});

module.exports = router;
