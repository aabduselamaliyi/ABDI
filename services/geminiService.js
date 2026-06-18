/**
 * ============================================================================
 * BEKANSI AI SALES ASSISTANT PLATFORM
 * GEMINI COGNITION & RECOMMENDATION SERVICE COUPLER
 * ============================================================================
 *
 * Exposes core AI systems capabilities. Interfaces with our fast REST recommendation
 * engine in geminiRecommendationService.js to analyze conversation threads
 * and match furniture configurations.
 */

const logger = require('../logger');
const GeminiRecommendationService = require('../geminiRecommendationService');

class GeminiService {
    /**
     * Connects with the core REST recommendation engine to provide customer solutions
     */
    static async getSolutionRecommendations(lead, customer, products, history) {
        try {
            logger.info('Forwarding AI recommendation job to the underlying core cognitive engine', {
                lead_id: lead ? lead.id : null,
                customer_id: customer ? customer.id : null,
                available_products_count: products ? products.length : 0
            });

            const result = await GeminiRecommendationService.generateRecommendations({
                lead,
                customer,
                availableProducts: products,
                conversationHistory: history
            });

            return result;
        } catch (error) {
            logger.error('Gemini cognition service handler encountered exception', {
                error: error.message,
                stack: error.stack
            });
            throw error;
        }
    }
}

module.exports = GeminiService;
