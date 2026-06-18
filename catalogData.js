/**
 * BEKANSI AI SALES ASSISTANT PLATFORM
 * HIGH-FIDELITY CATALOG DATASET FOR FURNITURE DESIGN ALBUMS
 * 
 * Contains 52 deeply descriptive furniture albums mapped across 5 core categories,
 * featuring multilingual values (English, Amharic, Afaan Oromo) and local wood species.
 */

const BASE_PRODUC_TEMPLATES = [
  // --- LIVING ROOM PRODUCTS ---
  {
    baseName: "Wanza Gara L-Sofa",
    baseNameAm: "ዋንዛ ጋራ ኤል ሶፋ",
    baseNameOm: "Sofa Gara Wanza",
    category: "living_room",
    style: "Modern Luxury",
    enDesc: "Luxury hand-carved Wanza wood frame with premium high-density cloud comforting cushions.",
    amDesc: "ከተመረጠ ምርጥ የዋንዛ እንጨት በባለሙያ የተሰራ፣ እጅግ ዘመናዊ እና ምቹ የሳሎን ኤል ቅርፅ ሶፋ።",
    omDesc: "Sofa Gara jabaa fi bareedaa mukka Wanza irraa tolfame, kan ammayyaa fi salphaatti mijatu.",
    dimensions: "300 x 210 x 85 cm",
    materials: ["Wanza (Cordia africana)", "Premium Foam", "Linen Fabric"],
    colors: ["Warm Amber", "Charcoal Grey", "Olive Green"],
    priceLower: 95000,
    priceUpper: 135000,
    productionTime: "14 Days",
    popularity: 98,
    img: "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&w=600&q=80"
  },
  {
    baseName: "Grar Slab Coffee Table",
    baseNameAm: "ግራር ባለሙሉ ስላብ ቡና ጠረጴዛ",
    baseNameOm: "Minja Bunaa Grar",
    category: "living_room",
    style: "Rustic Organic",
    enDesc: "Solid single-slab high-character Grar wood with matte industrial steel legs.",
    amDesc: "ከተፈጥሮአዊ የግራር እንጨት የተሰራ ቪንቴጅ የቡና ወይም የሻይ ማስተናገጃ ጠረጴዛ።",
    omDesc: "Minja bunaa mukka Grar irraa tolfame, boca uumamaa kan qabu fi jabaa.",
    dimensions: "120 x 80 x 42 cm",
    materials: ["Grar (Acacia)", "Industrial Steel"],
    colors: ["Natural Golden Honey", "Smoked Walnut"],
    priceLower: 28000,
    priceUpper: 42000,
    productionTime: "8 Days",
    popularity: 92,
    img: "https://images.unsplash.com/photo-1533090161767-e6ffed986c88?auto=format&fit=crop&w=600&q=80"
  },
  // --- DINING ROOM PRODUCTS ---
  {
    baseName: "Wanza Banquet Dining Table",
    baseNameAm: "ዋንዛ ባንክዌት የምግብ ጠረጴዛ",
    baseNameOm: "Minja Nyaataa Wanza",
    category: "dining_room",
    style: "Modern Minimalist",
    enDesc: "Elegant seamless solid Wanza wood table fitting 8-10 guests comfortably.",
    amDesc: "ለቤተሰብ እና ለግብዣ ምቹ የሆነ፣ እስከ 10 ሰው የሚያስተናግድ ውብ የዋንዛ እራት ጠረጴዛ።",
    omDesc: "Minja nyaataa maatii bal'aadhaaf ta'u mukka Wanza irraa hojjetame.",
    dimensions: "240 x 100 x 76 cm",
    materials: ["Wanza (Cordia africana)", "Protective Resin Coating"],
    colors: ["Natural Matte Lacquer", "Rich Teak Brown"],
    priceLower: 78000,
    priceUpper: 110000,
    productionTime: "12 Days",
    popularity: 95,
    img: "https://images.unsplash.com/photo-1615066390971-03e4e1c36ddf?auto=format&fit=crop&w=600&q=80"
  },
  {
    baseName: "Mahogany Symmetrical Sideboard",
    baseNameAm: "ማሆጋኒ ሲሜትሪካል ሳይድቦርድ",
    baseNameOm: "Kabaa Sideboard Mahogany",
    category: "dining_room",
    style: "Luxury Classic",
    enDesc: "Substantial credenza with storage drawers featuring hand-routed borders.",
    amDesc: "ፍጹም ጥራት ካለው የማሆጋኒ እንጨት የተሰራ፣ ዕቃዎችን ለማስቀመጥ ምቹ የሆነ የቤት እቃ።",
    omDesc: "Meeshaa nyaataa keessa kaa'u fi bareechu mukka Mahogany irraa tolfame.",
    dimensions: "180 x 45 x 85 cm",
    materials: ["Mahogany Wood", "Brass Hardware"],
    colors: ["Classic Red Mahogany", "Dark Espresso"],
    priceLower: 62000,
    priceUpper: 88000,
    productionTime: "16 Days",
    popularity: 89,
    img: "https://images.unsplash.com/photo-1595428774223-ef52624120d2?auto=format&fit=crop&w=600&q=80"
  },
  // --- BEDROOM PRODUCTS ---
  {
    baseName: "Abyssinia Velvet Platform Bed",
    baseNameAm: "አቢሲንያ ቬልቬት አልጋ",
    baseNameOm: "Siree Velvet Abyssinia",
    category: "bedroom",
    style: "Luxury Modern",
    enDesc: "Plush velvet back support integrated with solid durable Zigba block framing.",
    amDesc: "ከዚግባ ጠንካራ እንጨት የተሰራ እንዲሁም በምቹ ቬልቬት የተሸፈነ የንጉሳውያን አልጋ።",
    omDesc: "Siree jabaa fi laafaa mukka Zigba fi kafana velvet irraa tolfame.",
    dimensions: "200 x 180 x 120 cm",
    materials: ["Zigba Wood", "Royal Velvet", "Steel Brackets"],
    colors: ["Emerald Teal", "Warm Gold", "Imperial Blue"],
    priceLower: 85000,
    priceUpper: 125000,
    productionTime: "15 Days",
    popularity: 96,
    img: "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=600&q=80"
  },
  {
    baseName: "Wanza High-Chest Dresser",
    baseNameAm: "ዋንዛ ሃይ-ቼስት መኳኳያ ጠረጴዛ",
    baseNameOm: "Dresser Wanza",
    category: "bedroom",
    style: "Japandi Minimalist",
    enDesc: "Tall vertical wardrobe tower with sleek push-to-open functional drawers.",
    amDesc: "የልብስ እና የተለያዩ ቁሳቁሶችን ለማስቀመጥ ምቹ የዋንዛ መካከለኛ ቁመት ያለው ካቢኔ።",
    omDesc: "Dresser fi saanduqa uffata kaa'u mukka Wanza irraa hojjetame.",
    dimensions: "90 x 45 x 130 cm",
    materials: ["Wanza (Cordia africana)", "Satin Metal rails"],
    colors: ["Light Honey Wood", "Ebonized Oak Black"],
    priceLower: 38000,
    priceUpper: 55000,
    productionTime: "10 Days",
    popularity: 87,
    img: "https://images.unsplash.com/photo-1618220179428-22790b461013?auto=format&fit=crop&w=600&q=80"
  },
  // --- OFFICE PRODUCTS ---
  {
    baseName: "CEO Modular Executive Desk",
    baseNameAm: "ሲኢኦ ሞዱላር ቢሮ ጠረጴዛ",
    baseNameOm: "Minja Biriis CEO",
    category: "office",
    style: "Executive Corporate",
    enDesc: "Commanding workstation with hidden wire management, leather writing pad, and modular side storage.",
    amDesc: "ለዋና ስራ አስፈፃሚዎች የተሰራ፣ ዘመናዊ የስልክ እና ኮምፒውተር ገመድ ማሰባሰቢያ ያለው የማሆጋኒ ጠረጴዛ።",
    omDesc: "Minja hojii hoggantootaaf qophaaye, fannoo fi sanduuqa qabu.",
    dimensions: "210 x 95 x 75 cm",
    materials: ["Mahogany Wood", "Full-grain Leather", "Aluminum Trim"],
    colors: ["Premium Ebony Cherry", "Matte Charcoal Wood"],
    priceLower: 120000,
    priceUpper: 185000,
    productionTime: "20 Days",
    popularity: 94,
    img: "https://images.unsplash.com/photo-1524758631624-e2822e304c36?auto=format&fit=crop&w=600&q=80"
  },
  {
    baseName: "Asymmetrical Geometric Bookshelf",
    baseNameAm: "ጂኦሜትሪክ መፅሐፍ መደርደሪያ",
    baseNameOm: "Fannoo Kitaabaa",
    category: "office",
    style: "Nordic Industrial",
    enDesc: "Open-display wood shelving tower offering diverse configurations for high-end workspace organization.",
    amDesc: "ለቤት ወይም ለቢሮ የሚሆን፣ መጻሕፍትን እና ምርጥ የጌጣጌጥ እቃዎችን ለማስቀመጥ የሚያገለግል የዋንዛ መደርደሪያ።",
    omDesc: "Fannoo kitaaba ammayyaa mukka Wanza irraa hojjetame.",
    dimensions: "120 x 30 x 200 cm",
    materials: ["Wanza Wood", "Powder-Coated Steel Frame"],
    colors: ["Matte Natural Wanza", "Warm Oak Finish"],
    priceLower: 45000,
    priceUpper: 65000,
    productionTime: "9 Days",
    popularity: 85,
    img: "https://images.unsplash.com/photo-1540518614846-7eded433c457?auto=format&fit=crop&w=600&q=80"
  },
  // --- HERITAGE / PEOPLES PRODUCTS ---
  {
    baseName: "Hand-Carved Mesob Coffee Table",
    baseNameAm: "በእጅ የተቀረጸ ባህላዊ መሶብ ቡና ማቅረቢያ",
    baseNameOm: "Mesob Bunaa Aadaa",
    category: "heritage",
    style: "Traditional Heritage",
    enDesc: "Masterfully hand-routed traditional Ethiopian central centerpiece honoring our high hospitality culture.",
    amDesc: "ከዋንዛ የተሰራ፣ በእጅ ጥበብ የተቀረጸ ውብ የባህል መሶብ እና የቡና ማቅረቢያ ጠረጴዛ።",
    omDesc: "Mesob bunaa aadaa mukka Wanza irraa tolfame, kan baay'ee bareedu.",
    dimensions: "95 x 95 x 50 cm",
    materials: ["Select Wanza Wood", "Traditional Shellac Coat"],
    colors: ["Classic Imperial Amber", "Raw Honey Natural"],
    priceLower: 55000,
    priceUpper: 85000,
    productionTime: "18 Days",
    popularity: 99,
    img: "https://images.unsplash.com/photo-1567538096630-e0c55bd6374c?auto=format&fit=crop&w=600&q=80"
  },
  {
    baseName: "Grar Wood Royal Throne (Berchuma)",
    baseNameAm: "የግራር እንጨት የባህል ንጉሳዊ በርጩማ",
    baseNameOm: "Berchuma Moototaa Grar",
    category: "heritage",
    style: "Heritage Accent",
    enDesc: "Authentic single-wood-block carved Ethiopian master seating. Distinctive and heavy.",
    amDesc: "ከተመረጠ ጠንካራ የግራር ግንድ ተፈልፍሎ የተሰራ፣ ትውልድን የሚሻገር ባህላዊ በርጩማ።",
    omDesc: "Berchuma aadaa jabaa mukka guutuu tokko irraa bocomame.",
    dimensions: "45 x 45 x 48 cm",
    materials: ["Acacia (Grar) Core Wood", "Natural Bee Was Coating"],
    colors: ["Charred Rustic Gold", "Natural Grain Light"],
    priceLower: 18000,
    priceUpper: 26000,
    popularity: 91,
    productionTime: "7 Days",
    img: "https://images.unsplash.com/photo-1503602642458-232111445657?auto=format&fit=crop&w=600&q=80"
  }
];

// Dynamically generate the remaining 42 albums to reach exactly 52 items
// Utilizing random combinations of design styles, wood species, and dimensional attributes
export function generateFurnitureCatalog() {
  const result = [];
  const totalTarget = 52;
  
  // 1. Core Templates First (10 elements)
  BASE_PRODUC_TEMPLATES.forEach((temp, idx) => {
    const codePrefix = {
      living_room: "LIV",
      dining_room: "DIN",
      bedroom: "BED",
      office: "OFC",
      heritage: "HRT"
    }[temp.category];

    result.push({
      id: `alb-${100 + idx}`,
      album_code: `BS-${codePrefix}-${String(idx + 1).padStart(3, '0')}`,
      name: temp.baseName,
      name_am: temp.baseNameAm,
      name_om: temp.baseNameOm,
      category: temp.category,
      design_style: temp.style,
      description_en: temp.enDesc,
      description_am: temp.amDesc,
      description_om: temp.omDesc,
      dimensions: temp.dimensions,
      materials: temp.materials.join(", "),
      color_options: temp.colors.join(", "),
      estimated_production_time: temp.productionTime,
      price_range_lower: temp.priceLower,
      price_range_upper: temp.priceUpper,
      popularity_score: temp.popularity,
      tags: [temp.style.split(' ')[0], temp.materials[0].split(' ')[0], codePrefix],
      is_active: true,
      image_url: temp.img,
      images_gallery: [
        temp.img,
        // Alternate angles
        "https://images.unsplash.com/photo-1540518614846-7eded433c457?auto=format&fit=crop&w=600&q=80",
        "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=600&q=80"
      ]
    });
  });

  // 2. Secondary Generation Pools to make exactly 52 distinct products
  const WOOD_POOL = [
    { name: "Wanza", nameAm: "ዋንዛ", nameOm: "Wanza", botanical: "Cordia africana" },
    { name: "Grar", nameAm: "ግራር", nameOm: "Grar", botanical: "Acacia" },
    { name: "Mahogany", nameAm: "ማሆጋኒ", nameOm: "Mahogany", botanical: "Swietenia" },
    { name: "Zigba", nameAm: "ዚግባ", nameOm: "Zigba", botanical: "Podocarpus" },
    { name: "Kerero", nameAm: "ከረሮ", nameOm: "Kerero", botanical: "Aningeria" }
  ];

  const TYPE_POOLS = [
    {
      category: "living_room",
      code: "LIV",
      types: ["Sofa Base", "TV Frame", "Accent Endtable", "Lounge Couch", "Reading Armchair"],
      typesAm: ["ሶፋ ቤዝ", "የቲቪ ማስቀመጫ", "አጋዥ ጠረጴዛ", "ምቹ ሳሎን ሶፋ", "ንባብ ወንበር"],
      typesOm: ["Siree Sofa", "Meeshaa TV", "Minja Xiqqaa", "Sofa Bareedaa", "Teessuma Dubbisuu"],
      descEn: "Engineered with precise joint mortises and customized finishes for active urban households.",
      descAm: "ለዘመናዊ ከተማ ቤቶች በሚሆን መልኩ በልዩ ጥንቃቄ እና ማራኪ ዲዛይን የተሰራ የቤት እቃ።",
      descOm: "Manneen ammayyaaf kan ta'u, muxannoo fi ogummaa guddaadhan bocomame.",
      imgs: [
        "https://images.unsplash.com/photo-1484101403633-562f891dc89a?auto=format&fit=crop&w=600&q=80",
        "https://images.unsplash.com/photo-1540518614846-7eded433c457?auto=format&fit=crop&w=600&q=80",
        "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=600&q=80",
        "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&w=600&q=80"
      ]
    },
    {
      category: "dining_room",
      code: "DIN",
      types: ["Dining Bench", "Buffet Drawer", "Wine Shelving Credence", "Small breakfast Round-Table", "Barstool High"],
      typesAm: ["የምግብ ወንበር ረጅም", "ቡፌ መደርደሪያ", "የወይን ካቢኔት", "ቁርስ ማቅረቢያ ክብ ጠረጴዛ", "ረጅም ባር ወንበር"],
      typesOm: ["Tessoo Bal'aa", "Kaabineetii", "Kaabineetii Daadhii", "Minja Qarsaa", "Teessuma Bar Dheeraa"],
      descEn: "Designed to host memories. Strong timber alignments and water-resistant protective finishes.",
      descAm: "ትውስታዎችን ለማስተናገድ የተሰራ። ከጠነከሩ የኢትዮጵያ ጫካ እንጨቶች የተመረመረ እና የውሃ መከላከያ ቫርኒሽ የተቀባ።",
      descOm: "Mukka jabaa bishaan hin seenne irraa tolfame, maatii hundaaf mijaawaa.",
      imgs: [
        "https://images.unsplash.com/photo-1577140917170-285929fb55b7?auto=format&fit=crop&w=600&q=80",
        "https://images.unsplash.com/photo-1595428774223-ef52624120d2?auto=format&fit=crop&w=600&q=80",
        "https://images.unsplash.com/photo-1615066390971-03e4e1c36ddf?auto=format&fit=crop&w=600&q=80"
      ]
    },
    {
      category: "bedroom",
      code: "BED",
      types: ["Slating Nightboard", "Master Wardrobe with Mirror", "Full-Double Bedframe", "Minimalist Drawer Bedside", "Storage Bench Box"],
      typesAm: ["ምሽት መብራት ጠረጴዛ", "ባለ መስታወት ልብስ ቁምሳጥን", "ባለ ሁለት ሰው አልጋ", "አነስተኛ መደርደሪያ", "የልብስ መቀመጫ ሳጥን"],
      typesOm: ["Minja Shamaa", "Uffata mirrors qabu", "Siree guutuu", "Sanduuqa Xiqqaa", "Teessuma Saanduqaa"],
      descEn: "Ensuring deep comfort and natural acoustics through rich wooden fiber dampening overlays.",
      descAm: "ጥራት ባለው የተፈጥሮ የቤት እቃ አማካኝነት እጅግ ሰላማዊ እና ምቹ እንቅልፍ መፍጠሪያ።",
      descOm: "Bochuun isaa hirriiba gaarii fi nagaa fiduuf mukka uumamaa filatame irraayi.",
      imgs: [
        "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=600&q=80",
        "https://images.unsplash.com/photo-1618220179428-22790b461013?auto=format&fit=crop&w=600&q=80",
        "https://images.unsplash.com/photo-1540518614846-7eded433c457?auto=format&fit=crop&w=600&q=80"
      ]
    },
    {
      category: "office",
      code: "OFC",
      types: ["Double Task Desk", "Lateral Document Archive", "Reception Lobby counter", "Ergonomic Cushion Stool", "Shared Collaboration Table"],
      typesAm: ["ባለ ሁለት ሰው የቢሮ ጠረጴዛ", "የፋይል መዝገብ ማስቀመጫ", "የመስተንግዶ ጠረጴዛ", "ምቹ የቢሮ ወንበር", "የጋራ መስሪያ ትልቅ ጠረጴዛ"],
      typesOm: ["Minja Dachaa", "Kaabineetii Galmee", "Keessummeessituu Desk", "Tessoo Ergonomic", "Minja Hojii Gamtaa"],
      descEn: "Boost corporate output. Standard ergonomic measurements optimized for productivity and durability.",
      descAm: "የቢሮዎን ምርታማነት የሚያሳድጉ የቤት እቃዎች። ለረጅም አመታት ለታማኝ አገልግሎት በደረጃ የተመረቱ።",
      descOm: "Hojii ofisii fi dhaabbataaf kan ta'an, dandeettii fi jabinni isaanii kan mirkanaaye.",
      imgs: [
        "https://images.unsplash.com/photo-1524758631624-e2822e304c36?auto=format&fit=crop&w=600&q=80",
        "https://images.unsplash.com/photo-1517502884422-41eaaced0168?auto=format&fit=crop&w=600&q=80"
      ]
    },
    {
      category: "heritage",
      code: "HRT",
      types: ["Lalibela Geometric Shield", "Ethiopian Sahtin Treasure Box", "Berchuma Tall stool", "Coffee Beans Roast Stand", "Heritage Pillar Panel"],
      typesAm: ["ላሊበላ ጂኦሜትሪክ ጋሻ", "የባህል ሳጥን ውድ እቃዎች ማስቀመጫ", "በርጩማ ረጅም ወንበር", "የቡና እሸት መቁያ መቆሚያ", "ባህላዊ የስነ-ህንጻ ምሰሶ"],
      typesOm: ["Gachana Lalibela", "Saanduqa Maallaqaa Aadaa", "Berchuma Dheeraa", "Teessuma Qola Bunaa", "Mirkana Utubaa Aadaa"],
      descEn: "Handcrafted individually by master carpenters inside the heart of Addis Ababa and Semien regions.",
      descAm: "በአዲስ አበባ እና በጎንደር በእጅ ጥበብ ባለሙያዎች በብቸኝነት የተሰሩ የባህል ማስታወሻዎች።",
      descOm: "Muxannoo dhuunfaan tolfaman, ogummaa sanyii baroota dheeraa kan agarsiisan.",
      imgs: [
        "https://images.unsplash.com/photo-1567538096630-e0c55bd6374c?auto=format&fit=crop&w=600&q=80",
        "https://images.unsplash.com/photo-1503602642458-232111445657?auto=format&fit=crop&w=600&q=80"
      ]
    }
  ];

  const STYLE_POOL = [
    "Scandinavian Modern", "Japandi", "Industrial Loft", "Mid-Century Craft", 
    "Traditional Heritage", "Warm Organic", "Minimalist Luxury", "Rustic Cabin"
  ];

  let loopCounter = 0;
  while (result.length < totalTarget) {
    const typeObj = TYPE_POOLS[result.length % TYPE_POOLS.length];
    const wood = WOOD_POOL[(result.length + 3) % WOOD_POOL.length];
    const style = STYLE_POOL[(result.length * 7) % STYLE_POOL.length];
    
    const typeIndex = loopCounter % typeObj.types.length;
    const nameEng = `${wood.name} ${typeObj.types[typeIndex]}`;
    const nameAm = `${wood.nameAm} ${typeObj.typesAm[typeIndex]}`;
    const nameOm = `${wood.nameOm} ${typeObj.typesOm[typeIndex]}`;

    // Calculate dynamic realistic prices and dims
    const baseVal = 25000 + (result.length * 2800) % 95000;
    const priceLower = Math.floor(baseVal / 1000) * 1000;
    const priceUpper = Math.floor((baseVal * 1.35) / 1000) * 1000;
    const popularity = 75 + (result.length * 13) % 25;
    
    const dimsWidth = 100 + (result.length * 15) % 150;
    const dimsDepth = 40 + (result.length * 10) % 60;
    const dimsHeight = 45 + (result.length * 12) % 120;

    const imgUrl = typeObj.imgs[result.length % typeObj.imgs.length];

    result.push({
      id: `alb-${100 + result.length}`,
      album_code: `BS-${typeObj.code}-${String(result.length + 1).padStart(3, '0')}`,
      name: nameEng,
      name_am: nameAm,
      name_om: nameOm,
      category: typeObj.category,
      design_style: style,
      description_en: `${style} design theme. ${typeObj.descEn} Built with premium ${wood.name} select planks.`,
      description_am: `ባለሙያ የተነደፈ የ${style} ዲዛይን ስብስብ። ${typeObj.descAm} በጥራት ካደገ የ${wood.nameAm} ዛፍ የተመረተ።`,
      description_om: `ዲዛይን ${style}. ${typeObj.descOm} Mukka filatamaa ${wood.nameOm} irraa kan tolfame.`,
      dimensions: `${dimsWidth} x ${dimsDepth} x ${dimsHeight} cm`,
      materials: `${wood.name} (${wood.botanical}), Natural Varnish, Dowel Joints`,
      color_options: "Honey Amber, Antique Oak Stain, Natural Dark Matte",
      estimated_production_time: `${7 + (result.length * 2) % 15} Days`,
      popularity_score: popularity,
      tags: [style.split(' ')[0], wood.name, typeObj.code],
      is_active: true,
      image_url: imgUrl,
      images_gallery: [
        imgUrl,
        "https://images.unsplash.com/photo-1540518614846-7eded433c457?auto=format&fit=crop&w=600&q=80",
        "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=600&q=80"
      ]
    });

    if (typeIndex === typeObj.types.length - 1) {
      loopCounter++;
    }
  }

  return result;
}
