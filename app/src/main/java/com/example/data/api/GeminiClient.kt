package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    // High quality offline fallback responses for beautiful demo capability:
    private val DEFAULT_ANSWERS_EN = listOf(
        "Wanza is our signature hardwood! Our 'Gara' Curved Sofa costs 135,000 ETB with a 5-year warranty. Would you like us to generate a quotation for you?",
        "For dining rooms, the solid Mahogany 'Zid' Suite represents timeless luxury at 185,000 ETB. Hand-carved and warrantied for 10 years.",
        "We deliver custom orders in 3-4 weeks. Transport inside Addis Ababa is a flat rate of 5,000 ETB, and outside is computed based on distance.",
        "Yes, we support complete custom sizing for our sofas, beds, and office desks. Can I have your phone number so one of our design consultants can call you?",
        "Welcome to Bekansi! We craft custom high-end furniture using Ethiopia's finest hardwoods (Wanza, Mahogany, Grar). How can I assist you today?"
    )

    private val DEFAULT_ANSWERS_AM = listOf(
        "ዋንዛ ልዩ እንጨታችን ነው! የእኛ 'ጋራ' ሶፋ ዋጋ 135,000 ብር ሲሆን የ 5 ዓመት ዋስትና አለው። ዋጋዎችን ዝርዝር ማዘጋጀት እንድንጀምር ይፈልጋሉ?",
        "ለሳሎን ወይም ለምግብ ቤት ጠረጴዛ፣ ጽኑውን ማሆጋኒ 'ዚድ' ጠረጴዛ በ 185,000 ብር እንመክራለን። የ 10 ዓመት ዋስትና አለው።",
        "የማስረከብያ ጊዜው ከ3-4 ሳምንታት ነው። በአዲስ አበባ ውስጥ ማጓጓዣ 5,000 ብር ሲሆን ከከተማ ውጭ ባለው ርቀት ይሰላል።",
        "አዎ፣ በደንበኞቻችን ምርጫ ልክ ሶፋዎችን፣ አልጋዎችን እና ጠረጴዛዎችን እናስተካክላለን። የሽያጭ አማካሪ እንዲያገኝዎት ስልክ ቁጥርዎን መስጠት ይችላሉ?",
        "ወደ በካንሲ እንኳን ደህና መጡ! ጥራት ካለው የኢትዮጵያ ሀገር በቀል ጠንካራ እንጨቶች (ዋንዛ፣ ማሆጋኒ፣ ግራር) የተሰሩ ሳሎን፣ አልጋዎችን እናቀርባለን። ዛሬ በምን ልርዳዎት?"
    )

    private val DEFAULT_ANSWERS_OM = listOf(
        "Wanza'n mukkeen qulqullina olaanaa qaban keessaa isa tokko! Sofa 'Gara' keenya gatiin isaa 135,000 ETB yoo ta'u, wabii waggaa 5 qaba.",
        "Kofoo nyaataa 'Zid' mukeen Mahogany hojjetame gurgurtaa guddaa qaba, gatiin isaa 185,000 ETB, wabii waggaa 10 waliin.",
        "Ergisa dhimmoota addaa torban 3-4 gidduutti ni geessina. Finfinnee keessatti geejjibni 5,000 ETB yoo ta'u holqoota alaa ammoo herregama.",
        "Eeyyee, bal'ina fi dheerina sofaa ykn siree fedha keessan dhuunfaan ni mijeessina. Bilbila keessan nuuf kennuu dandeessu?",
        "Baga nagaan gara Bekansi Furniture dhuftan! Hardwood Itoophiyaa beekamaa (Wanza, Mahogany, Grar) irraa kan hojjetameedha. Akkamitti isin gargaaruu danda'a?"
    )

    suspend fun getAIResponse(
        prompt: String,
        chatHistory: List<Pair<String, Boolean>>,
        selectedLang: String,
        langConfig: com.example.data.model.LanguageConfig? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d(TAG, "API Key is template placeholder or blank. Using high-fidelity custom fallback dictionary matching user language")
            return@withContext getLocalFallbackResponse(prompt, selectedLang, langConfig)
        }

        val url = "$BASE_URL?key=$apiKey"
        val mediaType = "application/json; charset=utf-8".toMediaType()

        // Dynamic system prompt customization via configuration panel database overrides
        val customPromptAddendum = langConfig?.systemPromptOverride?.takeIf { it.isNotBlank() } ?: "Our showroom is in Addis Ababa. Custom delivery takes 3-4 weeks. Design consults are free."
        val fallbackPhrase = langConfig?.customFallback?.takeIf { it.isNotBlank() } ?: when (selectedLang) {
            "Amharic" -> "እባክዎን የሽያጭ ቡድናችን በዚህ ጉዳይ ላይ በበለጠ እንዲረዳዎት ይፍቀዱ።"
            "Afaan Oromo" -> "Mee dhimma kana irratti gurgurtonni keenya caalaatti akka isin gargaaran eeyyamaa."
            else -> "Please allow our sales team to assist you further on this specific matter."
        }

        val systemInstruction = """
            You are the expert conversational AI Sales Assistant of Bekansi Furniture & Interior Design (Addis Ababa, Ethiopia).
            Your goal is to turn visitors from WhatsApp, Facebook Messenger, Telegram, and Live Chat into warm sales Leads.
            You are professional, welcoming, and understand deep traditional and modern Ethiopian wood craftsmanship.
            You speak $selectedLang fluently. Respond ONLY in $selectedLang unless the user changes language.

            Bekansi Product Catalog & Information (Strict Knowledge Base):
            1. "Wanza Curved L-Sofa 'Gara'": 135,000 ETB. Made of solid Wanza (Cordia Africana) hardwood. Highly durable, luxurious, wrapped in local Habesha woven embroidery. Custom size-configurations are available. Warranty: 5 Years.
            2. "Mahogany Dining Suite 'Zid'": 185,000 ETB. Premium solid Mahogany wood. Set includes 8 intricately carved chairs and a majestic rectangular table. Heavy, gorgeous, deep reddish grain. Warranty: 10 Years.
            3. "King Floating Bed 'Sheger'": 110,000 ETB. Made from finest Ethiopian Acacia (Grar) wood. Creative lighting underneath, floating base. Warranty: 5 Years.
            4. "Dual-tone Credenza 'Bunna'": 32,000 ETB. Blend of Mahogany and Wanza details. Great for TV stands or coffee bars. Warranty: 3 Years.
            5. "Executive Desk 'Abay'": 95,000 ETB. Full solid Mahogany top, brass detailing, premium soft-close file drawers. Warranty: 5 Years.

            Administrative Custom Customization for $selectedLang / Current Rules:
            $customPromptAddendum

            Strict Guidelines:
            - If details requested are NOT listed or cannot be answered with the above catalog, you MUST reply exactly with this customizable fallback:
              "$fallbackPhrase"
            - Never invent custom discounts, delivery timelines (our custom delivery is strictly 3-4 weeks), or delivery pricing (5,000 ETB inside Addis, calculated on distance outside Addis).
            - Proactively prompt for contact details (Phone/Name/Email) if the customer shows interest in custom quotations, dimension changes, or color selections so our agents can reach out.
            - Keep responses warm, engaging, concise, and professional. Use formatting/bullet list.
        """.trimIndent()

        try {
            val root = JSONObject()

            // System instructions
            val systemObj = JSONObject()
            val systemParts = JSONArray()
            val systemPartText = JSONObject()
            systemPartText.put("text", systemInstruction)
            systemParts.put(systemPartText)
            systemObj.put("parts", systemParts)
            root.put("systemInstruction", systemObj)

            // Contents array
            val contentsArr = JSONArray()

            // Add relevant history limits
            val recentHistory = chatHistory.takeLast(6)
            for (turn in recentHistory) {
                val contentObj = JSONObject()
                contentObj.put("role", if (turn.second) "model" else "user")
                val partsObjArr = JSONArray()
                val textPart = JSONObject()
                textPart.put("text", turn.first)
                partsObjArr.put(textPart)
                contentObj.put("parts", partsObjArr)
                contentsArr.put(contentObj)
            }

            // Current prompt
            val currentTurn = JSONObject()
            currentTurn.put("role", "user")
            val currentParts = JSONArray()
            val currentPartText = JSONObject()
            currentPartText.put("text", prompt)
            currentParts.put(currentPartText)
            currentTurn.put("parts", currentParts)
            contentsArr.put(currentTurn)

            root.put("contents", contentsArr)

            val requestBody = root.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Unsuccessful response from Gemini API: ${response.code} ${response.message}")
                    return@withContext getLocalFallbackResponse(prompt, selectedLang, langConfig)
                }

                val bodyStr = response.body?.string() ?: ""
                val responseJson = JSONObject(bodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text", "No text part found")
                        }
                    }
                }
                Log.e(TAG, "Failed parsing candidate text in response")
                return@withContext getLocalFallbackResponse(prompt, selectedLang, langConfig)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Gemini Client execution", e)
            return@withContext getLocalFallbackResponse(prompt, selectedLang, langConfig)
        }
    }

    private fun getLocalFallbackResponse(
        prompt: String,
        lang: String,
        langConfig: com.example.data.model.LanguageConfig? = null
    ): String {
        val lower = prompt.lowercase()
        
        // If the admin has defined a completely custom fallback phrase, we use it for unexpected items!
        val customFallbackPhrase = langConfig?.customFallback?.takeIf { it.isNotBlank() }
        
        val answers = when(lang) {
            "Amharic" -> DEFAULT_ANSWERS_AM
            "Afaan Oromo" -> DEFAULT_ANSWERS_OM
            else -> DEFAULT_ANSWERS_EN
        }

        val customGreeting = langConfig?.customGreeting?.takeIf { it.isNotBlank() }

        // Contextual analysis of local request
        return when {
            lower.contains("recommend") || lower.contains("ምክር") || lower.contains("gorsa") || lower.contains("በጀት") || lower.contains("budget") -> {
                when(lang) {
                    "Amharic" -> "በገለጹት ፍላጎት እና የ 120,000 ብር በጀት ላይ በመመስረት BS-003 (የመኝታ ክፍል አልበም) ፣ BS-006 እና BS-009 የዲዛይን አልበሞችን እንመክርዎታለን። እባክዎን ቀጥታ ጥቅስ ለመጠየቅ ከላይ ያለውን ካታሎግ ይጎብኙ።"
                    "Afaan Oromo" -> "Haala nagaan dizaayinii filattani fi bajata keessan ETB 120,000 irratti hundaa'un, dizaayinii BS-003, BS-006 fi BS-009 isiniif gorsina. Dizaayiniin kun duguuggaa mukkeen beekomoo Itoophiyaa qabu."
                    else -> "Based on your preference for modern luxury designs and a budget of ETB 120,000, I highly recommend Bedroom Albums BS-003, BS-006, and BS-009. These feature exquisite Wanza and Grar hardwood finishes with integrated soft-close drawers."
                }
            }
            lower.contains("sofa") || lower.contains("ሶፋ") || lower.contains("gurgurtaa") -> answers[0]
            lower.contains("dining") || lower.contains("ማሆጋኒ") || lower.contains("ጠረጴዛ") || lower.contains("kofoo") -> answers[1]
            lower.contains("deliver") || lower.contains("ማጓጓዣ") || lower.contains("ጊዜ") || lower.contains("geessina") -> answers[2]
            lower.contains("custom") || lower.contains("ስልክ") || lower.contains("ቁጥር") || lower.contains("heere") -> answers[3]
            lower.contains("hello") || lower.contains("hi") || lower.contains("selam") || lower.contains("ሰላም") || lower.contains("baga") -> {
                customGreeting ?: answers[4]
            }
            else -> customFallbackPhrase ?: answers[4]
        }
    }
}
