/**
 * ============================================================================
 * BEKANSI AI SALES ASSISTANT PLATFORM
 * LEADS CONTROLLER - MODULAR DOMAIN LAYER
 * ============================================================================
 *
 * Implements high-level controller methods for handling lead pipeline actions,
 * decoupling routing structures from logical SQL execution.
 */

const logger = require('../logger');

/**
 * Controller class managing the leads collection
 */
class LeadsController {
    /**
     * Retrieves all leads for the scoped tenant
     */
    static async getLeads(req, res) {
        try {
            const { status, limit = 50, page = 1 } = req.query;
            const queryLimit = parseInt(limit, 10);
            const offset = (parseInt(page, 10) - 1) * queryLimit;

            const result = await req.dbExecute(async (txClient) => {
                let sqlQuery = `
                    SELECT l.*, c.first_name, c.last_name, c.phone_number 
                    FROM leads l
                    JOIN customers c ON l.customer_id = c.id
                    WHERE 1=1
                `;
                const params = [];
                let paramIndex = 1;

                if (status) {
                    sqlQuery += ` AND l.status = $${paramIndex}::lead_status`;
                    params.push(status);
                    paramIndex++;
                }

                sqlQuery += ` ORDER BY l.created_at DESC LIMIT $${paramIndex} OFFSET $${paramIndex + 1}`;
                params.push(queryLimit, offset);

                const { rows } = await txClient.query(sqlQuery, params);
                return rows;
            });

            return res.json({
                success: true,
                data: result
            });
        } catch (error) {
            logger.error('Error listing leads via controller', { error: error.message });
            return res.status(500).json({
                success: false,
                error: 'InternalServerError',
                message: 'Failed to query active leads pipeline.'
            });
        }
    }
}

module.exports = LeadsController;
