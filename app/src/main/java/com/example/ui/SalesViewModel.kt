package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClient
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.SalesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SalesViewModel(private val repository: SalesRepository) : ViewModel() {

    // Language configuration flows
    val allLanguages = repository.allLanguages.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val enabledLanguages = repository.enabledLanguages.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Core application flows
    val allLeads = repository.allLeads.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allProducts = repository.allProducts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allQuotations = repository.allQuotations.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Design Album State Flows
    val allCategories = repository.allCategories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allAlbums = repository.allAlbums.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allSelections = repository.allSelections.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allFavorites = repository.allFavorites.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allAnalytics = repository.allAnalytics.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Simulator attributes
    val activeChannel = MutableStateFlow("WhatsApp") // WhatsApp, Facebook, Telegram, LiveChat
    val selectedLanguage = MutableStateFlow("en") // "en", "am", "om"

    val currentChannelMessages = activeChannel.flatMapLatest { channel ->
        repository.getMessagesByChannel(channel)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // UI state indicators
    val isAIThinking = MutableStateFlow(false)
    val isSyncingProducts = MutableStateFlow(false)
    val syncErrorMsg = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            // Seed database tables with fine Ethiopian hardwood products and language configs
            repository.populateDefaultCatalogIfEmpty()
            repository.populateDefaultLanguagesIfEmpty()
            repository.populateDefaultLeadsIfEmpty()
            repository.populateDefaultAlbumsIfEmpty()
            
            // Send initial dynamic greeting from the active configured language if no chat history exists
            setupInitialGreetings()
        }
    }

    private fun setupInitialGreetings() {
        viewModelScope.launch {
            val channels = listOf("WhatsApp", "Facebook", "Telegram", "LiveChat")
            for (ch in channels) {
                val dbMsgs = repository.getMessagesByChannel(ch).first()
                if (dbMsgs.isEmpty()) {
                    val code = selectedLanguage.value
                    val lang = repository.getLanguageByCode(code) ?: getFallbackStaticLangConfig(code)
                    val greeting = lang.customGreeting.ifBlank { getFallbackGreeting(code) }
                    repository.insertMessage(
                        Conversation(
                            channel = ch,
                            sender = "AI",
                            messageText = greeting
                        )
                    )
                }
            }
        }
    }

    // Refresh greetings on language switch or admin change
    fun refreshInitialGreetings() {
        viewModelScope.launch {
            val code = selectedLanguage.value
            val lang = repository.getLanguageByCode(code) ?: getFallbackStaticLangConfig(code)
            val greeting = lang.customGreeting.ifBlank { getFallbackGreeting(code) }
            val channels = listOf("WhatsApp", "Facebook", "Telegram", "LiveChat")
            for (ch in channels) {
                repository.insertMessage(
                    Conversation(
                        channel = ch,
                        sender = "AI",
                        messageText = greeting
                    )
                )
            }
        }
    }

    // Admin commands to save custom configurations
    fun updateLanguageConfig(config: LanguageConfig) {
        viewModelScope.launch {
            repository.insertLanguage(config)
        }
    }

    fun sendCustomerMessage(messageText: String) {
        if (messageText.isBlank()) return
        val currentChan = activeChannel.value
        val langCode = selectedLanguage.value

        viewModelScope.launch {
            // Save customer's bubble message
            repository.insertMessage(
                Conversation(
                    channel = currentChan,
                    sender = "Customer",
                    messageText = messageText
                )
            )

            val lower = messageText.lowercase()
            if (lower.contains("view products") || lower.contains("browse designs") || lower.contains("show catalog") || lower.contains("furniture designs") ||
                lower.contains("አልበም") || lower.contains("ካታሎግ") || lower.contains("ምርቶች") || lower.contains("dizayinii") || lower.contains("gurgurtaa")
            ) {
                val replyText = when (langCode) {
                    "am" -> "ወደ በካንሲ የቤት ዕቃዎች ዲዛይን ማዕከለ-ስዕላት በደህና መጡ! እባክዎን ከታች ካሉት የዲዛይን አልበሞች ምድብ ውስጥ አንዱን ይምረጡ ወይም ቀጥታ የካታሎግ ታብ በመጫን ያስሱ።"
                    "om" -> "Gara Bekansi Furniture Design Gallery nagaan dhuftan! Mee gosa dizaayinii gadii keessaa tokko filadhaa ykn karaa Catalog dizaayinii barbaaddan ilaalaa."
                    else -> "Welcome to the Bekansi Furniture Design Gallery! Please choose a category to begin browsing our curated 50+ heirloom albums, or switch directly to the 'Design Albums' catalog tab above."
                }
                repository.insertMessage(
                    Conversation(
                        channel = currentChan,
                        sender = "AI",
                        messageText = replyText
                    )
                )
                return@launch
            }

            isAIThinking.value = true

            // Fetch active multi-lingual rules from the database to guide Gemini
            val activeConfig = repository.getLanguageByCode(langCode)
            val languageName = when (langCode) {
                "am" -> "Amharic"
                "om" -> "Afaan Oromo"
                else -> "English"
            }

            // Fallback immediately if administrative toggle turned this specific language off
            if (activeConfig != null && !activeConfig.isEnabled) {
                isAIThinking.value = false
                repository.insertMessage(
                    Conversation(
                        channel = currentChan,
                        sender = "AI",
                        messageText = "This channel's support for $languageName is currently deactivated. Please contact human agents at info@bekansi.com."
                    )
                )
                return@launch
            }

            // Retrieve conversation history context
            val historyFlow = repository.getMessagesByChannel(currentChan).first()
            val promptHistory = historyFlow.map { Pair(it.messageText, it.sender == "AI") }

            // Trigger AI response
            val replyText = GeminiClient.getAIResponse(
                prompt = messageText,
                chatHistory = promptHistory,
                selectedLang = languageName,
                langConfig = activeConfig
            )

            // Dynamic lead capture simulation when phone or contact info is provided
            autoCreateLeadFromMessageIfApplicable(messageText, replyText, langCode, currentChan)

            // Save AI reply
            repository.insertMessage(
                Conversation(
                    channel = currentChan,
                    sender = "AI",
                    messageText = replyText
                )
            )

            isAIThinking.value = false
        }
    }

    private suspend fun autoCreateLeadFromMessageIfApplicable(
        message: String,
        reply: String,
        langCode: String,
        channel: String
    ) {
        val phoneRegex = "(09|\\+2519|07|\\+2517)\\d{8}".toRegex()
        val foundPhone = phoneRegex.find(message)?.value
        
        if (foundPhone != null || message.lowercase().contains("phone") || message.lowercase().contains("ስልክ")) {
            val hasLead = repository.allLeads.first().any { it.phone == (foundPhone ?: "Inquired") }
            if (!hasLead) {
                repository.insertLead(
                    Lead(
                        name = "Visitor from $channel",
                        phone = foundPhone ?: "Pending Contact Info",
                        email = "captured@omnichannel.et",
                        status = "New",
                        source = channel,
                        requirements = message,
                        notes = "AI Captured. Lead expressed interest in multi-lingual dialogue.",
                        language = when (langCode) {
                            "am" -> "Amharic"
                            "om" -> "Afaan Oromo"
                            else -> "English"
                        }
                    )
                )
            }
        }
    }

    // Lead Management commands
    fun addLead(lead: Lead) {
        viewModelScope.launch {
            repository.insertLead(lead)
        }
    }

    fun updateLeadStatus(lead: Lead, newStatus: String) {
        viewModelScope.launch {
            repository.insertLead(lead.copy(status = newStatus))
        }
    }

    fun deleteLead(id: Int) {
        viewModelScope.launch {
            repository.deleteLead(id)
        }
    }

    // Product Catalog commands
    fun addProduct(product: Product) {
        viewModelScope.launch {
            repository.insertProduct(product)
        }
    }

    fun deleteProduct(id: Int) {
        viewModelScope.launch {
            repository.deleteProduct(id)
        }
    }

    fun syncProducts() {
        viewModelScope.launch {
            isSyncingProducts.value = true
            syncErrorMsg.value = null
            val result = repository.syncProductsFromPostgres(selectedLanguage.value)
            result.onSuccess {
                syncErrorMsg.value = null
            }.onFailure { err ->
                syncErrorMsg.value = err.message ?: "Failed link to external PostgreSQL"
            }
            isSyncingProducts.value = false
        }
    }

    // Product Album commands
    fun addAlbum(album: ProductAlbum) {
        viewModelScope.launch {
            repository.insertAlbum(album)
        }
    }

    // Quotation Engine commands
    fun generateInteractiveQuotation(
        lead: Lead,
        product: Product,
        materialsCost: Double,
        laborCost: Double,
        transportCost: Double,
        profitMarginPercent: Double,
        customDimensions: String,
        deliveryEstimate: String
    ): Quotation {
        val baseMaterials = if (materialsCost > 0) materialsCost else product.price * 0.4
        val baseLabor = if (laborCost > 0) laborCost else product.price * 0.2
        val baseTransport = if (transportCost > 0) transportCost else 5000.0 // Addis standard flat rate

        val rawCost = baseMaterials + baseLabor + baseTransport
        val marginAmt = rawCost * (profitMarginPercent / 100.0)
        val subtotal = rawCost + marginAmt
        val vat = subtotal * 0.15 // 15% Ethiopian VAT
        val total = subtotal + vat

        val customTerms = """
            1. 50% Advance deposit upon approval of this document.
            2. Remaining 50% payable upon delivery of standard carpentry work.
            3. Warranty: ${product.warranty} covers any warp or wood insect infestations.
            4. Delivery takes roughly $deliveryEstimate to Addis Ababa or surrounding areas.
        """.trimIndent()

        val quotation = Quotation(
            leadId = lead.id,
            leadName = lead.name,
            productName = product.name,
            dimensions = customDimensions.ifBlank { product.dimensions },
            material = product.material,
            laborCost = baseLabor,
            transportCost = baseTransport,
            materialCost = baseMaterials,
            subtotal = subtotal,
            vat = vat,
            margin = profitMarginPercent,
            total = total,
            deliveryTimeEstimate = deliveryEstimate,
            terms = customTerms
        )

        viewModelScope.launch {
            repository.insertQuotation(quotation)
            // Update lead status to contact quoted
            repository.insertLead(lead.copy(status = "Quoted"))
        }

        return quotation
    }

    fun deleteQuotation(id: Int) {
        viewModelScope.launch {
            repository.deleteQuotation(id)
        }
    }

    fun clearChatMessages() {
        viewModelScope.launch {
            repository.clearChannelMessages(activeChannel.value)
            // Re-setup initial custom greetings
            val code = selectedLanguage.value
            val lang = repository.getLanguageByCode(code) ?: getFallbackStaticLangConfig(code)
            val greeting = lang.customGreeting.ifBlank { getFallbackGreeting(code) }
            repository.insertMessage(
                Conversation(
                    channel = activeChannel.value,
                    sender = "AI",
                    messageText = greeting
                )
            )
        }
    }

    private fun getFallbackGreeting(code: String): String {
        return when (code) {
            "am" -> "እንኳን ወደ በካንሲ የቤት ዕቃዎች በደህና መጡ! በጥራት ከተመረጡ ሀገር በቀል ጠንካራ እንጨቶች (ዋንዛ፣ ማሆጋኒ፣ ግራር) የተሰሩ ሳሎን፣ አልጋዎችን እናቀርባለን። ዛሬ በምን ልርዳዎት?"
            "om" -> "Baga nagaan gara Bekansi Furniture dhuftan! Hardwood beekamaa Itoophiyaa (Wanza, Mahogany, Grar) irraa kan hojjetameedha. Akkamitti isin gargaaruu danda'a?"
            else -> "Welcome to Bekansi Furniture! We craft premium local hardwood masterpieces (Wanza, Mahogany, Acacia). How can we assist you today?"
        }
    }

    private fun getFallbackStaticLangConfig(code: String): LanguageConfig {
        return when (code) {
            "am" -> LanguageConfig("am", "Amharic", true, "", getFallbackGreeting("am"), "እባክዎን የሽያጭ ቡድናችን በዚህ ጉዳይ ላይ በበለጠ እንዲረዳዎት ይፍቀዱ።")
            "om" -> LanguageConfig("om", "Afaan Oromo", true, "", getFallbackGreeting("om"), "Mee dhimma kana irratti gurgurtonni keenya caalaatti akka isin gargaaran eeyyamaa.")
            else -> LanguageConfig("en", "English", true, "", getFallbackGreeting("en"), "Please allow our sales team to assist you further on this specific matter.")
        }
    }

    // --- Enterprise Album Support Actions ---
    fun incrementViewCount(albumId: String) {
        viewModelScope.launch {
            repository.incrementViewCount(albumId)
        }
    }

    fun toggleFavorite(phone: String, albumId: String) {
        viewModelScope.launch {
            val faves = repository.getFavoritesByCustomer(phone).first()
            val isFave = faves.any { it.albumId == albumId }
            if (isFave) {
                repository.removeFavorite(phone, albumId)
            } else {
                repository.insertFavorite(CustomerFavorite(customerPhone = phone, albumId = albumId))
            }
        }
    }

    fun requestComparison(phone: String, albumId1: String, albumId2: String) {
        viewModelScope.launch {
            repository.insertComparison(DesignComparison(customerPhone = phone, albumId1 = albumId1, albumId2 = albumId2))
        }
    }

    fun removeComparison(id: Int) {
        viewModelScope.launch {
            repository.deleteComparison(id)
        }
    }

    fun requestQuotationForAlbum(phone: String, name: String, location: String, albumId: String, budget: Double, requirements: String) {
        viewModelScope.launch {
            // First, find or create Lead in our CRM
            val existingLeads = repository.allLeads.first()
            val existingLead = existingLeads.find { it.phone == phone }
            val leadId = if (existingLead == null) {
                val newL = Lead(
                    name = name,
                    phone = phone,
                    email = "customer@bekansi.et",
                    status = "Quoted",
                    source = "LiveChat",
                    requirements = "Quotation requested on Album $albumId: $requirements",
                    notes = "Captured automatically during product album quotation request. Location: $location. Est. Budget: ETB $budget",
                    language = when (selectedLanguage.value) {
                        "am" -> "Amharic"
                        "om" -> "Afaan Oromo"
                        else -> "English"
                    }
                )
                repository.insertLead(newL).toInt()
            } else {
                repository.insertLead(existingLead.copy(status = "Quoted", notes = existingLead.notes + "\nUpdated budget: ETB $budget on $albumId. Location: $location"))
                existingLead.id
            }

            // Record customer selection
            repository.insertSelection(
                CustomerSelection(
                    leadId = leadId,
                    albumId = albumId,
                    requirements = requirements,
                    budget = budget
                )
            )

            // Auto-trigger message in simulated channel to active assistant
            val code = selectedLanguage.value
            val album = repository.getAlbumById(albumId)
            val albumName = album?.name ?: albumId
            val selectionMsg = when(code) {
                "am" -> "እኔ ስልኬ $phone ሲሆን ስሜ $name ነው። ለአልበም $albumName (በጀት: $budget ብር፣ ፍላጎት: $requirements) ዋጋ ጥያቄ አቅርቤያለሁ።"
                "om" -> "Maqaan kiyya $name, lakkoofsa bilbilaa $phone ti. Albumii $albumName (Budget: $budget ETB, fedha: $requirements) dizaayinii gurgurtaa fedha."
                else -> "My name is $name, phone: $phone. I am requesting a quotation for Album $albumName (Budget: $budget ETB, requirements: $requirements)"
            }
            sendCustomerMessage(selectionMsg)
        }
    }

    fun requestCustomizationForAlbum(phone: String, name: String, albumId: String, customizedText: String) {
        viewModelScope.launch {
            // Find or create Lead
            val existingLeads = repository.allLeads.first()
            val existingLead = existingLeads.find { it.phone == phone }
            val leadId = if (existingLead == null) {
                val newL = Lead(
                    name = name,
                    phone = phone,
                    email = "customer@bekansi.et",
                    status = "Contacted",
                    source = "LiveChat",
                    requirements = "Customization requested on $albumId: $customizedText",
                    notes = "Automatic capture for album design adaptation.",
                    language = when (selectedLanguage.value) {
                        "am" -> "Amharic"
                        "om" -> "Afaan Oromo"
                        else -> "English"
                    }
                )
                repository.insertLead(newL).toInt()
            } else {
                repository.insertLead(existingLead.copy(status = "Contacted"))
                existingLead.id
            }

            repository.insertSelection(
                CustomerSelection(
                    leadId = leadId,
                    albumId = albumId,
                    requirements = "Customization requested: $customizedText",
                    budget = 0.0
                )
            )

            val code = selectedLanguage.value
            val text = when (code) {
                "am" -> "ለአልበም $albumId ልዩ ማስተካከያ ጥያቄ፡ $customizedText (ስም: $name, ስልክ: $phone)"
                "om" -> "Gulaallii addaa Albumii $albumId tiif: $customizedText (Maqaa: $name, Bilbila: $phone)"
                else -> "Customization request for Album $albumId: $customizedText (Name: $name, Phone: $phone)"
            }
            sendCustomerMessage(text)
        }
    }
}

class SalesViewModelFactory(private val repository: SalesRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SalesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SalesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
