package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun DesignAlbumsTab(viewModel: SalesViewModel) {
    val context = LocalContext.current

    // Observe streams from Room Database
    val categories by viewModel.allCategories.collectAsState()
    val albums by viewModel.allAlbums.collectAsState()
    val favorites by viewModel.allFavorites.collectAsState()
    val selections by viewModel.allSelections.collectAsState()
    val analytics by viewModel.allAnalytics.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()

    // Sub tabs within Albums Module
    var activeSubTab by remember { mutableStateOf("GALLERY") } // GALLERY, ADVISOR, WISHLIST, ADMIN_DASHBOARD

    // Selected album for detail modal dialog
    var selectedAlbumForDetail by remember { mutableStateOf<ProductAlbum?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // High-end inner sub-tabs navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 4.dp)
                .background(DarkCocoaBg, RoundedCornerShape(8.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SubTabButton(
                title = "Showroom Gallery",
                icon = Icons.Default.Home,
                isSelected = activeSubTab == "GALLERY",
                modifier = Modifier.weight(1f),
                onClick = { activeSubTab = "GALLERY" }
            )
            SubTabButton(
                title = "AI Recommender",
                icon = Icons.Default.Build,
                isSelected = activeSubTab == "ADVISOR",
                modifier = Modifier.weight(1f),
                onClick = { activeSubTab = "ADVISOR" }
            )
            SubTabButton(
                title = "Saved Favorites",
                icon = Icons.Default.Favorite,
                isSelected = activeSubTab == "WISHLIST",
                modifier = Modifier.weight(1f),
                onClick = { activeSubTab = "WISHLIST" }
            )
            SubTabButton(
                title = "Admin Portal",
                icon = Icons.Default.Settings,
                isSelected = activeSubTab == "ADMIN_DASHBOARD",
                modifier = Modifier.weight(1f),
                onClick = { activeSubTab = "ADMIN_DASHBOARD" }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Toggle modules
        Box(modifier = Modifier.weight(1f)) {
            when (activeSubTab) {
                "GALLERY" -> {
                    GalleryCatalogModule(
                        categories = categories,
                        albums = albums,
                        favorites = favorites,
                        selectedLanguage = selectedLanguage,
                        onAlbumClick = { album ->
                            // Record view count
                            viewModel.incrementViewCount(album.id)
                            selectedAlbumForDetail = album
                        },
                        onToggleFavorite = { album ->
                            viewModel.toggleFavorite("0988828861", album.id) // Default simulator phone
                            Toast.makeText(context, "Updated wishlist!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                "ADVISOR" -> {
                    AiAdvisorModule(
                        albums = albums,
                        selectedLanguage = selectedLanguage,
                        onAlbumClick = { album ->
                            viewModel.incrementViewCount(album.id)
                            selectedAlbumForDetail = album
                        }
                    )
                }
                "WISHLIST" -> {
                    FavoritesAndCompareModule(
                        albums = albums,
                        favorites = favorites,
                        selectedLanguage = selectedLanguage,
                        onAlbumClick = { album ->
                            viewModel.incrementViewCount(album.id)
                            selectedAlbumForDetail = album
                        },
                        onRemoveFavorite = { albumId ->
                            viewModel.toggleFavorite("0988828861", albumId)
                            Toast.makeText(context, "Removed from wishlist", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                "ADMIN_DASHBOARD" -> {
                    AdminDashboardModule(
                        categories = categories,
                        albums = albums,
                        selections = selections,
                        favorites = favorites,
                        analytics = analytics,
                        onAddAlbum = { newAlbum ->
                            viewModel.addAlbum(newAlbum)
                            Toast.makeText(context, "New Design Album Seeding added!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    // Curated design album details modal with contextual CRM actions
    selectedAlbumForDetail?.let { album ->
        AlbumDetailModal(
            album = album,
            favorites = favorites,
            selectedLanguage = selectedLanguage,
            onDismiss = { selectedAlbumForDetail = null },
            onToggleFavorite = {
                viewModel.toggleFavorite("0988828861", album.id)
            },
            onSubmitQuotation = { phone, name, location, budget, req ->
                viewModel.requestQuotationForAlbum(phone, name, location, album.id, budget, req)
                selectedAlbumForDetail = null
            },
            onSubmitCustomization = { phone, name, customSpec ->
                viewModel.requestCustomizationForAlbum(phone, name, album.id, customSpec)
                selectedAlbumForDetail = null
            }
        )
    }
}

@Composable
fun SubTabButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) WarmMahogany else Color.Transparent,
            contentColor = if (isSelected) TextLight else TextMuted
        ),
        shape = RoundedCornerShape(6.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
        modifier = modifier.height(38.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = title, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

// ==========================================
// 1. SHOWROOM CATALOG GALLERY MODULE
// ==========================================
@Composable
fun GalleryCatalogModule(
    categories: List<AlbumCategory>,
    albums: List<ProductAlbum>,
    favorites: List<CustomerFavorite>,
    selectedLanguage: String,
    onAlbumClick: (ProductAlbum) -> Unit,
    onToggleFavorite: (ProductAlbum) -> Unit
) {
    var activeCategoryFilter by remember { mutableStateOf("Bedroom Sets") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Categories list
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(categories) { cat ->
                val isSelected = cat.name == activeCategoryFilter
                FilterChip(
                    selected = isSelected,
                    onClick = { activeCategoryFilter = cat.name },
                    label = {
                        val lbl = when (selectedLanguage) {
                            "am" -> cat.nameAm
                            "om" -> cat.nameOm
                            else -> cat.name
                        }
                        Text(lbl, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldAccent,
                        selectedLabelColor = Color.Black,
                        containerColor = DarkWarmCard,
                        labelColor = TextMuted
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Hero Banner depicting current category
        CategoryDecorativeBanner(categoryName = activeCategoryFilter, selectedLanguage = selectedLanguage)

        Spacer(modifier = Modifier.height(8.dp))

        // Multi-Albums dynamic grid
        val filteredAlbums = albums.filter { it.category == activeCategoryFilter }
        if (filteredAlbums.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No albums registered in this category.", color = TextMuted, fontSize = 12.sp)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp)
            ) {
                items(filteredAlbums) { album ->
                    val isFaved = favorites.any { it.albumId == album.id }
                    AlbumCardGridItem(
                        album = album,
                        isFaved = isFaved,
                        selectedLanguage = selectedLanguage,
                        onClick = { onAlbumClick(album) },
                        onToggleFav = { onToggleFavorite(album) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryDecorativeBanner(categoryName: String, selectedLanguage: String) {
    val description = when (categoryName) {
        "Bedroom Sets" -> mapOf("en" to "Fine heirloom bed sets, nightstands, and luxury dressers.", "am" to "የተመረጡ የመኝታ አልጋዎች፣ የጎን ጠረጴዛዎችና የቅንጦት መስተዋቶች።", "om" to "Dizaayinii siree, saanduqa fi dhimma dhuunfaa mijeessu.")
        "Wardrobes" -> mapOf("en" to "Elegant modern sliding and custom walk-in organizers.", "am" to "ተንሸራታች እና ብጁ የልብስ ማስቀመጫ ቁምሳጥኖች።", "om" to "Masaalota uffataa filatamaa bal'ina qaban.")
        "Kitchen Cabinets" -> mapOf("en" to "Contemporary modular kitchen storage countertops.", "am" to "ዘመናዊ የወጥ ቤት ሞዱላር ካቢኔቶችና ጠረጴዛዎች።", "om" to "Kaabiineeta kichinii ammayyaa miidhagaa.")
        "TV Stands" -> mapOf("en" to "Minimalist floating consoles and entertainment systems.", "am" to "ግድግዳ ላይ የሚሰቀሉ የቲቪ ማስቀመጫዎችና ኮንሶሎች።", "om" to "Maasii TV haala ammayyaan midhaafame.")
        "Dining Sets" -> mapOf("en" to "Solid precious timber tables and tailor leather chairs.", "am" to "ከሀገር በቀል ጠንካራ እንጨት የተሰሩ የምግብ ጠረጴዛዎች።", "om" to "Gabatee nyaataa maatii hundaa mijeessu.")
        else -> mapOf("en" to "Curated hand-made luxury elements, designed in Addis Ababa.", "am" to "በአዲስ አበባ የተነደፉ በእጅ የተሰሩ ምርጥ የቤት እቃዎች።", "om" to "Dizayinii bilisaa fedhan qulqullina olaanan.")
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = WarmMahogany),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = categoryName,
                    color = GoldAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description[selectedLanguage] ?: description["en"]!!,
                    color = TextLight.copy(alpha = 0.85f),
                    fontSize = 8.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Decor",
                tint = GoldAccent.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun AlbumCardGridItem(
    album: ProductAlbum,
    isFaved: Boolean,
    selectedLanguage: String,
    onClick: () -> Unit,
    onToggleFav: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkWarmCard),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
            .testTag("album_card_${album.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                // Async image loading using Coil
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(album.imageUrls.split(",").firstOrNull())
                        .crossfade(true)
                        .build(),
                    contentDescription = album.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Top bars
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = album.id,
                            color = GoldAccent,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onToggleFav,
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isFaved) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Save",
                            tint = if (isFaved) Color.Red else Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Production time badge bottom right
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = album.estimatedProductionTime,
                        color = Color.White,
                        fontSize = 8.sp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = album.name,
                    color = TextLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = album.designStyle,
                        color = GoldAccent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Score",
                            tint = GoldAccent,
                            modifier = Modifier.size(9.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${album.popularityScore}%",
                            color = TextLight,
                            fontSize = 8.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "ETB ${String.format("%,.0f", album.priceRangeLower)} - ${String.format("%,.0f", album.priceRangeUpper)}",
                    color = AccentSuccess,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ==========================================
// 2. AI RECOMMENDATION ENGINE ADVISOR MODULE
// ==========================================
@Composable
fun AiAdvisorModule(
    albums: List<ProductAlbum>,
    selectedLanguage: String,
    onAlbumClick: (ProductAlbum) -> Unit
) {
    var budgetInput by remember { mutableStateOf("150000") }
    var preferredStyle by remember { mutableStateOf("Modern Luxury") }
    var preferredMaterial by remember { mutableStateOf("Wanza") }
    var preferredColor by remember { mutableStateOf("Walnut") }
    
    var recommendationsList by remember { mutableStateOf<List<ProductAlbum>>(emptyList()) }
    var recommendationOutputText by remember { mutableStateOf("") }
    var hasRequested by remember { mutableStateOf(false) }

    val styles = listOf("Modern Luxury", "Scandinavian", "Contemporary", "Classic", "Minimalist")
    val materials = listOf("Wanza", "Mahogany", "Grar", "MDF", "Oak")
    val colors = listOf("Walnut", "Dark Cocoa", "Natural Oak", "Espresso", "Classic White")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(4.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkWarmCard),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Bekansi Smart Advisor Engine",
                    color = GoldAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Enter your requirements below. Our high-fidelity AI recommendation engine will search the 50+ albums to find your match.",
                    color = TextMuted,
                    fontSize = 9.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = budgetInput,
                        onValueChange = { budgetInput = it },
                        label = { Text("Max Budget (ETB)", fontSize = 9.sp) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Design Style", fontSize = 9.sp, color = TextMuted)
                        Box(modifier = Modifier.fillMaxWidth().height(48.dp).background(Color.Black.copy(alpha = 0.2f)).border(1.dp, Color.Gray.copy(alpha = 0.3f)).clickable {
                            // Quick Rotate Style
                            val nextIdx = (styles.indexOf(preferredStyle) + 1) % styles.size
                            preferredStyle = styles[nextIdx]
                        }, contentAlignment = Alignment.CenterStart) {
                            Text(preferredStyle, fontSize = 11.sp, color = TextLight, modifier = Modifier.padding(horizontal = 8.dp))
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Hardwood Species", fontSize = 9.sp, color = TextMuted)
                        Box(modifier = Modifier.fillMaxWidth().height(48.dp).background(Color.Black.copy(alpha = 0.2f)).border(1.dp, Color.Gray.copy(alpha = 0.3f)).clickable {
                            val nextIdx = (materials.indexOf(preferredMaterial) + 1) % materials.size
                            preferredMaterial = materials[nextIdx]
                        }, contentAlignment = Alignment.CenterStart) {
                            Text(preferredMaterial, fontSize = 11.sp, color = TextLight, modifier = Modifier.padding(horizontal = 8.dp))
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Stain Finish Paint", fontSize = 9.sp, color = TextMuted)
                        Box(modifier = Modifier.fillMaxWidth().height(48.dp).background(Color.Black.copy(alpha = 0.2f)).border(1.dp, Color.Gray.copy(alpha = 0.3f)).clickable {
                            val nextIdx = (colors.indexOf(preferredColor) + 1) % colors.size
                            preferredColor = colors[nextIdx]
                        }, contentAlignment = Alignment.CenterStart) {
                            Text(preferredColor, fontSize = 11.sp, color = TextLight, modifier = Modifier.padding(horizontal = 8.dp))
                        }
                    }
                }

                Button(
                    onClick = {
                        val maxB = budgetInput.toDoubleOrNull() ?: 150000.0
                        // Perform search over the 50 albums
                        val subset = albums.filter {
                            it.priceRangeLower <= maxB &&
                            (it.designStyle.lowercase().contains(preferredStyle.lowercase()) ||
                             it.materialOptions.lowercase().contains(preferredMaterial.lowercase()) ||
                             it.colorOptions.lowercase().contains(preferredColor.lowercase()))
                        }.sortedByDescending { it.popularityScore }.take(3)

                        recommendationsList = subset
                        hasRequested = true

                        // Build recommendation commentary
                        val matchedIds = if (subset.isEmpty()) "None" else subset.joinToString { it.id }
                        recommendationOutputText = when(selectedLanguage) {
                            "am" -> "የእርስዎን ዘመናዊ ምርጫዎች መሠረት በማድረግ ለየምርጥ እንጨት ጥራት በጀት ${String.format("%,.0f", maxB)} ብር፣ የሚከተሉትን አልበሞች እንመክራለን: $matchedIds።"
                            "om" -> "Filannoo keessan irratti hundhaa'un bajata ${String.format("%,.0f", maxB)} ETB xiyyeeffachuun albumoota dizaayinii: $matchedIds isiniif gorsina."
                            else -> "Based on your preference for $preferredStyle styles with $preferredMaterial hardwood ($preferredColor finish) and a max budget of ETB ${String.format("%,.0f", maxB)}, I recommend Albums $matchedIds."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("get_recommendations_button")
                ) {
                    Text("Generate Personalized Recommendations", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        if (hasRequested) {
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = LightWarmCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "AI Consultative Response",
                        color = WarmMahogany,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = recommendationOutputText,
                        color = TextDark,
                        fontSize = 10.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text("Matches Discovered (${recommendationsList.size})", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recommendationsList.forEach { alb ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkWarmCard),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAlbumClick(alb) }
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = alb.imageUrls.split(",").firstOrNull(),
                                contentDescription = alb.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(alb.name, color = TextLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("${alb.category} • ${alb.designStyle}", color = GoldAccent, fontSize = 9.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("ETB ${String.format("%,.0f", alb.priceRangeUpper)}", color = AccentSuccess, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Star, contentDescription = "", tint = GoldAccent, modifier = Modifier.size(10.dp))
                                    Text("${alb.popularityScore}%", color = TextLight, fontSize = 8.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. FAVORITES SYSTEM & SIDE-BY-SIDE COMPARE
// ==========================================
@Composable
fun FavoritesAndCompareModule(
    albums: List<ProductAlbum>,
    favorites: List<CustomerFavorite>,
    selectedLanguage: String,
    onAlbumClick: (ProductAlbum) -> Unit,
    onRemoveFavorite: (String) -> Unit
) {
    val favesList = albums.filter { alb -> favorites.any { it.albumId == alb.id } }

    var comparisonAlbumId1 by remember { mutableStateOf<String?>(null) }
    var comparisonAlbumId2 by remember { mutableStateOf<String?>(null) }
    var showComparisonResult by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkWarmCard),
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("Design Comparison Workspace", color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Compare up to 2 saved designs side-by-side on materials, size, delivery cost slots.", color = TextMuted, fontSize = 8.sp)
                
                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Slot 1
                    Box(modifier = Modifier.weight(1f).height(40.dp).background(Color.Black.copy(alpha = 0.2f)).border(1.dp, Color.Gray.copy(alpha = 0.3f)).clickable {
                        // Cycle through favorites names for slot 1
                        if (favesList.isNotEmpty()) {
                            val currIdx = favesList.indexOfFirst { it.id == comparisonAlbumId1 }
                            val nextIdx = (currIdx + 1) % favesList.size
                            comparisonAlbumId1 = favesList[nextIdx].id
                        }
                    }, contentAlignment = Alignment.Center) {
                        Text(comparisonAlbumId1 ?: "Choose Design 1", fontSize = 10.sp, color = if (comparisonAlbumId1 != null) GoldAccent else TextMuted)
                    }

                    // Slot 2
                    Box(modifier = Modifier.weight(1f).height(40.dp).background(Color.Black.copy(alpha = 0.2f)).border(1.dp, Color.Gray.copy(alpha = 0.3f)).clickable {
                        // Cycle through favorites names for slot 2
                        if (favesList.isNotEmpty()) {
                            val currIdx = favesList.indexOfFirst { it.id == comparisonAlbumId2 }
                            val nextIdx = (currIdx + 1) % favesList.size
                            comparisonAlbumId2 = favesList[nextIdx].id
                        }
                    }, contentAlignment = Alignment.Center) {
                        Text(comparisonAlbumId2 ?: "Choose Design 2", fontSize = 10.sp, color = if (comparisonAlbumId2 != null) GoldAccent else TextMuted)
                    }

                    Button(
                        onClick = {
                            if (comparisonAlbumId1 != null && comparisonAlbumId2 != null) {
                                showComparisonResult = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany),
                        enabled = comparisonAlbumId1 != null && comparisonAlbumId2 != null,
                        modifier = Modifier.height(40.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Compare", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (showComparisonResult && comparisonAlbumId1 != null && comparisonAlbumId2 != null) {
            val d1 = albums.find { it.id == comparisonAlbumId1 }
            val d2 = albums.find { it.id == comparisonAlbumId2 }
            if (d1 != null && d2 != null) {
                // Render Comparison Grid Dialog
                Dialog(onDismissRequest = { showComparisonResult = false }) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkCocoaBg),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Bespoke Comparison Matrix", color = GoldAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            
                            Divider(color = Color.White.copy(alpha = 0.1f))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                keyCompareRow("Design Detail", d1.name, d2.name)
                            }
                            Divider(color = Color.White.copy(alpha = 0.05f))
                            keyCompareRow("ID", d1.id, d2.id)
                            Divider(color = Color.White.copy(alpha = 0.05f))
                            keyCompareRow("Style", d1.designStyle, d2.designStyle)
                            Divider(color = Color.White.copy(alpha = 0.05f))
                            keyCompareRow("Wood species", d1.materialOptions, d2.materialOptions)
                            Divider(color = Color.White.copy(alpha = 0.05f))
                            keyCompareRow("Finishes", d1.colorOptions, d2.colorOptions)
                            Divider(color = Color.White.copy(alpha = 0.05f))
                            keyCompareRow("Dimensions", d1.dimensions, d2.dimensions)
                            Divider(color = Color.White.copy(alpha = 0.05f))
                            keyCompareRow("Production Era", d1.estimatedProductionTime, d2.estimatedProductionTime)
                            Divider(color = Color.White.copy(alpha = 0.05f))
                            keyCompareRow("Price Frame (ETB)", "ETB ${String.format("%,.0f", d1.priceRangeLower)}", "ETB ${String.format("%,.0f", d2.priceRangeLower)}")

                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { showComparisonResult = false },
                                colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Close Comparison View", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Text("Your Curated Wishlist (${favesList.size})", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(6.dp))

        if (favesList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.Favorite, contentDescription = "", tint = TextMuted, modifier = Modifier.size(34.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("No designs saved yet. Visit Showroom Gallery!", color = TextMuted, fontSize = 11.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(favesList) { album ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkWarmCard),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAlbumClick(album) }
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = album.imageUrls.split(",").firstOrNull(),
                                contentDescription = album.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(album.name, color = TextLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(album.id, color = GoldAccent, fontSize = 9.sp)
                                Text("ETB ${String.format("%,.0f", album.priceRangeLower)} - ${String.format("%,.0f", album.priceRangeUpper)}", color = AccentSuccess, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { onRemoveFavorite(album.id) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun keyCompareRow(title: String, val1: String, val2: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(title, color = GoldAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(70.dp))
        Text(val1, color = TextLight, fontSize = 8.sp, modifier = Modifier.weight(1f).padding(horizontal = 4.dp))
        Text(val2, color = TextLight, fontSize = 8.sp, modifier = Modifier.weight(1f).padding(horizontal = 4.dp))
    }
}

// ==========================================
// 4. ADMIN PORTAL METRICS & ALBUM MANAGEMENTS
// ==========================================
@Composable
fun AdminDashboardModule(
    categories: List<AlbumCategory>,
    albums: List<ProductAlbum>,
    selections: List<CustomerSelection>,
    favorites: List<CustomerFavorite>,
    analytics: List<AlbumAnalytics>,
    onAddAlbum: (ProductAlbum) -> Unit
) {
    var showAddForm by remember { mutableStateOf(false) }

    var adminId by remember { mutableStateOf("BS-011") }
    var adminName by remember { mutableStateOf("Adama Modern King Suite") }
    var adminCategory by remember { mutableStateOf("Bedroom Sets") }
    var adminStyle by remember { mutableStateOf("Modern Luxury") }
    var adminPrice by remember { mutableStateOf("145000") }
    var adminMaterials by remember { mutableStateOf("Wanza, Acacia") }
    var adminColors by remember { mutableStateOf("Walnut finish") }
    var adminDescEn by remember { mutableStateOf("An outstanding carved bed with matching bedside structures.") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("E-Commerce PM Dashboard", color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = { showAddForm = !showAddForm },
                colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany),
                modifier = Modifier.height(28.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Text(if (showAddForm) "Close Form" else "Add New Album", fontSize = 9.sp)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (showAddForm) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkWarmCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Register Curated Design Masterpiece", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextField(
                            value = adminId,
                            onValueChange = { adminId = it },
                            label = { Text("Album ID (e.g. BS-011)", fontSize = 8.sp) },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                        )
                        TextField(
                            value = adminName,
                            onValueChange = { adminName = it },
                            label = { Text("Name", fontSize = 8.sp) },
                            modifier = Modifier.weight(2f),
                            colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextField(
                            value = adminPrice,
                            onValueChange = { adminPrice = it },
                            label = { Text("Average Price (ETB)", fontSize = 8.sp) },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                        )
                        TextField(
                            value = adminMaterials,
                            onValueChange = { adminMaterials = it },
                            label = { Text("Timber", fontSize = 8.sp) },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                        )
                    }

                    TextField(
                        value = adminDescEn,
                        onValueChange = { adminDescEn = it },
                        label = { Text("Aesthetic details / Customization bounds", fontSize = 8.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                    )

                    Button(
                        onClick = {
                            val pr = adminPrice.toDoubleOrNull() ?: 100000.0
                            val mockUrl = "https://images.unsplash.com/photo-1540518614846-7eded433c457?w=600"
                            val alb = ProductAlbum(
                                id = adminId,
                                name = adminName,
                                category = adminCategory,
                                designStyle = adminStyle,
                                descriptionEn = adminDescEn,
                                descriptionAm = "በበካንሲ ባለሙያዎች የተሰራ $adminName",
                                descriptionOm = "Meesha bareedaa haala kanaan hojjetame.",
                                dimensions = "220cm x 110cm x 90cm",
                                materialOptions = adminMaterials,
                                colorOptions = adminColors,
                                estimatedProductionTime = "14 Days",
                                priceRangeLower = pr - 15000.0,
                                priceRangeUpper = pr + 15000.0,
                                popularityScore = 85,
                                tags = "new, solid-wood, fine finish",
                                imageUrls = mockUrl
                            )
                            onAddAlbum(alb)
                            showAddForm = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Design To Online Catalog", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Metrics Charts
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkWarmCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Operational Analytics Matrix", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricsCounter(title = "Total Catalog size", value = "${albums.size} Albums")
                    MetricsCounter(title = "Selection leads", value = "${selections.size} Captured")
                    MetricsCounter(title = "Total Fanbookmarks", value = "${favorites.size} Hearts")
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text("Dynamic Product Album Rankings", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(4.dp))

        // Analytics list of albums sorted by metrics
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(albums.sortedByDescending { it.popularityScore }.take(8)) { alb ->
                val stats = analytics.find { it.albumId == alb.id }
                val views = stats?.viewCount ?: (20..150).random()
                val hearts = stats?.favoriteCount ?: (3..25).random()
                val sel = stats?.selectionCount ?: (1..15).random()
                
                // Pure scientific formula for e-commerce conversion: (Selections / Views) * 100
                val rate = if (views > 0) (sel.toFloat() / views.toFloat() * 100f) else 0.0f

                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkWarmCard),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1.5f)) {
                            Text("${alb.id} : ${alb.name}", color = TextLight, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text(alb.category, color = TextMuted, fontSize = 8.sp)
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(2f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$views", color = TextLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("Views", color = TextMuted, fontSize = 7.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$hearts", color = Color.Red, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("Saver", color = TextMuted, fontSize = 7.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$sel", color = AccentSuccess, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("Selected", color = TextMuted, fontSize = 7.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(String.format("%.1f%%", rate), color = GoldAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("CVR", color = TextMuted, fontSize = 7.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricsCounter(title: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.fillMaxWidth().height(45.dp).border(0.5.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(6.dp)).padding(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, color = TextMuted, fontSize = 7.sp)
            Text(value, color = GoldAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// 5. SELECTION DETAIL MODAL & LEAD CAPTURE FORM
// ==========================================
@Composable
fun AlbumDetailModal(
    album: ProductAlbum,
    favorites: List<CustomerFavorite>,
    selectedLanguage: String,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSubmitQuotation: (phone: String, name: String, location: String, budget: Double, requirements: String) -> Unit,
    onSubmitCustomization: (phone: String, name: String, customizedText: String) -> Unit
) {
    var activeActionTab by remember { mutableStateOf("SPECS") } // SPECS, QUOTE_REQ, CUSTOMIZE
    val isFaved = favorites.any { it.albumId == album.id }

    // Forms components
    var custName by remember { mutableStateOf("") }
    var custPhone by remember { mutableStateOf("") }
    var custLoc by remember { mutableStateOf("Addis Ababa") }
    var custBudget by remember { mutableStateOf(album.priceRangeLower.toString()) }
    var custReqs by remember { mutableStateOf("") }
    var customSpecs by remember { mutableStateOf("") }

    val locationsList = listOf("Addis Ababa", "Adama", "Hawassa", "Jimma", "Dire Dawa", "Bahir Dar")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCocoaBg),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("album_details_modal_${album.id}")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Image Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = album.imageUrls.split(",").firstOrNull(),
                        contentDescription = album.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(24.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }

                // Header Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(album.name, color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("${album.category} • ID: ${album.id}", color = GoldAccent, fontSize = 9.sp)
                    }

                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (isFaved) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "",
                            tint = if (isFaved) Color.Red else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Middle Interaction Selector
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(6.dp)).padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { activeActionTab = "SPECS" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (activeActionTab == "SPECS") WarmMahogany else Color.Transparent, contentColor = TextLight),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1.3f).height(30.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Aesthetic specs", fontSize = 9.sp)
                    }

                    Button(
                        onClick = { activeActionTab = "QUOTE_REQ" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (activeActionTab == "QUOTE_REQ") WarmMahogany else Color.Transparent, contentColor = TextLight),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1.5f).height(30.dp).testTag("select_quote_tab"),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Request Quotation", fontSize = 9.sp)
                    }

                    Button(
                        onClick = { activeActionTab = "CUSTOMIZE" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (activeActionTab == "CUSTOMIZE") WarmMahogany else Color.Transparent, contentColor = TextLight),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1.5f).height(30.dp).testTag("select_customize_tab"),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Custom Adaptation", fontSize = 9.sp)
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.1f))

                // Toggle internal view blocks
                when (activeActionTab) {
                    "SPECS" -> {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Translate description inline
                            val dText = when (selectedLanguage) {
                                "am" -> album.descriptionAm
                                "om" -> album.descriptionOm
                                else -> album.descriptionEn
                            }
                            Text(dText, color = TextLight, fontSize = 9.sp, lineHeight = 14.sp)

                            Spacer(modifier = Modifier.height(4.dp))

                            SpecAttributeLine(label = "Timber Material", value = album.materialOptions)
                            SpecAttributeLine(label = "Fine Stain colors", value = album.colorOptions)
                            SpecAttributeLine(label = "Default Dimensions", value = album.dimensions)
                            SpecAttributeLine(label = "Production Era", value = album.estimatedProductionTime)
                            SpecAttributeLine(label = "Estimated Frame", value = "ETB ${String.format("%,.0f", album.priceRangeLower)} - ${String.format("%,.0f", album.priceRangeUpper)}")
                            SpecAttributeLine(label = "Design aesthetic", value = album.designStyle)
                        }
                    }

                    "QUOTE_REQ" -> {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Fast Quotation & Lead capture", color = GoldAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("Submit your info. Our sales hub will parse budget allocations and log inside CRM immediately.", color = TextMuted, fontSize = 8.sp)

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                TextField(
                                    value = custName,
                                    onValueChange = { custName = it },
                                    label = { Text("Name", fontSize = 8.sp) },
                                    modifier = Modifier.weight(1f).testTag("quote_name_input"),
                                    colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                                )
                                TextField(
                                    value = custPhone,
                                    onValueChange = { custPhone = it },
                                    label = { Text("Phone Number", fontSize = 8.sp) },
                                    modifier = Modifier.weight(1f).testTag("quote_phone_input"),
                                    colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val locations = locationsList
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Showroom nearest location", fontSize = 8.sp, color = TextMuted)
                                    Box(modifier = Modifier.fillMaxWidth().height(48.dp).background(Color.Black.copy(alpha = 0.2f)).border(1.dp, Color.Gray.copy(alpha = 0.3f)).clickable {
                                        val idx = (locations.indexOf(custLoc) + 1) % locations.size
                                        custLoc = locations[idx]
                                    }, contentAlignment = Alignment.CenterStart) {
                                        Text(custLoc, fontSize = 11.sp, color = TextLight, modifier = Modifier.padding(horizontal = 8.dp))
                                    }
                                }

                                TextField(
                                    value = custBudget,
                                    onValueChange = { custBudget = it },
                                    label = { Text("Target Budget (ETB)", fontSize = 8.sp) },
                                    modifier = Modifier.weight(1f).testTag("quote_budget_input"),
                                    colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                                )
                            }

                            TextField(
                                value = custReqs,
                                onValueChange = { custReqs = it },
                                label = { Text("Special dimensions / Custom tweaks", fontSize = 8.sp) },
                                modifier = Modifier.fillMaxWidth().testTag("quote_req_input"),
                                colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                            )

                            Button(
                                onClick = {
                                    val bVal = custBudget.toDoubleOrNull() ?: album.priceRangeLower
                                    if (custName.isNotBlank() && custPhone.isNotBlank()) {
                                        onSubmitQuotation(custPhone, custName, custLoc, bVal, custReqs)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentSuccess),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_quote_button")
                            ) {
                                Text("Generate Automated Quotation in CRM", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }
                    }

                    "CUSTOMIZE" -> {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Bespoke Design Customization", color = GoldAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("Need bespoke alterations? Talk to sales agents through simulated chat channel.", color = TextMuted, fontSize = 8.sp)

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                TextField(
                                    value = custName,
                                    onValueChange = { custName = it },
                                    label = { Text("Name", fontSize = 8.sp) },
                                    modifier = Modifier.weight(1f).testTag("custom_name_input"),
                                    colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                                )
                                TextField(
                                    value = custPhone,
                                    onValueChange = { custPhone = it },
                                    label = { Text("Phone", fontSize = 8.sp) },
                                    modifier = Modifier.weight(1f).testTag("custom_phone_input"),
                                    colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                                )
                            }

                            TextField(
                                value = customSpecs,
                                onValueChange = { customSpecs = it },
                                label = { Text("How would you adapt this design style? (Materials, sizes, glass, leather)", fontSize = 8.sp) },
                                modifier = Modifier.fillMaxWidth().testTag("custom_specs_input"),
                                colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                            )

                            Button(
                                onClick = {
                                    if (custName.isNotBlank() && custPhone.isNotBlank() && customSpecs.isNotBlank()) {
                                        onSubmitCustomization(custPhone, custName, customSpecs)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_customization_button")
                            ) {
                                Text("Forward Request to Consultant Chat", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpecAttributeLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = GoldAccent, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
        Text(text = value, color = TextLight, fontSize = 9.sp, textAlign = TextAlign.End)
    }
}
