package com.example.data.repository

import com.example.data.database.LeadDao
import com.example.data.database.ProductDao
import com.example.data.database.QuotationDao
import com.example.data.database.ConversationDao
import com.example.data.database.LanguageConfigDao
import com.example.data.database.AlbumCategoryDao
import com.example.data.database.ProductAlbumDao
import com.example.data.database.CustomerFavoriteDao
import com.example.data.database.CustomerSelectionDao
import com.example.data.database.DesignComparisonDao
import com.example.data.database.AlbumAnalyticsDao
import com.example.data.model.Lead
import com.example.data.model.Product
import com.example.data.model.Quotation
import com.example.data.model.Conversation
import com.example.data.model.LanguageConfig
import com.example.data.model.AlbumCategory
import com.example.data.model.ProductAlbum
import com.example.data.model.CustomerFavorite
import com.example.data.model.CustomerSelection
import com.example.data.model.DesignComparison
import com.example.data.model.AlbumAnalytics
import com.example.data.api.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SalesRepository(
    private val leadDao: LeadDao,
    private val productDao: ProductDao,
    private val quotationDao: QuotationDao,
    private val conversationDao: ConversationDao,
    private val languageConfigDao: LanguageConfigDao,
    private val albumCategoryDao: AlbumCategoryDao,
    private val productAlbumDao: ProductAlbumDao,
    private val customerFavoriteDao: CustomerFavoriteDao,
    private val customerSelectionDao: CustomerSelectionDao,
    private val designComparisonDao: DesignComparisonDao,
    private val albumAnalyticsDao: AlbumAnalyticsDao
) {
    val allLeads: Flow<List<Lead>> = leadDao.getAllLeads()
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allQuotations: Flow<List<Quotation>> = quotationDao.getAllQuotations()
    val leadCount: Flow<Int> = leadDao.getLeadCount()

    val allLanguages: Flow<List<LanguageConfig>> = languageConfigDao.getAllLanguages()
    val enabledLanguages: Flow<List<LanguageConfig>> = languageConfigDao.getEnabledLanguages()

    val allCategories: Flow<List<AlbumCategory>> = albumCategoryDao.getAllCategories()
    val allAlbums: Flow<List<ProductAlbum>> = productAlbumDao.getAllAlbums()
    val allSelections: Flow<List<CustomerSelection>> = customerSelectionDao.getAllSelections()
    val allFavorites: Flow<List<CustomerFavorite>> = customerFavoriteDao.getAllFavorites()
    val allAnalytics: Flow<List<AlbumAnalytics>> = albumAnalyticsDao.getAllAnalytics()

    suspend fun getLanguageByCode(code: String): LanguageConfig? = withContext(Dispatchers.IO) {
        languageConfigDao.getLanguageByCode(code)
    }

    suspend fun insertLanguage(config: LanguageConfig): Long = withContext(Dispatchers.IO) {
        languageConfigDao.insertLanguage(config)
    }

    fun getMessagesByChannel(channel: String): Flow<List<Conversation>> {
        return conversationDao.getMessagesByChannel(channel)
    }

    suspend fun insertLead(lead: Lead): Long = withContext(Dispatchers.IO) {
        leadDao.insertLead(lead)
    }

    suspend fun deleteLead(id: Int) = withContext(Dispatchers.IO) {
        leadDao.deleteLeadById(id)
    }

    suspend fun getLeadById(id: Int): Lead? = withContext(Dispatchers.IO) {
        leadDao.getLeadById(id)
    }

    suspend fun insertProduct(product: Product): Long = withContext(Dispatchers.IO) {
        productDao.insertProduct(product)
    }

    suspend fun deleteProduct(id: Int) = withContext(Dispatchers.IO) {
        productDao.deleteProductById(id)
    }

    suspend fun syncProductsFromPostgres(locale: String): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.productApiService.getProducts(locale = locale)
            if (response.success && response.data != null) {
                val dbProducts = response.data.mapIndexed { index, apiProd ->
                    val stockStatus = if (apiProd.inventoryCount > 0) "In Stock" else "Made to Order"
                    Product(
                        id = (index + 1) + 1000, // Offset remote products ID range
                        name = apiProd.name ?: "Unnamed Carved Furniture",
                        category = apiProd.categoryName ?: "Sofa",
                        price = apiProd.price,
                        material = "Guaranteed Hardwood (Wanza/Mahogany)",
                        dimensions = "Premium Tailored Sizing",
                        warranty = "10 Years Warranty",
                        description = apiProd.description ?: "Bespoke handcrafted carpentry directly synchronized with executive sales collection.",
                        stockStatus = stockStatus,
                        imageUrl = apiProd.imageUrls?.firstOrNull() ?: ""
                    )
                }
                if (dbProducts.isNotEmpty()) {
                    productDao.insertProducts(dbProducts)
                }
                Result.success(dbProducts)
            } else {
                Result.failure(Exception(response.message ?: "Failed to retrieve catalog products cleanly."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun insertQuotation(quotation: Quotation): Long = withContext(Dispatchers.IO) {
        quotationDao.insertQuotation(quotation)
    }

    suspend fun deleteQuotation(id: Int) = withContext(Dispatchers.IO) {
        quotationDao.deleteQuotationById(id)
    }

    suspend fun insertMessage(message: Conversation): Long = withContext(Dispatchers.IO) {
        conversationDao.insertMessage(message)
    }

    suspend fun clearChannelMessages(channel: String) = withContext(Dispatchers.IO) {
        conversationDao.clearChannelMessages(channel)
    }

    // --- Design Album Helpers ---
    fun getAlbumsByCategory(category: String): Flow<List<ProductAlbum>> {
        return productAlbumDao.getAlbumsByCategory(category)
    }

    suspend fun getAlbumById(id: String): ProductAlbum? = withContext(Dispatchers.IO) {
        productAlbumDao.getAlbumById(id)
    }

    suspend fun insertAlbum(album: ProductAlbum) = withContext(Dispatchers.IO) {
        productAlbumDao.insertAlbum(album)
    }

    suspend fun deleteAlbum(id: String) = withContext(Dispatchers.IO) {
        productAlbumDao.deleteAlbumById(id)
    }

    fun getFavoritesByCustomer(phone: String): Flow<List<CustomerFavorite>> {
        return customerFavoriteDao.getFavoritesByCustomer(phone)
    }

    suspend fun insertFavorite(favorite: CustomerFavorite): Long = withContext(Dispatchers.IO) {
        albumAnalyticsDao.incrementFavoriteCount(favorite.albumId)
        customerFavoriteDao.insertFavorite(favorite)
    }

    suspend fun removeFavorite(phone: String, albumId: String) = withContext(Dispatchers.IO) {
        customerFavoriteDao.removeFavorite(phone, albumId)
    }

    suspend fun insertSelection(selection: CustomerSelection): Long = withContext(Dispatchers.IO) {
        albumAnalyticsDao.incrementSelectionCount(selection.albumId)
        customerSelectionDao.insertSelection(selection)
    }

    fun getComparisonsByCustomer(phone: String): Flow<List<DesignComparison>> {
        return designComparisonDao.getComparisonsByCustomer(phone)
    }

    suspend fun insertComparison(comparison: DesignComparison): Long = withContext(Dispatchers.IO) {
        designComparisonDao.insertComparison(comparison)
    }

    suspend fun deleteComparison(id: Int) = withContext(Dispatchers.IO) {
        designComparisonDao.deleteComparisonById(id)
    }

    suspend fun incrementViewCount(albumId: String) = withContext(Dispatchers.IO) {
        albumAnalyticsDao.incrementViewCount(albumId)
    }

    // Populate default multilingual templates if empty.
    suspend fun populateDefaultLanguagesIfEmpty() = withContext(Dispatchers.IO) {
        val currentLangs = languageConfigDao.getAllLanguages().first()
        if (currentLangs.isEmpty()) {
            val defaults = listOf(
                LanguageConfig(
                    code = "en",
                    name = "English",
                    isEnabled = true,
                    customGreeting = "Welcome to Bekansi Furniture! We craft premium local hardwood masterpieces (Wanza, Mahogany, Acacia). How can we assist you today?",
                    customFallback = "Please allow our sales team to assist you further on this specific matter.",
                    systemPromptOverride = "Our showroom and workshop are located in Bishoftu City, Dukem Subcity. Custom orders take strictly 3-4 weeks to manufacture. Free design consulting is provided. Our phone numbers are 0988828861/0910824534."
                ),
                LanguageConfig(
                    code = "am",
                    name = "Amharic",
                    isEnabled = true,
                    customGreeting = "እንኳን ወደ በካንሲ የቤት ዕቃዎች በደህና መጡ! በጥራት ከተመረጡ ሀገር በቀል ጠንካራ እንጨቶች (ዋንዛ፣ ማሆጋኒ፣ ግራር) የተሰሩ ሶፋዎች እና ጠረጴዛዎች በምን እንዲረዳዎት ይፈልጋሉ?",
                    customFallback = "እባክዎን የሽያጭ ቡድናችን በዚህ ጉዳይ ላይ በበለጠ እንዲረዳዎት ይፍቀዱ።",
                    systemPromptOverride = "የማሳያ ክፍላችን እና ወርክሾፓችን በቢሾፍቱ ከተማ፣ ዱከም ክፍለ ከተማ ውስጥ ይገኛል። ትዕዛዞች በ3-4 ሳምንታት ውስጥ ተጠናቀው ይረከባሉ። የስልክ አድራሻችን 0988828861/0910824534 ነው።"
                ),
                LanguageConfig(
                    code = "om",
                    name = "Afaan Oromo",
                    isEnabled = true,
                    customGreeting = "Baga nagaan gara Bekansi Furniture dhuftan! Hardwood beekamaa Itoophiyaa (Wanza, Mahogany, Grar) irraa kan hojjetameedha. Akkamitti isin gargaaruu danda'a?",
                    customFallback = "Mee dhimma kana irratti gurgurtonni keenya caalaatti akka isin gargaaran eeyyamaa.",
                    systemPromptOverride = "Showroomiin fi mishiinni hojii keenya Magaalaa Bishooftuu, Kifla Magaalaa Dukam keessatti argama. Ergisa dhimmoota addaa torbannoottan 3-4 keessatti ni qopheessina. Bilbilli keenya 0988828861/0910824534."
                )
            )
            languageConfigDao.insertLanguages(defaults)
        }
    }

    // Populate catalog with high-end Ethiopian local hardwoods furniture.
    suspend fun populateDefaultCatalogIfEmpty() = withContext(Dispatchers.IO) {
        val currentProducts = productDao.getAllProducts().first()
        if (currentProducts.isEmpty()) {
            val defaults = listOf(
                Product(
                    name = "Wanza Curved L-Sofa 'Gara'",
                    category = "Sofa",
                    price = 135000.0,
                    material = "Wanza (Cordia Africana)",
                    dimensions = "270cm x 180cm x 85cm",
                    warranty = "5 Years",
                    description = "Stunning organic curve sectional sofa, handcrafted from fine local Wanza hardwood, cushioned with traditional woven Habesha fabric trims.",
                    stockStatus = "In Stock"
                ),
                Product(
                    name = "Mahogany Dining Suite 'Zid'",
                    category = "Dining Table",
                    price = 185000.0,
                    material = "Mahogany (Zid)",
                    dimensions = "240cm x 110cm x 76cm",
                    warranty = "10 Years",
                    description = "Grand 8-seater dining table of deep-hued solid Ethiopian Mahogany, pairing clean architectural lines with heirloom sturdiness.",
                    stockStatus = "Made to Order"
                ),
                Product(
                    name = "King Floating Bed 'Sheger'",
                    category = "Bed",
                    price = 110000.0,
                    material = "Grar (Acacia)",
                    dimensions = "200cm x 220cm x 110cm",
                    warranty = "5 Years",
                    description = "An ultra-modern floating king bed showing vibrant golden-to-dark wild grain patterns of premium kiln-dried Ethiopian Acacia.",
                    stockStatus = "In Stock"
                ),
                Product(
                    name = "Dual-tone Credenza 'Bunna'",
                    category = "Office",
                    price = 32000.0,
                    material = "Wanza & Mahogany",
                    dimensions = "160cm x 45cm x 75cm",
                    warranty = "3 Years",
                    description = "Mid-century credenza boasting integrated wood-hinged sliding doors and hand-made custom handles, perfect as a media console or buffet table.",
                    stockStatus = "In Stock"
                ),
                Product(
                    name = "Executive Desk 'Abay'",
                    category = "Office",
                    price = 95000.0,
                    material = "Mahogany (Zid)",
                    dimensions = "180cm x 90cm x 76cm",
                    warranty = "5 Years",
                    description = "Premium workspace command center utilizing warm Mahogany timber panels, featuring integrated storage drawers and elegant flush brass inlays.",
                    stockStatus = "Made to Order"
                )
            )
            productDao.insertProducts(defaults)
        }
    }

    suspend fun populateDefaultLeadsIfEmpty() = withContext(Dispatchers.IO) {
        val currentLeads = leadDao.getAllLeads().first()
        if (currentLeads.isEmpty()) {
            val defaults = listOf(
                Lead(
                    name = "Bekansi",
                    phone = "0910824534",
                    email = "contact@bekansi.com",
                    status = "New",
                    source = "WhatsApp",
                    requirements = "Inquiring about custom hardwood products",
                    notes = "Expresses interest in premium custom living layouts and mahogany finishes.",
                    language = "Afaan Oromo"
                ),
                Lead(
                    name = "Abdi Biya",
                    phone = "0988828861",
                    email = "abdi.biya@gmail.com",
                    status = "New",
                    source = "Telegram",
                    requirements = "Requesting about custom products inquiry",
                    notes = "Interested in modular workspace cabinets and leather cushioned layout customizations.",
                    language = "Amharic"
                )
            )
            for (lead in defaults) {
                leadDao.insertLead(lead)
            }
        }
    }

    // Populate 50+ exquisite product albums across 10 categories
    suspend fun populateDefaultAlbumsIfEmpty() = withContext(Dispatchers.IO) {
        val currentCategories = albumCategoryDao.getAllCategories().first()
        if (currentCategories.isEmpty()) {
            val categories = listOf(
                AlbumCategory("bedroom_sets", "Bedroom Sets", "የመኝታ ክፍል ስብስቦች", "Setti Siree", "Fine heirloom bed sets, nightstands, and dressing tables."),
                AlbumCategory("wardrobes", "Wardrobes", "የልብስ ቁምሳጥን", "Masaalota Uffataa", "Elegant multi-door sliding and walk-in clothing organizers."),
                AlbumCategory("kitchen_cabinets", "Kitchen Cabinets", "የወጥ ቤት ካቢኔቶች", "Kaabiineeta Kichinii", "Contemporary and shaker-style culinary countertops and storage."),
                AlbumCategory("tv_stands", "TV Stands", "የቲቪ ማስቀመጫዎች", "Maasii TV", "Minimalist and floating timber media consoles."),
                AlbumCategory("dining_sets", "Dining Sets", "የምግብ ጠረጴዛዎች", "Setti Nyaataa", "Bespoke hardwood dining tables with tailored luxury chairs."),
                AlbumCategory("office_furniture", "Office Furniture", "የቢሮ ዕቃዎች", "Meeshaalee Waajjiraa", "Executive desks, book cabinets, and ergonomic configurations."),
                AlbumCategory("sofas", "Sofas", "ጥራት ያላቸው ሶፋዎች", "Kofoo Sofa", "L-shaped sectionals, traditional Chesterfields, and organic lounges."),
                AlbumCategory("coffee_tables", "Coffee Tables", "የቡና ጠረጴዛዎች", "Gabatee Bunaa", "Intricately carved centerpiece coffee tables."),
                AlbumCategory("reception_furniture", "Reception Furniture", "የሪሴፕሽን ዕቃዎች", "Meeshaa Keessummeessaa", "Grand commercial hosting counters and greeting boards."),
                AlbumCategory("custom_furniture", "Custom Furniture Designs", "የብጁ ዕቃዎች አልበም", "Dizayinii Fedhaa", "Exclusive tailored catalog for bespoke customer needs.")
            )
            albumCategoryDao.insertCategories(categories)
        }

        val currentAlbums = productAlbumDao.getAllAlbums().first()
        if (currentAlbums.isEmpty()) {
            val albums = mutableListOf<ProductAlbum>()
            
            // 1. Bedroom Sets (10)
            val bedroomNames = listOf("Lalibela Royal Suite", "Simien Mountain Vista", "Sheger Floating Bed", "Axum Obelisk Grand", "Gondar Castle Heirloom", "Abay Source Minimalist", "Awash Safari Platform", "Bale Blossom Velvet", "Tana Dawn Trundle", "Sof Omar Cave Canopy")
            val bedroomStyles = listOf("Modern Luxury", "Scandinavian", "Contemporary", "Classic", "Minimalist", "Modern Luxury", "Contemporary", "Classic", "Scandinavian", "Rustic")
            val bedroomImages = listOf(
                "https://images.unsplash.com/photo-1505691938895-1758d7feb511?w=600",
                "https://images.unsplash.com/photo-1540518614846-7eded433c457?w=600",
                "https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=600",
                "https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?w=600",
                "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?w=600",
                "https://images.unsplash.com/photo-1616594039964-ae9021a400a0?w=600",
                "https://images.unsplash.com/photo-1617325247661-675ab4d61196?w=600",
                "https://images.unsplash.com/photo-1531835551805-16d864c8d311?w=600",
                "https://images.unsplash.com/photo-1618220179428-22790b461013?w=600",
                "https://images.unsplash.com/photo-1600585154526-990dced4db0d?w=600"
            )
            bedroomNames.forEachIndexed { index, name ->
                val id = String.format("BS-%03d", index + 1)
                val priceLower = 100000.0 + (index * 12000.0)
                val priceUpper = priceLower + 35000.0
                val pop = 80 + (index * 2)
                val style = bedroomStyles[index]
                albums.add(ProductAlbum(
                    id = id,
                    name = name,
                    category = "Bedroom Sets",
                    designStyle = style,
                    descriptionEn = "An exquisite $style bedroom creation showcasing top-tier Ethiopian timber crafted by Bekansi experts. Features modular headboards.",
                    descriptionAm = "በበካንሲ ባለሙያዎች የተሰራ ምርጥ የ $style መኝታ ቤት ዕቃ። ሞዱላር ጭንቅላት መደገፊያዎችን ያካትታል።",
                    descriptionOm = "Meeshaa kofoo siree bareedaa haala kanaan hojjetame. Qulqullina olaanaa qaba.",
                    dimensions = "200cm x 220cm x 120cm",
                    materialOptions = "Wanza, Mahogany, Grar, MDF",
                    colorOptions = "Walnut, Dark Cocoa, Natural Oak",
                    estimatedProductionTime = "18 Days",
                    priceRangeLower = priceLower,
                    priceRangeUpper = priceUpper,
                    popularityScore = pop,
                    tags = "bedroom, luxurious, solid wood, premium",
                    imageUrls = bedroomImages[index % bedroomImages.size]
                ))
            }

            // 2. Wardrobes (8)
            val wardrobeNames = listOf("Entoto Forest Walk-In", "Rift Valley sliding Wardrobe", "Adama Modern Crest", "Debre Zeit Lakeside Closet", "Dire Dawa French Colonial", "Harar Jegol Traditional", "Jimma Coffee Custom Wardrobe", "Hawassa Lake Breeze Wardrobe")
            val wardrobeImages = listOf(
                "https://images.unsplash.com/photo-1558882224-dda166733360?w=600",
                "https://images.unsplash.com/photo-1616046229478-9901c5536a45?w=600",
                "https://images.unsplash.com/photo-1595428774223-ef52624120d2?w=600",
                "https://images.unsplash.com/photo-1600565193348-f74bd3c7ccdf?w=600",
                "https://images.unsplash.com/photo-1618219908412-a29a1bb7b86e?w=600",
                "https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?w=600",
                "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?w=600",
                "https://images.unsplash.com/photo-1600210492486-724fe5c67fb0?w=600"
            )
            wardrobeNames.forEachIndexed { index, name ->
                val id = String.format("WD-%03d", index + 1)
                val priceLower = 60000.0 + (index * 8000.0)
                val priceUpper = priceLower + 20000.0
                val pop = 85 + (index % 3)
                val style = if (index % 2 == 0) "Modern Minimalist" else "Contemporary Oak"
                albums.add(ProductAlbum(
                    id = id,
                    name = name,
                    category = "Wardrobes",
                    designStyle = style,
                    descriptionEn = "Spacious $style storage system with sliding soft-close mechanisms, custom hanger rows and LED lighting options.",
                    descriptionAm = "ሰፊ የ $style አልባሳት ማስቀመጫ የቁምሳጥን ሲስተም። ተንሸራታች በሮች እና የኤልኢዲ መብራቶች አሉት።",
                    descriptionOm = "Saffisaan fi salphaatti uffata keessan kan gurmeessu. Bal'ina gaarii qaba.",
                    dimensions = "240cm x 60cm x 220cm",
                    materialOptions = "MDF, Veneer, Wanza, Mahogany",
                    colorOptions = "Espresso, Warm Cherry, Classic White",
                    estimatedProductionTime = "15 Days",
                    priceRangeLower = priceLower,
                    priceRangeUpper = priceUpper,
                    popularityScore = pop,
                    tags = "wardrobe, closet, storage, modern",
                    imageUrls = wardrobeImages[index % wardrobeImages.size]
                ))
            }

            // 3. Kitchen Cabinets (8)
            val kitchenNames = listOf("Gofa Modular Culinary", "Dukem Luxury High-Gloss", "Saris Granite Oak Kitchen", "Mercato Bulk Pantry Kitchen", "Kazanchis Classic Shaker", "Old Airport Walnut Gourmet", "Lideta Compact Chef Cabinets", "Meskel Square Royal Cabinets")
            val kitchenImages = listOf(
                "https://images.unsplash.com/photo-1556911220-e15b29be8c8f?w=600",
                "https://images.unsplash.com/photo-1556912173-3bb406ef7e77?w=600",
                "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=600",
                "https://images.unsplash.com/photo-1565183997392-2f6f122e5912?w=600",
                "https://images.unsplash.com/photo-1599809224414-f06b3f684497?w=600",
                "https://images.unsplash.com/photo-1507089947368-19c1da9775ae?w=600",
                "https://images.unsplash.com/photo-1513694203232-719a280e022f?w=600",
                "https://images.unsplash.com/photo-1600565192244-8c44abf3d7b5?w=600"
            )
            kitchenNames.forEachIndexed { index, name ->
                val id = String.format("KC-%03d", index + 1)
                val priceLower = 150000.0 + (index * 20000.0)
                val priceUpper = priceLower + 60000.0
                val pop = 80 + index
                albums.add(ProductAlbum(
                    id = id,
                    name = name,
                    category = "Kitchen Cabinets",
                    designStyle = "Contemporary Shaker",
                    descriptionEn = "Ergonomic modular kitchen cabinets featuring solid counter mounts, hydraulic soft-close drawer hinges and premium detailing.",
                    descriptionAm = "ምቹ የወጥ ቤት ዕቃዎች ካቢኔት። የሃይድሮሊክ በሮች፣ ጠንካራ መደርደሪያዎች እና ዘመናዊ ዲዛይን አሉት።",
                    descriptionOm = "Kaabiineeta kichinii ammayyaa fi salphaatti qulqullaa'u. Bareedina addaa fida.",
                    dimensions = "Customized layout according to kitchen dimensions",
                    materialOptions = "High Moisture Resistant MDF, Wanza, Grar, Acrylic",
                    colorOptions = "Charcoal Grey, High-Gloss White, Blue Slate",
                    estimatedProductionTime = "25 Days",
                    priceRangeLower = priceLower,
                    priceRangeUpper = priceUpper,
                    popularityScore = pop,
                    tags = "kitchen, cooking, modern, luxury",
                    imageUrls = kitchenImages[index % kitchenImages.size]
                ))
            }

            // 4. TV Stands (5)
            val tvNames = listOf("Choke Floating Console", "Omo Valley Organic TV Unit", "Semera Desert Lowline", "Dessie Mahogany Corner Unit", "Nekemte Modern Credenza")
            val tvImages = listOf(
                "https://images.unsplash.com/photo-1595428774223-ef52624120d2?w=600",
                "https://images.unsplash.com/photo-1603006905003-be475563bc59?w=600",
                "https://images.unsplash.com/photo-1532372320978-9b4d7a92b24d?w=600",
                "https://images.unsplash.com/photo-1493663284031-b7e3aefcae8e?w=600",
                "https://images.unsplash.com/photo-1540518614846-7eded433c457?w=600"
            )
            tvNames.forEachIndexed { index, name ->
                val id = String.format("TS-%03d", index + 1)
                val priceLower = 25000.0 + (index * 4000.0)
                val priceUpper = priceLower + 10000.0
                val pop = 88 + index
                albums.add(ProductAlbum(
                    id = id,
                    name = name,
                    category = "TV Stands",
                    designStyle = "Scandinavian Minimalist",
                    descriptionEn = "Low-profile timber TV console with integrated hidden cable routing, open equipment bays, and solid steel base legs.",
                    descriptionAm = "ከወለል ዝቅ ያለ የቲቪ ማስቀመጫ ኮንሶል ከኬብል ማስተላለፊያ እና ተጨማሪ መደርደሪያዎች ጋር የተሰራ።",
                    descriptionOm = "Maasii TV ammayyaa kan keebilii dhoksuu danda'u. Mukkeen filataman irraa hojjetame.",
                    dimensions = "180cm x 40cm x 50cm",
                    materialOptions = "Wanza, Mahogany, Premium MDF",
                    colorOptions = "Natural Oak, Ebony, Espresso",
                    estimatedProductionTime = "10 Days",
                    priceRangeLower = priceLower,
                    priceRangeUpper = priceUpper,
                    popularityScore = pop,
                    tags = "tv stand, console, living room, modern",
                    imageUrls = tvImages[index % tvImages.size]
                ))
            }

            // 5. Dining Sets (5)
            val diningNames = listOf("Zid Mahogany Majestic", "Abyssinia Round Dining Set", "Soddo Acacia Rustic Set", "Wondo Genet Forest Pine Table", "Arba Minch Twin Lake Dining")
            val diningImages = listOf(
                "https://images.unsplash.com/photo-1615066390971-03e4e1c36ddf?w=600",
                "https://images.unsplash.com/photo-1617806118233-18e1db207faf?w=600",
                "https://images.unsplash.com/photo-1577140917170-285929fb55b7?w=600",
                "https://images.unsplash.com/photo-1604014237800-1c9102c219da?w=600",
                "https://images.unsplash.com/photo-1595515106969-1ce29566ff1c?w=600"
            )
            diningNames.forEachIndexed { index, name ->
                val id = String.format("DS-%03d", index + 1)
                val priceLower = 110000.0 + (index * 15000.0)
                val priceUpper = priceLower + 40000.0
                val pop = 92 - index
                albums.add(ProductAlbum(
                    id = id,
                    name = name,
                    category = "Dining Sets",
                    designStyle = "Classic Luxury",
                    descriptionEn = "A grand dining masterpiece table equipped with bespoke crafted chairs, showcasing rich textures of precious Ethiopian trees.",
                    descriptionAm = "በሚያማምሩ ወንበሮች የታጀበ የላቀ የምግብ መመገቢያ ጠረጴዛ። ከሀገር በቀል ጠንካራ እንጨት የተሰራ።",
                    descriptionOm = "Setti Nyaataa baay'ee bareedu. Maatii keessaniif mijataa dha.",
                    dimensions = "220cm x 110cm x 76cm",
                    materialOptions = "Mahogany, Wanza, Acacia, Oak",
                    colorOptions = "Deep Mahogany Red, Golden Harvest Acacia, Midnight Espresso",
                    estimatedProductionTime = "16 Days",
                    priceRangeLower = priceLower,
                    priceRangeUpper = priceUpper,
                    popularityScore = pop,
                    tags = "dining, table, hardwood, classic",
                    imageUrls = diningImages[index % diningImages.size]
                ))
            }

            // 6. Office Furniture (5)
            val officeNames = listOf("Abay Commander Desk", "Ecowas Summit Mahogany Desk", "Gelada Backloader ergonomic", "Ras Alula solid carving", "Menelik Imperial Library")
            val officeImages = listOf(
                "https://images.unsplash.com/photo-1524758631624-e2822e304c36?w=600",
                "https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=600",
                "https://images.unsplash.com/photo-1513694203232-719a280e022f?w=600",
                "https://images.unsplash.com/photo-1497366216548-37526070297c?w=600",
                "https://images.unsplash.com/photo-1606857521015-7f9fcf423740?w=600"
            )
            officeNames.forEachIndexed { index, name ->
                val id = String.format("OF-%03d", index + 1)
                val priceLower = 45000.0 + (index * 10000.0)
                val priceUpper = priceLower + 25000.0
                val pop = 84 + index
                albums.add(ProductAlbum(
                    id = id,
                    name = name,
                    category = "Office Furniture",
                    designStyle = "Executive Traditional",
                    descriptionEn = "Commanding desk design featuring solid drawer blocks, embedded hand-stitched leather desktop pads, and brass hardware detailing.",
                    descriptionAm = "የቢሮ መሪዎች ጠረጴዛ ከጠንካራ እንጨት ተሰርቶ በቆዳ ማስተላለፊያዎች እና በናስ ዝርዝሮች ያጌጠ።",
                    descriptionOm = "Teessuma barataa fi waajjiraa filatamaa. Hojiif mijataa dha.",
                    dimensions = "200cm x 95cm x 76cm",
                    materialOptions = "Mahogany, Grar, Wanza",
                    colorOptions = "Saddle Brown, Dark Walnut, Natural Finish",
                    estimatedProductionTime = "14 Days",
                    priceRangeLower = priceLower,
                    priceRangeUpper = priceUpper,
                    popularityScore = pop,
                    tags = "office, command, desk, workplace",
                    imageUrls = officeImages[index % officeImages.size]
                ))
            }

            // 7. Sofas (4)
            val sofaNames = listOf("Gara Wanza Sectional", "Bale Chesterfield Classic", "Sodere Thermal Relaxer", "Langano Sandy Lounger")
            val sofaImages = listOf(
                "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=600",
                "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?w=600",
                "https://images.unsplash.com/photo-1484101403633-562f891dc89a?w=600",
                "https://images.unsplash.com/photo-1540518614846-7eded433c457?w=600"
            )
            sofaNames.forEachIndexed { index, name ->
                val id = String.format("SF-%03d", index + 1)
                val priceLower = 120000.0 + (index * 12000.0)
                val priceUpper = priceLower + 30000.0
                val pop = 90 + index
                albums.add(ProductAlbum(
                    id = id,
                    name = name,
                    category = "Sofas",
                    designStyle = "Contemporary Luxury",
                    descriptionEn = "An L-Shape sectional cushioned with premium density foams and framed in genuine local hardwood. Includes modular ottoman.",
                    descriptionAm = "ከከፍተኛ ጥራት ካለው አረፋ የተሰራ እና በጠንካራ ሀገር በቀል እንጨት የተዋቀረ ኤል-ቅርጽ ያለው ሶፋ።",
                    descriptionOm = "Sofa L-shape bareedaa fi jabaa. Gatiin isaa madaalawaa dha.",
                    dimensions = "280cm x 180cm x 85cm",
                    materialOptions = "Wanza, Cordia Africana, Microfiber, Leather",
                    colorOptions = "Carbon Grey, Linen Beige, Saddle Tan",
                    estimatedProductionTime = "15 Days",
                    priceRangeLower = priceLower,
                    priceRangeUpper = priceUpper,
                    popularityScore = pop,
                    tags = "sofa, living room, modern, sectional",
                    imageUrls = sofaImages[index % sofaImages.size]
                ))
            }

            // 8. Coffee Tables (2)
            val coffeeNames = listOf("Kaffa Coffee Bean Nest", "Danakil Lava Glass Table")
            val coffeeImages = listOf(
                "https://images.unsplash.com/photo-1533090161767-e6ffed986c88?w=600",
                "https://images.unsplash.com/photo-1600210491892-03d54c0aaf87?w=600"
            )
            coffeeNames.forEachIndexed { index, name ->
                val id = String.format("CT-%03d", index + 1)
                val priceLower = 18000.0 + (index * 7000.0)
                val priceUpper = priceLower + 6500.0
                val pop = 87 + index
                albums.add(ProductAlbum(
                    id = id,
                    name = name,
                    category = "Coffee Tables",
                    designStyle = "Organic Contemporary",
                    descriptionEn = "Handcrafted organic timber table mimicking geographical topography of famous Ethiopian valleys. Glass inlays.",
                    descriptionAm = "የኢትዮጵያን ተራሮችና ሸለቆዎች መልክዓ-ምድር የሚመስል በእጅ የተሰራ ድንቅ የቡና ጠረጴዛ።",
                    descriptionOm = "Gabatee bunaa baay'ee bareedu fi jabaa. Hawwatummaa qaba.",
                    dimensions = "120cm x 120cm x 40cm",
                    materialOptions = "Wanza Wood, Tempered Glass, Grar Roots",
                    colorOptions = "Golden Honey, Dark Espresso",
                    estimatedProductionTime = "7 Days",
                    priceRangeLower = priceLower,
                    priceRangeUpper = priceUpper,
                    popularityScore = pop,
                    tags = "coffee table, center table, living room, artistic",
                    imageUrls = coffeeImages[index % coffeeImages.size]
                ))
            }

            // 9. Reception Furniture (2)
            val receptionNames = listOf("Addis Gateway Counter", "Sheba Crown Welcome Board")
            val receptionImages = listOf(
                "https://images.unsplash.com/photo-1568992687947-868a62a9f521?w=600",
                "https://images.unsplash.com/photo-1571508601936-6ca847b47ae6?w=600"
            )
            receptionNames.forEachIndexed { index, name ->
                val id = String.format("RF-%03d", index + 1)
                val priceLower = 55000.0 + (index * 10000.0)
                val priceUpper = priceLower + 15000.0
                val pop = 82 + index
                albums.add(ProductAlbum(
                    id = id,
                    name = name,
                    category = "Reception Furniture",
                    designStyle = "Sleek Professional",
                    descriptionEn = "Commercial front greeting desk featuring premium quartz countertops, integrated wire trays, and gold leaf accents.",
                    descriptionAm = "ዘመናዊ የእንግዳ መቀበያ ዴስክ ከድንቅ ዲዛይኖችና በወርቅ ቅጠል ማሳመሪያዎች የተሰራ።",
                    descriptionOm = "Gabatee keessummeessaa ammayyaa fi dizaayinii haaraa qabu.",
                    dimensions = "240cm x 80cm x 105cm",
                    materialOptions = "Mahogany, Quartz Stone, MDF panels",
                    colorOptions = "Midnight Grey & Gold, White Coral & Walnut",
                    estimatedProductionTime = "12 Days",
                    priceRangeLower = priceLower,
                    priceRangeUpper = priceUpper,
                    popularityScore = pop,
                    tags = "reception, commercial, lobby, desk",
                    imageUrls = receptionImages[index % receptionImages.size]
                ))
            }

            // 10. Custom Master Album (1)
            albums.add(ProductAlbum(
                id = "CF-001",
                name = "Bespoke Ethiopian Vision Room",
                category = "Custom Furniture Designs",
                designStyle = "Architectural Bespoke",
                descriptionEn = "Tailor-made comprehensive interior layout designed entirely in collaboration with your architect. Premium solid hardwoods.",
                descriptionAm = "ከእርስዎ አርክቴክት ጋር በመሆን ሙሉ ለሙሉ በጥምረት የሚሰራ በእጅ የተጠበቀ የውስጥ ማስጌጫ ዕቃዎች ስብስብ።",
                descriptionOm = "Dizayinii dhuunfaa fedha keessan irratti hundhaa'ee hojjetamu.",
                dimensions = "Completely customized to client spatial blueprint",
                materialOptions = "Any local/imported hardwood species (Wanza, Mahogany, Grar, Tikur Enchet)",
                colorOptions = "Bespoke stains matched to daylight conditions",
                estimatedProductionTime = "30 Days",
                priceRangeLower = 250000.0,
                priceRangeUpper = 1000000.0,
                popularityScore = 99,
                tags = "custom, bespoke, luxury, mansion",
                imageUrls = "https://images.unsplash.com/photo-1513694203232-719a280e022f?w=600"
            ))

            productAlbumDao.insertAlbums(albums)
            
            // Seed initial analytics record for each album too
            albums.forEach { alb ->
                albumAnalyticsDao.insertAnalytics(AlbumAnalytics(alb.id, (20..120).random(), (2..10).random(), (3..20).random()))
            }
        }
    }
}
