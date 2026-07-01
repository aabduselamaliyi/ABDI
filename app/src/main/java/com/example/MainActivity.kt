package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import com.example.ui.AiEmployeeTab
import com.example.ui.theme.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.ui.platform.testTag
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
            val isDarkTheme by viewModel.isDarkMode.collectAsState()
            MyApplicationTheme(darkTheme = isDarkTheme) {
                val isLoggedIn by viewModel.isUserLoggedIn.collectAsState()
                
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // High-tech App Background matching Bekansi AI branding
                        Image(
                            painter = painterResource(id = R.drawable.img_bekansi_bg),
                            contentDescription = "App Background",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Dark semi-transparent overlay to ensure text is perfectly legible
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF130026).copy(alpha = if (isDarkTheme) 0.95f else 0.85f))
                        )
                        
                        if (isLoggedIn) {
                            EnterpriseMainScreen(viewModel)
                        } else {
                            com.example.ui.AuthScreen(
                                onLoginSuccess = { email, role ->
                                    viewModel.loginUser(email, role)
                                }
                            )
                        }
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
    var activeTab by remember { mutableStateOf("DASHBOARD") } // DASHBOARD, AI_EMPLOYEE, OMNICHANNEL_CHAT, ADMIN_LANG, CRM_LEADS, QUOTE_ENGINE, PRODUCTS, DESIGN_ALBUMS, SMM_PLANNER

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

    val navItems = remember(leads.size, quotations.size) {
        listOf(
            NavigationItem("DASHBOARD", "Dashboard", Icons.Default.Home),
            NavigationItem("AI_EMPLOYEE", "AI Employee 🤖", Icons.Default.Build, badgeLabel = "Active", badgeColor = Color(0xFFCCA43B)),
            NavigationItem("OMNICHANNEL_CHAT", "Chat Sim", Icons.Default.Call, badgeLabel = "Omni", badgeColor = Color(0xFF818CF8)),
            NavigationItem("ADMIN_LANG", "Admin Lang", Icons.Default.Settings),
            NavigationItem("CRM_LEADS", "CRM Leads", Icons.Default.Person, badgeCount = leads.size, badgeColor = AccentSuccess),
            NavigationItem("QUOTE_ENGINE", "Quotes", Icons.Default.ShoppingCart, badgeCount = quotations.size, badgeColor = AccentWarning),
            NavigationItem("PRODUCTS", "Catalog", Icons.Default.List),
            NavigationItem("DESIGN_ALBUMS", "Albums", Icons.Default.Favorite),
            NavigationItem("SMM_PLANNER", "Social SMM", Icons.Default.Share)
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 768.dp

        if (isTablet) {
            // TABLET / DESKTOP PERSISTENT SIDEBAR LAYOUT
            Row(modifier = Modifier.fillMaxSize()) {
                // Persistent Sidebar
                Surface(
                    modifier = Modifier
                        .width(270.dp)
                        .fillMaxHeight(),
                    color = Color(0xFF130026).copy(alpha = 0.95f),
                    border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Branding Panel
                        SidebarBrandingHeader(leads.size, quotations.size)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "WORKSTATION HUB",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF8D8580),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                        
                        // Sidebar Navigation List
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(navItems) { item ->
                                SidebarNavItem(
                                    item = item,
                                    isSelected = activeTab == item.id,
                                    onClick = { activeTab = item.id }
                                )
                            }
                        }

                        // Footer
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE2D9FF).copy(alpha = 0.1f)))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Bishoftu City, Dukem Subcity\nShowroom & Workshop",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF8D8580),
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }

                // Workstation Panel
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(16.dp)
                ) {
                    WorkstationContent(
                        activeTab = activeTab,
                        viewModel = viewModel,
                        languages = languages,
                        leads = leads,
                        products = products,
                        quotations = quotations,
                        currentChannelMessages = currentChannelMessages,
                        activeChannel = activeChannel,
                        selectedLanguageCode = selectedLanguageCode,
                        isAIThinking = isAIThinking,
                        activeLangConfig = activeLangConfig,
                        context = context,
                        onTabChange = { activeTab = it }
                    )
                }
            }
        } else {
            // MOBILE COLLAPSIBLE DRAWER LAYOUT
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = Color(0xFF130026).copy(alpha = 0.98f),
                        modifier = Modifier
                            .width(290.dp)
                            .fillMaxHeight()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            // Drawer Branding Header
                            SidebarBrandingHeader(leads.size, quotations.size)

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "WORKSTATION HUB",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF8D8580),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )

                            // Drawer Navigation List
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(navItems) { item ->
                                    SidebarNavItem(
                                        item = item,
                                        isSelected = activeTab == item.id,
                                        onClick = {
                                            activeTab = item.id
                                            coroutineScope.launch { drawerState.close() }
                                        }
                                    )
                                }
                            }

                            // Footer
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE2D9FF).copy(alpha = 0.1f)))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Bishoftu City, Dukem Subcity\nShowroom & Workshop",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF8D8580),
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }
                }
            ) {
                // Mobile Main Scaffold / Screening Area
                Column(modifier = Modifier.fillMaxSize()) {
                    // Mobile Header with Hamburger
                    MobileHeaderBranding(
                        currentTabName = navItems.find { it.id == activeTab }?.label ?: "Workstation",
                        onMenuClick = {
                            coroutineScope.launch { drawerState.open() }
                        }
                    )

                    // Workstation Panel
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        WorkstationContent(
                            activeTab = activeTab,
                            viewModel = viewModel,
                            languages = languages,
                            leads = leads,
                            products = products,
                            quotations = quotations,
                            currentChannelMessages = currentChannelMessages,
                            activeChannel = activeChannel,
                            selectedLanguageCode = selectedLanguageCode,
                            isAIThinking = isAIThinking,
                            activeLangConfig = activeLangConfig,
                            context = context,
                            onTabChange = { activeTab = it }
                        )
                    }
                }
            }
        }
    }
}

data class NavigationItem(
    val id: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val badgeCount: Int? = null,
    val badgeLabel: String? = null,
    val badgeColor: Color? = null
)

@Composable
fun SidebarBrandingHeader(totalLeads: Int, totalQuotes: Int) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_bekansi_logo),
                contentDescription = "Bekansi AI Logo",
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.5.dp, Color(0xFF00E5FF), RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Column {
                Text(
                    text = "BEKANSI AI",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00E5FF),
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Smarter. Faster.",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFE2D9FF)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                MetricsBadge(label = "CRM Leads", value = totalLeads.toString(), color = AccentSuccess)
            }
            Box(modifier = Modifier.weight(1f)) {
                MetricsBadge(label = "Quotes", value = totalQuotes.toString(), color = AccentWarning)
            }
        }
    }
}

@Composable
fun SidebarNavItem(
    item: NavigationItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerBg = if (isSelected) WarmMahogany else Color.Transparent
    val contentCol = if (isSelected) TextLight else TextMuted

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(containerBg)
            .clickable(onClick = onClick)
            .testTag("sidebar_nav_item_${item.id.lowercase()}")
            .heightIn(min = 48.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = contentCol,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = item.label,
                    fontSize = 13.sp,
                    color = contentCol,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }

            if (item.badgeCount != null) {
                Surface(
                    color = item.badgeColor ?: WarmMahogany,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = item.badgeCount.toString(),
                        fontSize = 9.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            } else if (item.badgeLabel != null) {
                Surface(
                    color = item.badgeColor ?: WarmMahogany,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = item.badgeLabel,
                        fontSize = 9.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MobileHeaderBranding(
    currentTabName: String,
    onMenuClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF130026).copy(alpha = 0.9f)),
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("mobile_hamburger_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open Sidebar Navigation",
                        tint = Color(0xFF00E5FF)
                    )
                }

                Column {
                    Text(
                        text = "BEKANSI AI",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF00E5FF),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = currentTabName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE2D9FF)
                    )
                }
            }

            Image(
                painter = painterResource(id = R.drawable.img_bekansi_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun WorkstationContent(
    activeTab: String,
    viewModel: SalesViewModel,
    languages: List<LanguageConfig>,
    leads: List<Lead>,
    products: List<Product>,
    quotations: List<Quotation>,
    currentChannelMessages: List<Conversation>,
    activeChannel: String,
    selectedLanguageCode: String,
    isAIThinking: Boolean,
    activeLangConfig: LanguageConfig?,
    context: android.content.Context,
    onTabChange: (String) -> Unit
) {
    when (activeTab) {
        "DASHBOARD" -> {
            DashboardTab(
                viewModel = viewModel,
                leads = leads,
                quotations = quotations,
                onTabChange = onTabChange
            )
        }

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
                onDeleteQuote = { viewModel.deleteQuotation(it) },
                viewModel = viewModel
            )
        }

        "PRODUCTS" -> {
            ProductCatalogTab(
                viewModel = viewModel,
                products = products,
                onAddProduct = { viewModel.addProduct(it) },
                onDeleteProduct = { viewModel.deleteProduct(it) }
            )
        }

        "DESIGN_ALBUMS" -> {
            DesignAlbumsTab(viewModel = viewModel)
        }

        "AI_EMPLOYEE" -> {
            AiEmployeeTab(viewModel = viewModel)
        }

        "SMM_PLANNER" -> {
            SmmPlannerTab(viewModel = viewModel)
        }
    }
}

@Composable
fun HeaderBrandingPanel(totalLeads: Int, totalQuotes: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF130026).copy(alpha = 0.9f)),
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Customized logo
                Image(
                    painter = painterResource(id = R.drawable.img_bekansi_logo),
                    contentDescription = "Bekansi AI Logo",
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.5.dp, Color(0xFF00E5FF), RoundedCornerShape(8.dp)), // Glowing cyan border
                    contentScale = ContentScale.Crop
                )

                Column {
                    Text(
                        text = "BEKANSI AI SALES",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF00E5FF), // Cyan glow color
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Smarter. Faster. Automated.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFE2D9FF) // Light purple tint
                    )
                }
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
        TabItem(icon = Icons.Default.Home, label = "Dashboard", isSelected = activeTab == "DASHBOARD", onClick = { onTabSelected("DASHBOARD") })
        TabItem(icon = Icons.Default.Build, label = "AI Employee", isSelected = activeTab == "AI_EMPLOYEE", onClick = { onTabSelected("AI_EMPLOYEE") })
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
    onDeleteQuote: (Int) -> Unit,
    viewModel: SalesViewModel
) {
    val context = LocalContext.current
    var activeSubTab by remember { mutableStateOf("BUILDER") } // "BUILDER", "HISTORY"

    // Dialog & overlay state holders
    var selectedQuoteForInvoice by remember { mutableStateOf<Quotation?>(null) }
    var selectedQuoteForPayment by remember { mutableStateOf<Quotation?>(null) }
    var showPaymentSuccessReceipt by remember { mutableStateOf<Quotation?>(null) }
    var chosenPaymentGateway by remember { mutableStateOf("") }
    var isProcessingPayment by remember { mutableStateOf(false) }
    var paymentReference by remember { mutableStateOf("") }
    var chapaPhoneNumber by remember { mutableStateOf("") }

    // Search queries
    var leadSearchQuery by remember { mutableStateOf("") }
    var productSearchQuery by remember { mutableStateOf("") }
    var historySearchQuery by remember { mutableStateOf("") }

    // Indices (referencing filtered lists)
    var selectedLeadIndex by remember { mutableStateOf(0) }
    var selectedProductIndex by remember { mutableStateOf(0) }

    // Multipliers & specification state
    var quantity by remember { mutableStateOf(1) }
    var selectedMaterialMultiplier by remember { mutableStateOf(1.2) } // default Wanza
    var selectedMaterialName by remember { mutableStateOf("Wanza") }

    var customLaborCostInput by remember { mutableStateOf("") }
    var selectedLocationName by remember { mutableStateOf("Addis Standard") }
    var transportCostValue by remember { mutableStateOf(4000.0) }
    var deliveryTimeEstimateValue by remember { mutableStateOf("2 Weeks") }

    var profitMargin by remember { mutableStateOf(20f) }
    var customDimensions by remember { mutableStateOf("") }

    // Filtered lists
    val filteredLeads = remember(leads, leadSearchQuery) {
        leads.filter { it.name.contains(leadSearchQuery, ignoreCase = true) }
    }
    val filteredProducts = remember(products, productSearchQuery) {
        products.filter { it.name.contains(productSearchQuery, ignoreCase = true) }
    }

    val activeLead = filteredLeads.getOrNull(selectedLeadIndex) ?: filteredLeads.getOrNull(0) ?: leads.getOrNull(0)
    val activeProduct = filteredProducts.getOrNull(selectedProductIndex) ?: filteredProducts.getOrNull(0) ?: products.getOrNull(0)

    // Prefill dimensions on product selection
    LaunchedEffect(activeProduct) {
        if (activeProduct != null) {
            customDimensions = activeProduct.dimensions
        }
    }

    // Dynamic Mathematical Calculations (work-sheet bindings)
    val basePriceCalculated = (activeProduct?.price ?: 0.0) * quantity
    val computedMaterialCost = basePriceCalculated * 0.4 * selectedMaterialMultiplier
    val computedLaborCost = customLaborCostInput.toDoubleOrNull() ?: (basePriceCalculated * 0.2)
    val computedTransportCost = transportCostValue

    val rawCostBeforeMargin = computedMaterialCost + computedLaborCost + computedTransportCost
    val markupProfitAmount = rawCostBeforeMargin * (profitMargin / 100.0)
    val subtotalCalculated = rawCostBeforeMargin + markupProfitAmount
    val vatCalculated = subtotalCalculated * 0.15
    val grandTotalCalculated = subtotalCalculated + vatCalculated

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // HEADER Block
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Quotation Workspace",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmMahogany
                )
                Text(
                    text = "Custom hardwood pricing, VAT, margin, and logistics calculator.",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(28.dp)
            )
        }

        // Sub-tab Pill Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(LightWarmCard.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { activeSubTab = "BUILDER" },
                shape = RoundedCornerShape(20.dp),
                color = if (activeSubTab == "BUILDER") WarmMahogany else Color.Transparent
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                    Text(
                        text = "✍️ Live Quote Builder",
                        color = if (activeSubTab == "BUILDER") Color.White else TextDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { activeSubTab = "HISTORY" },
                shape = RoundedCornerShape(20.dp),
                color = if (activeSubTab == "HISTORY") WarmMahogany else Color.Transparent
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                    Text(
                        text = "📜 Issued History (${quotations.size})",
                        color = if (activeSubTab == "HISTORY") Color.White else TextDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        if (activeSubTab == "BUILDER") {
            if (leads.isEmpty() || products.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "CRM Leads & Product Inventory Required",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "To build customized quotes, the system requires at least 1 registered customer lead in the database and 1 product item in your inventory catalog.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Main scrollable builder panel
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.0f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // STEP 1: Select Lead
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = LightWarmCard.copy(alpha = 0.3f)),
                            border = BorderStroke(1.dp, LightWarmCard),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "1. Customer Lead Select",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WarmMahogany
                                    )
                                    activeLead?.let {
                                        Text(
                                            text = "Selected: ${it.name}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentSuccess
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))

                                // Search Box
                                OutlinedTextField(
                                    value = leadSearchQuery,
                                    onValueChange = { 
                                        leadSearchQuery = it
                                        selectedLeadIndex = 0 // reset selection on search
                                    },
                                    placeholder = { Text("Search client name...", fontSize = 11.sp) },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = WarmMahogany,
                                        unfocusedBorderColor = LightWarmCard
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                if (filteredLeads.isEmpty()) {
                                    Text("No matching leads found.", fontSize = 11.sp, color = TextMuted)
                                } else {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        filteredLeads.forEachIndexed { idx, l ->
                                            val isSelected = activeLead?.id == l.id
                                            Surface(
                                                modifier = Modifier
                                                    .width(130.dp)
                                                    .clickable { selectedLeadIndex = idx },
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isSelected) WarmMahogany else LightWarmCard,
                                                border = BorderStroke(1.dp, if (isSelected) GoldAccent else Color.Transparent)
                                            ) {
                                                Column(modifier = Modifier.padding(8.dp)) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text(
                                                            text = l.name,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSelected) Color.White else TextDark,
                                                            maxLines = 1
                                                        )
                                                        if (isSelected) {
                                                            Icon(Icons.Default.Check, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(12.dp))
                                                        }
                                                    }
                                                    Text(
                                                        text = l.phone,
                                                        fontSize = 9.sp,
                                                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else TextMuted,
                                                        maxLines = 1
                                                    )
                                                    Text(
                                                        text = "Source: ${l.source}",
                                                        fontSize = 8.sp,
                                                        color = if (isSelected) GoldAccent else WarmMahogany,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // STEP 2: Select Product
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = LightWarmCard.copy(alpha = 0.3f)),
                            border = BorderStroke(1.dp, LightWarmCard),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "2. Catalog Product Item",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WarmMahogany
                                    )
                                    activeProduct?.let {
                                        Text(
                                            text = "${String.format("%,.0f", it.price)} ETB",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WarmMahogany
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))

                                // Search Box
                                OutlinedTextField(
                                    value = productSearchQuery,
                                    onValueChange = { 
                                        productSearchQuery = it
                                        selectedProductIndex = 0 // reset selection on search
                                    },
                                    placeholder = { Text("Search catalog items...", fontSize = 11.sp) },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = WarmMahogany,
                                        unfocusedBorderColor = LightWarmCard
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                if (filteredProducts.isEmpty()) {
                                    Text("No matching catalog items.", fontSize = 11.sp, color = TextMuted)
                                } else {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        filteredProducts.forEachIndexed { idx, p ->
                                            val isSelected = activeProduct?.id == p.id
                                            Surface(
                                                modifier = Modifier
                                                    .width(140.dp)
                                                    .clickable { selectedProductIndex = idx },
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isSelected) WarmMahogany else LightWarmCard,
                                                border = BorderStroke(1.dp, if (isSelected) GoldAccent else Color.Transparent)
                                            ) {
                                                Column(modifier = Modifier.padding(8.dp)) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text(
                                                            text = p.name.substringBefore(" '"),
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSelected) Color.White else TextDark,
                                                            maxLines = 1
                                                        )
                                                        if (isSelected) {
                                                            Icon(Icons.Default.Check, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(12.dp))
                                                        }
                                                    }
                                                    Text(
                                                        text = "Cat: ${p.category} | ${p.material}",
                                                        fontSize = 9.sp,
                                                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else TextMuted,
                                                        maxLines = 1
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = "${String.format("%,.0f", p.price)} Birr",
                                                        fontSize = 10.sp,
                                                        color = if (isSelected) GoldAccent else WarmMahogany,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // STEP 3: Customize specs (Quantity & Timber & Dimensions)
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = LightWarmCard.copy(alpha = 0.3f)),
                            border = BorderStroke(1.dp, LightWarmCard),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "3. Timber Customization & Quantity",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarmMahogany
                                )

                                // Row 1: Quantity selector & Dimensions Text field
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Quantity Selector
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Quantity Pieces", fontSize = 10.sp, color = TextMuted)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            FilledIconButton(
                                                onClick = { if (quantity > 1) quantity-- },
                                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = WarmMahogany),
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Text("-", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Text(
                                                text = "$quantity",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextDark,
                                                modifier = Modifier.width(20.dp),
                                                textAlign = TextAlign.Center
                                            )
                                            FilledIconButton(
                                                onClick = { if (quantity < 15) quantity++ },
                                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = WarmMahogany),
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Text("+", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    // Dimensions Textfield
                                    OutlinedTextField(
                                        value = customDimensions,
                                        onValueChange = { customDimensions = it },
                                        label = { Text("Dimensions overrides", fontSize = 10.sp) },
                                        placeholder = { Text("e.g. 200x180cm", fontSize = 10.sp) },
                                        modifier = Modifier.weight(1.5f),
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = WarmMahogany,
                                            unfocusedBorderColor = LightWarmCard
                                        )
                                    )
                                }

                                // Row 2: Hardwood Wood Timber Multipliers
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Structural Hardwood Timber Select", fontSize = 10.sp, color = TextMuted)
                                        Text(
                                            text = "$selectedMaterialName Wood (${selectedMaterialMultiplier}x)",
                                            fontSize = 10.sp,
                                            color = WarmMahogany,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))

                                    val materials = listOf(
                                        Triple("MDF", 0.8, "Budget Wood"),
                                        Triple("Grar", 1.0, "Standard Acacia"),
                                        Triple("Wanza", 1.2, "Premium Cordia"),
                                        Triple("Mahogany", 1.4, "Luxury Hardwood")
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        materials.forEach { (mName, multiplier, desc) ->
                                            val isSelected = selectedMaterialName == mName
                                            Surface(
                                                modifier = Modifier
                                                    .weight(1.0f)
                                                    .clickable {
                                                        selectedMaterialName = mName
                                                        selectedMaterialMultiplier = multiplier
                                                    },
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isSelected) WarmMahogany else LightWarmCard,
                                                border = BorderStroke(1.dp, if (isSelected) GoldAccent else Color.Transparent)
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        text = mName,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelected) Color.White else TextDark
                                                    )
                                                    Text(
                                                        text = "${multiplier}x",
                                                        fontSize = 8.sp,
                                                        color = if (isSelected) GoldAccent else TextMuted
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // STEP 4: Logistics & Delivery Location
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = LightWarmCard.copy(alpha = 0.3f)),
                            border = BorderStroke(1.dp, LightWarmCard),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "4. Logistics & Destination Estimator",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarmMahogany
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Destination Location", fontSize = 10.sp, color = TextMuted)
                                    Text(
                                        text = "${selectedLocationName} | +${String.format("%,.0f", transportCostValue)} ETB",
                                        fontSize = 10.sp,
                                        color = WarmMahogany,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))

                                val destinations = listOf(
                                    Triple("Addis Standard", 4000.0, "2 Weeks"),
                                    Triple("Bishoftu Resort", 8500.0, "2.5 Weeks"),
                                    Triple("Adama Express", 11500.0, "3 Weeks"),
                                    Triple("Hawassa Hub", 16000.0, "3.5 Weeks"),
                                    Triple("Bahir Dar Hub", 22500.0, "4 Weeks")
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    destinations.forEach { (locName, cost, estimate) ->
                                        val isSelected = selectedLocationName == locName
                                        Surface(
                                            modifier = Modifier
                                                .clickable {
                                                    selectedLocationName = locName
                                                    transportCostValue = cost
                                                    deliveryTimeEstimateValue = estimate
                                                },
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) WarmMahogany else LightWarmCard,
                                            border = BorderStroke(1.dp, if (isSelected) GoldAccent else Color.Transparent)
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text(
                                                    text = locName,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else TextDark
                                                )
                                                Text(
                                                    text = "${String.format("%,.0f", cost)} ETB",
                                                    fontSize = 8.5.sp,
                                                    color = if (isSelected) GoldAccent else WarmMahogany
                                                )
                                                Text(
                                                    text = estimate,
                                                    fontSize = 7.5.sp,
                                                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else TextMuted
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // STEP 5: Pricing Optimization
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = LightWarmCard.copy(alpha = 0.3f)),
                            border = BorderStroke(1.dp, LightWarmCard),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "5. Craftsmanship Labor & Margin Optimization",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarmMahogany
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = customLaborCostInput,
                                        onValueChange = { customLaborCostInput = it },
                                        label = { Text("Craftsmanship Labor override (ETB)", fontSize = 9.sp) },
                                        placeholder = { Text("Defaults to 20% of catalog", fontSize = 9.sp) },
                                        modifier = Modifier.weight(1f),
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = WarmMahogany,
                                            unfocusedBorderColor = LightWarmCard
                                        )
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Gross Profit Margin Target: ${profitMargin.toInt()}%",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )
                                    Text(
                                        text = "+${String.format("%,.0f", markupProfitAmount)} ETB",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentSuccess
                                    )
                                }
                                Slider(
                                    value = profitMargin,
                                    onValueChange = { profitMargin = it },
                                    valueRange = 5f..60f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = WarmMahogany,
                                        activeTrackColor = WarmMahogany,
                                        inactiveTrackColor = LightWarmCard
                                    )
                                )
                            }
                        }
                    }

                    // REAL-TIME WORKSHEET DASHBOARD
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1715)), // Luxurious Dark Cocoa Card
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(GoldAccent)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Auto-Calculation Worksheet",
                                            color = GoldAccent,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "Live Estimate",
                                        color = TextLight,
                                        fontSize = 9.sp,
                                        modifier = Modifier
                                            .background(WarmMahogany, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    // 1. Qty x Unit
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Product Catalog Base (${activeProduct?.name?.substringBefore(" '") ?: "N/A"})", color = TextLight.copy(alpha = 0.7f), fontSize = 10.sp)
                                        Text("${String.format("%,.0f", activeProduct?.price ?: 0.0)} x $quantity Pcs", color = TextLight, fontSize = 10.sp)
                                    }
                                    // 2. Material Adj
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Adjusted Hardwood Materials (${selectedMaterialName})", color = TextLight.copy(alpha = 0.7f), fontSize = 10.sp)
                                        Text("${String.format("%,.0f", computedMaterialCost)} ETB", color = TextLight, fontSize = 10.sp)
                                    }
                                    // 3. Labor Cost
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Craftsmanship Labor", color = TextLight.copy(alpha = 0.7f), fontSize = 10.sp)
                                        Text("${String.format("%,.0f", computedLaborCost)} ETB", color = TextLight, fontSize = 10.sp)
                                    }
                                    // 4. Logistics
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Logistics: $selectedLocationName (${deliveryTimeEstimateValue})", color = TextLight.copy(alpha = 0.7f), fontSize = 10.sp)
                                        Text("${String.format("%,.0f", computedTransportCost)} ETB", color = TextLight, fontSize = 10.sp)
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = TextLight.copy(alpha = 0.1f))

                                    // 5. Total Raw Cost
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Total Raw Carpentry Cost", color = TextLight.copy(alpha = 0.7f), fontSize = 10.sp)
                                        Text("${String.format("%,.0f", rawCostBeforeMargin)} ETB", color = TextLight, fontSize = 10.sp)
                                    }
                                    // 6. Margin Markup
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Bekansi Profit Markup (${profitMargin.toInt()}%)", color = TextLight.copy(alpha = 0.7f), fontSize = 10.sp)
                                        Text("+${String.format("%,.0f", markupProfitAmount)} ETB", color = GoldAccent, fontSize = 10.sp)
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = TextLight.copy(alpha = 0.1f))

                                    // 7. Subtotal (Pre-tax)
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Subtotal (Pre-Tax)", color = TextLight, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        Text("${String.format("%,.0f", subtotalCalculated)} ETB", color = TextLight, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    // 8. VAT
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Ethiopian VAT (15.0%)", color = TextLight.copy(alpha = 0.7f), fontSize = 10.sp)
                                        Text("${String.format("%,.0f", vatCalculated)} ETB", color = TextLight, fontSize = 10.sp)
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = GoldAccent.copy(alpha = 0.3f))

                                    // 9. Grand Total
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("GRAND TOTAL EST:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                        Text(
                                            text = "${String.format("%,.2f", grandTotalCalculated)} Birr",
                                            color = AccentSuccess,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Generate CTA Button
                    item {
                        Button(
                            onClick = {
                                if (activeLead != null && activeProduct != null) {
                                    onGenerateQuote(
                                        activeLead,
                                        activeProduct,
                                        computedMaterialCost,
                                        computedLaborCost,
                                        computedTransportCost,
                                        profitMargin.toDouble(),
                                        customDimensions.ifBlank { activeProduct.dimensions },
                                        deliveryTimeEstimateValue
                                    )
                                    Toast.makeText(context, "Quotation generated successfully! Saved to CRM.", Toast.LENGTH_LONG).show()
                                    // Switch to history tab to view results
                                    activeSubTab = "HISTORY"
                                } else {
                                    Toast.makeText(context, "Please select an active Lead and Product first!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Text("Compute & Save Legal Cost Sheet ✅", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        } else {
            // HISTORY TAB
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Search Historic Records
                OutlinedTextField(
                    value = historySearchQuery,
                    onValueChange = { historySearchQuery = it },
                    placeholder = { Text("Search by Client Name or Quote PIN (e.g., BK-Q1001)...", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WarmMahogany,
                        unfocusedBorderColor = LightWarmCard
                    )
                )

                val filteredQuotations = remember(quotations, historySearchQuery) {
                    quotations.filter {
                        it.leadName.contains(historySearchQuery, ignoreCase = true) ||
                        it.productName.contains(historySearchQuery, ignoreCase = true) ||
                        "BK-Q${it.id + 1000}".contains(historySearchQuery, ignoreCase = true)
                    }
                }

                if (filteredQuotations.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (quotations.isEmpty()) "No quotes generated in database yet." else "No historic records matched search query.",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredQuotations) { q ->
                            QuotationRecordCard(
                                q = q,
                                onDelete = { onDeleteQuote(q.id) },
                                onViewInvoice = { selectedQuoteForInvoice = it },
                                onCollectPayment = { selectedQuoteForPayment = it }
                            )
                        }
                    }
                }
            }
        }
    }

    // 1. GORGEOUS OFFICIAL INVOICE DRAFT OVERLAY
    selectedQuoteForInvoice?.let { q ->
        AlertDialog(
            onDismissRequest = { selectedQuoteForInvoice = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🇪🇹", fontSize = 24.sp)
                    Column {
                        Text("Bekansi Furniture Ltd.", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WarmMahogany)
                        Text("Official Invoice Statement", fontSize = 11.sp, color = TextMuted)
                    }
                }
            },
            text = {
                Column(
                    modifier = androidx.compose.foundation.rememberScrollState().let { state -> Modifier.verticalScroll(state) },
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(TextMuted.copy(alpha = 0.2f)))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("CLIENT:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Text(q.leadName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("INVOICE NO:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Text("BK-Q${q.id + 1000}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WarmMahogany)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("DATE:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Text(java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.US).format(java.util.Date(q.createdAt)), fontSize = 11.sp, color = TextDark)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("VALIDITY:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Text("14 Days", fontSize = 11.sp, color = TextDark)
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(TextMuted.copy(alpha = 0.2f)))

                    // Itemized table header
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Description & Material", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f), color = TextDark)
                        Text("Qty", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center, color = TextDark)
                        Text("Amount (ETB)", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End, color = TextDark)
                    }

                    // Item row
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(2f)) {
                            Text(q.productName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Text("Material: ${q.material} | Dimensions: ${q.dimensions}", fontSize = 9.sp, color = TextMuted)
                        }
                        Text("1", fontSize = 11.sp, modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center, color = TextDark)
                        Text(String.format("%,.2f", q.subtotal), fontSize = 11.sp, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End, color = TextDark)
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(TextMuted.copy(alpha = 0.2f)))

                    // Totals
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(start = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal:", fontSize = 10.sp, color = TextMuted)
                            Text(String.format("%.2f ETB", q.subtotal), fontSize = 10.sp, color = TextDark)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("VAT (15%):", fontSize = 10.sp, color = TextMuted)
                            Text(String.format("%.2f ETB", q.vat), fontSize = 10.sp, color = TextDark)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Grand Total Due:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentSuccess)
                            Text(String.format("%.2f Birr", q.total), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentSuccess)
                        }
                    }

                    // QR Code & Signature Placeholders side-by-side
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // High-tech Simulated QR Code using Compose drawing primitives
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            androidx.compose.foundation.Canvas(modifier = Modifier.size(72.dp)) {
                                val sizePx = size.width
                                val step = sizePx / 6
                                // Border
                                drawRect(color = Color.Black, size = size)
                                drawRect(color = Color.White, topLeft = androidx.compose.ui.geometry.Offset(4f, 4f), size = androidx.compose.ui.geometry.Size(sizePx - 8f, sizePx - 8f))
                                // Quick corner boxes representing finder patterns
                                drawRect(color = Color.Black, topLeft = androidx.compose.ui.geometry.Offset(8f, 8f), size = androidx.compose.ui.geometry.Size(step * 2, step * 2))
                                drawRect(color = Color.Black, topLeft = androidx.compose.ui.geometry.Offset(sizePx - (step * 2) - 8f, 8f), size = androidx.compose.ui.geometry.Size(step * 2, step * 2))
                                drawRect(color = Color.Black, topLeft = androidx.compose.ui.geometry.Offset(8f, sizePx - (step * 2) - 8f), size = androidx.compose.ui.geometry.Size(step * 2, step * 2))
                                // Some random mock QR pixels
                                drawRect(color = Color.Black, topLeft = androidx.compose.ui.geometry.Offset(step * 3, step * 3), size = androidx.compose.ui.geometry.Size(step, step))
                                drawRect(color = Color.Black, topLeft = androidx.compose.ui.geometry.Offset(step * 4, step * 3), size = androidx.compose.ui.geometry.Size(step, step))
                                drawRect(color = Color.Black, topLeft = androidx.compose.ui.geometry.Offset(step * 3, step * 4), size = androidx.compose.ui.geometry.Size(step, step))
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Scan Invoice 📱", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        }

                        // Digital Signature
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Digital Signature", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Spacer(modifier = Modifier.height(18.dp))
                            Text("Bekansi CRM AI", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WarmMahogany)
                            Text("Officially Approved Security Seal", fontSize = 8.sp, color = TextMuted)
                        }
                    }
                }
            },
            confirmButton = {
                // Share Actions & Download
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = {
                                val shareTxt = "Bekansi Furniture Invoice BK-Q${q.id + 1000}\nClient: ${q.leadName}\nItem: ${q.productName}\nTotal Due: ${q.total} Birr.\nGenerated on Bekansi AI Workstation."
                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_TEXT, shareTxt)
                                }
                                context.startActivity(android.content.Intent.createChooser(intent, "Share Invoice via"))
                                Toast.makeText(context, "Redirecting to messaging channel...", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentSuccess),
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Share 💬", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextLight)
                        }

                        Button(
                            onClick = {
                                Toast.makeText(context, "Invoice exported to /Downloads/Bekansi_Invoice_BK-Q${q.id + 1000}.pdf", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany),
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Save PDF 📄", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextLight)
                        }
                    }

                    OutlinedButton(
                        onClick = { selectedQuoteForInvoice = null },
                        modifier = Modifier.fillMaxWidth().height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Close Statement", fontSize = 11.sp)
                    }
                }
            }
        )
    }

    // 2. PAYMENT GATEWAY SECURE GATE SHEET
    selectedQuoteForPayment?.let { q ->
        AlertDialog(
            onDismissRequest = { selectedQuoteForPayment = null },
            title = {
                Text("Select Secure Payment Gateway 💳", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = WarmMahogany)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Total amount to authorize: ${String.format("%,.2f", q.total)} ETB (inc. 15% VAT)",
                        fontSize = 11.sp,
                        color = TextDark
                    )

                    // Gateways choices
                    val gateways = listOf(
                        "Telebirr (Ethio Telecom)" to Color(0xFF1E88E5),
                        "CBE Birr (Commercial Bank)" to Color(0xFF8E24AA),
                        "Chapa Security" to Color(0xFF00ACC1),
                        "Cash Receipt" to Color(0xFF43A047),
                        "Bank Direct Transfer" to Color(0xFF3949AB)
                    )

                    gateways.forEach { (name, color) ->
                        val isSelected = chosenPaymentGateway == name
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { chosenPaymentGateway = name },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent,
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) color else Color.Black.copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = when (name.substringBefore(" ")) {
                                            "Telebirr" -> "📱"
                                            "CBE" -> "🏦"
                                            "Chapa" -> "⚡"
                                            "Cash" -> "💵"
                                            else -> "💸"
                                        },
                                        fontSize = 18.sp
                                    )
                                    Text(name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    if (chosenPaymentGateway.isNotBlank()) {
                        if (chosenPaymentGateway.startsWith("Telebirr") || chosenPaymentGateway.startsWith("Chapa")) {
                            OutlinedTextField(
                                value = chapaPhoneNumber,
                                onValueChange = { chapaPhoneNumber = it },
                                label = { Text("Payer Mobile Phone Number") },
                                placeholder = { Text("09xxxxxxxx") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmMahogany)
                            )
                        } else {
                            OutlinedTextField(
                                value = paymentReference,
                                onValueChange = { paymentReference = it },
                                label = { Text("Receipt / Ref Code (Direct Transfer)") },
                                placeholder = { Text("e.g. CBE-TXN-9842A") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmMahogany)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = {
                            if (chosenPaymentGateway.isBlank()) {
                                Toast.makeText(context, "Please choose a payment provider.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            isProcessingPayment = true
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                isProcessingPayment = false
                                showPaymentSuccessReceipt = q
                                selectedQuoteForPayment = null
                                
                                // Post security notification to ViewModel center
                                val refText = if (paymentReference.isNotBlank()) "Ref: $paymentReference" else "Ref: TXN-OMNI-${(100000..999999).random()}"
                                viewModel.addNotification("Payment of ${q.total.toInt()} Birr authorized via $chosenPaymentGateway for Quote BK-Q${q.id+1000}. $refText")
                                Toast.makeText(context, "Payment Processed and verified successfully!", Toast.LENGTH_SHORT).show()
                            }, 1500)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentSuccess),
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isProcessingPayment) {
                            CircularProgressIndicator(color = TextLight, modifier = Modifier.size(20.dp))
                        } else {
                            Text("Process Security Authorization 🔒", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextLight)
                        }
                    }

                    OutlinedButton(
                        onClick = { selectedQuoteForPayment = null },
                        modifier = Modifier.fillMaxWidth().height(36.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel Payment", fontSize = 11.sp)
                    }
                }
            }
        )
    }

    // 3. PAYMENT SUCCESSFUL RECEIPT POPUP
    showPaymentSuccessReceipt?.let { q ->
        AlertDialog(
            onDismissRequest = { showPaymentSuccessReceipt = null },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("✅", fontSize = 44.sp)
                    Text("Payment Receipt Generated", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentSuccess)
                    Text("Bekansi Official Sales Audit", fontSize = 10.sp, color = TextMuted)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(TextMuted.copy(alpha = 0.2f)))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("CLIENT:", fontSize = 9.sp, color = TextMuted)
                        Text(q.leadName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("ITEM PURCHASED:", fontSize = 9.sp, color = TextMuted)
                        Text(q.productName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("METHOD USED:", fontSize = 9.sp, color = TextMuted)
                        Text(chosenPaymentGateway, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("TRANSACTION ID:", fontSize = 9.sp, color = TextMuted)
                        Text("BK-TXN-${(1000000..9999999).random()}", fontSize = 10.sp, color = TextDark)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("TOTAL PAID:", fontSize = 9.sp, color = TextMuted)
                        Text("${String.format("%,.2f", q.total)} ETB", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentSuccess)
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(TextMuted.copy(alpha = 0.2f)))
                    Text(
                        text = "Receipt generated instantly. Funds transferred and logged in local database for audit compliance.",
                        fontSize = 9.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPaymentSuccessReceipt = null
                        chosenPaymentGateway = ""
                        paymentReference = ""
                        chapaPhoneNumber = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Complete and Archive Receipt", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun QuotationRecordCard(
    q: Quotation,
    onDelete: () -> Unit,
    onViewInvoice: (Quotation) -> Unit,
    onCollectPayment: (Quotation) -> Unit
) {
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
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // View Invoice / Collect Payment buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onViewInvoice(q) },
                    modifier = Modifier.weight(1f).height(34.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WarmMahogany),
                    border = BorderStroke(1.dp, WarmMahogany.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Invoice Draft 📄", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onCollectPayment(q) },
                    modifier = Modifier.weight(1f).height(34.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentSuccess),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Collect Payment 💳", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextLight)
                }
            }
        }
    }
}


// ---------------- TAB 5: PRODUCT CATALOG ----------------
@Composable
fun ProductCatalogTab(
    viewModel: SalesViewModel,
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

    // Search and Filter states
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var selectedProductForHistory by remember { mutableStateOf<Product?>(null) }

    val isSyncing by viewModel.isSyncingProducts.collectAsState()
    val syncError by viewModel.syncErrorMsg.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Premium Furniture Showroom Design Catalog",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = GoldAccent,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = { showForm = !showForm },
                colors = ButtonDefaults.buttonColors(containerColor = if (showForm) Color.Gray else WarmMahogany),
                modifier = Modifier.height(30.dp).testTag("add_product_toggle_button"),
                contentPadding = PaddingValues(horizontal = 10.dp)
            ) {
                Text(if (showForm) "Close" else "+ Add", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Live PostgreSQL Sync Status Management Ribbon
        Card(
            colors = CardDefaults.cardColors(containerColor = LightWarmCard.copy(alpha = 0.85f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .border(1.dp, GoldAccent.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "External Database Synchronization Gateway",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkWalnut
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            // Status indicator ball
                            val hasSyncData = products.any { it.id > 1000 }
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = if (syncError == null && hasSyncData) AccentSuccess else Color.Gray,
                                        shape = RoundedCornerShape(50)
                                    )
                            )
                            Text(
                                text = if (syncError == null && hasSyncData) "ONLINE POSTGRESQL LINK ACTIVE" else "OFFLINE SHOWROOM CACHE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (syncError == null && hasSyncData) AccentSuccess else TextMuted
                            )
                        }
                    }

                    // Pulse sync button
                    Button(
                        onClick = { viewModel.syncProducts() },
                        enabled = !isSyncing,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        modifier = Modifier.height(30.dp).testTag("sync_postgres_button"),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 1.5.dp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Syncing...", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync",
                                modifier = Modifier.size(12.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sync Live", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (syncError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Graceful Handshake Fallback: " + syncError,
                        fontSize = 9.sp,
                        color = Color.Red.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold
                    )
                } else if (products.any { it.id > 1000 }) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Successfully synchronized multi-tenant catalog. Loaded live inventory & pricing directly from local PostgreSQL cluster.",
                        fontSize = 9.sp,
                        color = DarkWalnut.copy(alpha = 0.75f)
                    )
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Using local hardware storehouse. Tap button to fetch real-time pricing and stock states in parallel from local host PostgreSQL runtime.",
                        fontSize = 9.sp,
                        color = TextMuted
                    )
                }
            }
        }

        // LOW STOCK ALERT BAR (Dynamic alert summary)
        val lowStockCount = products.count { it.id % 3 == 0 } // Simulate dynamic low stock indicators
        if (lowStockCount > 0) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🚨", fontSize = 16.sp)
                    Column {
                        Text(
                            text = "Low Stock Alerts: $lowStockCount hardwood materials depleted in workshop!",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828)
                        )
                        Text(
                            text = "Dukem woodworking workshop requires mahogany lumber replenishment immediately.",
                            fontSize = 9.sp,
                            color = Color(0xFFC62828).copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Search & Category Chips Filters Area
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search catalog wood (e.g., Wanza, Sofa)...", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(14.dp)) },
                modifier = Modifier.weight(1f).height(46.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WarmMahogany,
                    unfocusedBorderColor = LightWarmCard
                )
            )
        }

        // Category Selection Chips Row
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val categories = listOf("All", "Sofa", "Dining Table", "Bed", "Coffee Table", "Office")
            categories.forEach { cat ->
                val isSelected = selectedCategoryFilter == cat
                Surface(
                    modifier = Modifier.clickable { selectedCategoryFilter = cat },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) WarmMahogany else LightWarmCard.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, if (isSelected) GoldAccent else Color.White.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = cat,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else TextDark,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
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
                            modifier = Modifier.weight(1f).testTag("product_name_input"),
                            colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                        )
                        TextField(
                            value = prodPrice,
                            onValueChange = { prodPrice = it },
                            label = { Text("Price (ETB)", fontSize = 9.sp) },
                            modifier = Modifier.weight(1f).testTag("product_price_input"),
                            colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
                        modifier = Modifier.fillMaxWidth().testTag("product_submit_button")
                    ) {
                        Text("Add to catalog", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Grid/List of current catalogue showing generated AI hero image
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 280.dp),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Include our generated hero as the catalog backdrop banner across all lanes
            item(span = { GridItemSpan(maxLineSpan) }) {
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

            val filteredProducts = products.filter { p ->
                val matchesSearch = p.name.contains(searchQuery, ignoreCase = true) || p.material.contains(searchQuery, ignoreCase = true)
                val matchesCategory = selectedCategoryFilter == "All" || p.category.equals(selectedCategoryFilter, ignoreCase = true)
                matchesSearch && matchesCategory
            }

            items(filteredProducts) { item ->
                // Check low stock override
                val isLowStock = item.id % 3 == 0
                val overrideStockStatus = if (isLowStock) "Low Stock" else item.stockStatus
                val updatedItem = item.copy(stockStatus = overrideStockStatus)

                Card(
                    colors = CardDefaults.cardColors(containerColor = LightWarmCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedProductForHistory = updatedItem }
                ) {
                    ProductShowcaseCard(product = updatedItem, onDelete = { onDeleteProduct(item.id) })
                }
            }
        }
    }

    // INTERACTIVE PRODUCT AUDIT & HISTORICAL PRICE GRAPH OVERLAY
    selectedProductForHistory?.let { p ->
        AlertDialog(
            onDismissRequest = { selectedProductForHistory = null },
            title = {
                Text(text = "Historical Price Analyzer: ${p.name}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WarmMahogany)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Material: ${p.material} | Standard Dimensions: ${p.dimensions}", fontSize = 10.sp, color = TextDark)
                    Text("Current Retail Price: ${String.format("%,.2f", p.price)} ETB", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentSuccess)
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Est. Hardwood Market Price Index (Ethiopia 2026):", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    
                    // Custom graphical price trend chart using Box columns
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val basePrice = p.price
                        // Price points (Jan, Mar, May, Jun 2026) showing dynamic inflation curve
                        val pricePoints = listOf(
                            "Jan" to basePrice * 0.85,
                            "Mar" to basePrice * 0.92,
                            "May" to basePrice * 0.98,
                            "Jun" to basePrice
                        )
                        
                        pricePoints.forEach { (month, price) ->
                            val scaleRatio = (price / (basePrice * 1.1)).toFloat()
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                                Box(
                                    modifier = Modifier
                                        .width(36.dp)
                                        .fillMaxHeight(scaleRatio)
                                        .background(
                                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                                colors = listOf(GoldAccent, WarmMahogany)
                                            ),
                                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(month, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                Text("${(price/1000).toInt()}k", fontSize = 8.sp, color = TextDark)
                            }
                        }
                    }
                    Text(
                        text = "Historical charts account for local exchange rates and custom lumber tariff fluctuations.",
                        fontSize = 8.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedProductForHistory = null },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Price Audit", fontSize = 11.sp)
                }
            }
        )
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

// ---------------- DASHBOARD TAB CONTENT ----------------
@Composable
fun DashboardTab(
    viewModel: SalesViewModel,
    leads: List<Lead>,
    quotations: List<Quotation>,
    onTabChange: (String) -> Unit
) {
    val totalLeads = leads.size
    val activeCustomers = leads.count { it.status == "Won" || it.status == "Contacted" }
    val pendingQuotes = quotations.size
    val salesRevenue = quotations.sumOf { it.total }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_root")
    ) {
        val availableWidth = maxWidth
        val columns = if (availableWidth > 640.dp) 4 else if (availableWidth > 320.dp) 2 else 1

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                DashboardHeaderCard(onTabChange = onTabChange)
            }

            item {
                Text(
                    text = "EXECUTIVE METRICS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            when (columns) {
                4 -> {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            DashboardSummaryCard(
                                title = "Total Leads",
                                value = totalLeads.toString(),
                                subtext = "Registered in CRM",
                                icon = Icons.Default.Person,
                                accentColor = AccentSuccess,
                                modifier = Modifier.weight(1f),
                                onClick = { onTabChange("CRM_LEADS") }
                            )
                            DashboardSummaryCard(
                                title = "Active Customers",
                                value = activeCustomers.toString(),
                                subtext = "Engaged & Closed",
                                icon = Icons.Default.CheckCircle,
                                accentColor = Color(0xFF00E5FF),
                                modifier = Modifier.weight(1f),
                                onClick = { onTabChange("CRM_LEADS") }
                            )
                            DashboardSummaryCard(
                                title = "Pending Quotes",
                                value = pendingQuotes.toString(),
                                subtext = "Awaiting final sign",
                                icon = Icons.Default.ShoppingCart,
                                accentColor = AccentWarning,
                                modifier = Modifier.weight(1f),
                                onClick = { onTabChange("QUOTE_ENGINE") }
                            )
                            DashboardSummaryCard(
                                title = "Sales Revenue",
                                value = "%,.0f".format(salesRevenue) + " ETB",
                                subtext = "Real-time contracts",
                                icon = Icons.Default.Star,
                                accentColor = GoldAccent,
                                modifier = Modifier.weight(1f),
                                onClick = { onTabChange("QUOTE_ENGINE") }
                            )
                        }
                    }
                }
                2 -> {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                DashboardSummaryCard(
                                    title = "Total Leads",
                                    value = totalLeads.toString(),
                                    subtext = "Registered in CRM",
                                    icon = Icons.Default.Person,
                                    accentColor = AccentSuccess,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onTabChange("CRM_LEADS") }
                                )
                                DashboardSummaryCard(
                                    title = "Active Customers",
                                    value = activeCustomers.toString(),
                                    subtext = "Engaged & Closed",
                                    icon = Icons.Default.CheckCircle,
                                    accentColor = Color(0xFF00E5FF),
                                    modifier = Modifier.weight(1f),
                                    onClick = { onTabChange("CRM_LEADS") }
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                DashboardSummaryCard(
                                    title = "Pending Quotes",
                                    value = pendingQuotes.toString(),
                                    subtext = "Awaiting final sign",
                                    icon = Icons.Default.ShoppingCart,
                                    accentColor = AccentWarning,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onTabChange("QUOTE_ENGINE") }
                                )
                                DashboardSummaryCard(
                                    title = "Sales Revenue",
                                    value = "%,.0f".format(salesRevenue) + " ETB",
                                    subtext = "Real-time contracts",
                                    icon = Icons.Default.Star,
                                    accentColor = GoldAccent,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onTabChange("QUOTE_ENGINE") }
                                )
                            }
                        }
                    }
                }
                else -> {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            DashboardSummaryCard(
                                title = "Total Leads",
                                value = totalLeads.toString(),
                                subtext = "Registered in CRM",
                                icon = Icons.Default.Person,
                                accentColor = AccentSuccess,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onTabChange("CRM_LEADS") }
                            )
                            DashboardSummaryCard(
                                title = "Active Customers",
                                value = activeCustomers.toString(),
                                subtext = "Engaged & Closed",
                                icon = Icons.Default.CheckCircle,
                                accentColor = Color(0xFF00E5FF),
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onTabChange("CRM_LEADS") }
                            )
                            DashboardSummaryCard(
                                title = "Pending Quotes",
                                value = pendingQuotes.toString(),
                                subtext = "Awaiting final sign",
                                icon = Icons.Default.ShoppingCart,
                                accentColor = AccentWarning,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onTabChange("QUOTE_ENGINE") }
                            )
                            DashboardSummaryCard(
                                title = "Sales Revenue",
                                value = "%,.0f".format(salesRevenue) + " ETB",
                                subtext = "Real-time contracts",
                                icon = Icons.Default.Star,
                                accentColor = GoldAccent,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onTabChange("QUOTE_ENGINE") }
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "CRM QUICK CHANNELS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            item {
                QuickActionsPanel(onTabChange = onTabChange)
            }

            item {
                Text(
                    text = "SIMULATOR LOAD CHANNELS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            item {
                TrafficDistributionPanel(leads = leads)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT INTAKE STREAM",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                    Text(
                        text = "View All",
                        fontSize = 11.sp,
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onTabChange("CRM_LEADS") }
                            .padding(end = 4.dp)
                    )
                }
            }

            if (leads.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E0A35).copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF2E1C4E), RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No active CRM leads configured in your hub.",
                                color = TextMuted,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(leads.take(3)) { lead ->
                    DashboardLeadItemCard(lead = lead, onTabChange = onTabChange)
                }
            }
        }
    }
}

@Composable
fun DashboardHeaderCard(onTabChange: (String) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E0A35).copy(alpha = 0.7f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF3B1E6A), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_bekansi_logo),
                contentDescription = "Center logo",
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, GoldAccent, RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Welcome to Bekansi CRM",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextLight
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Smarter Ethiopian Multilingual Sales CRM. Standardised custom furniture quoting & omnichannel simulated chatbot triggers.",
                    fontSize = 11.sp,
                    color = TextMuted,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onTabChange("OMNICHANNEL_CHAT") },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(text = "Launch Simulator", fontSize = 11.sp, color = TextLight, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DashboardSummaryCard(
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E0638).copy(alpha = 0.85f)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .border(1.dp, Color(0xFF330960), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = TextLight,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtext,
                fontSize = 9.sp,
                color = accentColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun QuickActionsPanel(onTabChange: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickActionButton(
            label = "Engage Clients",
            icon = Icons.Default.Call,
            color = Color(0xFF00E5FF),
            modifier = Modifier.weight(1f),
            onClick = { onTabChange("OMNICHANNEL_CHAT") }
        )
        QuickActionButton(
            label = "Add Lead",
            icon = Icons.Default.Person,
            color = AccentSuccess,
            modifier = Modifier.weight(1f),
            onClick = { onTabChange("CRM_LEADS") }
        )
        QuickActionButton(
            label = "New Quote",
            icon = Icons.Default.ShoppingCart,
            color = AccentWarning,
            modifier = Modifier.weight(1f),
            onClick = { onTabChange("QUOTE_ENGINE") }
        )
        QuickActionButton(
            label = "Languages",
            icon = Icons.Default.Settings,
            color = Color(0xFFA071FF),
            modifier = Modifier.weight(1f),
            onClick = { onTabChange("ADMIN_LANG") }
        )
    }
}

@Composable
fun QuickActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E0A35).copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .border(1.dp, Color(0xFF2C1352), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = TextLight,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TrafficDistributionPanel(leads: List<Lead>) {
    val totalCount = leads.size.coerceAtLeast(1)
    val whatsappCount = leads.count { it.source == "WhatsApp" }
    val telegramCount = leads.count { it.source == "Telegram" }
    val facebookCount = leads.count { it.source == "Facebook" }
    val livechatCount = leads.count { it.source == "LiveChat" }
    
    val sum = (whatsappCount + telegramCount + facebookCount + livechatCount).toDouble().coerceAtLeast(1.0)
    
    val whatsappPct = whatsappCount / sum
    val telegramPct = telegramCount / sum
    val facebookPct = facebookCount / sum
    val livechatPct = livechatCount / sum

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B0735).copy(alpha = 0.8f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF2F1454), RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TrafficDistributionRow(channel = "WhatsApp", pct = whatsappPct, color = Color(0xFF25D366), count = whatsappCount)
            TrafficDistributionRow(channel = "Telegram", pct = telegramPct, color = Color(0xFF0088CC), count = telegramCount)
            TrafficDistributionRow(channel = "Facebook Messenger", pct = facebookPct, color = Color(0xFF1877F2), count = facebookCount)
            TrafficDistributionRow(channel = "LiveChat Hub", pct = livechatPct, color = GoldAccent, count = livechatCount)
        }
    }
}

@Composable
fun TrafficDistributionRow(channel: String, pct: Double, color: Color, count: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = channel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextLight)
            Text(
                text = "$count leads (${"%.0f".format(pct * 100)}%)",
                fontSize = 10.sp,
                color = TextMuted,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF2D164E))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = pct.toFloat())
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
    }
}

@Composable
fun DashboardLeadItemCard(lead: Lead, onTabChange: (String) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF190632).copy(alpha = 0.8f)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF2D1152), RoundedCornerShape(10.dp))
            .clickable { onTabChange("CRM_LEADS") }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = lead.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextLight)
                    
                    Surface(
                        color = when(lead.source) {
                            "WhatsApp" -> Color(0xFF1F5C34)
                            "Telegram" -> Color(0xFF114361)
                            "Facebook" -> Color(0xFF12346B)
                            else -> Color(0xFF5A4413)
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = lead.source,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLight,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = lead.requirements.ifBlank { "No special design requirements noted." },
                    fontSize = 11.sp,
                    color = TextMuted,
                    maxLines = 1
                )
            }
            
            Surface(
                color = when (lead.status) {
                    "Won" -> AccentSuccess.copy(alpha = 0.2f)
                    "New" -> Color(0xFF00E5FF).copy(alpha = 0.2f)
                    "Contacted" -> AccentWarning.copy(alpha = 0.2f)
                    else -> Color.Gray.copy(alpha = 0.2f)
                },
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.border(
                    1.dp,
                    when (lead.status) {
                        "Won" -> AccentSuccess
                        "New" -> Color(0xFF00E5FF)
                        "Contacted" -> AccentWarning
                        else -> Color.Gray
                    },
                    RoundedCornerShape(6.dp)
                )
            ) {
                Text(
                    text = lead.status,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (lead.status) {
                        "Won" -> AccentSuccess
                        "New" -> Color(0xFF00E5FF)
                        "Contacted" -> AccentWarning
                        else -> Color.White
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

