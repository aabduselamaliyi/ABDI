import React, { useState, useMemo, useEffect } from 'react';
import { 
  Users, 
  Search, 
  Filter, 
  Globe, 
  Calendar, 
  ChevronDown, 
  ChevronRight,
  TrendingUp, 
  MessageSquare, 
  FileText, 
  ArrowUpRight, 
  MoreHorizontal,
  Mail,
  Phone,
  Settings,
  Circle,
  Eye,
  CheckCircle2,
  AlertCircle,
  Plus,
  LayoutDashboard,
  Tag,
  Briefcase,
  Layers,
  HelpCircle,
  Building2,
  Sparkles,
  Send,
  Lock,
  Check,
  X,
  RefreshCw,
  FileSpreadsheet,
  Clock,
  Menu,
  Printer,
  Percent,
  BarChart3,
  AlertTriangle,
  ShieldCheck,
  Moon,
  Sun,
  Trash2,
  ArrowUp,
  ArrowDown
} from 'lucide-react';
import ShareButton from './ShareButton';

// ============================================================================
// PRODUCTION-GRADE MULTI-TENANT SEEDED DATA MODEL (ISOLATED SAAS CONTEXTS)
// ============================================================================

const SEED_TENANTS = {
  bekansi: {
    id: 'bekansi',
    name: 'Bekansi Furniture & Interior Design',
    logo: '🪵',
    location: 'Bishoftu City, Dukem Subcity',
    contact: '0988828861 / 0910824534',
    currency: 'ETB',
    stats: { totalLeads: 248, activeCustomers: 142, pendingQuotes: 18, revenue: 5420000, convRate: 14.2, dealsWon: 86 },
    catalog: [
      { id: 'b1', name: "Wanza Curved L-Sofa 'Gara'", price: 135000, material: 'Solid Wanza', category: 'Living Room', stock: 12, performance: 'High Demand', desc: 'Prestige modular couch wrapped in native Habesha embroidery finish.' },
      { id: 'b2', name: "Mahogany Dining Suite 'Zid'", price: 185000, material: 'Red Mahogany', category: 'Dining Room', stock: 4, performance: 'Best Seller', desc: 'Opulent solid hardwood table with 8 masterfully carved chairs.' },
      { id: 'b3', name: "King Floating Bed 'Sheger'", price: 110000, material: 'Ethiopian Acacia', category: 'Bedroom Set', stock: 8, performance: 'Trending', desc: 'Sleek floating base with embedded low-voltage ambient LED frames.' },
      { id: 'b4', name: 'Solid Zigba Sideboard Drawer', price: 75000, material: 'Zigba Wood', category: 'Custom Cabinetry', stock: 15, performance: 'Steady', desc: 'Crafted storage console with clean traditional grain configurations.' },
      { id: 'b5', name: 'Premium Oak Kitchen Cabinets', price: 290000, material: 'White Oak & Glass', category: 'Custom Cabinetry', stock: 2, performance: 'High Value', desc: 'Full-wall custom panel kitchen installation with soft-close fixtures.' }
    ],
    leads: [
      { id: 'l1', name: 'Abdi Biya', phone: '+251 98 882 8861', stage: 'Qualified', source: 'Telegram', status: 'Active', value: 135000, email: 'abdi.biya@gmail.com', location: 'Bishoftu', date: '2026-06-18', score: 92, message: 'Inquiry about custom velvet Gara couch width adjustments.' },
      { id: 'l2', name: 'Dr. Yonas Admasu', phone: '+251 91 108 4534', stage: 'Negotiation', source: 'WhatsApp', status: 'Active', value: 245000, email: 'yonas.admasu@blacklion.et', location: 'Addis Ababa', date: '2026-06-16', score: 85, message: 'Wants premium dining suite and matching entryway console.' },
      { id: 'l3', name: 'Helen Yohannes', phone: '+251 90 234 1188', stage: 'New', source: 'Web Chat', status: 'New', value: 620000, email: 'helen.y@interioraddis.com', location: 'Hawassa', date: '2026-06-14', score: 68, message: 'Requires formal B2B desk packages proposal for design studio.' },
      { id: 'l4', name: 'Aster Tolossa', phone: '+251 91 140 1290', stage: 'Proposal', source: 'WhatsApp', status: 'Active', value: 380000, email: 'aster.t@gmail.com', location: 'Nazareth', date: '2026-06-12', score: 95, message: 'Awaiting scale kitchen cabinet layout sketches and material samples.' },
      { id: 'l5', name: 'Kedir Mohammed', phone: '+251 94 488 2211', stage: 'Lost', source: 'Facebook', status: 'Inactive', value: 110000, email: 'kedir.m@gmail.com', location: 'Dukem', date: '2026-06-08', score: 42, message: 'Inquired on floating bed but opted for budget metal frame alternative.' }
    ],
    conversations: [
      { id: 'c1', name: 'Abdi Biya', channel: 'Telegram', snippet: 'Can I select a custom gray upholstery?', messages: [
        { sender: 'user', text: 'Hello, what is the wood species in Gara L-Sofa?', time: '10:42 AM' },
        { sender: 'ai', text: 'Greeting Abdi! The Wanza L-Sofa Gara is built from solid Wanza (Cordia Africana) timber, kiln-dried to eliminate cracking. Our showroom is at Bishoftu, Dukem side.', time: '10:43 AM' },
        { sender: 'user', text: 'Can I select a custom gray upholstery for the Gara couch?', time: '10:45 AM' }
      ]},
      { id: 'c2', name: 'Dr. Yonas Admasu', channel: 'WhatsApp', snippet: 'Please draft the billing specifications.', messages: [
        { sender: 'user', text: 'Hello, do you ship finished dining sets to Addis?', time: 'Yesterday' },
        { sender: 'ai', text: 'Yes Dr. Yonas! We coordinate safe logistics with protective felt wrapping. Flat-rate delivery to Addis is 5,000 ETB.', time: 'Yesterday' },
        { sender: 'user', text: 'Please draft the billing specifications.', time: 'Yesterday' }
      ]}
    ]
  },
  zema: {
    id: 'zema',
    name: 'Zema Office Systems',
    logo: '🪑',
    location: 'Bole Road, Addis Ababa',
    contact: '0911556212 / 0922448899',
    currency: 'ETB',
    stats: { totalLeads: 112, activeCustomers: 96, pendingQuotes: 8, revenue: 3890000, convRate: 11.8, dealsWon: 52 },
    catalog: [
      { id: 'z1', name: "ErgoMax Task Chair 'Sit'", price: 18500, material: 'Mesh / Molded Base', category: 'Office', stock: 45, performance: 'Fast Moving', desc: 'Premium lumbar-adaptive ergonomic office chair.' },
      { id: 'z2', name: "Modular Workstation 'Sync'", price: 45000, material: 'Steel & Laminate Panel', category: 'Office', stock: 14, performance: 'Steady Demand', desc: 'Collaborative desk suite with neat cable pathways.' },
      { id: 'z3', name: "Executive Walnut Desk 'Lasta'", price: 125000, material: 'Solid Walnut Base', category: 'Office', stock: 3, performance: 'High Level', desc: 'Majestic walnut veneers with soft-close locking cabinets.' }
    ],
    leads: [
      { id: 'zl1', name: 'Almaz Tekle', phone: '+251 91 155 6212', stage: 'New', source: 'Web Chat', status: 'New', value: 185000, email: 'almaz.tekle@yahoo.com', location: 'Bole, Addis', date: '2026-06-18', score: 76, message: 'Wants to order 10 ErgoMax seating configurations for tech division.' },
      { id: 'zl2', name: 'Dawit Bekele', phone: '+251 92 144 5599', stage: 'Qualified', source: 'Telegram', status: 'Active', value: 90000, email: 'dbek@innovate.et', location: 'Kazanchis', date: '2026-06-15', score: 81, message: 'Interested in dual model Sync shared workstations.' }
    ],
    conversations: [
      { id: 'zc1', name: 'Almaz Tekle', channel: 'Web Chat', snippet: 'Do you offer custom mesh tints?', messages: [
        { sender: 'user', text: 'Hi, I need details on bulk seat pricing packages.', time: '11:00 AM' },
        { sender: 'ai', text: 'Welcome to Zema Office Systems! We provide competitive corporate tiers starting from 10 ergonomic desk units.', time: '11:05 AM' },
        { sender: 'user', text: 'Do you offer custom mesh tints?', time: '11:12 AM' }
      ]}
    ]
  },
  abyssinia: {
    id: 'abyssinia',
    name: 'Abyssinia Premium Hospitality',
    logo: '🏰',
    location: 'Hawassa Development Zone',
    contact: '0944112233',
    currency: 'ETB',
    stats: { totalLeads: 84, activeCustomers: 64, pendingQuotes: 22, revenue: 8150000, convRate: 18.5, dealsWon: 44 },
    catalog: [
      { id: 'a1', name: 'Suite Mahogany Imperial Bed', price: 230000, material: 'Solid Mahogany Wood', category: 'Hotel Suite', stock: 6, performance: 'Exclusive', desc: 'Bespoke grand layout tailored for national park resort bedrooms.' },
      { id: 'a2', name: 'Resort Lounge Wicker Chair', price: 42000, material: 'Bamboo & Canvas', category: 'Lobby Lounge', stock: 24, performance: 'Trending', desc: 'Eye-catching organic patio and outdoor poolside comfortable armchairs.' }
    ],
    leads: [
      { id: 'al1', name: 'Yohannes Kasahun', phone: '+251 94 411 2233', stage: 'Negotiation', source: 'WhatsApp', status: 'Active', value: 920000, email: 'ykasahun@lakesideresorts.com', location: 'Hawassa', date: '2026-06-17', score: 94, message: 'Structuring full lounge and bedroom furniture sets for 15 lake chalets.' }
    ],
    conversations: [
      { id: 'ac1', name: 'Yohannes Kasahun', channel: 'WhatsApp', snippet: 'We require fire-retardant spec reports.', messages: [
        { sender: 'user', text: 'Do cushions include flame resistive protection?', time: '3 Days Ago' },
        { sender: 'ai', text: 'Greeting Abyssinia resort partners! Yes, our upholstery foam includes certified flame resist and wool lining.', time: '3 Days Ago' },
        { sender: 'user', text: 'We require fire-retardant spec reports.', time: 'Yesterday' }
      ]}
    ]
  }
};

export default function CustomerDashboard() {
  // Global & Theme State
  const [darkMode, setDarkMode] = useState(false);
  const [currentTenantKey, setCurrentTenantKey] = useState('bekansi');
  const [currentPage, setCurrentPage] = useState('dashboard'); // login, dashboard, leads, customers, conversations, catalog, quotations, analytics, ai_panel, settings
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [copilotPanelOpen, setCopilotPanelOpen] = useState(true);

  // Tenant Isolated State (Local database simulator)
  const [tenantDatabase, setTenantDatabase] = useState(SEED_TENANTS);
  const tenant = useMemo(() => tenantDatabase[currentTenantKey], [tenantDatabase, currentTenantKey]);

  // Auth Context
  const [userSession, setUserSession] = useState({ isAuthenticated: true, username: 'aabduselamaliyi@gmail.com', level: 'Senior Principal Sales Director' });

  // Leads & Pipeline Filters
  const [leadSearch, setLeadSearch] = useState('');
  const [leadStageFilter, setLeadStageFilter] = useState('all');
  const [leadSourceFilter, setLeadSourceFilter] = useState('all');
  const [selectedLead, setSelectedLead] = useState(null);

  // Quick Lead Capture state
  const [showQuickLead, setShowQuickLead] = useState(false);
  const [newLeadForm, setNewLeadForm] = useState({ name: '', phone: '', email: '', value: '', stage: 'New', source: 'WhatsApp', message: '' });

  // Conversations State
  const [activeConvId, setActiveConvId] = useState('c1');
  const [chatInputText, setChatInputText] = useState('');
  const [isAiTyping, setIsAiTyping] = useState(false);
  const [suggestedReplies, setSuggestedReplies] = useState([
    "To customize dimensions, we require official measurements. Shall we schedule a visit?",
    "Our current promotional timber pricing is valid for 10 days. Would you like a quote?",
    "We offer 3 years structural warranty on all living room items. Do you need further terms?"
  ]);

  // Quotation Builder State
  const [quoteClient, setQuoteClient] = useState('Abdi Biya');
  const [quotePhone, setQuotePhone] = useState('+251 98 882 8861');
  const [quoteLocation, setQuoteLocation] = useState('Bishoftu');
  const [selectedProductIdForQuote, setSelectedProductIdForQuote] = useState('');
  const [quoteQuantity, setQuoteQuantity] = useState(1);
  const [quoteCustomMaterial, setQuoteCustomMaterial] = useState('');
  const [quoteItems, setQuoteItems] = useState([]);
  const [quoteDiscount, setQuoteDiscount] = useState(5); // 5% discount default
  const [quoteStatus, setQuoteStatus] = useState('Pending');

  // Unified global banner notifications
  const [alertBanner, setAlertBanner] = useState({ show: true, text: 'Active ERP Cloud node: Bishoftu. Bekansi AI engine fully authorized.' });

  // Interactive Persistent Copilot panel state
  const [copilotPrompt, setCopilotPrompt] = useState('Analyze lead profile Aster Tolossa and predict conversion time.');
  const [copilotResponse, setCopilotResponse] = useState('Hello! I am your Bekansi AI Copilot. Select an entity or type a command above to analyze CRM actions.');
  const [copilotLoading, setCopilotLoading] = useState(false);

  // Settings State
  const [usdExchangeRate, setUsdExchangeRate] = useState(118.4);
  const [billingPlan, setBillingPlan] = useState('Enterprise Growth Plus');

  // Trigger simulated Copilot intelligence
  const handleCopilotCommand = (cmdType, param = '') => {
    setCopilotLoading(true);
    setCopilotResponse('');
    setTimeout(() => {
      setCopilotLoading(false);
      switch(cmdType) {
        case 'REPLY':
          setCopilotResponse(`✨ [AI Suggested Response Draft]
Hello! Thank you for choosing Bekansi Furniture. Regarding the custom requirements, our engineering workshop can calibrate dimensions perfectly matching your room scale. The typical production lead time is 14 business days. Shall we prepare the formal quotation?`);
          break;
        case 'CONV_PREDICT':
          setCopilotResponse(`🔮 [AI Predictive Conversion Score]
• Lead Evaluated: ${param || 'Active Lead'}
• Win Probability: 94.2% (Very High - "Hot Lead")
• Conversion Forecast: Under 4 days.
• Risk Factor: Minor delay in structural drawing dispatch.
• Strategic Move: Push customized timber specs immediately to capture current wood species discount.`);
          break;
        case 'QUOTE_GENERATE':
          setCopilotResponse(`📄 [AI Automated Proposal Outline]
• Recipient: ${quoteClient || 'Valued Client'}
• Recommended Style Accent: Deluxe Gara Series in Wanza Hardwood
• Calculated Value: ~141,500 ETB
• ROI Projection: Premium durability eliminates depreciation for 10+ years. High-density resilience foam minimizes fabric fatigue.`);
          break;
        case 'SUMMARIZE':
          setCopilotResponse(`📝 [AI Conversation Synthesis]
Customer represents hotel expansion or luxury villa. Urgently desires fire-retardant wool rating specifications and logistics outline with flat-fee Bishoftu to Addis transport rates.`);
          break;
        case 'MARKETING_ADS':
          setCopilotResponse(`📣 [High-Impact Instagram Draft]
Headline: Redefining Luxury in Ethiopian Timber 🪵✨
Message: Handcrafted. Heartfelt. From our Bishoftu workshops to your dream sanctuary. Experience Wanza, Mahogany, and Acacia with Bekansi AI design optimizations!
Hashtags: #BekansiFurniture #BishoftuCraftsmanship #EthiopianDesign #ModernSaaS`);
          break;
        case 'ANALYZE_LEAD':
          setCopilotResponse(`🔍 [AI Lead Diagnostic Reports]
• Lead Name: ${param}
• Score: 92/100
• Needs: Custom adjustments, warranty guarantees
• Segment: Platinum Luxury Consumer`);
          break;
        default:
          setCopilotResponse(`✓ AI Analysis completed. Multi-Tenant database isolation verified. No critical risks detected.`);
      }
    }, 1100);
  };

  // Add items inside interactive invoice builder
  const handleAddProductToQuote = () => {
    const itemSelected = tenant.catalog.find(p => p.id === selectedProductIdForQuote);
    if (!itemSelected) return;

    const matchedItem = {
      id: itemSelected.id + '-' + Math.floor(Math.random() * 999),
      name: itemSelected.name,
      material: quoteCustomMaterial || itemSelected.material,
      quantity: quoteQuantity,
      unitPrice: itemSelected.price,
      subtotal: itemSelected.price * quoteQuantity
    };

    setQuoteItems([...quoteItems, matchedItem]);
    setSelectedProductIdForQuote('');
    setQuoteQuantity(1);
    setQuoteCustomMaterial('');
  };

  const handleRemoveQuoteItem = (itemId) => {
    setQuoteItems(quoteItems.filter(item => item.id !== itemId));
  };

  // Safe Invoice Math calculations
  const quoteCalculations = useMemo(() => {
    const subtotal = quoteItems.reduce((sum, item) => sum + item.subtotal, 0);
    const discountVal = (subtotal * quoteDiscount) / 100;
    const computedVat = (subtotal - discountVal) * 0.15; // 15% VAT
    const physicalDeliveryFee = subtotal > 0 ? 5000 : 0; // Flat fee
    const grandTotal = subtotal - discountVal + computedVat + physicalDeliveryFee;

    return { subtotal, discountVal, computedVat, physicalDeliveryFee, grandTotal };
  }, [quoteItems, quoteDiscount]);

  // Lead stages representation for Kanban pipeline
  const LEAD_STAGES = ['New', 'Qualified', 'Proposal', 'Negotiation', 'Won', 'Lost'];

  // Handle lead capture submission
  const handleCreateLead = (e) => {
    e.preventDefault();
    if (!newLeadForm.name || !newLeadForm.phone) {
      alert("Please fill name and contact phone.");
      return;
    }

    const createdLead = {
      id: 'l-new-' + Math.floor(Math.random() * 9999),
      name: newLeadForm.name,
      phone: newLeadForm.phone,
      email: newLeadForm.email || 'crm@bekansi.com',
      value: parseFloat(newLeadForm.value) || 85000,
      stage: newLeadForm.stage,
      source: newLeadForm.source,
      status: 'Active',
      location: 'Addis Ababa',
      date: new Date().toISOString().substring(0, 10),
      score: Math.floor(55 + Math.random() * 40),
      message: newLeadForm.message || 'Custom room scale consultation.'
    };

    setTenantDatabase(prev => {
      const activeTenant = prev[currentTenantKey];
      // Update totals
      const updatedTotal = activeTenant.stats.totalLeads + 1;
      const updatedRevenue = activeTenant.stats.revenue + createdLead.value;
      return {
        ...prev,
        [currentTenantKey]: {
          ...activeTenant,
          leads: [createdLead, ...activeTenant.leads],
          stats: { ...activeTenant.stats, totalLeads: updatedTotal, revenue: updatedRevenue }
        }
      };
    });

    setNewLeadForm({ name: '', phone: '', email: '', value: '', stage: 'New', source: 'WhatsApp', message: '' });
    setShowQuickLead(false);
    setAlertBanner({ show: true, text: `Successfully generated Lead tracking record for ${createdLead.name}!` });
  };

  // Handle conversion of stage inside UI
  const handleUpdateLeadStage = (leadId, nextStage) => {
    setTenantDatabase(prev => {
      const activeTenant = prev[currentTenantKey];
      const updatedLeads = activeTenant.leads.map(ld => {
        if (ld.id === leadId) {
          return { ...ld, stage: nextStage };
        }
        return ld;
      });
      return {
        ...prev,
        [currentTenantKey]: {
          ...activeTenant,
          leads: updatedLeads
        }
      };
    });
  };

  // Messaging interactive thread submission
  const handleSendChatMessage = () => {
    if (!chatInputText.trim()) return;

    const userMessage = {
      sender: 'user',
      text: chatInputText,
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    };

    setTenantDatabase(prev => {
      const activeTenant = prev[currentTenantKey];
      const updatedConvs = activeTenant.conversations.map(conv => {
        if (conv.id === activeConvId) {
          return {
            ...conv,
            snippet: userMessage.text,
            messages: [...conv.messages, userMessage]
          };
        }
        return conv;
      });
      return { ...prev, [currentTenantKey]: { ...activeTenant, conversations: updatedConvs } };
    });

    setChatInputText('');
    setIsAiTyping(true);

    setTimeout(() => {
      setIsAiTyping(false);
      const automatedResponse = {
        sender: 'ai',
        text: `[Bekansi Assistant] Understood. Our CRM has logged this request. Let me check custom timber availability or schedule a sketch mockup.`,
        time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      };
      setTenantDatabase(prev => {
        const activeTenant = prev[currentTenantKey];
        const updatedConvs = activeTenant.conversations.map(conv => {
          if (conv.id === activeConvId) {
            return {
              ...conv,
              messages: [...conv.messages, automatedResponse]
            };
          }
          return conv;
        });
        return { ...prev, [currentTenantKey]: { ...activeTenant, conversations: updatedConvs } };
      });
    }, 1500);
  };

  // Filter computation for Leads screen
  const filteredLeads = useMemo(() => {
    return tenant.leads.filter(item => {
      const matchesSearch = item.name.toLowerCase().includes(leadSearch.toLowerCase()) || 
                            item.phone.includes(leadSearch) || 
                            item.location.toLowerCase().includes(leadSearch.toLowerCase());
      const matchesStage = leadStageFilter === 'all' || item.stage === leadStageFilter;
      const matchesSource = leadSourceFilter === 'all' || item.source === leadSourceFilter;
      return matchesSearch && matchesStage && matchesSource;
    });
  }, [tenant.leads, leadSearch, leadStageFilter, leadSourceFilter]);

  // Selected Lead state detail
  const activeSelectedLead = useMemo(() => {
    return tenant.leads.find(l => l.id === selectedLead) || null;
  }, [tenant.leads, selectedLead]);

  return (
    <div className={`min-h-screen flex flex-col font-sans transition-colors duration-300 ${darkMode ? 'bg-[#0f172a] text-slate-100' : 'bg-[#f8fafc] text-[#1e293b]'}`}>
      
      {/* ENTERPRISE NOTIFICATION BANNER */}
      {alertBanner.show && (
        <div className="bg-blue-600 text-white px-4 py-2 text-xs font-bold flex items-center justify-between shadow-md transition-all">
          <div className="flex items-center gap-2">
            <Sparkles className="w-4 h-4 text-amber-300 animate-bounce" />
            <span>{alertBanner.text}</span>
          </div>
          <button onClick={() => setAlertBanner({ show: false, text: '' })} className="hover:opacity-80">
            <X className="w-4 h-4" />
          </button>
        </div>
      )}

      {/* RENDER LOGIN IF INTERNAL ACCESS LEVEL REJECTED (SANDBOX SIMULATION) */}
      {currentPage === 'login' && (
        <div className="flex-1 flex items-center justify-center p-6 bg-slate-900/5 min-h-[90vh]">
          <div className={`p-8 md:p-10 rounded-3xl shadow-2xl max-w-md w-full border ${darkMode ? 'bg-slate-900 border-slate-800' : 'bg-white border-slate-100'}`}>
            <div className="text-center mb-8">
              <span className="text-5xl">🛋️</span>
              <h2 className="text-3xl font-black tracking-tight mt-3">Bekansi AI</h2>
              <p className="text-xs text-slate-400 mt-1 uppercase tracking-wider">Enterprise CRM & Sales Assistant Portal</p>
            </div>
            <form onSubmit={(e) => { e.preventDefault(); setCurrentPage('dashboard'); }} className="space-y-4">
              <div>
                <label className="text-[11px] font-extrabold text-slate-400 block mb-1 uppercase">Cloud Username</label>
                <input type="text" value={userSession.username} disabled className="w-full bg-slate-200/50 border border-slate-300 rounded-xl p-3 text-sm text-slate-600 cursor-not-allowed font-medium" />
              </div>
              <div>
                <label className="text-[11px] font-extrabold text-slate-400 block mb-1 uppercase">Security Token</label>
                <input type="password" value="••••••••••••••" disabled className="w-full bg-slate-200/50 border border-slate-300 rounded-xl p-3 text-sm text-slate-600 cursor-not-allowed" />
              </div>
              <button type="submit" className="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold p-3.5 rounded-xl text-sm transition-all shadow-lg shadow-blue-500/20 flex items-center justify-center gap-2">
                <Lock className="w-4 h-4" /> Authenticate CRM Session
              </button>
            </form>
          </div>
        </div>
      )}

      {/* MAIN WORKSPACE WRAPPER */}
      {currentPage !== 'login' && (
        <div className="flex-1 flex flex-col md:flex-row">
          
          {/* LEFT ENTERPRISE SIDEBAR CONTAINER */}
          <aside className={`w-full md:w-64 flex flex-col border-r shrink-0 transition-colors ${darkMode ? 'bg-[#111c37] border-slate-800' : 'bg-[#0f172a] border-slate-800 text-slate-300'}`}>
            
            {/* Multi-Tenant Studio Switcher */}
            <div className="p-4 border-b border-slate-800/80">
              <label className="text-[10px] font-extrabold text-gray-400 block mb-1 uppercase tracking-[0.1em]">SaaS Business Domain</label>
              <div className="relative">
                <select 
                  value={currentTenantKey}
                  onChange={(e) => {
                    setCurrentTenantKey(e.target.value);
                    setAlertBanner({ show: true, text: `Active isolated server switched to: ${SEED_TENANTS[e.target.value].name}` });
                  }}
                  className="w-full bg-slate-900 border border-gray-800 rounded-xl p-2 px-3 text-xs text-blue-400 font-bold focus:ring-1 focus:ring-blue-500 cursor-pointer appearance-none outline-none"
                >
                  <option value="bekansi">🏠 Bekansi Furniture & Interior</option>
                  <option value="zema">🏢 Zema Office Systems</option>
                  <option value="abyssinia">🏰 Abyssinia Premium Hotel</option>
                </select>
                <ChevronDown className="w-3.5 h-3.5 absolute right-3 top-3 pointer-events-none text-slate-400" />
              </div>
            </div>

            {/* Sidebar Navigation */}
            <div className="flex-1 p-3 space-y-1 overflow-y-auto">
              <span className="text-[10px] font-extrabold text-gray-500 block mb-2 px-2 uppercase tracking-widest">SaaS Hub</span>
              
              <button onClick={() => { setCurrentPage('dashboard'); setMobileMenuOpen(false); }} className={`w-full text-left px-3 py-2.5 rounded-xl text-xs font-bold transition-all flex items-center justify-between ${currentPage === 'dashboard' ? 'bg-blue-600 text-white shadow-md' : 'hover:bg-slate-800 hover:text-white text-gray-400'}`}>
                <span className="flex items-center gap-2.5">
                  <LayoutDashboard className="w-4 h-4" /> Dashboard
                </span>
                <ChevronRight className="w-3 h-3 opacity-60" />
              </button>

              <button onClick={() => { setCurrentPage('leads'); setMobileMenuOpen(false); }} className={`w-full text-left px-3 py-2.5 rounded-xl text-xs font-bold transition-all flex items-center justify-between ${currentPage === 'leads' ? 'bg-blue-600 text-white shadow-md' : 'hover:bg-slate-800 hover:text-white text-gray-400'}`}>
                <span className="flex items-center gap-2.5">
                  <Briefcase className="w-4 h-4" /> CRM Leads
                </span>
                <span className="bg-slate-800/80 text-[10px] px-1.5 py-0.5 rounded text-blue-400">{tenant.leads.length}</span>
              </button>

              <button onClick={() => { setCurrentPage('customers'); setMobileMenuOpen(false); }} className={`w-full text-left px-3 py-2.5 rounded-xl text-xs font-bold transition-all flex items-center justify-between ${currentPage === 'customers' ? 'bg-blue-600 text-white shadow-md' : 'hover:bg-slate-800 hover:text-white text-gray-400'}`}>
                <span className="flex items-center gap-2.5">
                  <Users className="w-4 h-4" /> Customers CRM
                </span>
                <span className="bg-slate-800/50 text-[10px] px-2 py-0.5 rounded text-emerald-400">Hub</span>
              </button>

              <button onClick={() => { setCurrentPage('conversations'); setMobileMenuOpen(false); }} className={`w-full text-left px-3 py-2.5 rounded-xl text-xs font-bold transition-all flex items-center justify-between ${currentPage === 'conversations' ? 'bg-blue-600 text-white shadow-md' : 'hover:bg-slate-800 hover:text-white text-gray-400'}`}>
                <span className="flex items-center gap-2.5">
                  <MessageSquare className="w-4 h-4" /> Omnichannel Chat
                </span>
                <span className="bg-blue-500/20 text-blue-400 border border-blue-500/30 text-[9px] font-black px-1.5 py-0.5 rounded-full">SMART</span>
              </button>

              <button onClick={() => { setCurrentPage('quotations'); setMobileMenuOpen(false); }} className={`w-full text-left px-3 py-2.5 rounded-xl text-xs font-bold transition-all flex items-center justify-between ${currentPage === 'quotations' ? 'bg-blue-600 text-white shadow-md' : 'hover:bg-slate-800 hover:text-white text-gray-400'}`}>
                <span className="flex items-center gap-2.5">
                  <FileText className="w-4 h-4" /> Quotations Build
                </span>
                {quoteItems.length > 0 && <span className="bg-amber-500 text-slate-900 border text-[9px] font-black px-1.5 rounded-full">{quoteItems.length}</span>}
              </button>

              <button onClick={() => { setCurrentPage('catalog'); setMobileMenuOpen(false); }} className={`w-full text-left px-3 py-2.5 rounded-xl text-xs font-bold transition-all flex items-center justify-between ${currentPage === 'catalog' ? 'bg-blue-600 text-white shadow-md' : 'hover:bg-slate-800 hover:text-white text-gray-400'}`}>
                <span className="flex items-center gap-2.5">
                  <Layers className="w-4 h-4" /> Products Catalog
                </span>
                <ChevronRight className="w-3 h-3 opacity-60" />
              </button>

              <button onClick={() => { setCurrentPage('analytics'); setMobileMenuOpen(false); }} className={`w-full text-left px-3 py-2.5 rounded-xl text-xs font-bold transition-all flex items-center justify-between ${currentPage === 'analytics' ? 'bg-blue-600 text-white shadow-md' : 'hover:bg-slate-800 hover:text-white text-gray-400'}`}>
                <span className="flex items-center gap-2.5">
                  <TrendingUp className="w-4 h-4" /> Executive Analytics
                </span>
                <ChevronRight className="w-3 h-3 opacity-60" />
              </button>

              <button onClick={() => { setCurrentPage('ai_panel'); setMobileMenuOpen(false); }} className={`w-full text-left px-3 py-2.5 rounded-xl text-xs font-bold transition-all flex items-center justify-between ${currentPage === 'ai_panel' ? 'bg-blue-600 text-white shadow-md' : 'hover:bg-slate-800 hover:text-white text-gray-400'}`}>
                <span className="flex items-center gap-2.5 text-amber-400 font-extrabold animate-pulse">
                  <Sparkles className="w-4 h-4 text-amber-400" /> AI Assistant Desk
                </span>
                <ChevronRight className="w-3 h-3 opacity-60" />
              </button>

              <span className="text-[10px] font-extrabold text-gray-500 block pt-4 mb-2 px-2 uppercase tracking-widest">Settings</span>

              <button onClick={() => { setCurrentPage('settings'); setMobileMenuOpen(false); }} className={`w-full text-left px-3 py-2.5 rounded-xl text-xs font-bold transition-all flex items-center gap-2.5 ${currentPage === 'settings' ? 'bg-blue-600 text-white shadow-md' : 'hover:bg-slate-800 hover:text-white text-gray-400'}`}>
                <Settings className="w-4 h-4" /> System Preferences
              </button>

              <button onClick={() => setCurrentPage('login')} className="w-full text-left px-3 py-2.5 rounded-xl text-xs font-bold hover:bg-red-950/40 hover:text-red-400 text-gray-500 transition-all flex items-center gap-2.5">
                <X className="w-4 h-4" /> Log Out Session
              </button>
            </div>

            {/* Sidebar Corporate Footer */}
            <div className="p-4 border-t border-slate-800 text-[10px] text-slate-500 space-y-1 bg-slate-950/40">
              <p className="font-extrabold tracking-wider text-slate-400">BEKANSI AI PLATFORM</p>
              <p className="font-semibold text-xs text-blue-500">{tenant.location}</p>
              <p>© 2026 Bekansi Industries Ltd.</p>
            </div>
          </aside>

          {/* MAIN PAGE WORKSPACE CONTAINER */}
          <main className="flex-1 flex flex-col min-w-0">
            
            {/* TOP BAR BRANDING & CORE TOOLS */}
            <header className={`border-b p-4 sticky top-0 z-10 flex items-center justify-between shadow-sm transition-colors ${darkMode ? 'bg-slate-900 border-slate-800' : 'bg-white border-slate-100'}`}>
              <div className="flex items-center gap-3">
                {/* Mobile Menu Action */}
                <button onClick={() => setMobileMenuOpen(!mobileMenuOpen)} className="md:hidden p-2 text-slate-600 border rounded-lg">
                  <Menu className="w-5 h-5" />
                </button>
                <div className="flex items-center gap-2">
                  <span className="text-2xl">{tenant.logo}</span>
                  <div>
                    <h1 className="text-sm font-black tracking-tight leading-none uppercase">{tenant.name}</h1>
                    <p className="text-[11px] text-slate-400 font-bold mt-0.5 flex items-center gap-1">
                      <Globe className="w-3 h-3 text-blue-500" /> Authorized Multi-Tenant Cloud Cluster
                    </p>
                  </div>
                </div>
              </div>

              {/* Global Quick Action Tool Links */}
              <div className="flex items-center gap-3">
                
                {/* Theme Mode Toggle Button */}
                <button 
                  onClick={() => setDarkMode(!darkMode)} 
                  className={`p-2.5 rounded-xl border transition-colors ${darkMode ? 'bg-slate-800 border-slate-700 text-amber-400' : 'bg-slate-50 border-slate-250 text-slate-600 hover:bg-slate-100'}`}
                  title="Toggle Light or Dark Mode Schema"
                >
                  {darkMode ? <Sun className="w-4 h-4" /> : <Moon className="w-4 h-4" />}
                </button>

                {/* Copilot Sidebar Toggle Link */}
                <button 
                  onClick={() => setCopilotPanelOpen(!copilotPanelOpen)} 
                  className={`p-2 px-3 text-xs font-bold rounded-xl border flex items-center gap-2 transition-all ${copilotPanelOpen ? 'bg-blue-600 text-white border-blue-500 shadow-md' : 'bg-blue-50 text-blue-600 border-blue-100 hover:bg-blue-100'}`}
                >
                  <Sparkles className="w-3.5 h-3.5" />
                  <span className="hidden sm:inline">AI Copilot</span>
                </button>

                {/* New Lead Fast Action Drawer */}
                <button 
                  onClick={() => setShowQuickLead(true)}
                  className="bg-emerald-600 hover:bg-emerald-700 text-white font-extrabold text-xs p-2.5 px-3 rounded-xl flex items-center gap-1.5 transition-all shadow-md"
                >
                  <Plus className="w-3.5 h-3.5" />
                  <span className="hidden sm:inline">Quick Lead</span>
                </button>

                {/* Active Session Identifier info */}
                <div className="hidden lg:flex items-center gap-2 border-l border-slate-200/80 pl-4 text-right">
                  <div>
                    <p className="text-[11px] font-black">{userSession.username}</p>
                    <p className="text-[9px] text-slate-400 font-extrabold uppercase tracking-wider">{userSession.level}</p>
                  </div>
                  <div className="w-8 h-8 rounded-full bg-blue-100 border border-blue-200 text-blue-600 font-extrabold text-xs flex items-center justify-center">
                    AA
                  </div>
                </div>

              </div>
            </header>

            {/* CENTRAL INTERACTIVE SECTION (SAAS CONTENT WORKSPACE) */}
            <div className="flex-1 flex flex-col lg:flex-row relative">
              
              {/* PAGE MAIN CORE WORK STATION */}
              <div className="flex-1 p-4 md:p-6 lg:p-8 space-y-6 overflow-y-auto max-w-[1200px] w-full mx-auto">
                
                {/* 1. DASHBOARD OVERVIEW RENDER */}
                {currentPage === 'dashboard' && (
                  <div className="space-y-6">
                    <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                      <div>
                        <h2 className="text-2xl font-black tracking-tight">Executive CRM Intelligence Studio</h2>
                        <p className="text-slate-400 text-xs">Real-time indicators, AI demand signals, and localized conversion funnels.</p>
                      </div>
                      <div className="flex items-center gap-2 text-xs font-bold text-slate-400 bg-slate-150/10 p-2 rounded-xl border border-slate-200/10">
                        <Calendar className="w-4 h-4 text-blue-500" />
                        <span>Fiscal Period: 2026 Q2</span>
                      </div>
                    </div>

                    {/* METRICS STATS KPI CARDS */}
                    <div className="grid grid-cols-2 lg:grid-cols-6 gap-4">
                      
                      <div className={`p-4 rounded-2xl border transition-all ${darkMode ? 'bg-slate-900 border-slate-800' : 'bg-white border-slate-100 shadow-sm'} flex flex-col justify-between`}>
                        <div className="flex items-center justify-between mb-2">
                          <span className="text-[10px] font-black text-slate-400 uppercase tracking-wider">Total Leads</span>
                          <span className="p-1 rounded-lg bg-blue-100 text-blue-600"><Users className="w-3.5 h-3.5" /></span>
                        </div>
                        <h3 className="text-2xl font-black">{tenant.stats.totalLeads}</h3>
                        <p className="text-[10px] text-emerald-500 font-bold mt-1 flex items-center gap-0.5">
                          <ArrowUp className="w-3 h-3" /> +12.4% vs prev week
                        </p>
                      </div>

                      <div className={`p-4 rounded-2xl border transition-all ${darkMode ? 'bg-slate-900 border-slate-800' : 'bg-white border-slate-100 shadow-sm'} flex flex-col justify-between`}>
                        <div className="flex items-center justify-between mb-2">
                          <span className="text-[10px] font-black text-slate-400 uppercase tracking-wider">Active Customers</span>
                          <span className="p-1 rounded-lg bg-emerald-100 text-emerald-600"><ShieldCheck className="w-3.5 h-3.5" /></span>
                        </div>
                        <h3 className="text-2xl font-black">{tenant.stats.activeCustomers}</h3>
                        <p className="text-[10px] text-emerald-500 font-bold mt-1 flex items-center gap-0.5">
                          <ArrowUp className="w-3 h-3" /> +8.2% conversion
                        </p>
                      </div>

                      <div className={`p-4 rounded-2xl border transition-all ${darkMode ? 'bg-slate-900 border-slate-800' : 'bg-white border-slate-100 shadow-sm'} flex flex-col justify-between`}>
                        <div className="flex items-center justify-between mb-2">
                          <span className="text-[10px] font-black text-slate-400 uppercase tracking-wider">Revenue Stream</span>
                          <span className="p-1 rounded-lg bg-blue-100 text-blue-600 font-black text-xs">ETB</span>
                        </div>
                        <h3 className="text-2xl font-black font-mono">{(tenant.stats.revenue).toLocaleString()}</h3>
                        <p className="text-[10px] text-blue-400 font-bold mt-1">
                          ≈ ${(tenant.stats.revenue / usdExchangeRate).toLocaleString(undefined, {maximumFractionDigits:0})} USD
                        </p>
                      </div>

                      <div className={`p-4 rounded-2xl border transition-all ${darkMode ? 'bg-slate-900 border-slate-800' : 'bg-white border-slate-100 shadow-sm'} flex flex-col justify-between`}>
                        <div className="flex items-center justify-between mb-2">
                          <span className="text-[10px] font-black text-slate-400 uppercase tracking-wider">Conv. Rate</span>
                          <span className="p-1 rounded-lg bg-purple-100 text-purple-600"><Percent className="w-3.5 h-3.5" /></span>
                        </div>
                        <h3 className="text-2xl font-black">{tenant.stats.convRate}%</h3>
                        <p className="text-[10px] text-emerald-400 font-bold mt-1">
                          Industry Avg: 9.8%
                        </p>
                      </div>

                      <div className={`p-4 rounded-2xl border transition-all ${darkMode ? 'bg-slate-900 border-slate-800' : 'bg-white border-slate-100 shadow-sm'} flex flex-col justify-between`}>
                        <div className="flex items-center justify-between mb-2">
                          <span className="text-[10px] font-black text-slate-400 uppercase tracking-wider">Pending Quotes</span>
                          <span className="p-1 rounded-lg bg-amber-100 text-amber-600"><FileText className="w-3.5 h-3.5" /></span>
                        </div>
                        <h3 className="text-2xl font-black">{tenant.stats.pendingQuotes}</h3>
                        <p className="text-[10px] text-amber-500 font-extrabold mt-1">
                          Auto Match Ready
                        </p>
                      </div>

                      <div className={`p-4 rounded-2xl border transition-all ${darkMode ? 'bg-slate-900 border-slate-800' : 'bg-white border-slate-100 shadow-sm'} flex flex-col justify-between`}>
                        <div className="flex items-center justify-between mb-2">
                          <span className="text-[10px] font-black text-slate-400 uppercase tracking-wider">Deals Won</span>
                          <span className="p-1 rounded-lg bg-teal-100 text-teal-600"><CheckCircle2 className="w-3.5 h-3.5" /></span>
                        </div>
                        <h3 className="text-2xl font-black">{tenant.stats.dealsWon} Won</h3>
                        <p className="text-[10px] text-emerald-500 font-bold mt-1">
                          Target achieved 94%
                        </p>
                      </div>

                    </div>

                    {/* AI COGNITIVE INSIGHT CARDS */}
                    <div className="bg-gradient-to-r from-blue-900/40 to-indigo-900/40 border border-blue-500/30 p-5 rounded-2xl">
                      <div className="flex items-center gap-2 mb-3">
                        <Sparkles className="w-5 h-5 text-amber-400 animate-spin" />
                        <h4 className="text-sm font-black tracking-wide uppercase text-blue-300">Prescriptive CRM AI-Insights Engine</h4>
                      </div>
                      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                        
                        <div className="bg-slate-950/60 p-3.5 rounded-xl border border-blue-500/10">
                          <div className="flex items-center justify-between text-xs font-bold mb-1">
                            <span className="text-amber-400">🔥 Hot Lead Prompt Alert</span>
                            <span className="text-[10px] bg-red-500/20 text-red-400 p-0.5 px-1.5 rounded-full">Urgent</span>
                          </div>
                          <p className="text-slate-100 text-xs font-bold">Abdi Biya has clicked quotation. Win Prob: 94.2%</p>
                          <p className="text-[10px] text-slate-400 mt-1">Action: Send Wanza wood spec sheets and schedule Bishoftu site visits.</p>
                        </div>

                        <div className="bg-slate-950/60 p-3.5 rounded-xl border border-blue-500/10">
                          <div className="flex items-center justify-between text-xs font-bold mb-1">
                            <span className="text-blue-300">🔮 Product Demand Forecast</span>
                            <span className="text-[10px] bg-blue-500/20 text-blue-400 p-0.5 px-1.5 rounded-full">Optimized</span>
                          </div>
                          <p className="text-slate-100 text-xs font-bold">Wanza timber demand rising 28% next month.</p>
                          <p className="text-[10px] text-slate-400 mt-1">Action: Lock pre-dry inventory at Duke subcity storage yard now.</p>
                        </div>

                        <div className="bg-slate-950/60 p-3.5 rounded-xl border border-blue-500/10">
                          <div className="flex items-center justify-between text-xs font-bold mb-1">
                            <span className="text-emerald-400">⚠️ Risk Mitigation Trigger</span>
                            <span className="text-[10px] bg-amber-500/20 text-amber-400 p-0.5 px-1.5 rounded-full">Medium</span>
                          </div>
                          <p className="text-slate-100 text-xs font-bold">Dr. Yonas Admasu: Shipping delay risk.</p>
                          <p className="text-[10px] text-slate-400 mt-1">Action: Assign express flat-bed truck partner route.</p>
                        </div>

                      </div>
                    </div>

                    {/* EXECUTIVES CHARTS DEMAND GRID */}
                    <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
                      
                      {/* CSS-animated Revenue Projection Columns */}
                      <div className={`lg:col-span-8 p-5 rounded-2xl border ${darkMode ? 'bg-slate-900 border-slate-800' : 'bg-white border-slate-100 shadow-sm'}`}>
                        <div className="flex items-center justify-between mb-4">
                          <div>
                            <h4 className="text-sm font-black uppercase">Revenue Trend & Predictive Forecasts (ETB)</h4>
                            <p className="text-xs text-slate-400">Monthly invoice dispatch compared to AI linear trends.</p>
                          </div>
                          <span className="text-xs font-extrabold text-blue-500 uppercase tracking-widest bg-blue-500/10 p-1 px-2.5 rounded-lg">Realtime Cloud</span>
                        </div>
                        
                        {/* Interactive Graph bars */}
                        <div className="h-48 flex items-end justify-between gap-2 pt-6">
                          {[
                            { month: 'Jan', val: 3.2, color: 'bg-blue-300' },
                            { month: 'Feb', val: 3.8, color: 'bg-blue-400' },
                            { month: 'Mar', val: 4.5, color: 'bg-blue-500' },
                            { month: 'Apr', val: 5.1, color: 'bg-indigo-500 animate-pulse' },
                            { month: 'May', val: 5.8, color: 'bg-blue-600' },
                            { month: 'Jun (Now)', val: 6.4, color: 'bg-blue-600', isCurrent: true },
                            { month: 'Jul (AI)', val: 7.2, color: 'bg-indigo-600', isPredicted: true },
                            { month: 'Aug (AI)', val: 8.1, color: 'bg-indigo-700', isPredicted: true },
                          ].map((b, idx) => (
                            <div key={idx} className="flex-1 flex flex-col items-center h-full justify-end group">
                              <div className="text-[9px] font-black mb-1 opacity-0 group-hover:opacity-100 transition-opacity">
                                {b.val}M
                              </div>
                              <div 
                                className={`w-full rounded-t-lg transition-all cursor-pointer duration-500 ${b.color} group-hover:brightness-110`}
                                style={{ height: `${(b.val / 8.5) * 100}%` }}
                              />
                              <span className="text-[10px] font-bold mt-2 text-slate-400 whitespace-nowrap">{b.month}</span>
                            </div>
                          ))}
                        </div>
                      </div>

                      {/* Lead Conversion Funnel and Source Ring */}
                      <div className={`lg:col-span-4 p-5 rounded-2xl border ${darkMode ? 'bg-slate-900 border-slate-800' : 'bg-white border-slate-100 shadow-sm'} flex flex-col justify-between`}>
                        <div>
                          <h4 className="text-sm font-black uppercase mb-4">Traffic & Acquisition Funnel</h4>
                          <div className="space-y-3">
                            {[
                              { label: 'Inbound Inquiries', count: '482', percent: 100, color: 'bg-blue-500' },
                              { label: 'Qualified Opportunities', count: '248', percent: 64, color: 'bg-indigo-500' },
                              { label: 'Quotes / Proposals Sent', count: '112', percent: 38, color: 'bg-purple-500' },
                              { label: 'Closed Won Transactions', count: '86', percent: 18, color: 'bg-emerald-500' }
                            ].map((stage, sIdx) => (
                              <div key={sIdx} className="space-y-1">
                                <div className="flex items-center justify-between text-xs font-bold">
                                  <span className="text-slate-400">{stage.label}</span>
                                  <span>{stage.count} ({stage.percent}%)</span>
                                </div>
                                <div className="w-full h-2 bg-slate-200/20 rounded-full overflow-hidden">
                                  <div className={`h-full ${stage.color}`} style={{ width: `${stage.percent}%` }} />
                                </div>
                              </div>
                            ))}
                          </div>
                        </div>

                        <div className="pt-4 border-t border-slate-800/10 mt-4 flex items-center justify-between text-xs font-bold text-slate-400">
                          <span>Verified conversion index:</span>
                          <span className="text-emerald-500 font-extrabold flex items-center gap-0.5">
                            <TrendingUp className="w-3.5 h-3.5" /> High Performer
                          </span>
                        </div>
                      </div>

                    </div>

                    {/* RECENT SALES & CRM TIMELINE ACTIVITY */}
                    <div className={`p-5 rounded-2xl border ${darkMode ? 'bg-slate-900 border-slate-800' : 'bg-white border-slate-100 shadow-sm'}`}>
                      <h4 className="text-sm font-black uppercase mb-3">Enterprise Live CRM Actions Ledger</h4>
                      <div className="space-y-4">
                        {[
                          { text: 'Quotation verified and shipped to Dr. Yonas Admasu via API Node', partner: 'Dr. Yonas Admasu', val: '245,000 ETB', flag: 'Proposal Sent', time: '12 minutes ago', badge: 'bg-amber-100 text-amber-700' },
                          { text: 'Omnichannel message capture matched for Gara Couch specs width scale', partner: 'Abdi Biya', val: '135,000 ETB', flag: 'Qualified Hot', time: '1 hour ago', badge: 'bg-blue-100 text-blue-700' },
                          { text: 'Closed Won transition processed for Tufted Velvet hospitality catalog package', partner: 'Abyssinia Premium', val: '920,000 ETB', flag: 'Deal Won', time: '3 hours ago', badge: 'bg-emerald-100 text-emerald-700' },
                        ].map((act, aIdx) => (
                          <div key={aIdx} className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 p-3 rounded-xl hover:bg-slate-500/5 border border-transparent hover:border-slate-500/10 transition-all">
                            <div className="flex items-start gap-3">
                              <span className="text-xl">🛠️</span>
                              <div>
                                <p className="text-xs font-extrabold">{act.text}</p>
                                <p className="text-[10px] text-slate-400 font-medium">Logged client session • {act.time} • value: <span className="font-bold text-blue-500">{act.val}</span></p>
                              </div>
                            </div>
                            <span className={`text-[10px] font-black uppercase px-2 py-1 rounded-lg self-start sm:self-center ${act.badge}`}>{act.flag}</span>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>
                )}

                {/* 2. LEADS CRM SYSTEM */}
                {currentPage === 'leads' && (
                  <div className="space-y-6">
                    <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                      <div>
                        <h2 className="text-2xl font-black tracking-tight text-blue-500">Global Sales Leads Pipeline</h2>
                        <p className="text-slate-400 text-xs">Search, score, and transition deal cycles across visual stages.</p>
                      </div>
                      <button onClick={() => setShowQuickLead(true)} className="bg-blue-600 hover:bg-blue-700 text-white font-bold text-xs p-3 rounded-xl flex items-center justify-center gap-1.5 transition-all shadow-md self-start">
                        <Plus className="w-4 h-4" /> Add Cloud Opportunity
                      </button>
                    </div>

                    {/* FILTERS & DRILL DOWNS */}
                    <div className="flex flex-col sm:flex-row items-center gap-3">
                      <div className="relative flex-1 w-full">
                        <input 
                          type="text" 
                          placeholder="Search lead indices via name, location, or communication logs..." 
                          value={leadSearch}
                          onChange={(e) => setLeadSearch(e.target.value)}
                          className={`w-full p-2.5 pl-9 rounded-xl border text-xs font-bold outline-none outline-0 focus:ring-1 focus:ring-blue-500 ${darkMode ? 'bg-slate-900 border-slate-800 text-slate-100' : 'bg-white border-slate-200'}`} 
                        />
                        <Search className="w-4 h-4 absolute left-3 top-3.5 text-slate-400" />
                      </div>
                      
                      <div className="flex items-center gap-2 w-full sm:w-auto">
                        <select 
                          value={leadStageFilter} 
                          onChange={(e) => setLeadStageFilter(e.target.value)}
                          className="p-2.5 rounded-xl border text-xs font-bold bg-[#111c37] text-slate-200 border-slate-800 outline-none"
                        >
                          <option value="all">📊 All Pipelines</option>
                          {LEAD_STAGES.map(st => <option key={st} value={st}>Stage: {st}</option>)}
                        </select>

                        <select 
                          value={leadSourceFilter} 
                          onChange={(e) => setLeadSourceFilter(e.target.value)}
                          className="p-2.5 rounded-xl border text-xs font-bold bg-[#111c37] text-slate-200 border-slate-800 outline-none"
                        >
                          <option value="all">🔌 All Channels</option>
                          <option value="WhatsApp">WhatsApp</option>
                          <option value="Telegram">Telegram</option>
                          <option value="Facebook">Facebook</option>
                          <option value="Web Chat">Web Chat</option>
                        </select>
                      </div>
                    </div>

                    {/* KANBAN PIPE AND HIGH DENSITY CRM LIST */}
                    <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
                      
                      {/* Interactive Kanban Board */}
                      <div className="lg:col-span-8 space-y-4">
                        <h4 className="text-sm font-black uppercase text-slate-400">Pipeline Stages Dealboard</h4>
                        
                        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                          {['New', 'Qualified', 'Proposal', 'Negotiation', 'Won'].map((colStage) => {
                            const stageLeads = filteredLeads.filter(l => l.stage === colStage);
                            return (
                              <div key={colStage} className={`p-4 rounded-2xl border ${darkMode ? 'bg-slate-900/60 border-slate-800' : 'bg-slate-50 border-slate-200/50'} space-y-3`}>
                                <div className="flex items-center justify-between text-xs font-bold pb-2 border-b border-slate-800/10">
                                  <span className="uppercase text-slate-400 tracking-wider font-extrabold">{colStage}</span>
                                  <span className="bg-slate-700/25 p-0.5 px-2 rounded-full text-[10px] text-blue-400">{stageLeads.length}</span>
                                </div>
                                <div className="space-y-2.5 max-h-[350px] overflow-y-auto pr-1">
                                  {stageLeads.length === 0 ? (
                                    <p className="text-[11px] text-slate-400 italic text-center py-4">No active opportunities.</p>
                                  ) : (
                                    stageLeads.map(ld => (
                                      <div 
                                        key={ld.id} 
                                        onClick={() => setSelectedLead(ld.id)}
                                        className={`p-3 rounded-xl border cursor-pointer transition-all hover:-translate-y-0.5 hover:shadow-md ${selectedLead === ld.id ? 'bg-blue-600/10 border-blue-500 shadow-sm' : 'bg-slate-950/40 border-slate-800 hover:border-slate-600'}`}
                                      >
                                        <div className="flex items-center justify-between gap-1 mb-1.5">
                                          <span className="text-xs font-bold truncate text-slate-100">{ld.name}</span>
                                          <span className={`text-[9px] font-black p-0.5 px-1.5 rounded-full ${ld.score >= 80 ? 'bg-emerald-500/10 text-emerald-400' : 'bg-amber-500/10 text-amber-400'}`}>
                                            ⭐ {ld.score}
                                          </span>
                                        </div>
                                        <div className="flex items-center justify-between text-[10px] text-slate-400 font-bold mb-1.5">
                                          <span>{ld.location}</span>
                                          <span className="text-blue-400">{ld.value.toLocaleString()} ETB</span>
                                        </div>
                                        <div className="text-[9px] text-slate-400 bg-slate-950 p-1 px-1.5 rounded border border-slate-800/60 truncate">
                                          {ld.message}
                                        </div>
                                      </div>
                                    ))
                                  )}
                                </div>
                              </div>
                            );
                          })}
                        </div>
                      </div>

                      {/* Opportunity Details Workbench panel */}
                      <div className="lg:col-span-4">
                        <div className={`p-5 rounded-2xl border sticky top-24 ${darkMode ? 'bg-slate-900 border-slate-800' : 'bg-white border-slate-100 shadow-sm'}`}>
                          {activeSelectedLead ? (
                            <div className="space-y-5">
                              <div className="flex items-start justify-between pb-3 border-b border-slate-800/15">
                                <div>
                                  <h3 className="text-base font-black truncate">{activeSelectedLead.name}</h3>
                                  <p className="text-[11px] text-slate-400">{activeSelectedLead.email}</p>
                                </div>
                                <span className="bg-emerald-500/10 text-emerald-400 text-xs font-extrabold p-1 px-2.5 rounded-lg border border-emerald-500/25">
                                  Score: {activeSelectedLead.score}
                                </span>
                              </div>

                              <div className="grid grid-cols-2 gap-3 text-xs">
                                <div>
                                  <span className="text-[10px] uppercase font-bold text-slate-400 block mb-0.5">Contact Phone</span>
                                  <span className="font-extrabold">{activeSelectedLead.phone}</span>
                                </div>
                                <div>
                                  <span className="text-[10px] uppercase font-bold text-slate-400 block mb-0.5">Location Hub</span>
                                  <span className="font-extrabold">{activeSelectedLead.location}</span>
                                </div>
                                <div>
                                  <span className="text-[10px] uppercase font-bold text-slate-400 block mb-0.5">Inquiry Valuation</span>
                                  <span className="font-extrabold text-blue-500">{activeSelectedLead.value.toLocaleString()} ETB</span>
                                </div>
                                <div>
                                  <span className="text-[10px] uppercase font-bold text-slate-400 block mb-0.5">Source Channel</span>
                                  <span className="font-extrabold underline">{activeSelectedLead.source}</span>
                                </div>
                              </div>

                              <div>
                                <span className="text-[10px] uppercase font-bold text-slate-400 block mb-1">Customer Message Logs</span>
                                <p className="text-xs italic bg-slate-950 p-2.5 rounded-xl border border-slate-800 text-slate-300">
                                  "{activeSelectedLead.message}"
                                </p>
                              </div>

                              {/* AI Agent Recommendation block */}
                              <div className="bg-blue-950/60 p-3.5 rounded-xl border border-blue-500/20">
                                <span className="text-[10px] uppercase font-bold text-blue-400 flex items-center gap-1.5 mb-1.5">
                                  <Sparkles className="w-3.5 h-3.5" /> AI Recommended Next Strategy
                                </span>
                                <p className="text-[11px] text-slate-200">
                                  Detected category match. Client needs customized hardwood specs for {activeSelectedLead.location}. Run quote builder generator immediately to commit 15% booking reservation.
                                </p>
                                <div className="flex items-center gap-2 mt-3">
                                  <button 
                                    onClick={() => handleCopilotCommand('CONV_PREDICT', activeSelectedLead.name)}
                                    className="bg-blue-600 hover:bg-blue-700 text-white font-bold text-[10px] p-1.5 px-2.5 rounded-lg transition-all"
                                  >
                                    Run Predictor
                                  </button>
                                  <button 
                                    onClick={() => {
                                      setQuoteClient(activeSelectedLead.name);
                                      setQuotePhone(activeSelectedLead.phone);
                                      setQuoteLocation(activeSelectedLead.location);
                                      setCurrentPage('quotations');
                                    }}
                                    className="bg-slate-800 hover:bg-slate-700 text-slate-300 font-bold text-[10px] p-1.5 px-2.5 rounded-lg border border-slate-700 transition"
                                  >
                                    Build Quote
                                  </button>
                                </div>
                              </div>

                              {/* Transition Stage controller buttons */}
                              <div className="pt-3 border-t border-slate-800/10">
                                <span className="text-[10px] uppercase font-bold text-slate-400 block mb-2">Transition Stage</span>
                                <div className="flex flex-wrap gap-1.5">
                                  {LEAD_STAGES.map(st => (
                                    <button 
                                      key={st}
                                      onClick={() => handleUpdateLeadStage(activeSelectedLead.id, st)}
                                      className={`text-[9px] font-black p-1 px-2 rounded-lg border transition-all ${activeSelectedLead.stage === st ? 'bg-blue-600 text-white border-blue-500' : 'bg-slate-900 border-slate-800 hover:bg-slate-850'}`}
                                    >
                                      {st}
                                    </button>
                                  ))}
                                </div>
                              </div>

                            </div>
                          ) : (
                            <div className="text-center py-12 space-y-3">
                              <span className="text-4xl text-slate-500 block">🔍</span>
                              <p className="text-xs text-slate-400 font-extrabold uppercase">Inspect CRM Opportunity</p>
                              <p className="text-[11px] text-slate-500">Select any card in the pipeline columns layout to audit requirements and activate auto recommendations.</p>
                            </div>
                          )}
                        </div>
                      </div>

                    </div>
                  </div>
                )}

                {/* 3. CUSTOMER DATA HUB */}
                {currentPage === 'customers' && (
                  <div className="space-y-6">
                    <div>
                      <h2 className="text-2xl font-black tracking-tight">Enterprise Client Database</h2>
                      <p className="text-slate-400 text-xs">Analyze lifetime bookings, corporate health index, and proactive cross-selling.</p>
                    </div>

                    <div className={`p-5 rounded-2xl border ${darkMode ? 'bg-slate-900 border-slate-800' : 'bg-white border-slate-100 shadow-sm'}`}>
                      <div className="overflow-x-auto">
                        <table className="w-full text-left border-collapse text-xs">
                          <thead>
                            <tr className="border-b border-slate-800/10 uppercase font-extrabold text-slate-400">
                              <th className="py-3 px-2">Account Name</th>
                              <th className="py-3 px-2">Logistics Hub</th>
                              <th className="py-3 px-2">CLV Bookings</th>
                              <th className="py-3 px-2">Health Index</th>
                              <th className="py-3 px-2">Retention Forecast</th>
                              <th className="py-3 px-2 text-right font-extrabold">AI Recommendations</th>
                            </tr>
                          </thead>
                          <tbody className="divide-y divide-slate-800/10 font-bold">
                            {[
                              { name: 'Abyssinia Lakeside Resorts', location: 'Hawassa Development Zone', val: '8,150,000 ETB', clv: 'High Tier', health: '98%', rent: 'Stable', upsell: 'Pitch Tufted Suite Packages' },
                              { name: 'Zema Regional tech cubicles', location: 'Bole Road, Addis Ababa', val: '3,890,000 ETB', clv: 'Mid Tier', health: '85%', rent: 'Nurture', upsell: 'Offer ErgoMax bulk terms' },
                              { name: 'Dukem Prime Estates', location: 'Dukem industrial sector', val: '1,245,000 ETB', clv: 'Established', health: '91%', rent: 'Growth', upsell: 'Proactive oak sideboard upgrade' },
                              { name: 'Bishoftu Botanical Villas', location: 'Bishoftu Showroom', val: '750,000 ETB', clv: 'Established', health: '94%', rent: 'Stable', upsell: 'Wanza carved frames accent' }
                            ].map((row, rIdx) => (
                              <tr key={rIdx} className="hover:bg-slate-500/5 group transition-colors">
                                <td className="py-4 px-2 font-black text-slate-100 flex items-center gap-2">
                                  <span className="text-base group-hover:scale-110 transition-transform">🏢</span>
                                  <span>{row.name}</span>
                                </td>
                                <td className="py-4 px-2 text-slate-400 font-semibold">{row.location}</td>
                                <td className="py-4 px-2 text-blue-400 font-mono">{row.val}</td>
                                <td className="py-4 px-2">
                                  <span className="p-1 px-2.5 rounded-lg bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 font-mono">{row.health}</span>
                                </td>
                                <td className="py-4 px-2">
                                  <span className="p-1 px-2.5 rounded-lg bg-blue-500/10 text-blue-400 font-black">{row.rent}</span>
                                </td>
                                <td className="py-4 px-2 text-right">
                                  <button 
                                    onClick={() => handleCopilotCommand('CONV_PREDICT', row.name)}
                                    className="bg-slate-800 hover:bg-slate-700 text-slate-200 p-1.5 px-3 rounded-lg border border-slate-700 font-bold transition"
                                  >
                                    💡 {row.upsell}
                                  </button>
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    </div>
                  </div>
                )}

                {/* 4. OMNICHANNEL CHAT SCREEN */}
                {currentPage === 'conversations' && (
                  <div className="space-y-6">
                    <div>
                      <h2 className="text-2xl font-black tracking-tight text-blue-500">Omnichannel dialogue routing hub</h2>
                      <p className="text-slate-400 text-xs">Automate conversations across Telegram, WhatsApp, Facebook, and emails isolated under localized translation clusters.</p>
                    </div>

                    <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 min-h-[500px]">
                      
                      {/* Active Channels List Column */}
                      <div className="lg:col-span-4 space-y-3">
                        <span className="text-[10px] font-black uppercase text-slate-400 block tracking-widest">Active Thread Queues</span>
                        
                        <div className="space-y-2">
                          {tenant.conversations.map(conv => (
                            <div 
                              key={conv.id}
                              onClick={() => setActiveConvId(conv.id)}
                              className={`p-3.5 rounded-2xl border cursor-pointer transition-all ${activeConvId === conv.id ? 'bg-blue-600/15 border-blue-500 shadow-md' : 'bg-slate-900 border-slate-800 hover:border-slate-700'}`}
                            >
                              <div className="flex items-center justify-between mb-1">
                                <span className="font-extrabold text-[13px]">{conv.name}</span>
                                <span className="bg-slate-850 p-1 rounded-lg text-[9px] font-black text-blue-400 flex items-center gap-1">
                                  🛜 {conv.channel}
                                </span>
                              </div>
                              <p className="text-[11px] text-slate-400 font-semibold truncate">"{conv.snippet}"</p>
                            </div>
                          ))}
                        </div>
                      </div>

                      {/* Active Messaging Center Window */}
                      <div className={`lg:col-span-8 p-4 rounded-3xl border flex flex-col justify-between ${darkMode ? 'bg-slate-900 border-slate-800' : 'bg-white border-slate-200 shadow-md'}`}>
                        
                        <div className="pb-3 border-b border-slate-800/10 flex items-center justify-between">
                          <div className="flex items-center gap-2">
                            <span className="w-2.5 h-2.5 bg-emerald-500 rounded-full animate-ping" />
                            <h3 className="font-black text-sm uppercase">{tenant.conversations.find(c => c.id === activeConvId)?.name || 'Direct Channel'}</h3>
                          </div>
                          <span className="text-[10px] font-extrabold uppercase text-slate-400">Secure AES Endpoint Encrypted</span>
                        </div>

                        {/* Dialogue Stream */}
                        <div className="flex-1 py-4 space-y-3 overflow-y-auto max-h-[320px] font-medium text-xs">
                          {tenant.conversations.find(c => c.id === activeConvId)?.messages.map((m, idx) => (
                            <div key={idx} className={`flex ${m.sender === 'user' ? 'justify-start' : 'justify-end'}`}>
                              <div className={`p-3 rounded-2xl max-w-[85%] ${m.sender === 'user' ? 'bg-slate-800 text-slate-100 rounded-tl-none' : 'bg-blue-600 text-white rounded-tr-none shadow-sm'}`}>
                                <p className="leading-relaxed">{m.text}</p>
                                <span className="text-[9px] opacity-70 block text-right mt-1 font-bold">{m.time}</span>
                              </div>
                            </div>
                          ))}

                          {isAiTyping && (
                            <div className="flex justify-end p-2 text-xs italic text-blue-400 font-extrabold animate-pulse">
                              Bekansi AI drafting response...
                            </div>
                          )}
                        </div>

                        {/* Suggested quick replies trigger */}
                        <div className="mb-4">
                          <span className="text-[10px] uppercase font-extrabold text-slate-400 block mb-1.5">proactive suggested answers</span>
                          <div className="flex flex-col sm:flex-row gap-1.5 overflow-x-auto pb-1">
                            {suggestedReplies.map((sr, idx) => (
                              <button 
                                key={idx}
                                onClick={() => setChatInputText(sr)}
                                className="bg-slate-950 hover:bg-slate-850 p-2 text-left rounded-xl border border-gray-805 text-slate-300 transition-all font-semibold max-w-[320px] shrink-0 text-[10px] truncate"
                              >
                                {sr}
                              </button>
                            ))}
                          </div>
                        </div>

                        {/* Interactive Message input drawer */}
                        <div className="flex items-center gap-2 pt-3 border-t border-slate-800/10">
                          <input 
                            type="text" 
                            placeholder="Draft client message Amharic/English..." 
                            value={chatInputText}
                            onChange={(e) => setChatInputText(e.target.value)}
                            onKeyDown={(e) => e.key === 'Enter' && handleSendChatMessage()}
                            className={`flex-1 p-3 rounded-xl border text-xs outline-none focus:ring-1 focus:ring-blue-500 bg-transparent ${darkMode ? 'border-slate-800 text-slate-100' : 'border-slate-205 text-slate-900'}`} 
                          />
                          <button 
                            onClick={handleSendChatMessage}
                            className="bg-blue-600 hover:bg-blue-700 text-white font-extrabold p-3 rounded-xl transition shadow-md shrink-0"
                          >
                            <Send className="w-4 h-4" />
                          </button>
                        </div>

                      </div>

                    </div>
                  </div>
                )}

                {/* 5. DYNAMIC QUOTATION BUILDER ENGINE */}
                {currentPage === 'quotations' && (
                  <div className="space-y-6">
                    <div>
                      <h2 className="text-2xl font-black tracking-tight text-blue-500">Enterprise Quote Customizer Engine</h2>
                      <p className="text-slate-400 text-xs">Configure itemized wood finishes, calculate actual subtotal factoring 15% VAT, and broadcast client proposals instantly.</p>
                    </div>

                    <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
                      
                      {/* Interactive Configuration block */}
                      <div className={`lg:col-span-5 p-5 rounded-2xl border ${darkMode ? 'bg-slate-900 border-slate-800' : 'bg-white border-slate-100 shadow-sm'} space-y-4`}>
                        <h4 className="text-xs font-black uppercase text-slate-400 tracking-wider">Configure item limits</h4>
                        
                        <div className="space-y-3 text-xs font-bold">
                          <div>
                            <label className="text-[10px] text-slate-400 block mb-1 uppercase font-extrabold">Recipient Account Name</label>
                            <input type="text" value={quoteClient} onChange={(e) => setQuoteClient(e.target.value)} className="w-full bg-slate-950 border border-slate-800 rounded-xl p-2.5 text-xs text-slate-100 outline-none" />
                          </div>

                          <div className="grid grid-cols-2 gap-2">
                            <div>
                              <label className="text-[10px] text-slate-400 block mb-1 uppercase font-extrabold">Logistics Code (Phone)</label>
                              <input type="text" value={quotePhone} onChange={(e) => setQuotePhone(e.target.value)} className="w-full bg-slate-950 border border-slate-800 rounded-xl p-2.5 text-xs text-slate-100 outline-none" />
                            </div>
                            <div>
                              <label className="text-[10px] text-slate-400 block mb-1 uppercase font-extrabold">Delivery Subcity</label>
                              <input type="text" value={quoteLocation} onChange={(e) => setQuoteLocation(e.target.value)} className="w-full bg-slate-950 border border-slate-800 rounded-xl p-2.5 text-xs text-slate-100 outline-none" />
                            </div>
                          </div>

                          <div className="border-t border-slate-800/10 pt-3 space-y-3">
                            <div>
                              <label className="text-[10px] text-slate-400 block mb-1 uppercase font-extrabold">Product Selection</label>
                              <select 
                                value={selectedProductIdForQuote}
                                onChange={(e) => setSelectedProductIdForQuote(e.target.value)}
                                className="w-full bg-slate-950 border border-slate-800 rounded-xl p-2.5 text-xs text-blue-400 font-black outline-none"
                              >
                                <option value="">-- Choose item from catalog index --</option>
                                {tenant.catalog.map(prod => (
                                  <option key={prod.id} value={prod.id}>{prod.name} (base price: {prod.price.toLocaleString()} ETB)</option>
                                ))}
                              </select>
                            </div>

                            <div className="grid grid-cols-3 gap-2">
                              <div className="col-span-1">
                                <label className="text-[10px] text-slate-400 block mb-1 uppercase font-extrabold">Quantity</label>
                                <input type="number" min="1" value={quoteQuantity} onChange={(e) => setQuoteQuantity(parseInt(e.target.value, 10) || 1)} className="w-full bg-slate-950 border border-slate-800 rounded-xl p-2.5 text-xs text-slate-100 outline-none font-mono" />
                              </div>
                              <div className="col-span-2">
                                <label className="text-[10px] text-slate-400 block mb-1 uppercase font-extrabold">Timber Accent adjustments</label>
                                <input type="text" placeholder="e.g. Red Mahogany, Oak varnished" value={quoteCustomMaterial} onChange={(e) => setQuoteCustomMaterial(e.target.value)} className="w-full bg-slate-950 border border-slate-800 rounded-xl p-2.5 text-xs text-slate-100 outline-none" />
                              </div>
                            </div>

                            <button 
                              onClick={handleAddProductToQuote}
                              className="w-full bg-blue-600 hover:bg-blue-700 text-white font-extrabold text-xs p-3 rounded-xl transition shadow-md flex items-center justify-center gap-1.5"
                            >
                              <Plus className="w-3.5 h-3.5" /> Append item to Invoice Ledger
                            </button>
                          </div>

                        </div>
                      </div>

                      {/* Invoice sheet panel */}
                      <div className="lg:col-span-7">
                        <div className={`p-5 rounded-2xl border ${darkMode ? 'bg-slate-900 border-slate-800' : 'bg-white border-slate-150 shadow-md'} space-y-4`}>
                          
                          <div className="flex items-center justify-between pb-3 border-b border-dashed border-slate-800/10">
                            <div>
                              <span className="bg-amber-500 text-slate-950 text-[10px] font-black p-1 px-2.5 rounded-lg border border-amber-400">SAAS LIVE ESTIMATES</span>
                              <h4 className="text-xs font-black uppercase text-slate-400 mt-1">Proposal Sheet • Bekansi AI</h4>
                            </div>
                            <span className="text-xs font-mono text-slate-400">Date: {new Date().toISOString().substring(0, 10)}</span>
                          </div>

                          <div className="text-xs space-y-1 font-bold">
                            <p><span className="text-slate-400">Billing to:</span> {quoteClient || 'N/A'}</p>
                            <p><span className="text-slate-400">Logistics Destination:</span> {quoteLocation || 'N/A'} • logistics coordinate: <span className="text-blue-500">{quotePhone}</span></p>
                          </div>

                          {/* Itemized ledger table */}
                          <div className="overflow-x-auto">
                            <table className="w-full text-left text-[11px] border-collapse font-bold">
                              <thead>
                                <tr className="border-b border-slate-800/15 text-slate-400">
                                  <th className="py-2">Item</th>
                                  <th className="py-2">Specifications</th>
                                  <th className="py-2 text-center">Qty</th>
                                  <th className="py-2 text-right">Subtotal</th>
                                  <th className="py-2 text-right">Action</th>
                                </tr>
                              </thead>
                              <tbody className="divide-y divide-slate-800/10">
                                {quoteItems.length === 0 ? (
                                  <tr>
                                    <td colSpan="5" className="py-6 text-slate-400 text-center italic">No items drafted inside quotation list. Choose product above and click append.</td>
                                  </tr>
                                ) : (
                                  quoteItems.map((it) => (
                                    <tr key={it.id}>
                                      <td className="py-2.5 font-black text-slate-200">{it.name}</td>
                                      <td className="py-2.5 text-slate-400 font-semibold italic">{it.material}</td>
                                      <td className="py-2.5 text-center font-mono">{it.quantity}</td>
                                      <td className="py-2.5 text-right font-mono text-blue-400">{(it.subtotal).toLocaleString()} ETB</td>
                                      <td className="py-2.5 text-right">
                                        <button onClick={() => handleRemoveQuoteItem(it.id)} className="text-red-500 hover:text-red-600 p-1">
                                          <Trash2 className="w-3.5 h-3.5" />
                                        </button>
                                      </td>
                                    </tr>
                                  ))
                                )}
                              </tbody>
                            </table>
                          </div>

                          {/* Interactive discount Slider control */}
                          <div className="bg-slate-950 p-3 rounded-xl border border-slate-800/80 space-y-1.5 text-xs">
                            <div className="flex items-center justify-between font-bold">
                              <span className="text-slate-400 uppercase tracking-wider text-[10px] font-extrabold">Interactive discount slider: </span>
                              <span className="text-amber-500 font-mono font-black">{quoteDiscount}% Adjusted</span>
                            </div>
                            <input 
                              type="range" 
                              min="0" 
                              max="25" 
                              value={quoteDiscount} 
                              onChange={(e) => setQuoteDiscount(parseInt(e.target.value, 10))} 
                              className="w-full h-1.5 bg-slate-800 rounded-lg appearance-none cursor-pointer accent-blue-500" 
                            />
                          </div>

                          {/* Mathematical Summaries */}
                          <div className="border-t border-dashed border-slate-800/20 pt-3 space-y-1.5 text-xs text-slate-400 font-bold">
                            <div className="flex items-center justify-between">
                              <span>Raw Subtotal:</span>
                              <span className="font-mono text-slate-200">{(quoteCalculations.subtotal).toLocaleString()} ETB</span>
                            </div>
                            <div className="flex items-center justify-between text-amber-500">
                              <span>Applied Discount Value:</span>
                              <span className="font-mono">-{quoteCalculations.discountVal.toLocaleString()} ETB</span>
                            </div>
                            <div className="flex items-center justify-between">
                              <span>Vat (15% inclusive rules):</span>
                              <span className="font-mono text-slate-200">{(quoteCalculations.computedVat).toLocaleString()} ETB</span>
                            </div>
                            <div className="flex items-center justify-between">
                              <span>Flat delivery logistcs (Bishoftu Hub):</span>
                              <span className="font-mono text-slate-200">{(quoteCalculations.physicalDeliveryFee).toLocaleString()} ETB</span>
                            </div>
                            <div className="flex items-center justify-between text-base font-black text-slate-100 pt-2 border-t border-slate-800/10">
                              <span>Grand Total Estimate:</span>
                              <span className="font-mono text-blue-500">{quoteCalculations.grandTotal.toLocaleString()} ETB</span>
                            </div>
                          </div>

                          {/* Quotation dispatch sharing capabilities */}
                          <div className="flex flex-wrap items-center gap-2 pt-3 border-t border-slate-800/10">
                            
                            <button 
                              onClick={() => { window.print(); }} 
                              className="bg-slate-800 hover:bg-slate-700 text-slate-250 font-bold text-xs p-2.5 rounded-xl border border-slate-700 transition flex items-center gap-1"
                              title="Print current estimation proposal sheet"
                            >
                              <Printer className="w-3.5 h-3.5" /> Print Invoice
                            </button>

                            <ShareButton 
                              title={`Bekansi AI Custom Quotation - ${quoteClient || 'Client proposal'}`}
                              text={`Hello! Here is your bespoke custom furniture estimation prepared by Bekansi AI platform. Grand Estimations: ${quoteCalculations.grandTotal.toLocaleString()} ETB.`}
                              url={window.location.href}
                            />

                            <button 
                              onClick={() => {
                                handleCopilotCommand('CONV_PREDICT', quoteClient);
                              }}
                              className="bg-gradient-to-r from-blue-700 to-indigo-700 hover:brightness-110 text-white font-extrabold text-xs p-2.5 px-4 rounded-xl transition flex items-center gap-1.5 shrink-0"
                            >
                              <Sparkles className="w-3.5 h-3.5" /> AI Win Predictor
                            </button>
                          </div>

                        </div>
                      </div>

                    </div>
                  </div>
                )}

                {/* 6. PRODUCTS CATALOG & INVENTORY */}
                {currentPage === 'catalog' && (
                  <div className="space-y-6">
                    <div className="flex items-center justify-between">
                      <div>
                        <h2 className="text-2xl font-black tracking-tight text-blue-500">Authorized Furniture Inventory</h2>
                        <p className="text-slate-400 text-xs">Kiln-seasoned hardwood profiles, status updates, and demand warning triggers.</p>
                      </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                      {tenant.catalog.map(prod => (
                        <div 
                          key={prod.id} 
                          className={`p-5 rounded-3xl border transition-all hover:scale-[1.01] hover:shadow-lg ${darkMode ? 'bg-slate-900 border-slate-800' : 'bg-white border-slate-150'}`}
                        >
                          <div className="flex items-center justify-between mb-3 text-xs font-bold font-mono">
                            <span className="p-1 px-2 rounded-lg bg-blue-100 text-blue-600 uppercase text-[10px] tracking-widest">{prod.category}</span>
                            <span className={`p-1 px-2 rounded-lg text-[10px] uppercase font-black ${prod.stock <= 3 ? 'bg-red-500/10 text-red-400' : 'bg-emerald-500/10 text-emerald-400'}`}>
                              {prod.stock <= 3 ? '⚠️ Low Stock' : '✓ In stock'}
                            </span>
                          </div>

                          <h3 className="text-base font-black truncate">{prod.name}</h3>
                          <p className="text-xs text-slate-400 font-semibold italic mt-1">Core material: {prod.material}</p>
                          <p className="text-xs text-slate-400 mt-2 line-clamp-2">{prod.desc}</p>

                          <div className="pt-3 border-t border-slate-800/10 mt-3 flex items-center justify-between">
                            <div>
                              <span className="text-[10px] uppercase font-bold text-slate-400 block">Pricing rate</span>
                              <span className="font-mono font-black text-blue-500">{prod.price.toLocaleString()} ETB</span>
                            </div>
                            <span className="text-[10px] font-black uppercase bg-slate-100 text-slate-700 p-1 px-2 rounded-lg">
                              📈 {prod.performance}
                            </span>
                          </div>

                          <div className="bg-slate-950 p-2.5 rounded-xl border border-slate-800/60 mt-3.5 space-y-1 text-[10px]">
                            <div className="flex items-center justify-between font-bold text-blue-300">
                              <span>Demand prediction rate:</span>
                              <span>94% High Confidence</span>
                            </div>
                            <div className="w-full h-1.5 bg-slate-850 rounded-full overflow-hidden">
                              <div className="h-full bg-blue-500" style={{ width: '84%' }} />
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* 7. EXECUTIVE ANALYTICS SCREEN */}
                {currentPage === 'analytics' && (
                  <div className="space-y-6">
                    <div>
                      <h2 className="text-2xl font-black tracking-tight text-blue-500">CRM Executive analytics</h2>
                      <p className="text-slate-400 text-xs text-slate-400">Deep mathematical algorithms forecast revenue dispatches and material reserves tracking.</p>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      
                      <div className={`p-5 rounded-2xl border ${darkMode ? 'bg-slate-900 border-slate-800' : 'bg-white border-slate-150 text-slate-100'}`}>
                        <h3 className="text-sm font-black uppercase mb-3 text-slate-400">Quarterly growth prediction model</h3>
                        <div className="space-y-4 font-mono font-bold text-xs">
                          <div className="flex items-center justify-between">
                            <span>Predicted July Revenue:</span>
                            <span className="text-emerald-400">7.2M ETB (+12.5% Up)</span>
                          </div>
                          <div className="flex items-center justify-between">
                            <span>Predicted August Revenue:</span>
                            <span className="text-emerald-400">8.1M ETB (+26% Up)</span>
                          </div>
                          <div className="p-3 bg-slate-950 rounded-xl border border-slate-800/80 mt-2 font-sans font-normal text-xs text-slate-300">
                            🔥 Linear model correlates rising wood import limitations to increased local Wanza premium conversions. Direct campaign to Addis Ababa B2B architects recommended.
                          </div>
                        </div>
                      </div>

                      <div className={`p-5 rounded-2xl border ${darkMode ? 'bg-slate-900 border-slate-800' : 'bg-white border-slate-150 text-slate-100'}`}>
                        <h3 className="text-sm font-black uppercase mb-3 text-slate-400">Historical acquisition metrics</h3>
                        <div className="space-y-4 text-xs font-bold">
                          <div>
                            <div className="flex items-center justify-between text-slate-400 text-[11px] mb-1">
                              <span>WhatsApp Broadcast Campaigns</span>
                              <span>48% Contribution</span>
                            </div>
                            <div className="w-full h-2 bg-slate-800 rounded-full overflow-hidden">
                              <div className="h-full bg-emerald-500" style={{ width: '48%' }} />
                            </div>
                          </div>
                          <div>
                            <div className="flex items-center justify-between text-slate-400 text-[11px] mb-1">
                              <span>Telegram organic bots</span>
                              <span>36% Contribution</span>
                            </div>
                            <div className="w-full h-2 bg-slate-800 rounded-full overflow-hidden">
                              <div className="h-full bg-blue-500" style={{ width: '36%' }} />
                            </div>
                          </div>
                        </div>
                      </div>

                    </div>
                  </div>
                )}

                {/* 8. AI PANEL WORK COCKPIT */}
                {currentPage === 'ai_panel' && (
                  <div className="space-y-6">
                    <div>
                      <h2 className="text-2xl font-black tracking-tight text-amber-400 animate-pulse">Cognitive Prompt Workspace</h2>
                      <p className="text-slate-400 text-xs">Instruct the models for copy drafts, regional metrics summaries, and local transport pricing configurations.</p>
                    </div>

                    <div className={`p-5 rounded-3xl border ${darkMode ? 'bg-slate-900 border-slate-800' : 'bg-white border-slate-150 shadow-md'} space-y-4`}>
                      <textarea 
                        rows="4"
                        placeholder="Draft campaign content for living room beds, or analyze client conversations..."
                        value={copilotPrompt}
                        onChange={(e) => setCopilotPrompt(e.target.value)}
                        className="w-full bg-slate-950 border border-slate-800 rounded-2xl p-4 text-xs text-slate-100 font-mono outline-none focus:ring-1 focus:ring-amber-500"
                      />
                      <div className="flex items-center justify-end gap-2">
                        <button 
                          onClick={() => handleCopilotCommand('MARKETING_ADS')}
                          className="bg-slate-800 hover:bg-slate-705 p-2 px-4 rounded-xl border border-slate-700 text-xs font-bold transition"
                        >
                          📣 Ad Copier
                        </button>
                        <button 
                          onClick={() => handleCopilotCommand('REPLY')}
                          className="bg-gradient-to-r from-amber-500 to-indigo-600 hover:brightness-110 text-white font-extrabold text-xs p-2.5 px-5 rounded-xl transition shadow-lg"
                        >
                          ⚡ Active Prompt Model Run
                        </button>
                      </div>
                    </div>
                  </div>
                )}

                {/* 9. PREFERENCES AND WORKSPACE CONFIG */}
                {currentPage === 'settings' && (
                  <div className="space-y-6">
                    <div>
                      <h2 className="text-2xl font-black tracking-tight">System Preferences & Settings</h2>
                      <p className="text-slate-400 text-xs text-slate-400">Configure global currency indices, corporate workspace parameters, and custom billing models.</p>
                    </div>

                    <div className={`p-5 rounded-2xl border ${darkMode ? 'bg-slate-900 border-slate-800' : 'bg-white border-slate-150'} space-y-4 text-xs`}>
                      <h3 className="text-sm font-black uppercase text-slate-400">Corporate Config Sheet</h3>
                      
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 font-bold">
                        <div>
                          <label className="text-[10px] text-slate-400 block mb-1">Local USD exchange rate (Bank Index)</label>
                          <input type="number" step="0.1" value={usdExchangeRate} onChange={(e) => setUsdExchangeRate(parseFloat(e.target.value) || 115)} className="w-full bg-slate-950 border border-slate-800 rounded-xl p-2.5 outline-none font-mono text-slate-100" />
                        </div>
                        <div>
                          <label className="text-[10px] text-slate-400 block mb-1">Authorized billing model Tier</label>
                          <input type="text" value={billingPlan} disabled className="w-full bg-slate-850 border border-slate-800 rounded-xl p-2.5 text-slate-400 cursor-not-allowed" />
                        </div>
                      </div>

                      <div className="bg-slate-950/80 p-4 rounded-xl border border-slate-800/50 flex items-center justify-between mt-4">
                        <div>
                          <p className="font-extrabold text-slate-100">Synchronized ERP Connection check</p>
                          <p className="text-[10px] text-slate-400">Physical PostgreSQL DB Handshake verified.</p>
                        </div>
                        <span className="p-1 px-3 rounded-lg bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 text-[10px] font-black uppercase">Live Online</span>
                      </div>
                    </div>
                  </div>
                )}

              </div>

              {/* PERSISTENT SAAS RIGHT AI COPILOT DRAWER (DESKTOP PERSISTENT) */}
              {copilotPanelOpen && (
                <aside className={`w-full lg:w-80 border-t lg:border-t-0 lg:border-l p-4 flex flex-col shrink-0 space-y-4 max-h-screen sticky top-20 overflow-y-auto transition-colors ${darkMode ? 'bg-[#111c37] border-slate-800' : 'bg-white border-gray-200'}`}>
                  
                  <div className="flex items-center justify-between pb-2 border-b border-dashed border-slate-800/10">
                    <div className="flex items-center gap-1.5 text-blue-500">
                      <Sparkles className="w-4 h-4 text-amber-500 animate-spin" />
                      <h3 className="text-xs font-black uppercase tracking-wider">AI Copilot Core</h3>
                    </div>
                    <button onClick={() => setCopilotPanelOpen(false)} className="text-slate-400 hover:text-slate-100">
                      <X className="w-4.5 h-4.5" />
                    </button>
                  </div>

                  {/* Prompt Box */}
                  <div className="space-y-3 font-semibold text-xs">
                    
                    <div>
                      <label className="text-[10px] text-slate-400 block mb-1 uppercase font-extrabold">Copilot Action Prompt</label>
                      <input 
                        type="text" 
                        value={copilotPrompt}
                        onChange={(e) => setCopilotPrompt(e.target.value)}
                        className="w-full bg-slate-950 border border-slate-800 rounded-xl p-2.5 text-xs text-slate-100 outline-none" 
                        placeholder="Draft client email response..."
                      />
                    </div>

                    {/* Quick Trigger Buttons */}
                    <div className="grid grid-cols-2 gap-1.5">
                      <button onClick={() => handleCopilotCommand('REPLY')} className="bg-slate-950 hover:bg-slate-850 p-2 text-[10px] rounded-lg border border-slate-800 transition">💬 Suggested Reply</button>
                      <button onClick={() => handleCopilotCommand('CONV_PREDICT')} className="bg-slate-950 hover:bg-slate-850 p-2 text-[10px] rounded-lg border border-slate-800 transition">🔮 Predict win</button>
                      <button onClick={() => handleCopilotCommand('QUOTE_GENERATE')} className="bg-slate-950 hover:bg-slate-850 p-2 text-[10px] rounded-lg border border-slate-800 transition">📄 Quote Builder</button>
                      <button onClick={() => handleCopilotCommand('SUMMARIZE')} className="bg-slate-950 hover:bg-slate-850 p-2 text-[10px] rounded-lg border border-slate-800 transition">📝 Synthesize chat</button>
                    </div>

                    {/* Result Output Stream */}
                    <div className="pt-3 border-t border-slate-800/10">
                      <span className="text-[10px] text-slate-400 block mb-1.5 uppercase font-extrabold">Result Analysis Stream</span>
                      
                      {copilotLoading ? (
                        <div className="bg-slate-950 p-3 rounded-xl border border-slate-800/50 space-y-2 animate-pulse">
                          <div className="h-2 bg-slate-800 rounded w-3/4" />
                          <div className="h-2 bg-slate-800 rounded w-1/2" />
                          <div className="h-2 bg-slate-800 rounded w-5/6" />
                        </div>
                      ) : (
                        <div className="bg-slate-950 p-3 rounded-xl border border-slate-800/80 font-mono text-[10.5px] leading-relaxed text-slate-200 whitespace-pre-wrap">
                          {copilotResponse}
                        </div>
                      )}
                    </div>

                    {/* Fast presets */}
                    <div className="pt-2 border-t border-slate-800/10 space-y-2">
                      <span className="text-[10px] text-slate-400 block uppercase font-extrabold">Proactive triggers</span>
                      {[
                        { text: 'Marketing Instagram Copywriter', cmd: 'MARKETING_ADS' },
                        { text: 'Synthesize Yohannes inquiry', cmd: 'SUMMARIZE' },
                        { text: "Analyze Lead: Abdi Biya", cmd: 'ANALYZE_LEAD', param: 'Abdi Biya' }
                      ].map((preset, pIdx) => (
                        <button 
                          key={pIdx}
                          onClick={() => handleCopilotCommand(preset.cmd, preset.param)}
                          className="w-full text-left bg-slate-850 hover:bg-slate-800 p-2 rounded-xl text-[10px] transition-all border border-transparent hover:border-slate-700 block truncate"
                        >
                          🚀 {preset.text}
                        </button>
                      ))}
                    </div>

                  </div>
                </aside>
              )}

            </div>

            {/* MOBILE PERSISTENT BOTTOM TAB NAVIGATION */}
            <div className={`md:hidden sticky bottom-0 z-20 border-t flex items-center justify-around p-2.5 transition-colors ${darkMode ? 'bg-slate-900 border-slate-800' : 'bg-white border-slate-100'}`}>
              
              <button onClick={() => setCurrentPage('dashboard')} className={`flex flex-col items-center p-1 font-bold text-[9px] ${currentPage === 'dashboard' ? 'text-blue-500' : 'text-slate-400'}`}>
                <LayoutDashboard className="w-5 h-5 pointer-events-none" />
                <span>Dashboard</span>
              </button>

              <button onClick={() => setCurrentPage('leads')} className={`flex flex-col items-center p-1 font-bold text-[9px] ${currentPage === 'leads' ? 'text-blue-500' : 'text-slate-400'}`}>
                <Briefcase className="w-5 h-5 pointer-events-none" />
                <span>Leads</span>
              </button>

              <button onClick={() => setCurrentPage('conversations')} className={`flex flex-col items-center p-1 font-bold text-[9px] ${currentPage === 'conversations' ? 'text-blue-500' : 'text-slate-400'}`}>
                <MessageSquare className="w-5 h-5 pointer-events-none" />
                <span>Chat</span>
              </button>

              <button onClick={() => setCurrentPage('analytics')} className={`flex flex-col items-center p-1 font-bold text-[9px] ${currentPage === 'analytics' ? 'text-blue-500' : 'text-slate-400'}`}>
                <TrendingUp className="w-5 h-5 pointer-events-none" />
                <span>Analytics</span>
              </button>

              <button onClick={() => { setMobileMenuOpen(true); }} className="flex flex-col items-center p-1 font-bold text-[9px] text-slate-400">
                <Menu className="w-5 h-5 pointer-events-none" />
                <span>Menu</span>
              </button>

            </div>

          </main>

        </div>
      )}

      {/* QUICK OPPORTUNITY CAPTURE MODAL (OVERLAY DRAWER) */}
      {showQuickLead && (
        <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 p-6 rounded-3xl shadow-2xl max-w-md w-full relative space-y-4">
            <button onClick={() => setShowQuickLead(false)} className="absolute right-4 top-4 hover:brightness-110 text-slate-400">
              <X className="w-5 h-5" />
            </button>

            <div>
              <h3 className="text-xl font-black text-slate-100 flex items-center gap-1.5">
                <Sparkles className="w-5 h-5 text-amber-400" /> Cloud Lead Capture
              </h3>
              <p className="text-xs text-slate-400 font-semibold mt-1">Append qualified client demands directly to the isolated multi-tenant database ledger.</p>
            </div>

            <form onSubmit={handleCreateLead} className="space-y-3 text-xs font-bold font-sans">
              <div>
                <label className="text-slate-400 block mb-1">Company / Customer Name</label>
                <input 
                  type="text" 
                  value={newLeadForm.name}
                  onChange={(e) => setNewLeadForm({ ...newLeadForm, name: e.target.value })}
                  placeholder="e.g. Almaz Tekle"
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl p-2.5 text-slate-100 outline-none" 
                  required
                />
              </div>

              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="text-slate-400 block mb-1 font-extrabold">Active Phone</label>
                  <input 
                    type="text" 
                    value={newLeadForm.phone}
                    onChange={(e) => setNewLeadForm({ ...newLeadForm, phone: e.target.value })}
                    placeholder="+251 91 100 2200"
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl p-2.5 text-slate-100 outline-none" 
                    required
                  />
                </div>
                <div>
                  <label className="text-slate-400 block mb-1 font-extrabold">Email</label>
                  <input 
                    type="email" 
                    value={newLeadForm.email}
                    onChange={(e) => setNewLeadForm({ ...newLeadForm, email: e.target.value })}
                    placeholder="almaz@resort.com"
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl p-2.5 text-slate-100 outline-none" 
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="text-slate-400 block mb-1 font-extrabold">Deal value (ETB)</label>
                  <input 
                    type="number" 
                    value={newLeadForm.value}
                    onChange={(e) => setNewLeadForm({ ...newLeadForm, value: e.target.value })}
                    placeholder="85000"
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl p-2.5 text-slate-100 outline-none font-mono" 
                  />
                </div>
                <div>
                  <label className="text-slate-400 block mb-1 font-extrabold">Initial Pipeline Stage</label>
                  <select 
                    value={newLeadForm.stage}
                    onChange={(e) => setNewLeadForm({ ...newLeadForm, stage: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl p-2.5 text-blue-400 outline-none"
                  >
                    <option value="New">New</option>
                    <option value="Qualified">Qualified</option>
                    <option value="Proposal">Proposal</option>
                    <option value="Negotiation">Negotiation</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="text-slate-400 block mb-1">Requirements notes</label>
                <textarea 
                  value={newLeadForm.message}
                  onChange={(e) => setNewLeadForm({ ...newLeadForm, message: e.target.value })}
                  placeholder="e.g. Needs 10 high density ergo task chairs with white mesh"
                  rows="2"
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl p-2.5 text-slate-100 outline-none block"
                />
              </div>

              <div className="flex items-center justify-end gap-2 pt-2">
                <button type="button" onClick={() => setShowQuickLead(false)} className="bg-slate-850 hover:bg-slate-800 text-slate-400 p-2.5 rounded-xl border border-slate-800">
                  Cancel
                </button>
                <button type="submit" className="bg-emerald-600 hover:bg-emerald-700 text-white font-extrabold p-2.5 px-5 rounded-xl transition shadow-md">
                  Commit opportunity
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MOBILE EXPANDED MENU DRAWER */}
      {mobileMenuOpen && (
        <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-sm z-50 flex justify-end">
          <div className="bg-slate-900 border-l border-slate-800 w-72 max-w-[80vw] h-full p-4 flex flex-col justify-between">
            <div className="space-y-4">
              <div className="flex items-center justify-between pb-2 border-b border-slate-800/20">
                <span className="font-extrabold text-xs text-slate-400 tracking-wider">WORKSPACE NAVIGATION</span>
                <button onClick={() => setMobileMenuOpen(false)} className="text-slate-400 hover:text-slate-200">
                  <X className="w-5 h-5" />
                </button>
              </div>

              <div className="space-y-2 text-xs font-bold text-slate-400">
                {[
                  { id: 'dashboard', label: 'Main Dashboard', icon: <LayoutDashboard className="w-4 h-4" /> },
                  { id: 'leads', label: 'CRM Leads', icon: <Briefcase className="w-4 h-4" /> },
                  { id: 'customers', label: 'Customers CRM', icon: <Users className="w-4 h-4" /> },
                  { id: 'conversations', label: 'Omnichannel Chat', icon: <MessageSquare className="w-4 h-4" /> },
                  { id: 'catalog', label: 'Products Catalog', icon: <Layers className="w-4 h-4" /> },
                  { id: 'quotations', label: 'Quotations Build', icon: <FileText className="w-4 h-4" /> },
                  { id: 'analytics', label: 'Executive Analytics', icon: <TrendingUp className="w-4 h-4" /> },
                  { id: 'ai_panel', label: 'AI Assistant Desk', icon: <Sparkles className="w-4 h-4 text-amber-400" /> },
                  { id: 'settings', label: 'System Preferences', icon: <Settings className="w-4 h-4" /> },
                ].map((item) => (
                  <button 
                    key={item.id}
                    onClick={() => { setCurrentPage(item.id); setMobileMenuOpen(false); }}
                    className={`w-full text-left p-3 rounded-xl flex items-center gap-2.5 transition-all ${currentPage === item.id ? 'bg-blue-600 text-white font-extrabold shadow-sm' : 'hover:bg-slate-800 hover:text-white'}`}
                  >
                    {item.icon}
                    <span>{item.label}</span>
                  </button>
                ))}
              </div>
            </div>

            <div className="p-3 border-t border-slate-800/20 text-center">
              <button onClick={() => { setCurrentPage('login'); setMobileMenuOpen(false); }} className="bg-red-950/40 text-red-400 p-2 text-center rounded-xl font-bold transition-all w-full text-xs">
                Log Out Cloud Session
              </button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
}
