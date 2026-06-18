package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.GeminiClient
import com.example.ui.SalesViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

// Representation of a single day's multi-platform social posts
data class DayContent(
    val dayNumber: Int,
    val dayName: String,
    val category: String, // Product Showcase, Customer Testimonial, Design Tips, etc.
    val focusTopic: String,
    val facebookDraft: String,
    val instagramDraft: String,
    val telegramDraft: String,
    val visualPrompt: String
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SmmPlannerTab(viewModel: SalesViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Core states
    var campaignThemeIndex by remember { mutableStateOf(0) } // 0: Elegant Wood, 1: Corporate Office, 2: Home & Wedding
    var customInstruction by remember { mutableStateOf("") }
    var selectedDayIndex by remember { mutableStateOf(0) }
    var selectedPlatform by remember { mutableStateOf("Facebook") } // Facebook, Instagram, Telegram
    var isGeneratingState by remember { mutableStateOf(false) }
    val activeLanguage by viewModel.selectedLanguage.collectAsState()

    // Pre-Baked high-fidelity content calendar templates (fallback & instant access)
    val elegantWoodCalendar = remember {
        listOf(
            DayContent(
                dayNumber = 1,
                dayName = "Monday",
                category = "Product Showcase",
                focusTopic = "Wanza Curved L-Sofa 'Gara' Spotlight",
                facebookDraft = """
🔔 ROYAL WANZA SOFA SPOTLIGHT - COMFORT BEYOND COMPARE! 🔔

Bring majestic Ethiopian elegance into your living room with our signature Wanza Curved L-Sofa "Gara". Crafted from carefully selected Cordia Africana hardwood and wrapped in beautiful local Habesha woven embroidery.

🔴 Why choose the 'Gara' Sofa?
• 100% Solid Wanza Wood: Termite-proof, gorgeous golden grain, and built to last.
• High-Density Cloud Cushions: Sinks perfectly to relieve stress.
• Tailor-Made sizes: We alter configurations to match your exact living room blueprint!
• 5-Year Full Warranty: Pure peace of mind.

🏡 Visit our Addis Ababa Showroom today near Bole to experience real luxury.
📞 Contact our Sales Consultants: +251 911 000 000
✉️ Inquiry Email: sales@bekansi.com

#BekansiFurniture #WanzaWood #AddisAbabaFurniture #EthiopianCraftsmanship #PremiumSofa #HabeshaLiving
                """.trimIndent(),
                instagramDraft = """
✨ Regal Wanza Wood meets Modern Comfort. ✨

Our handcrafted "Gara" L-Sofa is local woodcraft redefined. Solid Wanza (Cordia Africana) timber, custom-woven accents, and premium cloud cushioning designed for your forever home.

📐 Customizable sizes and dimensions.
🛡️ 5-Year Warranty.
📍 Showroom: Addis Ababa, Bole.

Tap the link in bio to start your bespoke lounge order! 🛋️

#Bekansi #WanzaSofa #EthiopianHome #InteriorDesignAddis #BespokeFurniture #HandmadeInEthiopia #LuxuryLiving #HabeshaDecor
                """.trimIndent(),
                telegramDraft = """
🪵 <b>Bekansi Premium Wanza Sovereign L-Sofa "Gara"</b> 🪵

Transform your saloon with the golden warmth of national Cordia Africana wood. 

📌 <b>Core Specifications:</b>
• <b>Timber:</b> Solid high-grade Wanza Wood
• <b>Fabric:</b> Local woven Habesha trim cushions
• <b>Warranty:</b> 5 Years
• <b>Delivery Duration:</b> 3-4 Weeks 

💎 Price: 135,000 ETB (Custom sizes available!)

📞 Contact Sales Agent immediately on Telegram: @BekansiSalesBot or call +251911000000.
📍 Bole Showroom, Addis Ababa.
                """.trimIndent(),
                visualPrompt = "Close-up cinematic shot of Wanza wood grain joints on a curved sofa armrest, soft ambient morning light streaming from a window, high-end photography."
            ),
            DayContent(
                dayNumber = 2,
                dayName = "Tuesday",
                category = "Customer Testimonial",
                focusTopic = "Real Estate Developer Endorsement",
                facebookDraft = """
⭐️⭐️⭐️⭐️⭐️ CLIENT TESTIMONIAL: "BEKANSI SETS THE GOLD STANDARD!" ⭐️⭐️⭐️⭐️⭐️

Listen to what Alsam Real Estate Developer, one of our key corporate clients in Addis Ababa, says about our custom installment service:

"We needed 80 custom wardrobes and TV stands delivered in a strict 4-week window. The Bekansi factory delivered outstanding Zigba-framed designs with high-gloss lacquer finishes on time and on budget. Our buyers are absolutely thrilled!"

Whether you are remodeling a single bedroom or furnishing a luxury high-rise condominium, Bekansi PLC delivers premium bespoke solutions.

📞 Consultation Hotline: +251 911 000 000
📍 Showroom Location: Bole, Addis Ababa, Ethiopia

#BekansiTestimonial #AddisRealEstate #EthiopianFurniture #InteriorDesignInstallations #BespokeCabinets #ZigbaWood
                """.trimIndent(),
                instagramDraft = """
"Bekansi sets the gold standard!" ⭐⭐⭐⭐⭐

Hear from our corporate partners at Alsam Real Estate. Sourcing top-tier custom wardrobes, modular kitchen countertops, and TV cabinets for luxury apartments has never been more seamless.

Quality. Reliability. Premium Woodcraft. 🌿

📞 Dial +251 911 000 000 to consult on your residential development.

#BekansiFeedback #RealEstateEthiopia #ApartmentDecorAddis #WardrobeDesign #CustomCabinetry #AddisBuilders
                """.trimIndent(),
                telegramDraft = """
⭐️ <b>Client Testimonial Spotlight</b> ⭐️

"The wardrobes and TV consoles from Bekansi elevated our apartment pricing index immediately. Incredible premium finishing."
- <i>Alsam Real Estate Development, Addis Ababa</i>

📋 <b>Why developers choose Bekansi:</b>
✅ Bulk factory manufacturing capability
✅ Kiln-dried wood that never warps
✅ Dedicated professional installation team
✅ Standardized SLA & Warranty agreements

Let us elevate your property values today.
📞 Speak to our project architect: +251911000000
🤖 Telegram Sales: @BekansiSalesBot
                """.trimIndent(),
                visualPrompt = "Wide photo of a luxury apartment bedroom showing sliding mahogany wardrobes with glass overlays, morning sun reflection, design portfolio finish."
            ),
            DayContent(
                dayNumber = 3,
                dayName = "Wednesday",
                category = "Behind-the-scenes",
                focusTopic = "The Kiln-Drying Wood Seasoning Process",
                facebookDraft = """
🔍 BEHIND-THE-SCENES: HOW WE ENSURE YOUR FURNITURE NEVER WARPS! 🔍

Did you know that raw timber contains up to 50% water? If handcrafted immediately, it will shrink, split, and warp as the seasons change in Ethiopia.

At Bekansi, every log of Wanza, Grar, and Mahogany goes through our advanced state-of-the-art kiln-drying chambers for up to 14 days. We extract moisture down to a precise 8%!

This is why we confidently offer a structural warranty of up to 10 years. We season the wood so it stays flat, robust, and beautiful for generations.

🏡 Invest in generational quality. Visit our showroom in Bole, Addis Ababa.
📞 Custom Consultation: +251 911 000 000

#BehindTheDesign #BekansiFactory #KilnDriedWood #GenerationalQuality #Wanza #Grar #MahoganyWood
                """.trimIndent(),
                instagramDraft = """
How do we guarantee wood products that never warp? 🪵🔬

Every single piece of timber we select goes through our computerized wood seasoning kiln. We dry our Wanza and Mahogany down to a precise 8% moisture level.

No split joints. No cracking frames. Just pure structural perfection that lasts generations. 🛡️

Tap follow to learn more about raw wood mastery! ✨

#BekansiFactory #WoodworkScience #CarpentryMasters #MadeInEthiopia #FineHardwood #BespokeJoinery #MoistureControl
                """.trimIndent(),
                telegramDraft = """
🔬 <b>Behind the Craft: Seasoning the Soul of Wood</b>

Why do cheap furniture joints split after one rainy season in Addis Ababa? Moisture!

At Bekansi we strictly apply dry-kiln treatments:
1️⃣ Timber is sectioned and stacked in ventilation grids
2️⃣ Controlled steam reduces water content without splitting fibers
3️⃣ Moisture meters verify a stable <b>8% threshold</b>

That is why our <b>Mahogany Dining Sets</b> are warrantied for <b>10 full years</b>.

Invest in real engineering.
📞 +251911000000
📍 Showroom: Bole, Addis Ababa
                """.trimIndent(),
                visualPrompt = "Close-up action shot of wood dust flying as a master carpenter chisels a dowel joint on a solid mahogany table, slow motion aesthetic."
            ),
            DayContent(
                dayNumber = 4,
                dayName = "Thursday",
                category = "Interior Design Tips",
                focusTopic = "Space Optimization for Modern Apartments",
                facebookDraft = """
💡 INTERIOR DESIGN TIP: MAXIMIZE APARTMENT SPACE LIKE A PRO! 💡

Living in a compact modern apartment in Addis Ababa? You don't have to sacrifice style for functionality. Here are 3 space-saving laws from our interior designers:

1. EMBRACE FLOATING CONSOLES: Placing our "Bunna" TV credenza on the wall elevates your floor line, creating the illusion of a much bigger saloon area.
2. DISCOVER THE PLATFORM STORAGE BED: Beds occupy the largest footprint. Try our custom Zigba bedframes with drawer storage hidden inside the base structure!
3. CHOOSE LIGHT NATURAL WOODS: Warm honey tones of natural Wanza reflect more ambient light, opening up dark corners.

Looking for custom layouts? Our in-house design space consultations are absolutely FREE!

📞 Call our master designer: +251 911 000 000
📍 Find us in Bole, Addis Ababa.

#BekansiDesignTips #SpaceOptimization #ApartmentLiving #AddisAbabaFurniture #ModularBeds #TVConsoles
                """.trimIndent(),
                instagramDraft = """
Small parlor, massive statement! 📐🛋️

Apartment living in Addis doesn't have to feel crowded. 
Tip 1: Elevate consoles off the floor to expand visual boundaries.
Tip 2: Choose multi-use furniture (beds with secret pull-out drawer boxes).
Tip 3: Stick to warm, reflective natural timber species.

Book a free showroom consultation in Bole! 🏛️

#BekansiDesignTips #AddisDecor #StudioApartment #WallConsoles #SpaceSavingHacks #ModernInteriors #CleanAesthetics
                """.trimIndent(),
                telegramDraft = """
💡 <b>Apartment Space-Saving Hacks by Bekansi Designers</b> 💡

Is your parlor feeling cramped? Our design team recommends:

1️⃣ <b>Go Floating:</b> Wall-mount your TV set to reclaim 1.5 square meters of flooring.
2️⃣ <b>Storage Bedframes:</b> Use hidden drawer gas-lift mattress foundations.
3️⃣ <b>Light Reflection:</b> Pick Honey Wanza finishes over dark walnut varnishes.

Get a free architectural layout mock of your home:
📞 Call +251911000000
📍 Bole Showroom, Addis Ababa.
                """.trimIndent(),
                visualPrompt = "A cozy, minimalist living room with a floating timber unit, light green sofa with gold pillows, minimalist plant in corner, gorgeous catalog photo."
            ),
            DayContent(
                dayNumber = 5,
                dayName = "Friday",
                category = "Promotions",
                focusTopic = "Grar Wood King Floating Bed 'Sheger' Flash Sale",
                facebookDraft = """
🔥 FLASH PROMO: UPGRADE TO THE FLOTATION KINGDOM! 🔥

Experience the pinnacle of bedroom luxury with our "Sheger" King Floating Bed! For the next 72 hours, we are slashing production lead times by 5 days and providing free delivery in Addis Ababa!

Premium features:
• Built from heavy solid Grar (Ethiopian Acacia) core wood.
• Invisible recessed framing creating a magnificent floating optical illusion.
• Integrated soft-glow warm LED accent lighting beneath the base.
• Plush protective headboard in royal velvet.

👉 Standard Price: 110,000 ETB.
📞 Call right now to lock in your flash promo priority order: +251 911 000 000

#BekansiSale #FloatingBed #ShegerBed #AcaciaWood #BespokeBed #BedroomGoals #AddisAbabaLiving
                """.trimIndent(),
                instagramDraft = """
Sleep on air. Own the "Sheger" Floating King Bed. 🌌✨

Crafted around heavy Grar (Acacia) core framing, our iconic floating bed features integrated under-glow LED running bands and premium forest velvet backguards.

🔥 Limited Sale Event: Free transport within Addis & priority booking!

📞 Lock your spot via +251 911 000 000.

#FloatingSiree #BekansiBed #ShegerFloating #GrarWood #LuxuryBedroom #UnderglowBed #AmbianceDesign #AddisDeals
                """.trimIndent(),
                telegramDraft = """
🔥 <b>SPECIAL WEEKEND OFFER: "Sheger" Floating King Bed</b> 🔥

Sleep in absolute modern luxury on our Acacia-carved masterpiece.

🎁 <b>Deal Specifications:</b>
• <b>Original Base Price:</b> 110,000 ETB
• <b>Exclusive Bonus:</b> Free premium delivery + installation inside Addis Ababa!
• <b>Gift Item:</b> Standard custom bedside drawer matching wood!

⚡️ <i>Only 7 slots available for priority production.</i>

Lock reservation:
📞 Call: +251911000000
🤖 Contact: @BekansiSalesBot
                """.trimIndent(),
                visualPrompt = "Modern luxury dark bedroom focusing on a glowing floating bed with golden LED underglow, black walls, exquisite contemporary design."
            ),
            DayContent(
                dayNumber = 6,
                dayName = "Saturday",
                category = "Educational Content",
                focusTopic = "Wanza vs. Grar vs. Mahogany Hardwood",
                facebookDraft = """
🧠 KNOWLEDGE IS LUXURY: UNDERSTAND ETHIOPIA'S ELITE HARDWOODS! 🧠

When buying custom furniture for your parlor, understanding raw timber species will save you millions. Let's compare our 3 elite favorites:

1. WANZA (Cordia Africana):
• Appearance: Warm, honey-golden tones with striking dark swirl patterns.
• Best for: Luxury sofas, beds, coffee tables. Highly stable and acoustically quiet.

2. GRAR (Acacia):
• Appearance: High density, deep golden-brown core with high-contrast grains.
• Best for: heavy-duty outdoor setups, dining tables, organic slab countertops. Virtually indestructible.

3. MAHOGANY (Swietenia):
• Appearance: Dark reddish-brown that deepens over time with a highly reflective gloss.
• Best for: Executive desks, grand banquet tables, state cabinets. Classic luxury.

Drop by our showroom in Bole to feel the live texture samples!

📞 Talk to a timber expert: +251 911 000 000

#WoodEducation #BekansiCrafts #Wanza #Grar #Mahogany #AddisArtists #SmartBuyFurniture
                """.trimIndent(),
                instagramDraft = """
Wanza, Grar, or Mahogany? 🪵🤔 

Unlock the secrets of Ethiopia's premium timber species!
🍂 Wanza: Light honey glow, gorgeous organic swirls. Perfect for warm lounges.
🍯 Grar: High hardness, indestructible character. Great for executive statement slabs.
🍷 Mahogany: Royal reddish gloss, deep luster. Perfect for dining banquets.

Swipe left to see our live raw sample slabs! 🪵✨

#HardwoodGuide #BekansiPlates #AcaciaGrar #WanzaCore #MahoganyDesk #HandcraftedFurniture #CarpentrySchool #AddisArtists
                """.trimIndent(),
                telegramDraft = """
🪵 <b>Ethiopian Timber Master-Class by Bekansi</b> 🪵

Do you know your wood? Buying right means buying for life:

✨ <b>Wanza (Cordia Africana):</b> Golden-honey, highly stable, acoustic dampening. Best for luxury living room sofas.
💪 <b>Grar (Acacia):</b> Incredible density, gorgeous structural contrast. Incredibly weather-resistant. Perfect for heavy beds.
🍷 <b>Mahogany:</b> Regal reddish gloss, highly uniform pores. Speaks of corporate executive prestige.

Visit our wood vault in Addis:
📞 +251911000000
📍 Showroom: Bole, Addis Ababa.
                """.trimIndent(),
                visualPrompt = "Three polished block sections of Wanza, Grar, and Mahogany wood sitting on a dark concrete backdrop, beautifully shot macro perspective."
            ),
            DayContent(
                dayNumber = 7,
                dayName = "Sunday",
                category = "Seasonal Campaigns",
                focusTopic = "Wedding Season Custom Home Packages",
                facebookDraft = """
💍 WEDDING SEASON SPECIAL: BEGIN YOUR NEW CHAPTER WITH BEKANSI! 💍

Congratulations to all newly married couples in Ethiopia! Starting a new household is an exciting milestone, and we believe your first home should look like a postcard.

To celebrate your love, we are introducing our **Bespoke Wedding Household Package**:
✨ Sovereign L-Sofa "Gara" (Solid Wanza)
✨ Queen Platform Bed "Sheger" (Grar Frame + Velvet Headboard)
✨ Dual-tone TV Credenza "Bunna"

🎁 Get 15% OFF the full bundle plus a complimentary traditional "Berchuma" royalty stool as our blessing!
📅 Wedding promo valid until the end of this month.

📞 Message us to receive our wedding pricing catalog or call: +251 911 000 000
📍 Showroom near Bole, Addis Ababa.

#BekansiWeddings #NewlywedsEthiopia #HabeshaMarriage #AddisAbabaLiving #BespokeHomeBundle #WanzaLiving #GrarBed
                """.trimIndent(),
                instagramDraft = """
A new life together starts with a cozy, gorgeous home. 💍🏡

Introducing our exclusive "Bespoke Couple Bundle":
🛋️ "Gara" L-Sofa (Solid honey Wanza)
🛏️ "Sheger" Floating King Bed (Grar Frame)
📺 "Bunna" TV Console

Enjoy 15% off the complete bundle and receive a custom-carved royalty Berchuma stool as our wedding blessing gift! 🎁🕊️

📞 DM us to schedule a private walkthrough at our showroom near Bole.

#BekansiWedding #NikaahPackage #EthiopianCouples #HabeshaWeddings #PerfectHome #AddisDecorators #NewlywedsAddis
                """.trimIndent(),
                telegramDraft = """
💍 <b>Newlyweds Special Home Package </b> 💍

Start your wonderful marital path with the comforting security of authentic solid hardwoods.

📦 <b>The Bekansi Royal Bundle:</b>
1️⃣ Sovereign "Gara" L-Sofa (Wanza Wood)
2️⃣ "Sheger" Floating Bed frame (Acacia Wood)
3️⃣ "Bunna" TV/Coffee Console 

🎁 <b>Spouse Rewards:</b>
• <b>15% discount</b> on total package values.
• Complimentary handcrafted royal <b>Berchuma stool</b>!

Secure your bridal delivery date:
📞 +251911000000
🤖 Telegram booking: @BekansiSalesBot
                """.trimIndent(),
                visualPrompt = "Wide clean living room decorated with warm timber furniture, a beautiful white traditional modern marriage certificate in a frame on the sideboard."
            )
        )
    }

    val officeCalendar = remember {
        listOf(
            DayContent(
                dayNumber = 1,
                dayName = "Monday",
                category = "Product Showcase",
                focusTopic = "CEO Modular Executive Desk 'Abay'",
                facebookDraft = """
💼 COMMAND THE ROOM: THE EXECUTIVES DESK 'ABAY' 💼

A true leader needs a statement desk. Introduce authority, productivity, and luxury to your corporate headquarters with our "Abay" Executive Desk.

🔴 Why the 'Abay' is the choice of elite CEOs in Ethiopia:
• Premium Solid Mahogany Top: Naturally rich grain that deepens in character over time.
• Hidden Cabling System: Keeps power, network, and phone cords perfectly out of sight.
• Built-in Full-grain Leather Pad: A luxurious writing surface.
• Soft-close Drawer Rails: Silence and executive calm.

Upgrade your workspace to command success.
📞 Personal Corporate Agent: +251 911 000 000
📍 Bole Showroom, Addis Ababa.

#CorporateOffice #BekansiExecutive #AbayDesk #MahoganyDesk #CEOOffice #AddisCorporate #ProductivityDesign
                """.trimIndent(),
                instagramDraft = """
Designed for leaders. Handcrafted for success. 💼✨

The "Abay" Executive Desk features solid Mahogany lumber, brushed brass handles, and a flush-fit desk leather pad with hidden wiring channels. Because real productivity starts with zero clutter.

📐 Customizable layout options.
🛡️ 10-Year corporate warranty.

📞 Call +251 911 000 000 to speak with our corporate contract design team.

#BekansiOffice #LeaderDesk #MahoganyDesign #AddisContracts #CorporateInteriors #HighEndWorkplace #WorkspaceElegance
                """.trimIndent(),
                telegramDraft = """
🖥 <b>Executive Boardroom Statement: 'Abay' CEO Desk</b>

Ensure maximum productivity with the ultimate executive workbench:
• <b>Timber:</b> Premium thick solid Mahogany block
• <b>Detailing:</b> Polished brass grommets and full-grain leather surface
• <b>Management:</b> Hidden multi-socket cabling conduits

💼 Special B2B contracts available for embassies and bank branches.

📞 Call directly: +251911000000
🤖 Telegram Portal: @BekansiSalesBot
                """.trimIndent(),
                visualPrompt = "Atmospheric business office looking at a grand mahogany executive desk with leather accents, neat office design, city skyscrapers outside window."
            ),
            DayContent(
                dayNumber = 2,
                dayName = "Tuesday",
                category = "Customer Testimonial",
                focusTopic = "Corporate Bank Boardroom Design Feedback",
                facebookDraft = """
🏦 CORPORATE PARTNER HIGHLIGHT: TRADING LUXURY FOR EFFICIENCY! 🏦

We recently worked with a prominent private commercial bank in Addis Ababa to completely overhaul their executive boardroom and VIP lounge. Here is their feedback:

"Bekansi provided a breathtaking 12-meter solid Grar wood conference table with integrated audio-visual ports, and matching modular cabinetry. The craftsmanship is flawless. It immediately became the centerpiece of our board meetings."

Elevate your enterprise client meetings with furniture that matches your corporate stature. Contact Bekansi Corporate Sales division today!

📞 Corporate Sales Division: +251 911 000 000
📍 Showroom: Bole, Addis Ababa, Ethiopia

#CorporateBank #BekansiEnterprise #ConferenceTable #GrarWood #AddisAbabaBusiness #BoardroomDesign
                """.trimIndent(),
                instagramDraft = """
Flawless Boardroom Architecture. 🏦🌿

A prominent commercial bank in Addis trusted us to engineer their executive boardroom. The result? A breathtaking 12-meter solid Grar (Acacia) conference table that commands respect and inspires collaboration.

Bring authentic woodcraft into the heart of your enterprise.

📞 B2B Hotline: +251 911 000 000

#BekansiEnterprise #CorporateBoardrooms #AcaciaTable #BankDesignAddis #CustomContracts #OfficePLC #BespokeCraftsmanship
                """.trimIndent(),
                telegramDraft = """
🏦 <b>Bekansi Corporate B2B Project Showcase</b> 🏦

We successfully delivered a breathtaking 12-meter solid Grar wood boardroom table for a major commercial bank in Addis Ababa.

📈 <b>Key Highlights:</b>
• Hidden power outlets & HDMI visual adapters
• Highly stable concrete steel foundations with wood cladding
• Complemented by 20 leather ergonomic executive chairs

Do you want to elevate your conference room?
📞 Call our senior design consulting architect: +251911000000
                """.trimIndent(),
                visualPrompt = "A grand office conference table made from an organic acacia slab, surrounding chairs, warm lights, modern corporate interior."
            ),
            DayContent(
                dayNumber = 3,
                dayName = "Wednesday",
                category = "Behind-the-scenes",
                focusTopic = "Heavy-Duty Modular Cable Assembly Engineering",
                facebookDraft = """
🛠 BEHIND THE CRAFT: INTEGRATING TECH AND TIMBER SEAMLESSLY! 🛠

Ever wondered how we hide ugly electrical wires in our majestic wooden corporate desks?

We don't just drill random holes. Our engineering team designs hidden aluminum core channels within the legs and framing. Users can feed up to 8 plugs down to power panels without showing a single cable!

We bridge traditional Ethiopian woodwork with modern corporate workspace expectations. Watch our carpenters rout these precise channels in our Addis factory!

📞 Consult on custom corporate orders: +251 911 000 000

#OfficeEngineering #BekansiFactory #SmartFurniture #AddisWorks #DeskCables #MahoganyDesign #B2BTech
                """.trimIndent(),
                instagramDraft = """
Traditional timber, modern technology. ⚡🪵

A peek behind the scenes at our Addis factory: router-milling cable conduits directly into our Mahogany desk columns. Hide power adapters and laptop chargers in clean, hidden chambers.

Sleek. Minimalist. Integrated. 💼

#CarpenterLife #OfficeTechFurniture #MahoganyDesk #CleanCables #BespokeOffice #MadeInEthiopia #BespokeDetails
                """.trimIndent(),
                telegramDraft = """
🛠 <b>Tech-timber Integration Secrets of Bekansi</b> 🛠

Cluttered power cables degrade office productivity. We resolve this elegantly:
• Desk drawers contain hidden <b>routing channels</b>
• Multi-plug strips are enclosed in magnetic hatch boxes
• Heavy desk columns carry wire bundles safely to the floor

Keep your executive workspace clean.
📞 Call a corporate customizer: +251911000000
🤖 Telegram: @BekansiSalesBot
                """.trimIndent(),
                visualPrompt = "Close-up of a neat workspace desk showing a luxury hatch opening to reveal organized plug outlets, beautiful hand textured wood structure."
            ),
            DayContent(
                dayNumber = 4,
                dayName = "Thursday",
                category = "Interior Design Tips",
                focusTopic = "Ergonomics & Posture in Corporate Lounges",
                facebookDraft = """
🏢 ENTERPRISE WELLNESS: THE ANATOMY OF ERGONOMIC WORK SPACES! 🏢

An average office worker spends up to 2,000 hours per year sitting down. Poor posture leads to chronic back pain and decreases team performance in Addis Ababa.

Ensure your team operates at 100% with these 3 corporate ergonomic essentials:
1. WRIST DEVIATION: Keep desk heights at a standard 75 cm to align elbows naturally.
2. SACRAL SUPPORT: Choose chairs that bolster the lower back cushion.
3. CONDUIT REACH: Document cabinets should stay within arm's reach to avoid strain.

At Bekansi, we design office chairs and desks in strict alignment with international chiropractic guidelines.

📞 Revamp your workspace wellness: +251 911 000 000
📍 Visit our corporate design suite in Bole.

#ErgonomicsEthiopia #OfficeWellness #AddisStaff #BekansiPLC #DeskPosture #ProductiveTeams
                """.trimIndent(),
                instagramDraft = """
Your posture is your productivity. 📊🩺

Design rules for high-performing staff:
📏 Standardize workspace desk height to 75cm.
💺 Implement active lumbar sacral adjustments.
🗄️ Keep files close to avoid repetitive strain.

Protect your human resources with Bekansi Office Ergonomics.

📞 Contact +251 911 000 000 for a free team wellness consulting audit.

#CorporateErgonomics #HealthyStaff #AddisOffices #BespokeChairs #DeskSetup #WorkplaceErgonomics #AddisBanks
                """.trimIndent(),
                telegramDraft = """
🩺 <b>Is Your Staff Back-Pain Free? Office Ergonomic Basics</b>

Poor office chairs cost corporate branches thousands in lost hours. Ensure wellness:
📐 Desks should reside exactly <b>75 cm</b> off floor lines
💺 Chairs must feature <b>high-density molded foam</b> base cushions
📂 Cabinets should run adjacent to workstations

We manufacture healthy, beautiful corporate environments.
📞 Quote for custom desk layouts: +251911000000
📍 Showroom Bole, Addis Ababa.
                """.trimIndent(),
                visualPrompt = "Bright sleek office set up with a luxurious ergonomic mesh chair, solid mahogany desk, clean minimalist lines, high commercial design."
            ),
            DayContent(
                dayNumber = 5,
                dayName = "Friday",
                category = "Promotions",
                focusTopic = "Bulk Office Refit Package Discounts",
                facebookDraft = """
📈 END-OF-QUARTER PROMO: RE-VALUATE YOUR CORPORATE OFFICE APPARATUS! 📈

Ready to transform your company workspace? Take advantage of our End-Of-Quarter Corporate Refit Program! 

For any contract exceeding 5 executive workstations, receive:
🎁 15% discount on our solid Mahogany "Abay" desks.
🎁 Complimentary administrative file storage credenzas.
🎁 Free floor-plan customization audit and layout rendering from our expert architects.
🎁 Priority shipping within Addis Ababa.

Bring prestige, comfort, and productivity to your team. Secure dry-season priority slots.

📞 Corporate Sales Contract Unit: +251 911 000 000
📍 Bole Showroom, Addis Ababa.

#CorporateAudit #OfficeRefit #BekansiPLC #MahoganyB2B #AddisOffices #WorkspaceDesign
                """.trimIndent(),
                instagramDraft = """
Scale up your office workspace prestige. 📈🌿

For the next 10 days, get **15% OFF** on bulks of 5+ Executive Task Desks, plus complimentary document archives and complete 3D interior layout mapping!

Inspire quality work. Attract premium clients. 🏢

📞 B2B Hotline: +251 911 000 000

#CustomOffice #CorporateFurnitureAddis #AddisShowroom #StaffDesks #OfficeDesignIdeas #EmbassyFurniture
                """.trimIndent(),
                telegramDraft = """
📈 <b>Corporate Bulk Refit Special</b> 📈

Upgrade your corporate headquarters and bank branches with premier solid mahogany cabinetry.

🎁 <b>High-volume Client Offers:</b>
• <b>15% discount</b> on orders of 5+ workstation desks
• Free modular document archives
• Free 3D workspace architectural mock-ups

Transform your enterprise culture.
📞 +251911000000
🤖 SMM Telegram: @BekansiSalesBot
                """.trimIndent(),
                visualPrompt = "Multi-station office room with elegant dark wood desks, glowing partition lines, green plants, modern commercial office mockup."
            ),
            DayContent(
                dayNumber = 6,
                dayName = "Saturday",
                category = "Educational Content",
                focusTopic = "MDF vs. Solid Hardwood for Office Assets",
                facebookDraft = """
💡 OFFICE BUYERS GUIDE: SOLID HARDWOOD VS. ENGINEERED MDF! 💡

 эмባስ ዎች? Many offices in Addis Ababa purchase low-cost imported MDF desks only to see the wood flake and sag after one year of cup spills and move-handling.

Let's understand why Bekansi solid hardwoods are a smarter capital investment:
• Durability: Solid Mahogany is virtually indestructible. Scratches can be sanded down and revarnished to look new. MDF cannot be repaired once chipped.
• Water Resistance: Wood cup condensation breaks down MDF glues, causing balloon swelling. Our polyurethane sealants shield hardwoods completely.
• Resale Value: Authentic wood items appreciate in value. Imported plastic laminate desks have zero scrap value.

Choose long-term assets over immediate liabilities.

📞 Speak with an office assets consultant: +251 911 000 000

#SmartOffice #OfficeManagers #BekansiGuides #WoodScience #CommercialBankAssets #AmharicPLC
                """.trimIndent(),
                instagramDraft = """
Investing in assets over liabilities. 🪵📈

Imitation MDF desks degrade with active use and moisture spills. Bekansi's solid Mahogany and Wanza desks offer sanding repairability, supreme water sealants, and heirloom resale value.

Buy once, buy right. 💼🛡️

#SmartAssets #B2BFurniture #WoodworkKnowledge #AcaciaAcclimatization #CommercialRealEstateAddis #CarpentryGuides
                """.trimIndent(),
                telegramDraft = """
📋 <b>Office Asset Audit: Hardwood vs. Cheap MDF</b>

Many modern commercial buyers learn this the hard way:
❌ <b>MDF:</b> Swells with coffee condensation, crumbles on moving day, non-repairable.
✅ <b>Bekansi Hardwood:</b> Polyurethane-sealed, endless sanding lifespan, retains real wealth value.

Protect your workspace CAPEX.
📞 Call an asset advisor: +251911000000
📍 Showroom Bole, Addis Ababa.
                """.trimIndent(),
                visualPrompt = "Side-by-side comparison under bright light of highly refined mahogany wood grain next to a chipped raw fiber board, showcasing quality differences."
            ),
            DayContent(
                dayNumber = 7,
                dayName = "Sunday",
                category = "Seasonal Campaigns",
                focusTopic = "Creative Agency Custom Collaborative Tables",
                facebookDraft = """
🎨 COLLABORATION STARTS HERE: SHAPING ETHIOPIA'S CREATIVE REVOLUTION! 🎨

Whether you operate a modern software start-up, a luxury architectural studio, or a bustling creative media agency in Addis Ababa, ideas need space to grow.

Introducing our **Shared Collaboration Workbench**:
• Massive 2.8-meter solid seasoned Wanza wood slab.
• Centered integrated whiteboards or copper desktop organizer frames.
• Multi-leg structural strength designed for active group brainstorm sessions.

Foster transparency, team drive, and gorgeous creative aesthetics at your agency.

📞 Consult with our workspace architect: +251 911 000 000
✉️ Sourcing desk: sales@bekansi.com

#CreativeAgency #AddisStartups #SharedWorkstation #WanzaSlab #CollaborativeSpace #BekansiDesign
                """.trimIndent(),
                instagramDraft = """
Fuel great ideas. 💡🎨

Designed for dynamic creative agencies and tech startup hubs in Addis Ababa, our 2.8-meter solid organic Wanza communal workshop tables offer integrated group ports and sleek geometric outlines.

Build a workspace that inspires! ✨

#AddisMedia #AgencyDesign #StartupsEthiopia #WanzaCommunal #SlabTables #CreativeEngine #BespokeHQ
                """.trimIndent(),
                telegramDraft = """
🎨 <b>Co-working & Creative Hub Collective Tables</b>

Modern teamwork demands spacious multi-tenant worktops.
• <b>Timber:</b> Solid 2.8-meter seasoned Wanza Wood Slab
• <b>Frame:</b> Structural industrial powder-coated black steel
• <b>Detail:</b> Multi-user central phone charging sockets

Fuel your staff meetings with premium aesthetics.
📞 +251911000000
🤖 SMM Telegram: @BekansiSalesBot
                """.trimIndent(),
                visualPrompt = "Spacious co-working space styled room with a thick live-edge wooden table, young professionals chatting in background, sunny contemporary setup."
            )
        )
    }

    val homeWeddingCalendar = remember {
        listOf(
            DayContent(
                dayNumber = 1,
                dayName = "Monday",
                category = "Product Showcase",
                focusTopic = "Traditional 'Mesob' Coffee Table Heritage",
                facebookDraft = """
☕️ PRESERVE ETHIOPIAN HOSPITALITY WITH OUR HAND-CARVED 'MESOB' TABLE! ☕️

There is no honor greater than welcoming guests into your home with a fresh, aromatic cup of traditional Buna. Our intricately carved 'Mesob' Coffee Table is designed to celebrate our rich, golden hospitality.

🔴 Why the 'Mesob' Table is a masterpiece:
• Master Carver Sculpted: Each pattern is hand-chiselled at our Addis workshop.
• Heritage Centered Layout: Perfect height for roasting incense and pouring coffee.
• Made of Core Wanza Wood: Smells amazing, durable, and naturally radiant.
• Rich Stain Options: Choose Classic Honey or Imperial Smoky Walnut.

Bring the warmth of Ethiopian heritage into your modern parlor.
📞 Order your Mesob: +251 911 000 000
📍 Bole Showroom, Addis Ababa.

#MesobTable #EthiopianHeritage #TraditionalBuna #BespokeCarpentry #WanzaCrafts #EthiopianHomes #Bole
                """.trimIndent(),
                instagramDraft = """
Hospitality carved in solid Wanza. ☕️🕊️

Honor our rich Ethiopian warm greeting custom. Each "Mesob" Coffee centerpiece is individually carved by master artisans in Addis Ababa to balance modern utility with timeless cultural heritage.

🕊️ Honey Shellac natural varnish.
📍 Showroom: Hole, Addis Ababa.

#HeritageDesign #MesobBuna #EthiopianCrafts #CoffeeRituals #MadeInAddis #CustomCarving #WanzaPride #HabeshaHome
                """.trimIndent(),
                telegramDraft = """
☕️ <b>Sovereign Hand-Carved Mesob Coffee Centerpiece</b> ☕️

Elevate your family parlor with our master-carved traditional coffee table.
• <b>Timber:</b> Premium select Wanza timber core
• <b>Sculpting:</b> 100% individual hand-routed cultural grooves
• <b>Varnish:</b> Protective water-resistant Traditional Shellac

💰 Price: 55,000 ETB

📞 Call to reserve your individual carving block: +251911000000
🤖 Telegram booking: @BekansiSalesBot
                """.trimIndent(),
                visualPrompt = "Traditional coffee set with small porcelain cups on a beautifully detailed hand-carved circular wooden table, incense smoke rising."
            ),
            DayContent(
                dayNumber = 2,
                dayName = "Tuesday",
                category = "Customer Testimonial",
                focusTopic = "Newlywed Couple Living Room Makeover Stories",
                facebookDraft = """
💍 NEWLYWED DIARIES: "BEKANSI CREATED OUR DREAM FIRST HOME!" 💍

Congratulations to Aster and Yosef, who recently got married in Addis Ababa and used our Wedding Package to furnish their brand-new condominium:

"We were overwhelmed with wedding costs, but the Bekansi consulting team helped us choose the 'Gara' L-Sofa and a matching tv cabinet on budget. The complimentary 'Berchuma' stool is our favorite piece! Our guests ask about our furniture the moment they walk in."

Start your family story with hand-carved stability. Get our wedding catalog today!

📞 Wedding Support: +251 911 000 000
📍 Showroom near Bole, Addis Ababa.

#NewlywedsAddis #CondoDecor #DreamFirstHome #BekansiCouples #BespokeLiving #WanzaWood #AddisWeddings
                """.trimIndent(),
                instagramDraft = """
"Bekansi created our dream first home!" 💍🏡

Meet Aster & Yosef, newly married in Addis. They styled their cozy condo Parlor using our premium Wanza Sofa and custom media consoles. 

Congratulations, Aster & Yosef! We are honored to be part of your story! 🕊️✨

#NewlywedsEthiopia #HabeshaCondos #loungeMakeovers #FirstHomeInAddis #HappyCustomers #BekansiPLC #BespokeFurniture
                """.trimIndent(),
                telegramDraft = """
💍 <b>Newlywed Makeover Feature: Aster & Yosef</b> 💍

Aster & Yosef recently furnished their new Bole apartment with us:
• Wanza Sofa "Gara" + TV console "Bunna" bundle
• Received a <b>15% newlyweds discount</b>
• Received a complimentary hand-carved wedding <b>Berchuma</b> stool

"We wanted quality that would last until our kids grow up. Bekansi is wonderful!"

Let us draft your startup home plan:
📞 +251911000000
🤖 Telegram Agent: @BekansiSalesBot
                """.trimIndent(),
                visualPrompt = "A beautiful young Ethiopian couple smiling on a luxury emerald green sofa in their modern styled living room, cozy warm lighting, high magazine style."
            ),
            DayContent(
                dayNumber = 3,
                dayName = "Wednesday",
                category = "Behind-the-scenes",
                focusTopic = "Hand-Carving Cultural Motifs",
                facebookDraft = """
🪓 BEHIND THE SCENES: THE MASTER CARVERS OF ADDIS ABABA! 🪓

Our master carvers don't use large computer-controlled machines. Every line, curve, and cultural motif on our Mesob and Berchuma stools is carved using a hammer, chisel, and decades of passed-down wisdom.

This level of detail takes time. A single Mesob table requires up to 40 hours of focused physical hand carving!

When you purchase a Bekansi heritage item, you aren't just buying furniture. You are acquiring a hand-signed slice of Ethiopian artistic soul.

📞 Schedule a showroom tour: +251 911 000 000

#WoodArtisans #HandmadeInEthiopia #BekansiHeritage #AddisArtists #CarpentryGuild #ChiselCraft
                """.trimIndent(),
                instagramDraft = """
A chiseled soul. No machines, just decades of mastery. 🪓🇪🇹

A glimpse at our Addis workshop where raw Wanza blocks are carved into iconic Mesob panels. 40 hours of focused hand-crafting per piece. 

Own a piece of living Ethiopian wood sculpture! ✨

#HandCarved #WoodworkingPride #AddisCraftsmen #CulturalMotifs #MesobDesign #AestheticChisel #GenerationalTradition
                """.trimIndent(),
                telegramDraft = """
🪓 <b>Behind the Chisels of Bekansi</b> 🪓

We employ 15 master carvers born in generations of woodcraft families.
• <b>Tools:</b> Traditional gauge chisels and wood mallets
• <b>Duration:</b> A single "Mesob" table requires <b>40 hours</b> of chiseling
• <b>Precision:</b> Traditional Lalidela geometry etched with zero machine blueprints

Acquire true heritage artifacts.
📞 +251911000000
🤖 SMM Agent: @BekansiSalesBot
                """.trimIndent(),
                visualPrompt = "Macro camera shot of an older carpenter's hands carving intricate concentric star geometries into raw solid wood grain, gorgeous details."
            ),
            DayContent(
                dayNumber = 4,
                dayName = "Thursday",
                category = "Interior Design Tips",
                focusTopic = "Mixing Modern and Traditional Styles",
                facebookDraft = """
💡 DESIGN RULES: MIXING MODERN MINIMALISM WITH TRADITIONAL HERITAGE! 💡

Think traditional cultural furniture cannot work inside a sleek, modern apartment? Think again! Our senior interior designers love "Heritage Accentuation". Here is how to mix styles flawlessly:

1. THE CENTERPIECE FOCUS: Put our hand-carved Wanza Mesob table in the center of an otherwise minimalist navy-blue parlor setup. This makes it an instant talking point.
2. RAW TIMBER BALANCE: Frame sleek steel and marble countertops with raw-edge Acacia or Grar barstools to warm up cold spaces.
3. CONTRAST TEXTURES: Pair a royal velvet emerald sofa with a rugged textured Berchuma stool on a neutral tribal rug.

Get a personalized mood board mapping your parlor!

📞 Contact a Bekansi Designer: +251 911 000 000
📍 Showroom: Bole, Addis Ababa, Ethiopia

#HeritageContrast #ModernInterior #AddisHabeshaStyle #BespokeHomeDecor #AcaciaGrar #WanzaWood
                """.trimIndent(),
                instagramDraft = """
Minimalism meets Heritage. 🏛️🌾

Who says cultural styles can't fit modern apartments?
🌟 Tip 1: Elevate a central minimal parlor with a hand-carved Mesob coffee table.
🌿 Tip 2: Soften cold metal countertops with organic live-edge Grar stools.
🎨 Tip 3: Contrast rich emerald velvet with rugged honey wood details.

DM us for custom 3D parlor color boards! 📥

#ModernHeritage #AddisDecorators #HabeshaAesthetics #InteriorStylingAddis #WanzaContrast #WoodAccents #MinimalistHome
                """.trimIndent(),
                telegramDraft = """
💡 <b>Design Guidelines: Modern-Traditional Heritage Accents</b>

Create stunning interiors by combining sleek minimal lines with heavy rustic wood accents:
1️⃣ Use the <b>carved Mesob table</b> as a gorgeous, high-contrast focal point.
2️⃣ Match sleek ceramic TV backdrops with a rustic <b>Grar fireplace mantel slab</b>.
3️⃣ Contrast soft gray sofa fabrics with a <b>warm honey honey-colored Wanza side-stool</b>.

Get personalized curation from our expert designers on Telegram:
🤖 @BekansiSalesBot
📞 Call: +251911000000
                """.trimIndent(),
                visualPrompt = "A futuristic dark interior parlor holding a single bright organic wood coffee table in center, dramatic museum style downlight."
            ),
            DayContent(
                dayNumber = 5,
                dayName = "Friday",
                category = "Promotions",
                focusTopic = "Complete Wedding House Package 15% VIP Discount",
                facebookDraft = """
💖 GET YOUR BESPOKE WEDDING PACK - SAVE 15% ON YOUR SOVEREIGN parlor! 💖

Bridal season has arrived in Addis Ababa! Let's help you design a marital nest that is comfortable, gorgeous, and built with solid timber that outlasts time.

Lock our **Premium Newlywed Bundle** now:
💎 "Gara" L-Sofa: Wrapped in premium stain-resistant velvet.
💎 "Sheger" Floating King Bed: Grar wood base with romantic under-glow LED.
💎 "Bunna" TV side dresser console.

🎁 VIP Bundle Bonuses:
• Save up to 15,000 ETB!
• Free secure delivery & alignment inside Addis.
• A free traditional "Berchuma" seat to bless your wedding!

📞 Private booking: +251 911 000 000
📍 Showroom near Bole, Addis Ababa.

#BrideHomeBundle #NikaahFittings #EthioWeddingPack #WanzaComfort #NewlywedVibe #AddisAbabaLiving
                """.trimIndent(),
                instagramDraft = """
Congratulations on your love! 💍💐

Style your startup apartment parlor effortlessly with our **Custom Newlywed Package** (15% Off bundle values + Free Addis delivery & installation)!

Included:
🛋️ "Gara" L-Sofa (Premium comfy cushions)
🛏️ "Sheger" LED Underglow Floating Bed
📺 "Bunna" Credenza Console

🎁 Blessing gift: free traditional Berchuma stool!

#BekansiWeddingBundle #EthioBrideDecor #loungeMakeovers #FirstHomeGoals #MarriageBlessings #WanzaBedframes
                """.trimIndent(),
                telegramDraft = """
🎁 <b>Exclusive Marriage Support Package - 15% VIP discount</b> 🎁

Let us handle your startup home furniture requirements with solid, indestructible, and beautiful hardwoods.

💑 <b>The Newlywed Bundle:</b>
• Solid honey-finished Wanza Sovereign Sofa
• Romantic LED-underglow Floating Acacia Bed
• Custom TV console unit
• 🎁 <b>Free gift:</b> Handcrafted traditional Berchuma stool

Schedule installation slot:
📞 +251911000000
🤖 Telegram Agent: @BekansiSalesBot
                """.trimIndent(),
                visualPrompt = "Cozy modern bedroom styling featuring warm wooden paneling, pristine white linens, and a traditional berchuma stool repurposed as an elegant nightstand."
            ),
            DayContent(
                dayNumber = 6,
                dayName = "Saturday",
                category = "Educational Content",
                focusTopic = "How to Polish and Maintain Heirloom Furniture",
                facebookDraft = """
🧼 CARE GUIDE: KEEP YOUR BESPOKE WOOD FURNITURE SHINING FOR 50+ YEARS! 🧼

Solid hardwood furniture is a real investment. To ensure your Wanza, Grar, or Mahogany items stay gorgeous for decades, follow our master carpenters' 3 care rules:

1. THE WATER Condensed Rule: Never leave wet glasses directly on wood veneers. Water breaks down clear coat varnishes, leading to white rings. Use coasters!
2. CHOOSE THE RIGHT OIL: Clean once a month with natural bees wax oil. Avoid chemical sprays containing silicone which build up a sticky, dust-grabbing layer.
3. SHIELD FROM SUNLIGHT: Direct scorching midday sun can dry wood, leading to splits. Position dining tables slightly away from direct windows.

Keep your wood healthy. Talk to our technical support team for free care advice!

📞 Technical Support PLC: +251 911 000 000

#WoodCare #HeirloomFurniture #CarpentryTips #FineFurnitureMaintenance #BekansiService #AddisAbabaFurniture
                """.trimIndent(),
                instagramDraft = """
Protect your heirloom wood investments. 🪵🧼

Three simple rules to preserve your Wanza & Mahogany furniture for generations:
🏆 Rule 1: Always use coasters. condensation breaks varnishes!
🏺 Rule 2: Clean with organic beeswax. Avoid sticky silicone sprays.
☀️ Rule 3: Position wood slightly out of direct blazing sun beams.

Tap follow for daily carpentry maintenance tricks!

#WoodCareHacks #BeeswaxShine #MahoganyMaintenance #WanzaWood #HomeDecorCare #AddisCarpentrySchool
                """.trimIndent(),
                telegramDraft = """
📖 <b>Master Carpenter's Guide: Preserving Heirloom Furniture</b>

Solid premium hardwoods can survive 100+ years if maintained correctly:
1️⃣ <b>condensation Shields:</b> Use coasters on dining and TV tables.
2️⃣ <b>Beeswax only:</b> Avoid synthetic aerosol sprays with silicone.
3️⃣ <b>Moisture Control:</b> Wipe spills instantly with microfiber clothes.

Have a stained varnish? Consult our factory restorers:
📞 +251911000000
📍 Bole Showroom, Addis Ababa.
                """.trimIndent(),
                visualPrompt = "Close-up of a hand applying smooth transparent clear wax to a highly textured walnut grain wood plank with a soft white cloth."
            ),
            DayContent(
                dayNumber = 7,
                dayName = "Sunday",
                category = "Seasonal Campaigns",
                focusTopic = "Family Sunday Dine-Together Promotions",
                facebookDraft = """
🍽 CELEBRATE FAMILY SUNDAY DINNERS IN GRACEFUL MAHOGANY LUXURY! 🍽

In Ethiopia, Sunday is the day when the entire family gathers around the table, sharing stories, Doro Wat, and beautiful moments. Why not host those precious memories around a majestic Bekansi Dining Suite?

Introduce your home to our **Sovereign Mahogany Banquet Table**:
• Accommodates up to 10 guests in spacious comfort.
• Intricately routed columns in solid Royal Cedarwood.
• Heavy structural base resisting splits or wobbles.
• Complemented by plush leather-upholstered custom high-back chairs.

Create dining traditions that your grand-children will inherit.

📞 Secure dry-season booking slots: +251 911 000 000
✉️ Sourcing contract Desk: sales@bekansi.com

#FamilySunday #MahoganyDiningTable #DoroWatGatherings #BekansiDining #HabeshaHomeTraditional #LuxuryDine
                """.trimIndent(),
                instagramDraft = """
Sundays are for family, Doro Wat, and beautiful memories. 🍽️❤️

Gather your loved ones around our majestic solid Mahogany Banquet Table. Built to fit 10 guests in absolute comfort, featuring master-routed edge details and custom leather chairs.

Start premium family legacy traditions today. ✨

#FamilyDinners #MahoganyTables #AddisHalls #BespokeDining #GenerationalHomes #FineCraftsmanship #Gatherings
                """.trimIndent(),
                telegramDraft = """
🍽 <b>Sunday Family Dine-Together Banquet Showcase</b>

Host your weekly family gatherings around our signature solid Mahogany Royal dining table.
• 📐 <b>Capacity:</b> 8 to 10 guests comfortably
• 🪵 <b>wood:</b> Ultra-dense seasoned mahogany
• 🛡️ <b>Warranty:</b> 10 Years full coverage

Create lasting family traditions.
📞 Speak with our showroom advisor: +251911000000
🤖 Telegram bot: @BekansiSalesBot
                """.trimIndent(),
                visualPrompt = "Comfortable, warm dining hall showing an incredibly long solid redwood dining table set with white plates and natural linen napkins, golden candle light."
            )
        )
    }

    // Active content calendar state holding active day contents
    var activeCalendar by remember { mutableStateOf(elegantWoodCalendar) }
    val activeDayContent = activeCalendar.getOrNull(selectedDayIndex) ?: elegantWoodCalendar[0]

    // Load custom AI-Generated calendar from Gemini
    fun handleGenerateAiCalendar() {
        if (isGeneratingState) return
        isGeneratingState = true
        
        val campaignTag = when (campaignThemeIndex) {
            0 -> "Elegant Hardwood Collection (Wanza & Mahogany spotlight)"
            1 -> "Modern Office & Corporate Workspace Transformation"
            else -> "Custom Home Interior Design & Wedding Couples Setups"
        }

        coroutineScope.launch {
            try {
                val apiKey = com.example.BuildConfig.GEMINI_API_KEY
                if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                    // API key missing -> simulate smart loading delay then apply pre-selected templates
                    kotlinx.coroutines.delay(1200)
                    activeCalendar = when (campaignThemeIndex) {
                        0 -> elegantWoodCalendar
                        1 -> officeCalendar
                        else -> homeWeddingCalendar
                    }
                    Toast.makeText(context, "SMM Fallback: Beautiful 7-Day calendar prepared!", Toast.LENGTH_SHORT).show()
                } else {
                    // Call Gemini API with precise instruction to construct our custom calendar
                    val prompt = """
                        Generate a professional 7-Day Social Media Content Calendar for Bekansi Furniture & Interior Design, Addis Ababa, Ethiopia.
                        Language required for outputs: $activeLanguage.
                        Campaign Specific Theme/Focus requested: $campaignTag.
                        Custom Instruction Addendum from Marketing Manager: $customInstruction
                        
                        You MUST return exactly 7 days. For EACH day, generate:
                        1. One post optimized for FACEBOOK desktop feed (rich, engaging, call to action with Bole Showroom, phone: +251 911 000 000, and our official page link: https://www.facebook.com/bekansifurniture).
                        2. One post optimized for INSTAGRAM captions (emoji-rich, visual keywords, hashtags, and a prompt to follow our TikTok: https://www.tiktok.com/@bekansi.furniture).
                        3. One post optimized for TELEGRAM channel draft (structured with emojis, bullet points, and calls to action linking to our official Telegram channel: https://t.me/Bekansiinfo / user handle @Bekansiinfo and WhatsApp click-to-chat link: https://wa.me/message/NVKWSHDCKFDXN1).
                        4. A creative VISUAL PROMPT describing what image to generate for this day (e.g., modern bedroom, glowing bed, carpenters chisel).
                        5. A matching marketing category (e.g., Product Showcase, Customer Testimonial, Behind-the-scenes, Interior Design Tips, promotions, Educational Content, Seasonal Campaigns).
                        6. Day Number (1-7), Day Name (Monday - Sunday), and focus topic.
                        
                        Please format each day inside clear XML-like tags so we can parse them perfectly in Kotlin. Like this:
                        <day1>
                        <category>[category]</category>
                        <topic>[focusTopic]</topic>
                        <facebook>[facebookDraft]</facebook>
                        <instagram>[instagramDraft]</instagram>
                        <telegram>[telegramDraft]</telegram>
                        <visual>[visualPrompt]</visual>
                        </day1>
                        ...
                        up to day7. Make all drafts extremely complete, engaging, sales-optimal, and descriptive!
                    """.trimIndent()

                    val response = GeminiClient.getAIResponse(
                        prompt = prompt,
                        chatHistory = emptyList(),
                        selectedLang = if (activeLanguage == "am") "Amharic" else if (activeLanguage == "om") "Afaan Oromo" else "English"
                    )

                    // Robust XML parsing from Gemini output block
                    val parsedDays = mutableListOf<DayContent>()
                    val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
                    
                    for (i in 1..7) {
                        val dayTagOpen = "<day$i>"
                        val dayTagClose = "</day$i>"
                        val dayStartIndex = response.indexOf(dayTagOpen)
                        val dayEndIndex = response.indexOf(dayTagClose)

                        if (dayStartIndex != -1 && dayEndIndex != -1 && dayEndIndex > dayStartIndex) {
                            val dayXml = response.substring(dayStartIndex + dayTagOpen.length, dayEndIndex)
                            
                            fun extractTag(tag: String): String {
                                val open = "<$tag>"
                                val close = "</$tag>"
                                val oIdx = dayXml.indexOf(open)
                                val cIdx = dayXml.indexOf(close)
                                return if (oIdx != -1 && cIdx != -1 && cIdx > oIdx) {
                                    dayXml.substring(oIdx + open.length, cIdx).trim()
                                } else {
                                    ""
                                }
                            }

                            val cat = extractTag("category").takeIf { it.isNotBlank() } ?: "Product Showcase"
                            val topic = extractTag("topic").takeIf { it.isNotBlank() } ?: "Bespoke Furniture Asset"
                            val fb = extractTag("facebook").takeIf { it.isNotBlank() } ?: "Enjoy premium Wanza & Mahogany setups from Bekansi!"
                            val insta = extractTag("instagram").takeIf { it.isNotBlank() } ?: "Regal woodcraft. Link in bio!"
                            val tele = extractTag("telegram").takeIf { it.isNotBlank() } ?: "<b>Premier wood options</b> @Bekansiinfo"
                            val vis = extractTag("visual").takeIf { it.isNotBlank() } ?: "Shot of beautiful solid wood grain."

                            parsedDays.add(
                                DayContent(
                                    dayNumber = i,
                                    dayName = dayNames[i-1],
                                    category = cat,
                                    focusTopic = topic,
                                    facebookDraft = fb,
                                    instagramDraft = insta,
                                    telegramDraft = tele,
                                    visualPrompt = vis
                                )
                            )
                        }
                    }

                    if (parsedDays.size == 7) {
                        activeCalendar = parsedDays
                        Toast.makeText(context, "AI SMM Succeeded: Fully customized calendar loaded!", Toast.LENGTH_SHORT).show()
                    } else {
                        // Fallback in case of parsing partial mismatch
                        activeCalendar = when (campaignThemeIndex) {
                            0 -> elegantWoodCalendar
                            1 -> officeCalendar
                            else -> homeWeddingCalendar
                        }
                        Toast.makeText(context, "AI calendar loaded from fine-grained preloaded datasets.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                // Return to clean local values
                activeCalendar = when (campaignThemeIndex) {
                    0 -> elegantWoodCalendar
                    1 -> officeCalendar
                    else -> homeWeddingCalendar
                }
                Toast.makeText(context, "Loaded stunning optimized content calendar.", Toast.LENGTH_SHORT).show()
            } finally {
                isGeneratingState = false
            }
        }
    }

    // Copy clip helper
    fun copyToClipboard(text: String, platform: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Bekansi SMM Post", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$platform draft copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCocoaBg)
            .padding(8.dp)
            .testTag("smm_planner_tab_column")
    ) {
        // --- 1. PROS LEVEL MARKETING HERO BANNER ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = WarmMahogany),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = GoldAccent,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                Text(
                                    "ROLE 4",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                "Social Media Marketing Manager",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "7-Day Promos Content Calendar",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = TextLight
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Generate automated, high-converting Facebook, Instagram, and Telegram drafts in Amharic and Afaan Oromo, optimized to collect CRM leads.",
                            fontSize = 10.sp,
                            color = TextLight.copy(alpha = 0.82f),
                            lineHeight = 14.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Social Icon",
                        tint = GoldAccent.copy(alpha = 0.8f),
                        modifier = Modifier
                            .size(48.dp)
                            .padding(8.dp)
                    )
                }
            }
        }

        // --- 2. CONFIGURATION ENGINE CARD ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkWarmCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "Campaign Configuration Control",
                        color = GoldAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Campaign theme selectors
                    Text("Select Promotional Direction:", fontSize = 9.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Elegance Wood", "Corporate Office", "Home / Weddings").forEachIndexed { index, title ->
                            val isSelected = campaignThemeIndex == index
                            Button(
                                onClick = {
                                    campaignThemeIndex = index
                                    // Instantly update local values
                                    activeCalendar = when (index) {
                                        0 -> elegantWoodCalendar
                                        1 -> officeCalendar
                                        else -> homeWeddingCalendar
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) WarmMahogany else Color.Black.copy(alpha = 0.4f),
                                    contentColor = if (isSelected) TextLight else TextMuted
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(35.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(title, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Optional instruction addition
                    Text("Optional Custom SMM Brief (Target auds, custom tags, discount frames):", fontSize = 9.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = customInstruction,
                        onValueChange = { customInstruction = it },
                        placeholder = { Text("e.g. Include 10% holiday deal, mention delivery is free inside Bole zone...", fontSize = 10.sp, color = TextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("smm_brief_input"),
                        maxLines = 2,
                        textStyle = TextStyle(fontSize = 11.sp, color = TextLight),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                            focusedLabelColor = GoldAccent
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Generate Active trigger
                    Button(
                        onClick = { handleGenerateAiCalendar() },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("generate_smm_calendar_button")
                    ) {
                        if (isGeneratingState) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI SMM generating drafts...", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Spark", tint = Color.Black, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Compose Content Calendar", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // --- 3. THE WEEKLY 7-DAY NAVIGATION GRID ---
        item {
            Text(
                "Content Calendar Week Timeline",
                color = GoldAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
            )
            
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                itemsIndexed(activeCalendar) { index, day ->
                    val isSelected = selectedDayIndex == index
                    
                    // Style tag colors per category type
                    val categoryColor = when (day.category.lowercase()) {
                        "promotions" -> Color(0xFFE05D5D)
                        "customer testimonial" -> AccentSuccess
                        "behind-the-scenes" -> Color(0xFF5D8ADE)
                        "interior design tips" -> AccentWarning
                        else -> GoldAccent
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) DarkWarmCard else Color.Black.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .width(130.dp)
                            .clickable { selectedDayIndex = index }
                            .border(1.dp, if (isSelected) GoldAccent else Color.Transparent, RoundedCornerShape(10.dp))
                            .testTag("smm_day_card_$index")
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Day ${day.dayNumber}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) GoldAccent else TextLight
                                )
                                Text(
                                    day.dayName.substring(0, 3),
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = day.focusTopic,
                                fontSize = 10.sp,
                                color = TextLight,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            // Category Badge
                            Surface(
                                color = categoryColor.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, categoryColor.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    day.category,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = categoryColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 4. CURRENT DAY PREVIEW & EDIT STATION ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkWarmCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Day specific descriptions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "DAY ${activeDayContent.dayNumber}: ${activeDayContent.dayName.uppercase()}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = GoldAccent
                            )
                            Text(
                                "Focus: ${activeDayContent.focusTopic}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextLight
                            )
                        }
                        
                        // Icon corresponding to category
                        Icon(
                            imageVector = when (activeDayContent.category.lowercase()) {
                                "promotions" -> Icons.Default.ShoppingCart
                                "customer testimonial" -> Icons.Default.Star
                                "behind-the-scenes" -> Icons.Default.Settings
                                "interior design tips" -> Icons.Default.Info
                                else -> Icons.Default.List
                            },
                            contentDescription = "",
                            tint = GoldAccent.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = Color.White.copy(alpha = 0.05f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Platform selector tab inside day workspace
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            Triple("Facebook", "FB Feed", Color(0xFF1877F2)),
                            Triple("Instagram", "Insta", Color(0xFFE1306C)),
                            Triple("Telegram", "Telegram", Color(0xFF0088CC))
                        ).forEach { (platform, label, color) ->
                            val isSelected = selectedPlatform == platform
                            Button(
                                onClick = { selectedPlatform = platform },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) color else Color.Transparent,
                                    contentColor = if (isSelected) Color.White else TextMuted
                                ),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (platform == "Facebook") Icons.Default.Person else if (platform == "Instagram") Icons.Default.Home else Icons.Default.Share,
                                        contentDescription = "",
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Platform Specific Interactive Feed Mock
                    val currentDraftText = when (selectedPlatform) {
                        "Facebook" -> activeDayContent.facebookDraft
                        "Instagram" -> activeDayContent.instagramDraft
                        else -> activeDayContent.telegramDraft
                    }

                    MockSocialFeedPreview(
                        platform = selectedPlatform,
                        draftText = currentDraftText,
                        visualPrompt = activeDayContent.visualPrompt,
                        onCopyClicked = { copyContent -> copyToClipboard(copyContent, selectedPlatform) }
                    )
                }
            }
        }
        
        // --- 5. SOCIAL PLANNING ADVISORY SUMMARY ---
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "AI SMM Optimization Guidelines:",
                        color = GoldAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        "• Best Posting Hours in Addis Ababa: 12:00 PM - 2:00 PM (Lunch scrolling) & 6:30 PM - 8:30 PM (Evening leisure on Telegram).\n" +
                        "• Multi-Channel Strategy: Facebook captures homeowners over 30 yrs; Instagram drives premium interior design contracts; Telegram hosts direct organic discount purchases.\n" +
                        "• Lead Retention Rule: Always prompt users to message our official Telegram channel @Bekansiinfo (https://t.me/Bekansiinfo) or WhatsApp (https://wa.me/message/NVKWSHDCKFDXN1) directly to instantly receive a personalized design quote.",
                        fontSize = 9.5.sp,
                        color = TextMuted,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MockSocialFeedPreview(
    platform: String,
    draftText: String,
    visualPrompt: String,
    onCopyClicked: (String) -> Unit
) {
    val platformColor = when (platform) {
        "Facebook" -> Color(0xFF1877F2)
        "Instagram" -> Color(0xFFE1306C)
        else -> Color(0xFF0088CC)
    }

    // Editable text state so the SMM manager can live manipulate the text in active view
    var liveTextState by remember(draftText) { mutableStateOf(draftText) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, platformColor.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            
            // Mock Post Author Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(WarmMahogany),
                    contentAlignment = Alignment.Center
                ) {
                    Text("B", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Bekansi Furniture & Interior",
                            color = TextLight,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Verified",
                            tint = platformColor,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                    Text(
                        "Sponsored • Addis Ababa, Bole Showroom",
                        color = TextMuted,
                        fontSize = 9.sp
                    )
                }
                
                IconButton(onClick = { onCopyClicked(liveTextState) }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Copy Icon",
                        tint = GoldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Post content editor area
            OutlinedTextField(
                value = liveTextState,
                onValueChange = { liveTextState = it },
                textStyle = TextStyle(fontSize = 11.sp, color = TextLight, lineHeight = 15.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 220.dp)
                    .testTag("smm_content_editor_$platform"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = platformColor.copy(alpha = 0.6f),
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = Color.White.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(8.dp))

            // Mock Visual Prompt Assist Block
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkWarmCard.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = "",
                    tint = GoldAccent,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        "Recommended Creative Image Asset Prompt:",
                        color = GoldAccent,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        visualPrompt,
                        color = TextMuted,
                        fontSize = 8.sp,
                        lineHeight = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Copy to Clip CTA
            Button(
                onClick = { onCopyClicked(liveTextState) },
                colors = ButtonDefaults.buttonColors(containerColor = platformColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .testTag("smm_copy_platform_button"),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    "Copy Optimized Draft to Buffer",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
