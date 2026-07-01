package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Conversation
import com.example.data.model.Lead
import com.example.data.model.Quotation
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ============================================================================
 * BEKANSI AI - HIGH-FIDELITY MOBILE AUTONOMOUS BUSINESS AGENT WORKSPACE
 * ============================================================================
 */
@Composable
fun AiEmployeeTab(
    viewModel: SalesViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Core state flows from SalesViewModel
    val leads by viewModel.allLeads.collectAsState()
    val quotes by viewModel.allQuotations.collectAsState()
    
    // Agent lifecycle controls
    var isAgentEnabled by remember { mutableStateOf(true) }
    var isCycleRunning by remember { mutableStateOf(false) }
    var lastExecutionTime by remember { mutableStateOf("2 minutes ago") }
    
    // Statistics & Counters (Simulating real-time AI CRM actions)
    var totalRepliesSent by remember { mutableStateOf(42) }
    var quotesGeneratedByAi by remember { mutableStateOf(19) }
    var leadsAnalyzedCount by remember { mutableStateOf(104) }
    
    // Logs representing autonomous auto-messages
    var aiLogs by remember { mutableStateOf(listOf(
        "🤖 [08:00 AM] Scheduled Morning Campaign: 'Modern Luxury Bed' posted successfully to Facebook.",
        "💬 [09:15 AM] Auto-Reply: Responded to Abiel M. regarding Sofa Pricing in Afaan Oromo.",
        "💰 [11:43 AM] Auto-Quotation: AI Generated quotation for Wanza Cabinet and sent to WhatsApp (+2519114532..).",
        "🔥 [01:10 PM] Scorecard Update: Lead 'Tsegaye K.' score calculated to (12/15) -> Promoted to HOT BUYER.",
        "📲 [02:30 PM] Outbound Follow-up: Sentinel system triggered follow-up nudge for 'Tsegaye K.' via Telegram.",
        "⚠️ [03:45 PM] Escalation: Transferring complex customized MDF layout query to human designer Abdi Biya."
    )) }
    
    // Simulated live terminal tracker
    LaunchedEffect(isAgentEnabled) {
        if (isAgentEnabled) {
            while (true) {
                delay(12000) // update periodically
                val randomLog = listOf(
                    "💬 [Auto-SMS] Outbound follow-up sent to Lead ID #${(10..99).random()} Status: 'Contacted'",
                    "🔥 [Sentinel] Analyzed buyer interest for ${(3..12).random()} leads. CRM syncing done.",
                    "🧠 [Brain] Running autonomous SMM metrics check... Lead flow stable.",
                    "💬 [Telegram] Direct auto-reply dispatched: 'Bishoftu Dukem showroom is open!'",
                    "💰 [Quoter] AI Pricing engine issued timber quote estimates. Total: ${(45000..120000).random()} ETB."
                ).random()
                aiLogs = (aiLogs.takeLast(15) + randomLog)
                totalRepliesSent += (0..1).random()
                leadsAnalyzedCount += (1..3).random()
                if ((0..10).random() > 8) {
                    quotesGeneratedByAi += 1
                }
            }
        }
    }
    
    // Filter hot leads
    val hotLeads = remember(leads) {
        leads.map { lead ->
            // Compute dynamic buyer scores based on simulated criteria
            val score = when {
                lead.status.lowercase() == "won" -> 15
                lead.status.lowercase() == "quoted" -> 12
                lead.status.lowercase() == "contacted" -> 8
                else -> 5
            }
            Pair(lead, score)
        }.sortedByDescending { it.second }
    }
    
    // Main UI Box with warm cocoa gradient background
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkCocoaBg)
            .padding(12.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            
            // --- HEADER: AUTOMATION CONTROL MODULE ---
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkWarmCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Pulsing state indicator
                                    Box(
                                        modifier = Modifier
                                            .size(9.dp)
                                            .clip(CircleShape)
                                            .background(if (isAgentEnabled) AccentSuccess else Color.Gray)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Bekansi AI Employee Agent",
                                        color = GoldAccent,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = if (isAgentEnabled) "Autonomous Mode: ACTIVE" else "Autonomous Mode: SLEEPING",
                                    color = TextLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            
                            Switch(
                                checked = isAgentEnabled,
                                onCheckedChange = {
                                    isAgentEnabled = it
                                    val toastMsg = if (it) "AI Autonomous employee armed and active! 🤖" else "AI Employee on standby."
                                    Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = GoldAccent,
                                    checkedTrackColor = WarmMahogany,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = Color.Black
                                ),
                                modifier = Modifier.testTag("ai_agent_master_switch")
                            )
                        }
                        
                        Divider(
                            color = Color.White.copy(alpha = 0.05f),
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                        
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Last Brain Iteration: $lastExecutionTime",
                                color = TextMuted,
                                fontSize = 10.5.sp
                            )
                            
                            Button(
                                onClick = {
                                    if (!isAgentEnabled) {
                                        Toast.makeText(context, "Please enable AI Employee first!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    scope.launch {
                                        isCycleRunning = true
                                        Toast.makeText(context, "🧠 Executing AI Decision & Auto-Follow loops...", Toast.LENGTH_SHORT).show()
                                        delay(2500)
                                        isCycleRunning = false
                                        lastExecutionTime = "Just now"
                                        totalRepliesSent += (1..3).random()
                                        leadsAnalyzedCount = leads.size + (20..50).random()
                                        aiLogs = aiLogs + "🧠 [${System.currentTimeMillis().run { "Manual" }}] Brain Cycle Executed: CRM updated. Dynamic scoring verified."
                                        Toast.makeText(context, "Autonomous loop done! CRM updated.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("run_autonomous_loop"),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                enabled = !isCycleRunning
                            ) {
                                if (isCycleRunning) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Thinking...", fontSize = 11.sp)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "",
                                        modifier = Modifier.size(13.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text("Run Loop Now", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
            
            // --- STATISTICS DASHBOARD SECTION ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Box 1: Leads Analyzed
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkWarmCard),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Person, contentDescription = "", tint = GoldAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Analyzed Leads", fontSize = 9.sp, color = TextMuted, textAlign = TextAlign.Center)
                            Text("$leadsAnalyzedCount", fontSize = 16.sp, fontWeight = FontWeight.Black, color = TextLight)
                        }
                    }
                    
                    // Box 2: Auto-Replies
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkWarmCard),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "", tint = Color(0xFF818CF8), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Auto-Replies Sent", fontSize = 9.sp, color = TextMuted, textAlign = TextAlign.Center)
                            Text("$totalRepliesSent", fontSize = 16.sp, fontWeight = FontWeight.Black, color = TextLight)
                        }
                    }
                    
                    // Box 3: AI Quotes Issued
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkWarmCard),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "", tint = AccentWarning, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("AI Quotes Sent", fontSize = 9.sp, color = TextMuted, textAlign = TextAlign.Center)
                            Text("$quotesGeneratedByAi", fontSize = 16.sp, fontWeight = FontWeight.Black, color = TextLight)
                        }
                    }
                }
            }
            
            // --- SECTION: SALES/CRM LEADS SCORING (HOT DECAY ENGINE) ---
            item {
                Text(
                    text = "🔥 Real-Time Hot Leads Identification (Autopilot Track)",
                    color = GoldAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.height(3.dp))
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkWarmCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Hot leads are dynamically promoted based on client interactions, quotes created, and WhatsApp velocity indicators. The system triggers automated SMS & Telegram campaigns to hot leads under 3 hours.",
                            color = TextMuted,
                            fontSize = 9.5.sp,
                            lineHeight = 13.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        if (hotLeads.isEmpty()) {
                            Text(
                                text = "No leads captured in CRM database to analyze currently.",
                                color = TextMuted,
                                fontSize = 10.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        } else {
                            hotLeads.take(4).forEach { (lead, score) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                        .padding(8.dp)
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = lead.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = TextLight
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        if (score >= 10) Color(0xFFEF4444).copy(alpha = 0.2f)
                                                        else AccentSuccess.copy(alpha = 0.2f),
                                                        RoundedCornerShape(4.dp)
                                                    )
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (score >= 10) "🔥 HOT BUYER" else "⚡ ACQUISITION",
                                                    fontSize = 7.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (score >= 10) Color(0xFFFCA5A5) else Color(0xFFA7F3D0)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Source: ${lead.source} | Phone: ${lead.phone}",
                                            fontSize = 9.sp,
                                            color = TextMuted
                                        )
                                        Text(
                                            text = "Interest: ${lead.requirements.take(45)}...",
                                            fontSize = 8.sp,
                                            color = TextMuted,
                                            maxLines = 1
                                        )
                                    }
                                    
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Score: $score/15",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp,
                                            color = GoldAccent
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Button(
                                            onClick = {
                                                Toast.makeText(context, "Outbound follow-up sent to ${lead.name} (${lead.phone}) successfully!", Toast.LENGTH_SHORT).show()
                                                aiLogs = aiLogs + "💬 [Sentinel Outbound] Dispatched nurturing campaign to ${lead.name} regarding custom cabinet options."
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.height(22.dp),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                        ) {
                                            Text("Follow up", fontSize = 8.sp, color = Color.White)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }
                }
            }
            
            // --- SECTION: AUTOMATED AI QUOTATIONS HISTORY ---
            item {
                Text(
                    text = "📋 Automated Timber Estimates (AI Issued)",
                    color = GoldAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.height(3.dp))
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkWarmCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Every time a lead requests details, the quoter analyzes current timber costs (seasoned Wanza, Mahogany, or Grar) and publishes binding cost sheets automatically.",
                            color = TextMuted,
                            fontSize = 9.5.sp,
                            lineHeight = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Fake visual auto-generated quotes list
                        val mockQuotes = listOf(
                            Triple("Abiel M.", "Wanza Curved L-Sofa", "68,000 ETB"),
                            Triple("Martha G.", "Regal King Canopy Bed", "112,500 ETB"),
                            Triple("Tsegaye K.", "Seasoned Mahogany Dining 8-Chair", "89,000 ETB")
                        )
                        
                        mockQuotes.forEach { (name, item, total) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .padding(6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("$name", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextLight)
                                    Text("Product: $item", fontSize = 8.5.sp, color = TextMuted)
                                    Text("Status: Auto-generated & sent ✅", fontSize = 8.sp, color = AccentSuccess)
                                }
                                Text("$total", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                            }
                        }
                    }
                }
            }

            // --- SECTION: INTELLIGENT HOOTSUITE BRONCO WORKSPACE (DECISION ENGINE JSON) ---
            item {
                Text(
                    text = "🧠 Autonomous Decision Engine Output (JSON Map)",
                    color = GoldAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = """
                                {
                                  "agent_id": "bekansi_employee_sentinel",
                                  "analyzed_cycle": "2026-06-21T04:40:00Z",
                                  "decisions": {
                                    "hotLeadsCount": ${hotLeads.count { it.second >= 10 }},
                                    "newInquiriesQueue": ${hotLeads.count { it.second < 8 }},
                                    "quoteRequestsDispatched": $quotesGeneratedByAi,
                                    "needMarketingBoost": ${hotLeads.size < 5},
                                    "activeCampaignTheme": "Kiln Seasoned Wanza Showcase",
                                    "system_memory": {
                                      "last_outcome": "Lead conversions up 18% in Addis Ababa"
                                    }
                                  }
                                }
                            """.trimIndent(),
                            color = Color(0xFF34D399),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        )
                    }
                }
            }

            // --- SECTION: AUTONOMOUS CRM MESSAGE TERMINAL FEED ---
            item {
                Text(
                    text = "📟 CRM Auto-Response Feed (Terminal View)",
                    color = GoldAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF110B0A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(modifier = Modifier.padding(10.dp)) {
                        val state = rememberLazyListState()
                        // Scroll automatically to bottom as logs arrive
                        LaunchedEffect(aiLogs.size) {
                            state.animateScrollToItem(aiLogs.size - 1)
                        }
                        
                        LazyColumn(
                            state = state,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(aiLogs) { log ->
                                Text(
                                    text = log,
                                    color = if (log.contains("🤖")) Color(0xFF60A5FA) 
                                            else if (log.contains("💬")) Color(0xFF34D399) 
                                            else if (log.contains("🔥")) Color(0xFFF87171) 
                                            else if (log.contains("⚠️")) Color(0xFFFBBF24) 
                                            else Color(0xFFD1D5DB),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 12.sp
                                )
                            }
                        }
                        
                        // Terminal shadow overlay at top & bottom
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(15.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFF110B0A), Color.Transparent)
                                    )
                                )
                                .align(Alignment.TopCenter)
                        )
                    }
                }
            }
            
            // --- SECTION: CASE ESCALATIONS (HUMAN HANDOFF QUEUE) ---
            item {
                Text(
                    text = "🚨 Safe Escalations Queue (Human Action)",
                    color = GoldAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkWarmCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "When complex wood blueprints, bulk hotel supply estimates, or specialized interior color coordination mapping is requested, the Autonomous Agent routes the cases seamlessly to senior human consultants.",
                            color = TextMuted,
                            fontSize = 9.5.sp,
                            lineHeight = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val escalations = listOf(
                            Triple("Lideta Office Plaza LLC", "50 ergonomic mesh stools & curved conference tables", "Abdi Biya"),
                            Triple("Bishoftu Resort Ltd", "Custom luxury sun loungers with weatherproof cushions", "Bekansi")
                        )
                        
                        escalations.forEach { (client, need, assigned) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = client,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.5.sp,
                                        color = TextLight
                                    )
                                    Text(
                                        text = "Request: $need",
                                        fontSize = 8.5.sp,
                                        color = TextMuted,
                                        lineHeight = 11.sp
                                    )
                                    Text(
                                        text = "Routed: Escalated to senior $assigned",
                                        fontSize = 8.sp,
                                        color = AccentWarning,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                
                                Button(
                                    onClick = {
                                        Toast.makeText(context, "Contact details copied for $assigned to handle immediately!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.height(24.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text("Handle", fontSize = 8.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
            
            // Padding gap bottom
            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
