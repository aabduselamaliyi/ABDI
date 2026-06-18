/**
 * ============================================================================
 * BEKANSI AI SALES ASSISTANT PLATFORM
 * LEAD ENTITY MODEL DECORATOR
 * ============================================================================
 *
 * Provides logical sanitization, schema verification, and calculation models for
 * the CRM Lead entity structure.
 */

class Lead {
    /**
     * Map a raw DB row to a sanitized domain entity
     */
    static fromRow(row) {
        if (!row) return null;
        return {
            id: row.id,
            organizationId: row.organization_id,
            customerId: row.customer_id,
            assignedAgentId: row.assigned_agent_id,
            status: row.status,
            source: row.source,
            leadScore: parseInt(row.lead_score, 10) || 0,
            requirements: row.requirements || '',
            estimatedBudget: parseFloat(row.estimated_budget) || 0.00,
            notes: row.notes || '',
            followUpAt: row.follow_up_at,
            createdAt: row.created_at,
            updatedAt: row.updated_at
        };
    }

    /**
     * Classifies temperature based on CRM lead score boundaries
     * - 🔥 Hot Lead: Score >= 75
     * - 🟡 Warm Lead: Score between 40 and 74
     * - ⚪ Cold Lead: Score < 40
     */
    static getLeadStatus(score) {
        if (score >= 75) return 'HOT';
        if (score >= 40) return 'WARM';
        return 'COLD';
    }
}

module.exports = Lead;
