package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.data.database.AppDatabase
import com.example.data.model.Conversation
import com.example.data.model.LanguageConfig
import com.example.data.model.Lead
import com.example.data.model.Product
import com.example.data.model.Quotation
import com.example.data.repository.SalesRepository
import com.example.ui.SalesViewModel
import com.example.ui.SalesViewModelFactory
import com.example.ui.DesignAlbumsTab
import com.example.ui.SmmPlannerTab
import com.example.ui.theme.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Setup local database layer
        val database = AppDatabase.getDatabase(this)
        val repository = SalesRepository(
            leadDao = database.leadDao(),
            productDao = database.productDao(),
            quotationDao = database.quotationDao(),
            conversationDao = database.conversationDao(),
            languageConfigDao = database.languageConfigDao(),
            albumCategoryDao = database.albumCategoryDao(),
            productAlbumDao = database.productAlbumDao(),
            customerFavoriteDao = database.customerFavoriteDao(),
            customerSelectionDao = database.customerSelectionDao(),
            designComparisonDao = database.designComparisonDao(),
            albumAnalyticsDao = database.albumAnalyticsDao()
        )

        val factory = SalesViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[SalesViewModel::class.java]

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        EnterpriseMainScreen(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun EnterpriseMainScreen(viewModel: SalesViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Screen State
    var activeTab by remember { mutableStateOf("OMNICHANNEL_CHAT") } // OMNICHANNEL_CHAT, ADMIN_LANG, CRM_LEADS, QUOTE_ENGINE, PRODUCTS

    // Fetch Flow States
    val languages by viewModel.allLanguages.collectAsState()
    val leads by viewModel.allLeads.collectAsState()
    val products by viewModel.allProducts.collectAsState()
    val quotations by viewModel.allQuotations.collectAsState()
    val currentChannelMessages by viewModel.currentChannelMessages.collectAsState()
    val activeChannel by viewModel.activeChannel.collectAsState()
    val selectedLanguageCode by viewModel.selectedLanguage.collectAsState()
    val isAIThinking by viewModel.isAIThinking.collectAsState()

    // Find custom configurations
    val activeLangConfig = languages.firstOrNull { it.code == selectedLanguageCode }

    Column(modifier = Modifier.fillMaxSize()) {
        // High-end Ethiopian Showcase Header
        HeaderBrandingPanel(
            totalLeads = leads.size,
            totalQuotes = quotations.size
        )

        // Tab Row selector
        TabSelectorRow(
            activeTab = activeTab,
            onTabSelected = { activeTab = it }
        )

        // Main platform workstation
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            when (activeTab) {
                "OMNICHANNEL_CHAT" -> {
                    OmnichannelChatTab(
                        activeChannel = activeChannel,
                        onChannelUpdated = { viewModel.activeChannel.value = it },
                        selectedLanguage = selectedLanguageCode,
                        onLanguageUpdated = {
                            viewModel.selectedLanguage.value = it
                            viewModel.refreshInitialGreetings()
                            Toast.makeText(context, "Switched chat simulation to language: $it", Toast.LENGTH_SHORT).show()
                        },
                        messages = currentChannelMessages,
                        isAIThinking = isAIThinking,
                        langConfig = activeLangConfig,
                        onMessageSent = { viewModel.sendCustomerMessage(it) },
                        onClearChat = { viewModel.clearChatMessages() }
                    )
                }

                "ADMIN_LANG" -> {
                    MultilingualAdminTab(
                        languages = languages,
                        onUpdateLanguages = { viewModel.updateLanguageConfig(it) }
                    )
                }

                "CRM_LEADS" -> {
                    CrmLeadsTab(
                        leads = leads,
                        onAddLead = { viewModel.addLead(it) },
                        onUpdateStatus = { lead, stat -> viewModel.updateLeadStatus(lead, stat) },
                        onDeleteLead = { viewModel.deleteLead(it) }
                    )
                }

                "QUOTE_ENGINE" -> {
                    QuotationEngineTab(
                        leads = leads,
                        products = products,
                        quotations = quotations,
                        onGenerateQuote = { lead, prod, mat, lab, transport, prof, dims, est ->
                            viewModel.generateInteractiveQuotation(lead, prod, mat, lab, transport, prof, dims, est)
                        },
                        onDeleteQuote = { viewModel.deleteQuotation(it) }
                    )
                }

                "PRODUCTS" -> {
                    ProductCatalogTab(
                        products = products,
                        onAddProduct = { viewModel.addProduct(it) },
                        onDeleteProduct = { viewModel.deleteProduct(it) }
                    )
                }

                "DESIGN_ALBUMS" -> {
                    DesignAlbumsTab(viewModel = viewModel)
                }

                "SMM_PLANNER" -> {
                    SmmPlannerTab(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun HeaderBrandingPanel(totalLeads: Int, totalQuotes: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "BEKANSI AI SALES PLATFORM",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Ethiopian Multilingual Omnichannel Hub & Bespoke Interior CRM",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // High level indicators
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricsBadge(label = "CRM Leads", value = totalLeads.toString(), color = AccentSuccess)
                MetricsBadge(label = "Quotations", value = totalQuotes.toString(), color = AccentWarning)
            }
        }
    }
}

@Composable
fun MetricsBadge(label: String, value: String, color: Color) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Normal, color = TextMuted)
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun TabSelectorRow(activeTab: String, onTabSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCocoaBg)
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TabItem(icon = Icons.Default.Call, label = "Chat Sim", isSelected = activeTab == "OMNICHANNEL_CHAT", onClick = { onTabSelected("OMNICHANNEL_CHAT") })
        TabItem(icon = Icons.Default.Settings, label = "Admin Lang", isSelected = activeTab == "ADMIN_LANG", onClick = { onTabSelected("ADMIN_LANG") })
        TabItem(icon = Icons.Default.Person, label = "CRM Leads", isSelected = activeTab == "CRM_LEADS", onClick = { onTabSelected("CRM_LEADS") })
        TabItem(icon = Icons.Default.ShoppingCart, label = "Quotes", isSelected = activeTab == "QUOTE_ENGINE", onClick = { onTabSelected("QUOTE_ENGINE") })
        TabItem(icon = Icons.Default.List, label = "Catalog", isSelected = activeTab == "PRODUCTS", onClick = { onTabSelected("PRODUCTS") })
        TabItem(icon = Icons.Default.Favorite, label = "Albums", isSelected = activeTab == "DESIGN_ALBUMS", onClick = { onTabSelected("DESIGN_ALBUMS") })
        TabItem(icon = Icons.Default.Share, label = "Social SMM", isSelected = activeTab == "SMM_PLANNER", onClick = { onTabSelected("SMM_PLANNER") })
    }
}

@Composable
fun TabItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    val containerBg = if (isSelected) WarmMahogany else Color.Transparent
    val contentCol = if (isSelected) TextLight else TextMuted

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(containerBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = contentCol, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = contentCol,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

// ---------------- TAB 1: OMNICHANNEL CHAT SIMULATOR ----------------
@Composable
fun OmnichannelChatTab(
    activeChannel: String,
    onChannelUpdated: (String) -> Unit,
    selectedLanguage: String,
    onLanguageUpdated: (String) -> Unit,
    messages: List<Conversation>,
    isAIThinking: Boolean,
    langConfig: LanguageConfig?,
    onMessageSent: (String) -> Unit,
    onClearChat: () -> Unit
) {
    var promptInput by remember { mutableStateOf("") }
    val uriHandler = LocalUriHandler.current

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Live Omnichannel Simulator",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GoldAccent
            )
            Button(
                onClick = onClearChat,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = WarmMahogany),
                modifier = Modifier.height(30.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Clear Chat", modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset Chat", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Channels Row Selector (WhatsApp, Telegram, Messenger, LiveChat)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val chans = listOf(
                "WhatsApp" to AccentSuccess,
                "Facebook" to Color(0xFF1877F2),
                "Telegram" to Color(0xFF0088CC),
                "LiveChat" to WarmMahogany
            )
            chans.forEach { (ch, col) ->
                val active = activeChannel == ch
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (active) col else MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onChannelUpdated(ch) }
                        .border(
                            width = 1.dp,
                            color = if (active) Color.Transparent else TextMuted.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    Text(
                        text = ch,
                        fontSize = 11.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        color = if (active) TextLight else TextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Official Bekansi Channels Connect Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share, 
                        contentDescription = "Official Channels", 
                        tint = GoldAccent, 
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Connect with Bekansi Official Channels:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val channels = listOf(
                        Triple("WhatsApp", "https://wa.me/message/NVKWSHDCKFDXN1", AccentSuccess),
                        Triple("Telegram", "https://t.me/Bekansiinfo", Color(0xFF0088CC)),
                        Triple("Facebook", "https://www.facebook.com/bekansifurniture", Color(0xFF1877F2)),
                        Triple("TikTok", "https://www.tiktok.com/@bekansi.furniture?_r=1&_t=ZS-97IUNHSGOO5", Color(0xFFFE2C55))
                    )
                    channels.forEach { (name, link, color) ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = color.copy(alpha = 0.15f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { uriHandler.openUri(link) }
                                .border(
                                    width = 1.dp,
                                    color = color.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(20.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when(name) {
                                        "WhatsApp" -> Icons.Default.Call
                                        "Telegram" -> Icons.Default.Send
                                        "TikTok" -> Icons.Default.PlayArrow
                                        else -> Icons.Default.Share
                                    },
                                    contentDescription = name,
                                    tint = color,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = name,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = color,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Language toggle row (Warning if Disabled by administrator in Admin panel)
        Card(
            colors = CardDefaults.cardColors(containerColor = LightWarmCard.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = "Lang", tint = WarmMahogany, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Dialogue Language:", fontSize = 11.sp, color = TextDark)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val langs = listOf("en" to "English", "am" to "አማርኛ", "om" to "Afaan Oromo")
                    langs.forEach { (code, label) ->
                        val active = selectedLanguage == code
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (active) WarmMahogany else LightWarmCard,
                            modifier = Modifier
                                .clickable { onLanguageUpdated(code) }
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (active) TextLight else TextDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Active notification warning if admin disabled language
        if (langConfig != null && !langConfig.isEnabled) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = "Deactivated", tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Warning: Administrative control has deactivated '${langConfig.name}' on this system. Responses will fall back to human agent bypass.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Chat conversation bubble window
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .border(2.dp, LightWarmCard, RoundedCornerShape(10.dp))
                .background(LightWarmCard.copy(alpha = 0.1f))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                reverseLayout = false
            ) {
                items(messages) { msg ->
                    ChatBubble(msg = msg)
                }

                if (isAIThinking) {
                    item {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = GoldAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Bekansi Sales AI is processing...", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            }
        }

        // Helpful pre-configured demo phrases targeting hardwoods & furniture
        Text("Demo helper shortcuts:", fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(bottom = 2.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val suggestions = when(selectedLanguage) {
                "am" -> listOf("ስለ ዋንዛ ሶፋ ንገረኝ", "ማጓጓዣ ዋጋ ስንት ነው?", "ስልክ ቁጥሬ 0988828861 ነው")
                "om" -> listOf("Sofa 'Gara' gatii firra", "Gurgurtaa mukkeen Wanza", "Bilbilli kiyya 0910824534")
                else -> listOf("Price for Wanza Gara Sofa", "Dining Table 'Zid' inquiry", "Phone: 0988828861 custom")
            }
            suggestions.forEach { prompt ->
                Surface(
                    color = LightWarmCard,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .clickable { promptInput = prompt }
                        .weight(1f)
                ) {
                    Text(
                        text = prompt,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextDark,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Chat text entry controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = promptInput,
                onValueChange = { promptInput = it },
                placeholder = { Text("Ask something about Wanza Sofas or Mahogany tables...", fontSize = 12.sp) },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = WarmMahogany,
                    unfocusedContainerColor = LightWarmCard,
                    focusedContainerColor = LightWarmCard
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            FloatingActionButton(
                onClick = {
                    if (promptInput.isNotBlank()) {
                        onMessageSent(promptInput)
                        promptInput = ""
                    }
                },
                containerColor = WarmMahogany,
                contentColor = TextLight,
                modifier = Modifier.size(50.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}

@Composable
fun ChatBubble(msg: Conversation) {
    val isAI = msg.sender == "AI"
    val align = if (isAI) Alignment.Start else Alignment.End
    val bg = if (isAI) LightWarmCard else WarmMahogany
    val textCol = if (isAI) TextDark else TextLight

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isAI) 2.dp else 12.dp,
                bottomEnd = if (isAI) 12.dp else 2.dp
            ),
            color = bg,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // Sender badge
                Text(
                    text = if (isAI) "Bekansi AI Sales" else "Customer Sim",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isAI) WarmMahogany else GoldAccent
                )
                Spacer(modifier = Modifier.height(2.dp))
                // Message text
                Text(
                    text = msg.messageText,
                    fontSize = 12.sp,
                    color = textCol,
                    lineHeight = 16.sp
                )
            }
        }
    }
}


// ---------------- TAB 2: MULTILINGUAL ADMINISTRATION ----------------
@Composable
fun MultilingualAdminTab(
    languages: List<LanguageConfig>,
    onUpdateLanguages: (LanguageConfig) -> Unit
) {
    val context = LocalContext.current
    var activeLangToEdit by remember { mutableStateOf("en") }

    // Forms
    val currentToEdit = languages.find { it.code == activeLangToEdit }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightWarmCard.copy(alpha = 0.05f))
    ) {
        Text(
            text = "Multilingual Admin Control Dashboard",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = GoldAccent
        )
        Text(
            text = "Enable/disable Amharic, English, or Afaan Oromo and customize knowledge base overrides or triggers stored permanently inside the Room DB.",
            fontSize = 11.sp,
            color = TextMuted,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Select Language tabs to edit
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            languages.forEach { config ->
                val selected = activeLangToEdit == config.code
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (selected) WarmMahogany else LightWarmCard,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeLangToEdit = config.code }
                        .border(
                            1.dp,
                            if (config.isEnabled) AccentSuccess else Color.Red,
                            RoundedCornerShape(10.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = config.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) TextLight else TextDark
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(if (config.isEnabled) AccentSuccess else Color.Red, RoundedCornerShape(3.dp))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (config.isEnabled) "Enabled" else "Disabled",
                                fontSize = 9.sp,
                                color = if (selected) TextLight.copy(alpha = 0.8f) else TextMuted
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (currentToEdit != null) {
            // Local form states to reflect updates
            var formEnabled by remember(currentToEdit.code) { mutableStateOf(currentToEdit.isEnabled) }
            var formGreeting by remember(currentToEdit.code) { mutableStateOf(currentToEdit.customGreeting) }
            var formFallback by remember(currentToEdit.code) { mutableStateOf(currentToEdit.customFallback) }
            var formPromptOverride by remember(currentToEdit.code) { mutableStateOf(currentToEdit.systemPromptOverride) }

            Card(
                colors = CardDefaults.cardColors(containerColor = LightWarmCard),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Settings for ${currentToEdit.name}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarmMahogany
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (formEnabled) "Language Active" else "Deactivated",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (formEnabled) AccentSuccess else Color.Red
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Switch(
                                    checked = formEnabled,
                                    onCheckedChange = { formEnabled = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = LightWarmCard,
                                        checkedTrackColor = AccentSuccess
                                    )
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Custom AI Greeting (Initial Trigger):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Introduced dynamically to customers on platform connection",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                        TextField(
                            value = formGreeting,
                            onValueChange = { formGreeting = it },
                            colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                        )
                    }

                    item {
                        Text(
                            text = "Custom Fallback Phrase (Inability Response):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Triggered strictly if queried parameters lack matching catalog definitions",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                        TextField(
                            value = formFallback,
                            onValueChange = { formFallback = it },
                            colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                        )
                    }

                    item {
                        Text(
                            text = "AI System instructions & Knowledge Base overrides:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Inject showroom policies or custom catalog metrics directly to Gemini logic context",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                        TextField(
                            value = formPromptOverride,
                            onValueChange = { formPromptOverride = it },
                            colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                        )
                    }

                    item {
                        Button(
                            onClick = {
                                val savedConfig = currentToEdit.copy(
                                    isEnabled = formEnabled,
                                    customGreeting = formGreeting,
                                    customFallback = formFallback,
                                    systemPromptOverride = formPromptOverride
                                )
                                onUpdateLanguages(savedConfig)
                                Toast.makeText(context, "Configurations for ${currentToEdit.name} saved successfully!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Configurations to Local Room DB", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}


// ---------------- TAB 3: LEAD PIPELINE CRM ----------------
@Composable
fun CrmLeadsTab(
    leads: List<Lead>,
    onAddLead: (Lead) -> Unit,
    onUpdateStatus: (Lead, String) -> Unit,
    onDeleteLead: (Int) -> Unit
) {
    var leadName by remember { mutableStateOf("") }
    var leadPhone by remember { mutableStateOf("") }
    var leadRequirements by remember { mutableStateOf("") }
    var showAddForm by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Salesforce-like CRM Pipeline",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GoldAccent
            )
            Button(
                onClick = { showAddForm = !showAddForm },
                colors = ButtonDefaults.buttonColors(containerColor = if (showAddForm) Color.Gray else WarmMahogany),
                modifier = Modifier.height(30.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Icon(
                    imageVector = if (showAddForm) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = "Toggle add",
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (showAddForm) "Close" else "Add Lead", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        if (showAddForm) {
            Card(
                colors = CardDefaults.cardColors(containerColor = LightWarmCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Register Manual Customer Lead", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WarmMahogany)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextField(
                            value = leadName,
                            onValueChange = { leadName = it },
                            label = { Text("Name", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                        )
                        TextField(
                            value = leadPhone,
                            onValueChange = { leadPhone = it },
                            label = { Text("Phone Number", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                        )
                    }
                    TextField(
                        value = leadRequirements,
                        onValueChange = { leadRequirements = it },
                        label = { Text("Customer Interest / Requirements", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                    )
                    Button(
                        onClick = {
                            if (leadName.isNotBlank() && leadPhone.isNotBlank()) {
                                onAddLead(
                                    Lead(
                                        name = leadName,
                                        phone = leadPhone,
                                        email = "web-leads@bekansi.et",
                                        status = "New",
                                        source = "LiveChat",
                                        requirements = leadRequirements,
                                        notes = "Manually recorded via CRM Suite Console.",
                                        language = "English"
                                    )
                                )
                                leadName = ""
                                leadPhone = ""
                                leadRequirements = ""
                                showAddForm = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Manual Lead", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (leads.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Person, contentDescription = "Empty Leads", tint = TextMuted, modifier = Modifier.size(48.dp))
                    Text("No customer leads captured yet", style = MaterialTheme.typography.bodyLarge, color = TextMuted)
                    Text("Interact with the simulator or append leads manually", fontSize = 11.sp, color = TextMuted)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(leads) { lead ->
                    LeadCrmCard(
                        lead = lead,
                        onUpdateStatus = { onUpdateStatus(lead, it) },
                        onDelete = { onDeleteLead(lead.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun LeadCrmCard(lead: Lead, onUpdateStatus: (String) -> Unit, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = LightWarmCard),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = lead.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Text(text = "Phone: ${lead.phone}", fontSize = 11.sp, color = TextMuted)
                }

                // Lead source pill
                val sourceColor = when (lead.source) {
                    "WhatsApp" -> AccentSuccess
                    "Facebook" -> Color(0xFF1877F2)
                    "Telegram" -> Color(0xFF0088CC)
                    else -> WarmMahogany
                }
                Surface(
                    color = sourceColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = lead.source,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = sourceColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Inquiry: \"${lead.requirements}\"",
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = TextDark,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Stage statuses selector (New, Contacted, Quoted, Won, Lost)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Stage: ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    val stages = listOf("New", "Contacted", "Quoted", "Won", "Lost")
                    stages.forEach { stage ->
                        val selected = lead.status == stage
                        val bg = if (selected) {
                            when (stage) {
                                "Won" -> AccentSuccess
                                "Lost" -> Color.Red
                                "Quoted" -> AccentWarning
                                else -> WarmMahogany
                            }
                        } else Color.Transparent

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(bg)
                                .clickable { onUpdateStatus(stage) }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = stage,
                                fontSize = 9.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) TextLight else TextDark
                            )
                        }
                    }
                }

                // Delete lead control icon
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete lead",
                    tint = Color.Red.copy(alpha = 0.5f),
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onDelete() }
                )
            }
        }
    }
}


// ---------------- TAB 4: QUOTATION ENGINE ----------------
@Composable
fun QuotationEngineTab(
    leads: List<Lead>,
    products: List<Product>,
    quotations: List<Quotation>,
    onGenerateQuote: (Lead, Product, Double, Double, Double, Double, String, String) -> Unit,
    onDeleteQuote: (Int) -> Unit
) {
    val context = LocalContext.current

    // Active generate form states
    var selectedLeadIndex by remember { mutableStateOf(0) }
    var selectedProductIndex by remember { mutableStateOf(0) }
    var profitMargin by remember { mutableStateOf(20f) }
    var customMaterialsCost by remember { mutableStateOf("") }
    var customLaborCost by remember { mutableStateOf("") }
    var customTransportCost by remember { mutableStateOf("5000") } // Addis Standard 5000 Birr
    var customDimensions by remember { mutableStateOf("") }
    var customDeliveryEstimate by remember { mutableStateOf("3 Weeks") }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "AI-Driven Quotation Engine (VAT & Margin Calculator)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = GoldAccent
        )
        Text(
            text = "Generate professional legal quotes for custom hardwood designs using automatic Ethiopian tax rates (15% VAT).",
            fontSize = 11.sp,
            color = TextMuted,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        if (leads.isEmpty() || products.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = "Requires at least 1 lead inside the CRM and 1 product item inside the inventory catalog to function. Please register items in adjacent tabs.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(10.dp)
                )
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = LightWarmCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text("1. Target Customer Lead Selection", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WarmMahogany)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            leads.forEachIndexed { i, l ->
                                val active = selectedLeadIndex == i
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (active) WarmMahogany else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier
                                        .clickable { selectedLeadIndex = i }
                                        .weight(1f)
                                ) {
                                    Text(
                                        text = l.name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (active) TextLight else TextDark,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text("2. Catalog Base Product Pairing", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WarmMahogany)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            products.take(3).forEachIndexed { i, p ->
                                val active = selectedProductIndex == i
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (active) WarmMahogany else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier
                                        .clickable { selectedProductIndex = i }
                                        .weight(1f)
                                ) {
                                    Text(
                                        text = p.name.substringBefore(" '"),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (active) TextLight else TextDark,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text("3. Cost Parameters & Custom Dimensions", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WarmMahogany)
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TextField(
                                value = customDimensions,
                                onValueChange = { customDimensions = it },
                                placeholder = { Text("Dimensions overrides", fontSize = 10.sp) },
                                label = { Text("Size (e.g. 270x190cm)", fontSize = 8.sp) },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                            )
                            TextField(
                                value = customDeliveryEstimate,
                                onValueChange = { customDeliveryEstimate = it },
                                label = { Text("Timeframe (e.g. 3 Weeks)", fontSize = 8.sp) },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                            )
                        }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TextField(
                                value = customMaterialsCost,
                                onValueChange = { customMaterialsCost = it },
                                label = { Text("Material Cost (ETB)", fontSize = 8.sp) },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                            )
                            TextField(
                                value = customLaborCost,
                                onValueChange = { customLaborCost = it },
                                label = { Text("Labor Cost (ETB)", fontSize = 8.sp) },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                            )
                            TextField(
                                value = customTransportCost,
                                onValueChange = { customTransportCost = it },
                                label = { Text("Transport (ETB)", fontSize = 8.sp) },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Desired Profit Margin: ${profitMargin.toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Slider(
                                value = profitMargin,
                                onValueChange = { profitMargin = it },
                                valueRange = 5f..60f,
                                modifier = Modifier.width(180.dp),
                                colors = SliderDefaults.colors(thumbColor = WarmMahogany, activeTrackColor = WarmMahogany)
                            )
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                val lead = leads.getOrNull(selectedLeadIndex)
                                val product = products.getOrNull(selectedProductIndex)
                                if (lead != null && product != null) {
                                    val mat = customMaterialsCost.toDoubleOrNull() ?: 0.0
                                    val lab = customLaborCost.toDoubleOrNull() ?: 0.0
                                    val trans = customTransportCost.toDoubleOrNull() ?: 0.0

                                    onGenerateQuote(
                                        lead,
                                        product,
                                        mat,
                                        lab,
                                        trans,
                                        profitMargin.toDouble(),
                                        customDimensions,
                                        customDeliveryEstimate
                                    )
                                    Toast.makeText(context, "Quotation generated successfully to database!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Compute and Generate Printable Legal Quote", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // History list of generated quotations with printable layouts
        Text("Historic Quotation Records", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
        if (quotations.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No quotes issued yet", fontSize = 12.sp, color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quotations) { q ->
                    QuotationRecordCard(q = q, onDelete = { onDeleteQuote(q.id) })
                }
            }
        }
    }
}

@Composable
fun QuotationRecordCard(q: Quotation, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = LightWarmCard),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "QUOTE PIN: BK-Q${q.id + 1000}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WarmMahogany)
                    Text(text = "Client: ${q.leadName}", fontSize = 11.sp, color = TextDark)
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Item: ${q.productName}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text(text = "Size: ${q.dimensions}", fontSize = 11.sp, color = TextDark)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = TextMuted.copy(alpha = 0.2f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Materials + Craftsmanship Cost:", fontSize = 10.sp, color = TextMuted)
                        Text(text = "${String.format("%.2f", q.materialCost + q.laborCost)} ETB", fontSize = 10.sp, color = TextDark)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Transport Delivery to site:", fontSize = 10.sp, color = TextMuted)
                        Text(text = "${String.format("%.2f", q.transportCost)} ETB", fontSize = 10.sp, color = TextDark)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Mark-up profit margin (${q.margin.toInt()}%):", fontSize = 10.sp, color = TextMuted)
                        Text(text = "${String.format("%.2f", q.subtotal - (q.materialCost + q.laborCost + q.transportCost))} ETB", fontSize = 10.sp, color = TextDark)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Subtotal (Pre-tax):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text(text = "${String.format("%.2f", q.subtotal)} ETB", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Ethiopian VAT (15.0%):", fontSize = 10.sp, color = TextMuted)
                        Text(text = "${String.format("%.2f", q.vat)} ETB", fontSize = 10.sp, color = TextDark)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = TextMuted.copy(alpha = 0.2f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "GRAND TOTAL:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentSuccess)
                        Text(text = "${String.format("%.2f", q.total)} Birr", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentSuccess)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Warranty cover + delivery timeline estimated at ${q.deliveryTimeEstimate}.", fontSize = 9.sp, color = TextMuted)
        }
    }
}


// ---------------- TAB 5: PRODUCT CATALOG ----------------
@Composable
fun ProductCatalogTab(
    products: List<Product>,
    onAddProduct: (Product) -> Unit,
    onDeleteProduct: (Int) -> Unit
) {
    var showForm by remember { mutableStateOf(false) }
    var prodName by remember { mutableStateOf("") }
    var prodPrice by remember { mutableStateOf("") }
    var prodMaterial by remember { mutableStateOf("Wanza") }
    var prodWarranty by remember { mutableStateOf("5 Years") }
    var prodDescription by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Premium Furniture Showroom Design Catalog",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = GoldAccent
            )
            Button(
                onClick = { showForm = !showForm },
                colors = ButtonDefaults.buttonColors(containerColor = if (showForm) Color.Gray else WarmMahogany),
                modifier = Modifier.height(28.dp),
                contentPadding = PaddingValues(horizontal = 10.dp)
            ) {
                Text(if (showForm) "Close" else "+ Add", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (showForm) {
            Card(
                colors = CardDefaults.cardColors(containerColor = LightWarmCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Register New Carved Timber Craft", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WarmMahogany)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextField(
                            value = prodName,
                            onValueChange = { prodName = it },
                            label = { Text("Item Name", fontSize = 9.sp) },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                        )
                        TextField(
                            value = prodPrice,
                            onValueChange = { prodPrice = it },
                            label = { Text("Price (ETB)", fontSize = 9.sp) },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Materials option list (Wanza, Mahogany, Grar)
                        TextField(
                            value = prodMaterial,
                            onValueChange = { prodMaterial = it },
                            label = { Text("Hardwood (Wanza, Mahogany, Grar)", fontSize = 9.sp) },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                        )
                        TextField(
                            value = prodWarranty,
                            onValueChange = { prodWarranty = it },
                            label = { Text("Warranty Era", fontSize = 9.sp) },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                        )
                    }
                    TextField(
                        value = prodDescription,
                        onValueChange = { prodDescription = it },
                        label = { Text("Product Description / Craft details", fontSize = 9.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                    )
                    Button(
                        onClick = {
                            val pr = prodPrice.toDoubleOrNull() ?: 0.0
                            if (prodName.isNotBlank() && pr > 0) {
                                onAddProduct(
                                    Product(
                                        name = prodName,
                                        category = "Custom Sofa",
                                        price = pr,
                                        material = prodMaterial,
                                        dimensions = "Customized upon draft",
                                        warranty = prodWarranty,
                                        description = prodDescription,
                                        stockStatus = "Made to Order"
                                    )
                                )
                                prodName = ""
                                prodPrice = ""
                                prodDescription = ""
                                showForm = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add to catalog", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Grid/List of current catalogue showing generated AI hero image
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Include our generated hero as the catalog backdrop banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_furniture),
                        contentDescription = "Showroom Hero",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f))
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Addis Ababa Flagship Showroom",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                        Text(
                            text = "Experience hand-crafted organic carving wood furniture made with legacy mastery.",
                            fontSize = 10.sp,
                            color = TextLight.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            items(products) { item ->
                ProductShowcaseCard(product = item, onDelete = { onDeleteProduct(item.id) })
            }
        }
    }
}

@Composable
fun ProductShowcaseCard(product: Product, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = LightWarmCard),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = product.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Material: ${product.material}",
                            fontSize = 10.sp,
                            color = WarmMahogany,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "• Warranty: ${product.warranty}",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${String.format("%,.0f", product.price)} ETB",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentSuccess
                    )
                    Surface(
                        color = WarmMahogany.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = product.stockStatus,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkWalnut,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = product.description,
                fontSize = 11.sp,
                color = TextDark.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Standard dimensions: ${product.dimensions}", fontSize = 9.sp, color = TextMuted)
                
                // Allow deleting custom added products
                if (product.id > 5) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red.copy(alpha = 0.6f),
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onDelete() }
                    )
                }
            }
        }
    }
}
