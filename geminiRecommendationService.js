/**
 * ============================================================================
 * BEKANSI AI SALES ASSISTANT PLATFORM
 * MULTILINGUAL GEMINI API RECOMMENDATION SERVICE LAYER
 * ============================================================================
 * 
 * Implements a production-grade service to analyze customer leads & history,
 * cross-reference them with the multi-tenant furniture catalog, and generate 
 * personalized furniture recommendations in Amharic, Afaan Oromo, and English.
 * 
 * Strictly complies with the following guidelines:
 * 1. Uses the official enterprise model 'gemini-3.5-flash' for robust text processing.
 * 2. Does NOT hardcode API credentials. Reads from process.env.GEMINI_API_KEY.
 * 3. Incorporates safety bounds: Will never invent product prices or delivery times.
 * 4. Outputs structured, parseable JSON containing recommendation context,
 *    localized customer greeting, tailored product guides, and follow-up qualifiers.
 */

const logger = require('./logger');

// Native Node.js fetch is supported in Node 18+. Fallback to axios/node-fetch if legacy.
const GEMINI_API_KEY = process.env.GEMINI_API_KEY;
const GEMINI_MODEL = process.env.GEMINI_MODEL || 'gemini-3.5-flash';

/**
 * Enterprise Gemini Recommendation Service
 */
const GeminiRecommendationService = {

    /**
     * Generates a personalized high-converting product recommendation bundle.
     * 
     * @param {Object} params
     * @param {Object} params.lead Contextual lead details (budget, source, notes, language preference)
     * @param {Object} params.customer Contextual customer details (name, segment, preferred language)
     * @param {Array<Object>} params.availableProducts Structured product listings with localized JSONB names/specs
     * @param {Array<Object>} params.conversationHistory Recent conversation messages between Bot/Agent and Customer
     * @returns {Promise<Object>} Structured recommendation JSON payload
     */
    generateRecommendations: async ({ lead, customer, availableProducts, conversationHistory = [] }) => {
        if (!GEMINI_API_KEY) {
            logger.error('GeminiRecommendationService Config Error: GEMINI_API_KEY is not defined.');
            throw new Error('GEN_AI_CREDENTIALS_MISSING');
        }

        // Determine target language context (Afaan Oromo, Amharic, or English)
        const targetLocale = (customer.preferred_language || lead.language || 'am').toLowerCase();
        const languageMap = {
            'am': 'Amharic (አማርኛ)',
            'om': 'Afaan Oromo (Oromoo)',
            'en': 'English'
        };
        const targetLanguageName = languageMap[targetLocale] || languageMap['am'];

        // Normalize available catalog items to present to the LLM contextually
        const catalogContext = availableProducts.map(p => ({
            id: p.id,
            sku: p.sku,
            names: p.names, // localized jsonb {"en": "...", "am": "...", "om": "..."}
            price: `${p.price} ${p.currency || 'ETB'}`,
            specifications: p.specifications || {},
            inventory: p.inventory_count,
            is_active: p.is_active
        }));

        // Normalize last 5 conversation messages to preserve intent context
        const recentMessages = conversationHistory.slice(-5).map(m => ({
            sender: m.sender_type,
            text: m.raw_content
        }));

        // Formulate strict enterprise instructions ensuring safety rules
        const systemProps = {
            instructions: `You are the chief AI Sales Design consultant for Bekansi Furniture & Interior Design in Addis Ababa, Ethiopia.
Your task is to analyze the customer lead's metadata, budget constraint, requirements, and communication log, and cross-reference them with Bekansi's active localized product catalog to generate the absolute perfect furniture recommendation.

CRITICAL RULES TO PRESERVE TRUST & SECURE REVENUE:
1. MANDATORY LANGUAGE: Respond entirely inside the requested JSON localized string fields in "${targetLanguageName}".
2. NO INVENTED DISCOUNTS/PRICES: You are strictly forbidden from inventing prices. Only refer to the catalog list price or state transparently that custom design quotations can be configured by a sales engineer.
3. NO INVENTED DELIVERY DATES: Never assure or promise delivery dates (e.g. "it will arrive on Tuesday"). Tell the user delivery takes 7-14 standard working days depending on finish customizations.
4. HONESTY BY DESIGN: If the customer's budget is lower than any catalog match, recommend the closest match and politely explain that Bekansi specializes in custom manufacturing and can adjust materials/sizes to reach their target value.
`,
            task: `Generate a structured recommendation payload for the following context:
Customer Profile:
- Name: ${customer.first_name} ${customer.last_name || ''}
- Target Language: ${targetLanguageName} (${targetLocale})
- Lead Profile: Focus on requirements: "${lead.requirements || 'None provided'}".
- Budgets: ${lead.estimated_budget ? `${lead.estimated_budget} ETB` : 'Flexible (Not specified)'}
- Notes: "${lead.notes || 'No secondary notes'}"

Bekansi Product Catalog Context:
${JSON.stringify(catalogContext, null, 2)}

Recent Conversation Snippets:
${JSON.stringify(recentMessages, null, 2)}

Ensure you return a single JSON object matching the schema below.`
        };

        // Standardized Schema matching HubSpot/Salesforce AI outputs
        const RESPONSE_SCHEMA = {
            type: "OBJECT",
            properties: {
                recommended_products: {
                    type: "ARRAY",
                    description: "Sorted list of products from the catalog matching this lead's direct requirements.",
                    items: {
                        type: "OBJECT",
                        properties: {
                            product_id: { type: "STRING" },
                            sku: { type: "STRING" },
                            recommendation_reason: { 
                                type: "STRING", 
                                description: `Clear explanation of why this matches their space or budget, written in "${targetLanguageName}".`
                            },
                            pitch_points: {
                                type: "ARRAY",
                                items: { type: "STRING" },
                                description: `Highlighting features like material type, space saving, or structural stability, written in "${targetLanguageName}".`
                            }
                        },
                        required: ["product_id", "sku", "recommendation_reason", "pitch_points"]
                    }
                },
                localized_intro_greeting: {
                    type: "STRING",
                    description: `An elegant, high-impact conversational greeting in "${targetLanguageName}" addressing the customer by name, introducing Bekansi's bespoke craftsmanship.`
                },
                sales_pitch_body: {
                    type: "STRING",
                    description: `A cohesive, visually suggestive interior-design consultation message in "${targetLanguageName}". Paint a picture of how these custom selections fit their home and fulfill their requirements.`
                },
                qualification_questions: {
                    type: "ARRAY",
                    items: { type: "STRING" },
                    description: `3 polite qualifying follow-up questions in "${targetLanguageName}" to gather critical parameters (e.g., custom sizes, timber mahogany vs wanza, delivery destination).`
                },
                upsell_ideas: {
                    type: "ARRAY",
                    items: { type: "STRING" },
                    description: `Complementary items (e.g. coffee tables for sofa leads, stools for dining leads) in "${targetLanguageName}".`
                }
            },
            required: [
                "recommended_products",
                "localized_intro_greeting",
                "sales_pitch_body",
                "qualification_questions",
                "upsell_ideas"
            ]
        };

        try {
            // REST call payload built according to Gemini API Guidelines
            const endpoint = `https://generativelanguage.googleapis.com/v1beta/models/${GEMINI_MODEL}:generateContent?key=${GEMINI_API_KEY}`;
            const startTime = Date.now();
            const response = await fetch(endpoint, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    contents: [
                        {
                            role: 'user',
                            parts: [
                                { text: `${systemProps.instructions}\n\n${systemProps.task}` }
                            ]
                        }
                    ],
                    generationConfig: {
                        temperature: 0.15, // Keep recommendations highly accurate & consistent
                        responseMimeType: "application/json",
                        responseSchema: RESPONSE_SCHEMA
                    }
                })
            });

            const latency = Date.now() - startTime;

            if (!response.ok) {
                const errBody = await response.json().catch(() => ({}));
                logger.error('Gemini API Integration HTTP Call Failed', {
                    status_code: response.status,
                    error_payload: errBody,
                    latency_ms: latency,
                    customer_name: `${customer.first_name} ${customer.last_name || ''}`,
                    catalog_size: catalogContext.length
                });
                throw new Error(`GEMINI_API_FAILURE: HTTP_${response.status}`);
            }

            const payloadResult = await response.json();
            
            // Extract the generated JSON text safely
            const textContent = payloadResult?.candidates?.[0]?.content?.parts?.[0]?.text;
            if (!textContent) {
                logger.warn('Gemini API returned an empty prompt completion response content', {
                    latency_ms: latency,
                    customer_name: `${customer.first_name} ${customer.last_name || ''}`
                });
                throw new Error('GEMINI_API_EMPTY_RESPONSE');
            }

            // Return parsed valid JSON matching the exact schema definition
            const recommendationBundle = JSON.parse(textContent);

            // Log successful generation
            logger.info('Personalized product recommendation generated successfully via Gemini', {
                model: GEMINI_MODEL,
                latency_ms: latency,
                customer_name: `${customer.first_name} ${customer.last_name || ''}`,
                recommendations_count: recommendationBundle.recommended_products ? recommendationBundle.recommended_products.length : 0
            });

            return recommendationBundle;

        } catch (error) {
            logger.error('Gemini Recommendation Generation Failure in service layer', {
                customer_name: `${customer.first_name} ${customer.last_name || ''}`,
                error: error.message,
                stack: error.stack
            });
            throw error;
        }
    }
};

module.exports = GeminiRecommendationService;
