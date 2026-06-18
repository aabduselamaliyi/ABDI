import React, { useState, useMemo } from 'react';
import { 
  generateFurnitureCatalog 
} from './catalogData';
import { 
  Search, 
  Filter, 
  Globe, 
  Heart, 
  Sparkles, 
  Layers, 
  Grid, 
  List, 
  Star, 
  Clock, 
  Compass, 
  Printer, 
  Share2, 
  Check, 
  X, 
  FileText, 
  RefreshCcw, 
  ChevronRight, 
  Info, 
  CheckCircle,
  HelpCircle,
  TrendingUp,
  Sliders,
  DollarSign
} from 'lucide-react';

/**
 * BEKANSI AI SALES ASSISTANT PLATFORM
 * ENTERPRISE MULTI-TENANT FURNITURE DESIGN CATALOG (REACT + TAILWIND CSS)
 * 
 * Provides interactive client-facing and internal sales views to browse, filter,
 * filter by Ethiopian local wood species (Wanza, Grar, Mahogany), customize, and export
 * high-fidelity design proposals for 52 distinct catalog albums.
 */

export default function FurnitureCatalog() {
  // Load generated seed catalog
  const databaseAlbums = useMemo(() => generateFurnitureCatalog(), []);

  // UI Filter and Display States
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('all'); // all, living_room, dining_room, bedroom, office, heritage
  const [selectedMaterial, setSelectedMaterial] = useState('all'); // all, Wanza, Grar, Mahogany, Zigba, Kerero
  const [selectedStyle, setSelectedStyle] = useState('all'); // all, Scandinavian, Japandi, Industrial, Luxury, Traditional
  const [pricingFilter, setPricingFilter] = useState(250000); // Max budget limit (ETB)
  const [sortBy, setSortBy] = useState('popularity'); // popularity, price_asc, price_desc, code
  const [viewMode, setViewMode] = useState('grid'); // grid, list
  const [selectedLocale, setSelectedLocale] = useState('am'); // am (Amharic), om (Afaan Oromo), en (English)
  const [favorites, setFavorites] = useState(['alb-100', 'alb-108']); // Seed some default favorites
  const [selectedAlbumId, setSelectedAlbumId] = useState('alb-100'); // Default focused album
  const [activeThumbnailIndex, setActiveThumbnailIndex] = useState(0);

  // Dynamic customization states for focused product
  const [customWood, setCustomWood] = useState('Match Original'); // Wanza Wood Premium, Mahogany Heartwood, Grar Acacia Extra, Zigba Prime
  const [customFinish, setCustomFinish] = useState('Natural Matte Lacquer');
  const [customFabric, setCustomFabric] = useState('No Fabric / Natural Wood');
  const [customSizeAdjust, setCustomSizeAdjust] = useState('Standard Size'); // Standard, Large (+15%), Compact (-10%)
  
  // Lead Generation modal & quote states
  const [showQuoteSuccess, setShowQuoteSuccess] = useState(false);
  const [quoteCustomerName, setQuoteCustomerName] = useState('Abebe Balcha');
  const [quoteNotes, setQuoteNotes] = useState('Requested hand-carved coffee cup holders embedded.');

  // AI design recommendation mock state
  const [aiHelperMessage, setAiHelperMessage] = useState(null);
  const [isAiLoading, setIsAiLoading] = useState(false);

  // Static list of categories for filter tabs
  const CATEGORIES = [
    { id: 'all', label: 'All Designs', count: databaseAlbums.length },
    { id: 'living_room', label: 'Living Rooms (ሳሎን)', count: databaseAlbums.filter(a => a.category === 'living_room').length },
    { id: 'dining_room', label: 'Dining Rooms (እራት)', count: databaseAlbums.filter(a => a.category === 'dining_room').length },
    { id: 'bedroom', label: 'Bedrooms (መኝታ)', count: databaseAlbums.filter(a => a.category === 'bedroom').length },
    { id: 'office', label: 'Corporate Office (ቢሮ)', count: databaseAlbums.filter(a => a.category === 'office').length },
    { id: 'heritage', label: 'Heritage Accent (ባህላዊ)', count: databaseAlbums.filter(a => a.category === 'heritage').length }
  ];

  // Extraction of all unique wood types and styles for filters
  const WOOD_SPECIES = ['all', 'Wanza', 'Grar', 'Mahogany', 'Zigba', 'Kerero'];
  const DESIGN_STYLES = ['all', 'Scandinavian', 'Japandi', 'Industrial', 'Modern', 'Traditional', 'Minimalist', 'Heritage', 'Rustic'];

  // Toggle favorite helper
  const handleToggleFavorite = (id, e) => {
    if (e) e.stopPropagation();
    if (favorites.includes(id)) {
      setFavorites(favorites.filter(favId => favId !== id));
    } else {
      setFavorites([...favorites, id]);
    }
  };

  // Run filtering computation
  const filteredAlbums = useMemo(() => {
    return databaseAlbums.filter(album => {
      // 1. Search Query Match
      const searchLower = searchQuery.toLowerCase();
      const matchesSearch = 
        album.name.toLowerCase().includes(searchLower) ||
        (album.name_am && album.name_am.includes(searchLower)) ||
        (album.name_om && album.name_om.toLowerCase().includes(searchLower)) ||
        album.album_code.toLowerCase().includes(searchLower) ||
        album.materials.toLowerCase().includes(searchLower) ||
        album.design_style.toLowerCase().includes(searchLower) ||
        album.tags.some(t => t.toLowerCase().includes(searchLower));

      // 2. Category Filter Match
      const matchesCategory = selectedCategory === 'all' || album.category === selectedCategory;

      // 3. Material/Wood Match
      const matchesMaterial = selectedMaterial === 'all' || album.materials.toLowerCase().includes(selectedMaterial.toLowerCase());

      // 4. Style Filter Match
      const matchesStyle = selectedStyle === 'all' || album.design_style.toLowerCase().includes(selectedStyle.toLowerCase());

      // 5. Budget boundary checks
      const matchesPrice = album.price_range_lower <= pricingFilter;

      return matchesSearch && matchesCategory && matchesMaterial && matchesStyle && matchesPrice;
    }).sort((a, b) => {
      if (sortBy === 'popularity') {
        return b.popularity_score - a.popularity_score;
      }
      if (sortBy === 'price_asc') {
        return a.price_range_lower - b.price_range_lower;
      }
      if (sortBy === 'price_desc') {
        return b.price_range_lower - a.price_range_lower;
      }
      if (sortBy === 'code') {
        return a.album_code.localeCompare(b.album_code);
      }
      return 0;
    });
  }, [databaseAlbums, searchQuery, selectedCategory, selectedMaterial, selectedStyle, pricingFilter, sortBy]);

  // Focused album detail entity
  const focusedAlbum = useMemo(() => {
    return databaseAlbums.find(a => a.id === selectedAlbumId) || databaseAlbums[0];
  }, [databaseAlbums, selectedAlbumId]);

  // Reset customizer settings when focused product changes
  React.useEffect(() => {
    if (focusedAlbum) {
      setCustomWood('Match Original');
      setCustomFinish('Natural Matte Lacquer');
      setCustomFabric('No Fabric / Natural Wood');
      setCustomSizeAdjust('Standard Size');
      setActiveThumbnailIndex(0);
      setAiHelperMessage(null);
    }
  }, [focusedAlbum]);

  // Pricing calculator based on customization choices
  const computedPriceEstimate = useMemo(() => {
    if (!focusedAlbum) return { lower: 0, upper: 0 };
    let factor = 1.0;

    // Wood species premium
    if (customWood === 'Wanza Wood Premium (Elite)') factor += 0.20;
    else if (customWood === 'Mahogany Heartwood (Imported)') factor += 0.25;
    else if (customWood === 'Grar Acacia Extra (Weatherproof)') factor += 0.15;
    else if (customWood === 'Zigba Prime Grade') factor += 0.05;

    // Finish adjustments
    if (customFinish === 'Classic High-Gloss') factor += 0.05;
    else if (customFinish === 'Traditional Shellac Hand-rubbed') factor += 0.10;

    // Fabric adjustments
    if (customFabric === 'Plush Royal Velvet') factor += 0.08;
    else if (customFabric === 'Heavyweight Linen Weave') factor += 0.05;

    // Scaling factor
    if (customSizeAdjust === 'Large Layout (+15% dimensions)') factor += 0.15;
    else if (customSizeAdjust === 'Compact Studio Frame (-10%)') factor -= 0.10;

    return {
      lower: Math.round(focusedAlbum.price_range_lower * factor),
      upper: Math.round(focusedAlbum.price_range_upper * factor)
    };
  }, [focusedAlbum, customWood, customFinish, customFabric, customSizeAdjust]);

  // Mock AI Design assistant response trigger
  const handleAiDesignAssistant = () => {
    setIsAiLoading(true);
    setAiHelperMessage(null);

    setTimeout(() => {
      setIsAiLoading(false);
      const suggestions = [
        `🎨 **Aesthetic Pairing**: For the "${focusedAlbum.name}", we suggest pairing it with high-contrast brass lighting and a soft neutral ivory rug. This accentuates the bold texture of ${focusedAlbum.materials.split(',')[0]}.`,
        `🪵 **Wood Acclimatization Note**: Wanza and Grar woods perform wonderfully in Addis Ababa's moderate humidity. Finishing with a Shellac technique highlights natural core lines.`,
        `📈 **Corporate Utility**: Users who selected "${focusedAlbum.album_code}" typically configure L-Shaped office seating together with corresponding Mahogany file credentials for cohesive corporate aesthetics.`
      ];
      setAiHelperMessage(suggestions[Math.floor(Math.random() * suggestions.length)]);
    }, 850);
  };

  // Export proposal logic (Success Toast)
  const handleExportProposal = () => {
    setShowQuoteSuccess(true);
    setTimeout(() => {
      setShowQuoteSuccess(false);
    }, 4500);
  };

  return (
    <div className="w-full bg-slate-950 text-white min-h-screen p-6 font-sans">
      
      {/* Top Professional Header Navigation */}
      <div className="flex flex-col xl:flex-row xl:items-center justify-between border-b border-slate-900 pb-6 mb-6 gap-4">
        <div>
          <div className="flex items-center gap-2 mb-2">
            <span className="bg-amber-500/10 text-amber-500 text-[10px] uppercase font-bold tracking-widest px-2.5 py-1 rounded-md border border-amber-500/15">
              Multi-Tenant Catalog Module
            </span>
            <span className="flex items-center gap-1.5 text-[11px] text-slate-500">
              <Compass className="w-3.5 h-3.5 text-slate-500" />
              Tenant ID Isolation: RLS Restricted
            </span>
          </div>
          <h1 className="text-2xl md:text-3xl font-black text-slate-50 tracking-tight">
            Furniture Design Albums
          </h1>
          <p className="text-slate-400 text-sm mt-1">
            Browse and coordinate 52 premium handcrafted assets with localized multilingual configurations (አማርኛ / Afaan Oromo).
          </p>
        </div>

        {/* Localized Language Switcher & Workspace Actions */}
        <div className="flex flex-wrap items-center gap-3">
          <div className="flex items-center bg-slate-900 border border-slate-800 rounded-xl p-1 gap-1">
            <span className="text-xs text-slate-500 px-2 font-medium">Render Language:</span>
            <button 
              onClick={() => setSelectedLocale('am')}
              className={`px-3 py-1 rounded-lg text-xs font-bold transition-all ${selectedLocale === 'am' ? 'bg-amber-500 text-slate-950 font-black' : 'text-slate-400 hover:text-white'}`}
            >
              አማ (Amharic)
            </button>
            <button 
              onClick={() => setSelectedLocale('om')}
              className={`px-3 py-1 rounded-lg text-xs font-bold transition-all ${selectedLocale === 'om' ? 'bg-emerald-500 text-slate-950 font-black' : 'text-slate-400 hover:text-white'}`}
            >
              OM (Oromo)
            </button>
            <button 
              onClick={() => setSelectedLocale('en')}
              className={`px-3 py-1 rounded-lg text-xs font-bold transition-all ${selectedLocale === 'en' ? 'bg-blue-600 text-white font-black' : 'text-slate-400 hover:text-white'}`}
            >
              EN (English)
            </button>
          </div>

          <div className="flex items-center gap-2">
            <button 
              onClick={() => { setSelectedCategory('all'); setSelectedMaterial('all'); setSearchQuery(''); }}
              className="bg-slate-900 hover:bg-slate-850 p-2.5 px-3.5 rounded-xl border border-slate-800 transition-all text-slate-400 hover:text-white text-xs font-semibold flex items-center gap-1.5"
            >
              <RefreshCcw className="w-3.5 h-3.5" />
              Reset Config
            </button>
          </div>
        </div>
      </div>

      {/* Database KPI Dashboard Strip */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
        <div className="bg-slate-900 border border-slate-900 rounded-xl p-4 relative overflow-hidden">
          <div className="absolute top-3 right-3 opacity-20"><Layers className="w-8 h-8 text-slate-400" /></div>
          <p className="text-[10px] text-slate-400 font-bold uppercase tracking-wider">Indexed Albums</p>
          <h3 className="text-2xl font-black text-white mt-1">52</h3>
          <p className="text-[10.5px] text-slate-500 mt-1">Sourced from Addis Wood Labs</p>
        </div>
        <div className="bg-slate-900 border border-slate-900 rounded-xl p-4 relative overflow-hidden">
          <div className="absolute top-3 right-3 opacity-20"><Star className="w-8 h-8 text-amber-500" /></div>
          <p className="text-[10px] text-slate-400 font-bold uppercase tracking-wider">Premium Wanza Ratio</p>
          <h3 className="text-2xl font-black text-amber-500 mt-1">38%</h3>
          <p className="text-[10.5px] text-slate-500 mt-1">Cordia africana core stock</p>
        </div>
        <div className="bg-slate-900 border border-slate-900 rounded-xl p-4 relative overflow-hidden">
          <div className="absolute top-3 right-3 opacity-20"><Clock className="w-8 h-8 text-emerald-500" /></div>
          <p className="text-[10px] text-slate-400 font-bold uppercase tracking-wider">Average Build Cycle</p>
          <h3 className="text-2xl font-black text-emerald-400 mt-1">11 <span className="text-[11px] font-medium text-slate-400">Days</span></h3>
          <p className="text-[10.5px] text-slate-500 mt-1">Kiln-dried preparation time</p>
        </div>
        <div className="bg-slate-900 border border-slate-900 rounded-xl p-4 relative overflow-hidden">
          <div className="absolute top-3 right-3 opacity-20"><Heart className="w-8 h-8 text-rose-500" /></div>
          <p className="text-[10px] text-slate-400 font-bold uppercase tracking-wider">Design Wishlist</p>
          <h3 className="text-2xl font-black text-rose-500 mt-1">{favorites.length}</h3>
          <p className="text-[10.5px] text-slate-500 mt-1">Active customer favorites</p>
        </div>
      </div>

      {/* Main Structural Area: Two-Column Workspace Layout (Catalog Grid Left, Detailed Workspace Right) */}
      <div className="grid grid-cols-1 xl:grid-cols-4 gap-6">

        {/* 3-Part Column Grid (Search, Categories, and Products list) */}
        <div className="xl:col-span-3 space-y-4">
          
          {/* Interactive Multi-filtration Card Shelf */}
          <div className="bg-slate-900 border border-slate-900 p-4 rounded-xl space-y-4">
            
            {/* Search Input and Toggles */}
            <div className="flex flex-col lg:flex-row items-center gap-3">
              <div className="relative flex-1 w-full">
                <Search className="w-4 h-4 ml-3.5 absolute top-1/2 -translate-y-1/2 text-slate-500" />
                <input
                  type="text"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  placeholder="Query by album code, English name, Amharic, design style, or material..."
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl py-2.5 pl-10 pr-4 text-xs text-slate-200 placeholder-slate-500 focus:outline-none focus:border-amber-500/50 focus:ring-1 focus:ring-amber-500/25 transition-all text-ellipsis"
                />
              </div>

              {/* Grid / List details, Sort and Price Slider */}
              <div className="flex flex-wrap items-center gap-2.5 w-full lg:w-auto">
                
                {/* Wood Specific Filter */}
                <div className="flex items-center bg-slate-950 border border-slate-800 rounded-xl px-2.5 py-1.5 text-xs text-slate-300 gap-1.5">
                  <span className="text-slate-500 font-semibold">Wood:</span>
                  <select 
                    value={selectedMaterial}
                    onChange={(e) => setSelectedMaterial(e.target.value)}
                    className="bg-transparent border-none text-slate-200 font-bold focus:outline-none cursor-pointer pr-1"
                  >
                    <option value="all">Any Timber</option>
                    {WOOD_SPECIES.filter(w => w !== 'all').map(wood => (
                      <option key={wood} value={wood}>{wood}</option>
                    ))}
                  </select>
                </div>

                {/* Style Specific Filter */}
                <div className="flex items-center bg-slate-950 border border-slate-800 rounded-xl px-2.5 py-1.5 text-xs text-slate-300 gap-1.5">
                  <span className="text-slate-500 font-semibold">Style:</span>
                  <select 
                    value={selectedStyle}
                    onChange={(e) => setSelectedStyle(e.target.value)}
                    className="bg-transparent border-none text-slate-200 font-bold focus:outline-none cursor-pointer pr-1"
                  >
                    <option value="all">Any Style</option>
                    {DESIGN_STYLES.filter(s => s !== 'all').map(style => (
                      <option key={style} value={style}>{style}</option>
                    ))}
                  </select>
                </div>

                {/* Ordering Dropdown */}
                <div className="flex items-center bg-slate-950 border border-slate-800 rounded-xl px-2.5 py-1.5 text-xs text-slate-300 gap-1.5">
                  <span className="text-slate-500 font-semibold">Order:</span>
                  <select 
                    value={sortBy}
                    onChange={(e) => setSortBy(e.target.value)}
                    className="bg-transparent border-none text-slate-200 font-bold focus:outline-none cursor-pointer pr-1"
                  >
                    <option value="popularity">Popularity Rating</option>
                    <option value="price_asc">Price: Low to High</option>
                    <option value="price_desc">Price: High to Low</option>
                    <option value="code">Album Code ID</option>
                  </select>
                </div>

                {/* Grid Mode Swappers */}
                <div className="flex items-center bg-slate-950 p-1.5 rounded-xl border border-slate-800 gap-1">
                  <button 
                    onClick={() => setViewMode('grid')}
                    className={`p-1.5 rounded-lg transition-colors ${viewMode === 'grid' ? 'bg-slate-850 text-amber-500' : 'text-slate-500 hover:text-slate-300'}`}
                  >
                    <Grid className="w-3.5 h-3.5" />
                  </button>
                  <button 
                    onClick={() => setViewMode('list')}
                    className={`p-1.5 rounded-lg transition-colors ${viewMode === 'list' ? 'bg-slate-850 text-amber-500' : 'text-slate-500 hover:text-slate-300'}`}
                  >
                    <List className="w-3.5 h-3.5" />
                  </button>
                </div>

              </div>
            </div>

            {/* Budget Range Slider */}
            <div className="flex flex-col sm:flex-row sm:items-center justify-between border-t border-slate-850/50 pt-3 gap-3">
              <div className="flex items-center gap-2">
                <Sliders className="w-3.5 h-3.5 text-amber-500" />
                <span className="text-xs text-slate-300 font-semibold">Maximum Starting Price Range limit:</span>
              </div>
              <div className="flex items-center gap-4 flex-1 max-w-md">
                <input 
                  type="range"
                  min="15000"
                  max="200000"
                  step="5000"
                  value={pricingFilter}
                  onChange={(e) => setPricingFilter(parseInt(e.target.value))}
                  className="w-full accent-amber-500 bg-slate-950 h-1.5 rounded-lg appearance-none cursor-pointer"
                />
                <span className="text-xs font-black text-amber-500 bg-amber-500/10 border border-amber-500/15 p-1 px-2.5 rounded-lg whitespace-nowrap">
                  ≤ {pricingFilter.toLocaleString()} ETB
                </span>
              </div>
            </div>

          </div>

          {/* Horizontally scrolling Category Filter tabs */}
          <div className="flex items-center gap-2 flex-wrap pb-1 overflow-x-auto whitespace-nowrap">
            {CATEGORIES.map((cat) => (
              <button
                key={cat.id}
                onClick={() => setSelectedCategory(cat.id)}
                className={`px-4 py-2.5 rounded-lg text-xs font-semibold border transition-all flex items-center gap-2 ${
                  selectedCategory === cat.id
                    ? 'bg-amber-500 text-slate-950 font-black border-amber-500 shadow-md'
                    : 'bg-slate-900 border-slate-850 hover:bg-slate-850 text-slate-400 hover:text-slate-200'
                }`}
              >
                {cat.label}
                <span className={`px-1.5 py-0.5 rounded-md text-[10px] ${selectedCategory === cat.id ? 'bg-slate-950/20 text-slate-950 font-bold' : 'bg-slate-950 text-slate-500'}`}>
                  {cat.count}
                </span>
              </button>
            ))}
          </div>

          {/* Results feedback banner */}
          <div className="flex items-center justify-between text-xs text-slate-400 px-1">
            <p>Database queries resolved <span className="font-bold text-amber-500">{filteredAlbums.length}</span> albums matching schema constraints</p>
            {searchQuery && (
              <button 
                onClick={() => setSearchQuery('')}
                className="text-slate-500 hover:text-white underline"
              >
                Clear Search
              </button>
            )}
          </div>

          {/* core View render area */}
          {filteredAlbums.length === 0 ? (
            <div className="bg-slate-900/40 border border-dashed border-slate-800 rounded-2xl p-16 text-center space-y-4">
              <div className="w-12 h-12 bg-slate-900 border border-slate-800 rounded-xl flex items-center justify-center mx-auto text-slate-500">
                <Info className="w-6 h-6" />
              </div>
              <div className="max-w-md mx-auto space-y-1">
                <h4 className="font-bold text-slate-100 text-sm">No albums match criteria</h4>
                <p className="text-xs text-slate-500 leading-relaxed">
                  Adjust your wood filter, increase your starting price threshold, or type another character to query. Every record is tenant-isolated.
                </p>
              </div>
              <button 
                onClick={() => { setSelectedCategory('all'); setSelectedMaterial('all'); setSelectedStyle('all'); setPricingFilter(250000); setSearchQuery(''); }}
                className="bg-amber-500 hover:bg-amber-400 text-slate-950 font-extrabold text-xs px-4 py-2 rounded-xl transition-all"
              >
                Reset All Filters
              </button>
            </div>
          ) : viewMode === 'grid' ? (
            
            /* Responsive Grid View */
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
              {filteredAlbums.map((album) => {
                const isSelected = album.id === selectedAlbumId;
                const isFavorite = favorites.includes(album.id);
                
                // Fetch dynamic localized display parameters
                let localizedName = album.name;
                let localizedDesc = album.description_en;

                if (selectedLocale === 'am' && album.name_am) {
                  localizedName = album.name_am;
                  localizedDesc = album.description_am || album.description_en;
                } else if (selectedLocale === 'om' && album.name_om) {
                  localizedName = album.name_om;
                  localizedDesc = album.description_om || album.description_en;
                }

                return (
                  <div
                    key={album.id}
                    onClick={() => setSelectedAlbumId(album.id)}
                    className={`bg-slate-900 rounded-2xl overflow-hidden border transition-all cursor-pointer group flex flex-col justify-between ${
                      isSelected 
                        ? 'border-amber-500 shadow-2xl shadow-amber-500/5 bg-slate-900/90 ring-1 ring-amber-500/20' 
                        : 'border-slate-900 hover:border-slate-800 hover:bg-slate-900/60'
                    }`}
                  >
                    {/* Catalog Image Thumbnail Gallery box */}
                    <div className="relative aspect-[4/3] bg-slate-950 overflow-hidden">
                      <img 
                        src={album.image_url} 
                        alt={album.name}
                        className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
                        onError={(e) => {
                          e.target.src = "https://images.unsplash.com/photo-1540518614846-7eded433c457?auto=format&fit=crop&w=600&q=80";
                        }}
                      />
                      
                      {/* Floating overlay panels */}
                      <div className="absolute inset-0 bg-gradient-to-t from-slate-950 via-transparent to-transparent opacity-60" />
                      
                      {/* Album Code ID badge */}
                      <div className="absolute top-3 left-3 bg-slate-950/80 backdrop-blur-md border border-slate-800 text-[10px] font-black tracking-widest px-2.5 py-1 rounded-md">
                        {album.album_code}
                      </div>

                      {/* Favorite Toggle button */}
                      <button
                        onClick={(e) => handleToggleFavorite(album.id, e)}
                        className={`absolute top-3 right-3 p-2 rounded-lg border backdrop-blur-md transition-all ${
                          isFavorite 
                            ? 'bg-rose-500/10 border-rose-500/30 text-rose-500' 
                            : 'bg-slate-950/70 border-slate-800 text-slate-400 hover:text-white'
                        }`}
                      >
                        <Heart className={`w-3.5 h-3.5 ${isFavorite ? 'fill-rose-500' : ''}`} />
                      </button>

                      <div className="absolute bottom-3 left-3 right-3 flex items-center justify-between">
                        {/* Material designation */}
                        <span className="text-[10px] uppercase font-bold text-slate-400 bg-slate-950/80 backdrop-blur-md px-2 py-0.5 rounded-md border border-slate-850">
                          {album.materials.split(',')[0]}
                        </span>
                        
                        {/* Rating block */}
                        <div className="flex items-center gap-1 text-[11px] bg-slate-950/80 backdrop-blur-md px-2 py-0.5 rounded-md text-amber-500 border border-slate-850 font-bold">
                          <Star className="w-3 h-3 fill-amber-500" />
                          {album.popularity_score}
                        </div>
                      </div>
                    </div>

                    {/* Meta Card Details */}
                    <div className="p-4 space-y-3 flex-1 flex flex-col justify-between">
                      <div className="space-y-1">
                        <div className="flex items-start justify-between gap-2">
                          <h4 className={`text-sm font-extrabold tracking-tight transition-colors line-clamp-1 ${isSelected ? 'text-amber-500' : 'text-slate-100 group-hover:text-amber-500'}`}>
                            {localizedName}
                          </h4>
                        </div>
                        <p className="text-[11.5px] text-slate-500 line-clamp-2 leading-relaxed">
                          {localizedDesc}
                        </p>
                      </div>

                      {/* Pricing range specifications and visual details */}
                      <div className="pt-2.5 border-t border-slate-850/50 flex items-center justify-between">
                        <div>
                          <span className="text-[9px] text-slate-500 uppercase tracking-wider block">Estimated Price Range</span>
                          <span className="text-xs font-black text-slate-200">
                            {album.price_range_lower.toLocaleString()} - {album.price_range_upper.toLocaleString()} <span className="text-[10px] text-slate-400 font-semibold tracking-wide">ETB</span>
                          </span>
                        </div>

                        <div className="text-right">
                          <span className="text-[9px] text-slate-500 uppercase tracking-wider block">Build Cycle</span>
                          <span className="text-[11.5px] font-bold text-slate-300 flex items-center gap-1 justify-end">
                            <Clock className="w-3 h-3" /> {album.estimated_production_time}
                          </span>
                        </div>
                      </div>

                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            
            /* Responsive List View */
            <div className="bg-slate-900 border border-slate-900 rounded-xl overflow-hidden divide-y divide-slate-850 shadow-xl">
              {filteredAlbums.map((album) => {
                const isSelected = album.id === selectedAlbumId;
                const isFavorite = favorites.includes(album.id);
                
                // Fetch localized display parameters
                let localizedName = album.name;
                if (selectedLocale === 'am' && album.name_am) localizedName = album.name_am;
                else if (selectedLocale === 'om' && album.name_om) localizedName = album.name_om;

                return (
                  <div
                    key={album.id}
                    onClick={() => setSelectedAlbumId(album.id)}
                    className={`p-4 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 cursor-pointer transition-colors ${
                      isSelected ? 'bg-slate-850' : 'hover:bg-slate-850/50'
                    }`}
                  >
                    <div className="flex items-center gap-4 flex-1">
                      {/* Sizable Thumbnail */}
                      <div className="w-16 h-16 rounded-lg overflow-hidden bg-slate-950 border border-slate-800 shrink-0 relative">
                        <img 
                          src={album.image_url} 
                          alt="" 
                          className="w-full h-full object-cover"
                          onError={(e) => {
                            e.target.src = "https://images.unsplash.com/photo-1540518614846-7eded433c457?auto=format&fit=crop&w=600&q=80";
                          }}
                        />
                      </div>
                      <div>
                        <div className="flex items-center gap-2">
                          <span className="text-[10px] font-black bg-slate-950 p-0.5 px-2 rounded text-slate-500 border border-slate-800">{album.album_code}</span>
                          <h4 className={`text-xs font-black ${isSelected ? 'text-amber-500' : 'text-slate-200'}`}>{localizedName}</h4>
                        </div>
                        <p className="text-[11px] text-slate-500 mt-1 lines-clamp-1">{album.materials} (Style: {album.design_style})</p>
                      </div>
                    </div>

                    <div className="flex items-center gap-6 justify-between sm:justify-end w-full sm:w-auto shrink-0">
                      <div className="text-left sm:text-right">
                        <span className="text-[9px] text-slate-500 uppercase block">Starting Price</span>
                        <span className="text-xs font-black text-slate-200">{album.price_range_lower.toLocaleString()} ETB</span>
                      </div>
                      <div className="text-left sm:text-right">
                        <span className="text-[9px] text-slate-500 uppercase block">Build Cycle</span>
                        <span className="text-xs font-bold text-slate-400">{album.estimated_production_time}</span>
                      </div>

                      {/* Fast Action List buttons */}
                      <div className="flex items-center gap-2">
                        <button
                          onClick={(e) => handleToggleFavorite(album.id, e)}
                          className={`p-2 rounded-lg border transition-all ${
                            isFavorite 
                              ? 'bg-rose-500/10 border-rose-500/20 text-rose-500' 
                              : 'bg-slate-950 border-slate-800 text-slate-500 hover:text-white'
                          }`}
                        >
                          <Heart className="w-3.5 h-3.5 fill-current" />
                        </button>
                        <ChevronRight className="w-4 h-4 text-slate-600" />
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}

        </div>

        {/* =========================================================================
            RIGHT COLUMN: PREDICTIVE ACTIVE WORKSPACE DETAIL PANEL
            ========================================================================= */}
        <div className="xl:col-span-1">
          <div className="bg-slate-900 border border-slate-900 p-5 rounded-2xl space-y-5 sticky top-6 shadow-2xl">
            
            {/* Workspace details header */}
            <div className="flex items-center justify-between border-b border-slate-850 pb-4">
              <h4 className="text-xs font-black text-slate-200 uppercase tracking-widest flex items-center gap-1.5/2">
                <span className="w-2.5 h-2.5 rounded-full bg-amber-500 animate-pulse" /> Custom Workstation
              </h4>
              <span className="text-[10px] text-slate-500 font-mono">
                {focusedAlbum.album_code}
              </span>
            </div>

            {/* Micro Gallery View Carousel inside workspace */}
            <div className="space-y-2">
              <div className="aspect-[1.5/1] bg-slate-950 rounded-xl overflow-hidden border border-slate-800 relative">
                <img 
                  src={focusedAlbum.images_gallery[activeThumbnailIndex] || focusedAlbum.image_url} 
                  alt="" 
                  className="w-full h-full object-cover" 
                  onError={(e) => {
                    e.target.src = "https://images.unsplash.com/photo-1540518614846-7eded433c457?auto=format&fit=crop&w=600&q=80";
                  }}
                />
                
                {/* Visual Popularity score on focus */}
                <div className="absolute top-2.5 right-2.5 bg-slate-950/80 border border-amber-500/30 text-[9.5px] text-amber-500 px-2 py-0.5 rounded-md font-bold flex items-center gap-1">
                  <Star className="w-3 h-3 fill-current" />
                  {focusedAlbum.popularity_score}% popular
                </div>
              </div>

              {/* Gallery Mini-Thumbnails Row */}
              <div className="grid grid-cols-3 gap-2">
                {focusedAlbum.images_gallery.map((img, index) => (
                  <button
                    key={index}
                    onClick={() => setActiveThumbnailIndex(index)}
                    className={`aspect-[4/3] rounded-lg overflow-hidden border transition-all bg-slate-950 ${
                      activeThumbnailIndex === index 
                        ? 'border-amber-500 ring-2 ring-amber-500/20' 
                        : 'border-slate-800 hover:border-slate-600'
                    }`}
                  >
                    <img 
                      src={img} 
                      alt="" 
                      className="w-full h-full object-cover" 
                      onError={(e) => {
                        e.target.src = "https://images.unsplash.com/photo-1540518614846-7eded433c457?auto=format&fit=crop&w=600&q=80";
                      }}
                    />
                  </button>
                ))}
              </div>
            </div>

            {/* Descriptive texts with localized tags */}
            <div className="space-y-1">
              <span className="bg-slate-950 text-[9.5px] uppercase font-bold text-slate-500 px-2 py-0.5 rounded-md border border-slate-850">
                {focusedAlbum.design_style} design
              </span>
              <h3 className="text-base font-black text-slate-100 mt-2">
                {selectedLocale === 'am' ? (focusedAlbum.name_am || focusedAlbum.name) : selectedLocale === 'om' ? (focusedAlbum.name_om || focusedAlbum.name) : focusedAlbum.name}
              </h3>
              <p className="text-xs text-slate-400 leading-relaxed pt-1 italic">
                "{selectedLocale === 'am' ? (focusedAlbum.description_am || focusedAlbum.description_en) : selectedLocale === 'om' ? (focusedAlbum.description_om || focusedAlbum.description_en) : focusedAlbum.description_en}"
              </p>
            </div>

            {/* Technical specifications blocks */}
            <div className="bg-slate-950 p-3 rounded-xl border border-slate-850 text-[11px] space-y-2">
              <div className="flex justify-between">
                <span className="text-slate-500 font-semibold">Dimensions:</span>
                <span className="text-slate-300 font-medium">{focusedAlbum.dimensions}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500 font-semibold">Base Timbers:</span>
                <span className="text-slate-300 font-medium truncate max-w-[150px]">{focusedAlbum.materials.split(',')[0]}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500 font-semibold">Prep Cycle:</span>
                <span className="text-slate-300 font-medium">{focusedAlbum.estimated_production_time}</span>
              </div>
            </div>

            {/* Customizer parameters options (Dynamic recalculations) */}
            <div className="space-y-3 pt-2 border-t border-slate-850/50">
              <h4 className="text-[10px] font-black tracking-widest text-slate-400 uppercase">Configuration Customizer:</h4>
              
              {/* Wood Upgraded Type selector */}
              <div className="space-y-1">
                <label className="text-[10px] text-slate-500 block">Upgrade Wood Grade:</label>
                <select 
                  value={customWood}
                  onChange={(e) => setCustomWood(e.target.value)}
                  className="w-full bg-slate-950 text-[11px] text-slate-300 border border-slate-800 rounded-lg p-2 focus:outline-none focus:border-amber-500"
                >
                  <option value="Match Original">Match Original ({focusedAlbum.materials.split(',')[0]})</option>
                  <option value="Wanza Wood Premium (Elite)">Wanza Wood Premium (+20%)</option>
                  <option value="Mahogany Heartwood (Imported)">Mahogany Heartwood (+25%)</option>
                  <option value="Grar Acacia Extra (Weatherproof)">Grar Acacia Extra (+15%)</option>
                  <option value="Zigba Prime Grade">Zigba Prime Grade (+5%)</option>
                </select>
              </div>

              {/* Finishing Customizer */}
              <div className="space-y-1">
                <label className="text-[10px] text-slate-500 block">Select Polyurethane Coating:</label>
                <select 
                  value={customFinish}
                  onChange={(e) => setCustomFinish(e.target.value)}
                  className="w-full bg-slate-950 text-[11px] text-slate-300 border border-slate-800 rounded-lg p-2 focus:outline-none focus:border-amber-500"
                >
                  <option value="Natural Matte Lacquer">Natural Matte Lacquer (Default)</option>
                  <option value="Classic High-Gloss">Classic High-Gloss Finishing (+5%)</option>
                  <option value="Traditional Shellac Hand-rubbed">Shellac hand-rubbed traditional coat (+10%)</option>
                </select>
              </div>

              {/* Fabric/Leather upholstery option */}
              <div className="space-y-1">
                <label className="text-[10px] text-slate-500 block">Upgrade Fabric Grade:</label>
                <select 
                  value={customFabric}
                  onChange={(e) => setCustomFabric(e.target.value)}
                  className="w-full bg-slate-950 text-[11px] text-slate-300 border border-slate-800 rounded-lg p-2 focus:outline-none focus:border-amber-500"
                >
                  <option value="No Fabric / Natural Wood">Un-upholstered / Pure Timber core</option>
                  <option value="Plush Royal Velvet">Plush Royal Velvet (Heavy) (+8%)</option>
                  <option value="Heavyweight Linen Weave">Heavyweight Linen Weave Fabric (+5%)</option>
                </select>
              </div>

              {/* Sizing Scaling options */}
              <div className="space-y-1">
                <label className="text-[10px] text-slate-500 block">Custom Dimensions Scale:</label>
                <select 
                  value={customSizeAdjust}
                  onChange={(e) => setCustomSizeAdjust(e.target.value)}
                  className="w-full bg-slate-950 text-[11px] text-slate-300 border border-slate-800 rounded-lg p-2 focus:outline-none focus:border-amber-500"
                >
                  <option value="Standard Size">Standard specifications dimensions</option>
                  <option value="Large Layout (+15% dimensions)">Large Layout / Sectional stretch (+15%)</option>
                  <option value="Compact Studio Frame (-10%)">Compact Studio frame optimization (-10%)</option>
                </select>
              </div>
            </div>

            {/* Dynamic Price Calculation Overlay */}
            <div className="bg-amber-500/10 border border-amber-500/20 p-3.5 rounded-xl space-y-1">
              <span className="text-[9.5px] text-slate-400 font-bold block uppercase tracking-wider">Dynamic Customizable Pricing</span>
              <div className="flex items-baseline justify-between">
                <span className="text-lg font-black text-amber-500">
                  {computedPriceEstimate.lower.toLocaleString()} - {computedPriceEstimate.upper.toLocaleString()}
                </span>
                <span className="text-xs font-bold text-slate-400">ETB Birr</span>
              </div>
              <p className="text-[9.5px] text-slate-500 leading-tight">
                *Estimated prices are multi-tenant references and fluctuate based on timber exchange in Addis Ababa.
              </p>
            </div>

            {/* AI Assistant Tool embedded right on product focus */}
            <div className="bg-slate-950 rounded-xl p-3.5 border border-slate-850 space-y-2.5">
              <div className="flex items-center justify-between">
                <span className="text-[10px] font-black uppercase text-slate-400 flex items-center gap-1">
                  <Sparkles className="w-3.5 h-3.5 text-amber-500 animate-bounce" /> AI Design Assistant
                </span>
                <button 
                  onClick={handleAiDesignAssistant}
                  disabled={isAiLoading}
                  className="text-[10px] text-amber-500 hover:text-amber-400 font-bold disabled:opacity-40"
                >
                  {isAiLoading ? 'Synthesizing...' : 'Consult AI'}
                </button>
              </div>

              {isAiLoading && (
                <div className="space-y-1.5 py-1">
                  <div className="h-2 bg-slate-900 rounded animate-pulse w-full" />
                  <div className="h-2 bg-slate-900 rounded animate-pulse w-5/6" />
                </div>
              )}

              {aiHelperMessage && (
                <div className="bg-slate-900 p-2.5 rounded-lg border border-slate-850">
                  <p className="text-[11px] text-slate-300 leading-relaxed select-all">
                    {aiHelperMessage}
                  </p>
                </div>
              )}
            </div>

            {/* Proposal Generation Input & Export Actions */}
            <div className="space-y-2.5 pt-2 border-t border-slate-850/50">
              <div className="space-y-1">
                <label className="text-[10px] text-slate-400 block font-semibold">Coordinate with Lead/Customer:</label>
                <input 
                  type="text"
                  value={quoteCustomerName}
                  onChange={(e) => setQuoteCustomerName(e.target.value)}
                  className="w-full bg-slate-950 text-[11px] border border-slate-850 rounded-lg p-2 text-slate-300 focus:outline-none"
                  placeholder="Customer Full Name (e.g., Abebe)"
                />
              </div>

              <div className="flex gap-2">
                <button 
                  onClick={handleExportProposal}
                  className="flex-1 bg-amber-500 hover:bg-amber-400 text-slate-950 font-black text-xs py-3 rounded-xl transition-all shadow-lg flex items-center justify-center gap-1.5"
                >
                  <FileText className="w-3.5 h-3.5" />
                  Issue Design Quote
                </button>
                <button 
                  onClick={() => alert(`Direct tenant webhook link: https://t.me/BekansiSalesBot?start=quote_${focusedAlbum.album_code}`)}
                  className="bg-slate-950 border border-slate-800 hover:bg-slate-850 p-2.5 rounded-xl transition-all text-slate-300"
                >
                  <Share2 className="w-4 h-4" />
                </button>
              </div>

              {/* Toast response trigger */}
              {showQuoteSuccess && (
                <div className="bg-emerald-500/10 border border-emerald-500/20 p-3 rounded-xl flex items-start gap-2.5 text-emerald-400 text-xs">
                  <CheckCircle className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
                  <div>
                    <h5 className="font-extrabold">Enterprise Lead Quotation Generated!</h5>
                    <p className="text-[11px] opacity-90 mt-0.5">
                      Assigned to customer "{quoteCustomerName}" with custom options. Multi-tenant RLS locked in schema.sql.
                    </p>
                  </div>
                </div>
              )}
            </div>

          </div>
        </div>

      </div>

      {/* Standard professional enterprise footer */}
      <div className="flex flex-col sm:flex-row items-center justify-between text-xs text-slate-500 pt-6 mt-8 border-t border-slate-900 gap-3">
        <span className="flex items-center gap-1">
          <Globe className="w-3.5 h-3.5" />
          Active Organization: Bekansi Premium Woodworkers PLC
        </span>
        <div className="flex items-center gap-4">
          <span>Enterprise Database: Postgres v15 + Room Android Cache</span>
          <span>Last modified: T-2026</span>
        </div>
      </div>

    </div>
  );
}
