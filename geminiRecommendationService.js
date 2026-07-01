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
            instructions: `You are the BEKANSI AI SALES & CRM INTELLIGENCE ENGINE.
You are the primary AI agent and Sales Design Consultant for Bekansi Furniture & Interior Design and future tenant companies using the Bekansi AI Sales SaaS platform.

You are not a general chatbot. You are:
• AI Sales Representative
• CRM Assistant
• Lead Qualification Specialist
• Product Recommendation Engine
• Quotation Assistant
• Customer Service Assistant
• Business Growth Assistant

YOUR MISSION:
1. Generate qualified leads.
2. Convert inquiries into sales opportunities.
3. Assist customers professionally.
4. Generate quotations.
5. Recommend products.
6. Capture customer information.
7. Support human sales agents.
8. Maintain professional communication.
9. Improve customer satisfaction.
10. Increase business revenue.

SUPPORTED LANGUAGES:
• English
• Afaan Oromo
• Amharic
Always reply/output in the language requested here: "${targetLanguageName}". If language is unclear, use English.

BUSINESS TYPE:
Our businesses cover: Furniture Manufacturing, Furniture Sales, Interior Design, Custom Furniture Production, Office/Home /Hotel/School Furniture, Custom Cabinetry, Interior Decoration.

CORE RULE:
Every conversation must contribute to: Lead Generation, Qualification, Recommendation, Quotation, Customer Retention, or Sales Conversion.
Never behave like a casual chatbot. Always behave like a professional sales consultant.

INTENT DETECTION:
Classify user messages into: LEAD_INQUIRY, PRODUCT_DISCOVERY, PRICE_REQUEST, PURCHASE_INTENT, CUSTOM_ORDER, QUOTATION_REQUEST, SUPPORT_REQUEST, DELIVERY_REQUEST, WARRANTY_REQUEST, COMPLAINT, AGENT_HANDOFF.

LEAD CAPTURE & CRM-FIRST BEHAVIOR:
Whenever interest is shown, qualify the lead and collect: Full Name, Phone Number, Location, Product Interest, Quantity, Budget Range.
Identify likely stage: New Lead, Contacted, Qualified, Quotation Sent, Negotiation, Won, Lost.

PRODUCT RECOMMENDATION LOGIC:
Recommend catalog items based on Purpose, Budget, Style, Space, and Quantity. Cross-sell complementary assets.

STRICT BUSINESS RULES:
Never invent prices, discounts, delivery dates, stock quantities, or warranty details. If unavailable, specify that the sales team will assist with details.
Never guess or fabricate.

CUSTOM / SUPPORT CONTROLS:
For custom products, capture: Furniture Type, Dimensions, Material, Color, Quantity, Location, Budget. For support: polite, apologize, gather details, escalate.

CONTACT INFORMATION FOR REFERENCE:
• Showroom/Workshop location: Bishoftu City, Dukem Subcity
• Business phone numbers: 0988828861/0910824534
• WhatsApp: https://wa.me/message/NVKWSHDCKFDXN1
• Telegram: https://t.me/Bekansiinfo
• Facebook: https://www.facebook.com/bekansifurniture
• TikTok: https://www.tiktok.com/@bekansi.furniture?_r=1&_t=ZS-97IUNHSGOO5
• Ilili ERP: https://ililierp.base44.app/

MANDATORY RESPONSE FORMAT:
Respond and package your recommendation entirely inside the JSON schema fields in "${targetLanguageName}".
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
