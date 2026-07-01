import React, { useState, useEffect, useMemo } from 'react';
import { 
  Users, 
  Search, 
  Filter, 
  Globe, 
  Calendar, 
  ArrowUpRight, 
  Activity, 
  Circle, 
  Phone, 
  MessageSquare, 
  Sparkles, 
  DollarSign, 
  X, 
  Plus, 
  AlertCircle, 
  CheckCircle, 
  Clock, 
  ChevronRight, 
  ChevronLeft,
  Sliders, 
  Briefcase,
  RefreshCw,
  FileText,
  UserCheck,
  TrendingUp,
  Tag
} from 'lucide-react';

/**
 * ============================================================================
 * BEKANSI AI SALES ASSISTANT PLATFORM
 * INTERACTIVE CRM DEAL PROGRESSION KANBAN BOARD (REACT + TAILWIND)
 * ============================================================================
 * 
 * Tracks sales pipeline stages from initial Lead validation to final transaction success.
 * Key Capabilities:
 * 1. Synchronizes with real-world Express PG/PostgreSQL active endpoints (`/api/v1/leads`).
 * 2. Graceful handshake offline state utilizing deep simulation logs.
 * 3. Drag and Drop stage progression updating physical PostgreSQL database rows instantly.
 * 4. Micro-interactivity: interactive slider score controls, customizable budget trackers.
 */

const PIPELINE_COLUMNS = [
  { id: 'new', label: 'New Inbound', color: 'border-blue-500/20 text-blue-400 bg-blue-500/5 hover:bg-blue-500/10' },
  { id: 'contacted', label: 'Contacted', color: 'border-amber-500/20 text-amber-400 bg-amber-500/5 hover:bg-amber-500/10' },
  { id: 'qualified', label: 'Qualified Hot', color: 'border-indigo-500/20 text-indigo-400 bg-indigo-500/5 hover:bg-indigo-500/10' },
  { id: 'proposal_sent', label: 'Proposal Sent', color: 'border-pink-500/20 text-pink-400 bg-pink-500/5 hover:bg-pink-500/10' },
  { id: 'negotiation', label: 'Negotiation', color: 'border-yellow-500/20 text-yellow-400 bg-yellow-500/5 hover:bg-yellow-500/10' },
  { id: 'won', label: 'Closed Won ⭐', color: 'border-emerald-500/20 text-emerald-400 bg-emerald-500/5 hover:bg-emerald-500/10' },
  { id: 'lost', label: 'Closed Lost ❌', color: 'border-slate-700/20 text-slate-400 bg-slate-800/10 hover:bg-slate-800/20' }
];

const LOCAL_STORAGE_KEY = 'bekansi_kanban_offline_leads';

const FALLBACK_DEALS = [
  {
    id: 1,
    status: 'new',
    customer_first_name: 'Abebe',
    customer_last_name: 'Kebede',
    customer_phone: '+251 91 122 3344',
    customer_lang: 'am', // Amharic
    estimated_budget: 145000,
    lead_score: 75,
    source: 'whatsapp',
    requirements: 'Traditional Wanza wood dining table with 6 hand-routed mahogany chairs.',
    notes: 'Aesthetic luxury. Requests golden oak varnish finish. Delivery target: Dukem subcity.',
    created_at: '2026-06-18T10:00:00Z',
  },
  {
    id: 2,
    status: 'contacted',
    customer_first_name: 'Fasika',
    customer_last_name: 'Tekle',
    customer_phone: '+251 92 011 2233',
    customer_lang: 'en',
    estimated_budget: 280000,
    lead_score: 82,
    source: 'telegram',
    requirements: 'Complete customized bedroom set including double-panel sliding wardrobe.',
    notes: 'Responded with bedroom portfolio. Client sorting room dimensions today.',
    created_at: '2026-06-17T11:30:00Z',
  },
  {
    id: 3,
    status: 'qualified',
    customer_first_name: 'Tolera',
    customer_last_name: 'Gemechu',
    customer_phone: '+251 94 455 6677',
    customer_lang: 'om', // Afaan Oromo
    estimated_budget: 850000,
    lead_score: 95,
    source: 'web_chat',
    requirements: 'L-shape corporate workspace cubicles and conference desks for tech hub.',
    notes: 'Urgent enterprise project. 100% Tax-exempt organization client. Validated budget.',
    created_at: '2026-06-16T14:45:00Z',
  },
  {
    id: 4,
    status: 'proposal_sent',
    customer_first_name: 'Sarah',
    customer_last_name: 'Aliyi',
    customer_phone: '+251 91 388 9900',
    customer_lang: 'am',
    estimated_budget: 95000,
    lead_score: 60,
    source: 'facebook_messenger',
    requirements: 'Low-profile carved geometric coffee table centerpiece.',
    notes: 'Sent PDF design draft with pricing. Waiting for wood-species selection confirmation.',
    created_at: '2026-06-15T09:12:00Z',
  },
  {
    id: 5,
    status: 'negotiation',
    customer_first_name: 'Yonas',
    customer_last_name: 'Binyam',
    customer_phone: '+251 96 233 4455',
    customer_lang: 'am',
    estimated_budget: 320000,
    lead_score: 88,
    source: 'mobile_app',
    requirements: 'Premium entertainment TV console unit with backlighting frame.',
    notes: 'Negotiates free delivery to Ayat Residence. Willing to settle tomorrow.',
    created_at: '2026-06-14T16:20:00Z',
  },
  {
    id: 6,
    status: 'won',
    customer_first_name: 'Daniel',
    customer_last_name: 'Hailu',
    customer_phone: '+251 90 123 4567',
    customer_lang: 'en',
    estimated_budget: 450000,
    lead_score: 100,
    source: 'voice',
    requirements: 'High-end Zigba reception lobby layout styled with brass metal linings.',
    notes: 'Closed! Received 50% deposit receipts. Forwarded specs to manufacturing plant.',
    created_at: '2026-06-12T13:10:00Z',
  },
  {
    id: 7,
    status: 'lost',
    customer_first_name: 'Martha',
    customer_last_name: 'Tesfaye',
    customer_phone: '+251 91 555 6688',
    customer_lang: 'am',
    estimated_budget: 60000,
    lead_score: 35,
    source: 'telegram',
    requirements: 'Compact modular study bookshelves.',
    notes: 'Lost to cheaper metal-alternative retail imports. Keep for retargeting campaigns.',
    created_at: '2026-06-10T11:00:00Z',
  }
];

export default function DealsKanbanBoard() {
  // Sync state
  const [leads, setLeads] = useState([]);
  const [isApiConnected, setIsApiConnected] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState(null);

  // Filters state
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedLanguage, setSelectedLanguage] = useState('all');
  const [selectedSource, setSelectedSource] = useState('all');

  // Drag state
  const [activeCardId, setActiveCardId] = useState(null);
  const [dragOverColumn, setDragOverColumn] = useState(null);

  // Modal forms
  const [isAddOpen, setIsAddOpen] = useState(false);
  const [selectedLeadForEdit, setSelectedLeadForEdit] = useState(null);
  const [toastMessage, setToastMessage] = useState(null);

  // Form states (Add Lead)
  const [newLeadForm, setNewLeadForm] = useState({
    first_name: '',
    last_name: '',
    phone_number: '',
    email: '',
    estimated_budget: '',
    requirements: '',
    notes: '',
    source: 'web_chat',
    preferred_language: 'am',
    lead_score: 50
  });

  // Load leads from API with local cache resilience
  const loadLeads = async () => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      const response = await fetch('/api/v1/leads');
      if (response.ok) {
        const json = await response.json();
        if (json.success && Array.isArray(json.data)) {
          setLeads(json.data);
          setIsApiConnected(true);
          localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(json.data));
          showToast('Synchronized with live PostgreSQL Database successfully.');
        } else {
          throw new Error(json.message || 'Malformed business telemetry API payload.');
        }
      } else {
        throw new Error(`HTTP Session Error: Status ${response.status}`);
      }
    } catch (err) {
      console.warn('Postgres connection bypassed. Initializing design sandbox cache context:', err.message);
      setIsApiConnected(false);
      
      // Look up localized storage backup first
      const stored = localStorage.getItem(LOCAL_STORAGE_KEY);
      if (stored) {
        try {
          setLeads(JSON.parse(stored));
        } catch {
          setLeads(FALLBACK_DEALS);
        }
      } else {
        setLeads(FALLBACK_DEALS);
        localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(FALLBACK_DEALS));
      }
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadLeads();
  }, []);

  const showToast = (msg) => {
    setToastMessage(msg);
    setTimeout(() => {
      setToastMessage(null);
    }, 4500);
  };

  // Create new lead in database or localized state
  const handleCreateLeadSubmit = async (e) => {
    e.preventDefault();
    if (!newLeadForm.first_name || !newLeadForm.phone_number) {
      alert('First Name and Phone Number are required fields.');
      return;
    }

    const leadPayload = {
      phone_number: newLeadForm.phone_number,
      first_name: newLeadForm.first_name,
      last_name: newLeadForm.last_name || null,
      email: newLeadForm.email || null,
      source: newLeadForm.source,
      requirements: newLeadForm.requirements,
      estimated_budget: newLeadForm.estimated_budget ? parseFloat(newLeadForm.estimated_budget) : null,
      notes: newLeadForm.notes,
      preferred_language: newLeadForm.preferred_language,
      lead_score: parseInt(newLeadForm.lead_score, 10)
    };

    setIsLoading(true);
    try {
      if (isApiConnected) {
        const response = await fetch('/api/v1/leads', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(leadPayload)
        });
        const resJson = await response.json();
        if (response.ok && resJson.success) {
          showToast(`Created new CRM Deal card for ${newLeadForm.first_name}.`);
          loadLeads();
          setIsAddOpen(false);
        } else {
          throw new Error(resJson.message || 'Handshake failed during transactional block.');
        }
      } else {
        // Mock offline pipeline insert
        const newLocalModel = {
          id: Math.max(...leads.map(l => l.id), 0) + 1,
          status: 'new',
          customer_first_name: newLeadForm.first_name,
          customer_last_name: newLeadForm.last_name || '',
          customer_phone: newLeadForm.phone_number,
          customer_lang: newLeadForm.preferred_language,
          estimated_budget: leadPayload.estimated_budget || 0,
          lead_score: leadPayload.lead_score,
          source: newLeadForm.source,
          requirements: newLeadForm.requirements || 'Handcrafted carved cabinetry',
          notes: newLeadForm.notes || 'Registered through offline catalog wizard.',
          created_at: new Date().toISOString()
        };
        const updatedList = [newLocalModel, ...leads];
        setLeads(updatedList);
        localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(updatedList));
        showToast('Created Offline Deal Workspace card.');
        setIsAddOpen(false);
      }
    } catch (err) {
      alert(`Pipeline write failure: ${err.message}`);
    } finally {
      setIsLoading(false);
      setNewLeadForm({
        first_name: '',
        last_name: '',
        phone_number: '',
        email: '',
        estimated_budget: '',
        requirements: '',
        notes: '',
        source: 'web_chat',
        preferred_language: 'am',
        lead_score: 50
      });
    }
  };

  // Update lead status pipeline placement (Drag and drop or controls)
  const handleUpdateStatus = async (leadId, targetStatus) => {
    const updated = leads.map(l => {
      if (l.id === leadId) {
        return { ...l, status: targetStatus };
      }
      return l;
    });

    setLeads(updated);
    if (!isApiConnected) {
      localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(updated));
    }

    const stateLabel = PIPELINE_COLUMNS.find(col => col.id === targetStatus)?.label || targetStatus;
    showToast(`Shifted deal workflow to: [${stateLabel}]`);

    try {
      if (isApiConnected) {
        const response = await fetch(`/api/v1/leads/${leadId}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ status: targetStatus })
        });
        const resJson = await response.json();
        if (!response.ok || !resJson.success) {
          throw new Error(resJson.message || 'Write boundary deadlock.');
        }
      }
    } catch (err) {
      console.error('Failed to notify backend PostgreSQL state update:', err.message);
      showToast('Offline cache synced. PostgreSQL handshake failed.');
    }
  };

  // Multi-attribute lead modification
  const handleSaveEditSubmit = async (e) => {
    e.preventDefault();
    if (!selectedLeadForEdit) return;

    const leadId = selectedLeadForEdit.id;
    const updatePayload = {
      requirements: selectedLeadForEdit.requirements,
      estimated_budget: selectedLeadForEdit.estimated_budget ? parseFloat(selectedLeadForEdit.estimated_budget) : null,
      notes: selectedLeadForEdit.notes,
      lead_score: parseInt(selectedLeadForEdit.lead_score, 10),
      status: selectedLeadForEdit.status
    };

    setIsLoading(true);
    try {
      if (isApiConnected) {
        const response = await fetch(`/api/v1/leads/${leadId}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(updatePayload)
        });
        const resJson = await response.json();
        if (response.ok && resJson.success) {
          showToast(`Updated transaction metadata for ${selectedLeadForEdit.customer_first_name}`);
          loadLeads();
          setSelectedLeadForEdit(null);
        } else {
          throw new Error(resJson.message || 'Update failed during scope check.');
        }
      } else {
        const updatedList = leads.map(l => {
          if (l.id === leadId) {
            return {
              ...l,
              requirements: selectedLeadForEdit.requirements,
              estimated_budget: parseFloat(selectedLeadForEdit.estimated_budget) || 0,
              notes: selectedLeadForEdit.notes,
              lead_score: parseInt(selectedLeadForEdit.lead_score, 10),
              status: selectedLeadForEdit.status
            };
          }
          return l;
        });
        setLeads(updatedList);
        localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(updatedList));
        showToast('Local CRM logs updated.');
        setSelectedLeadForEdit(null);
      }
    } catch (err) {
      alert(`Update error: ${err.message}`);
    } finally {
      setIsLoading(false);
    }
  };

  // Drag and Drop implementation
  const handleDragStart = (e, cardId) => {
    setActiveCardId(cardId);
    e.dataTransfer.setData('text/plain', cardId);
    e.dataTransfer.effectAllowed = 'move';
  };

  const handleDragOver = (e, colId) => {
    e.preventDefault();
    setDragOverColumn(colId);
  };

  const handleDrop = (e, colId) => {
    e.preventDefault();
    const cardIdStr = e.dataTransfer.getData('text/plain');
    const cardId = parseInt(cardIdStr, 10) || cardIdStr;
    
    if (cardId) {
      const activeCard = leads.find(l => l.id === cardId);
      if (activeCard && activeCard.status !== colId) {
        handleUpdateStatus(cardId, colId);
      }
    }
    
    setActiveCardId(null);
    setDragOverColumn(null);
  };

  const handleDragEnd = () => {
    setActiveCardId(null);
    setDragOverColumn(null);
  };

  // Filter conditions
  const filteredDeals = useMemo(() => {
    return leads.filter(deal => {
      const searchTerms = searchQuery.toLowerCase();
      const firstName = deal.customer_first_name || '';
      const lastName = deal.customer_last_name || '';
      const phone = deal.customer_phone || deal.phone_number || '';
      const reqs = deal.requirements || '';
      
      const matchesSearch = 
        firstName.toLowerCase().includes(searchTerms) ||
        lastName.toLowerCase().includes(searchTerms) ||
        phone.includes(searchTerms) ||
        reqs.toLowerCase().includes(searchTerms);

      const matchesLang = selectedLanguage === 'all' || deal.customer_lang === selectedLanguage || deal.preferred_language === selectedLanguage;
      const matchesSource = selectedSource === 'all' || deal.source === selectedSource;

      return matchesSearch && matchesLang && matchesSource;
    });
  }, [leads, searchQuery, selectedLanguage, selectedSource]);

  // Aggregate stats per status column
  const totalsByStage = useMemo(() => {
    const stats = {};
    PIPELINE_COLUMNS.forEach(col => {
      const colDeals = filteredDeals.filter(d => d.status === col.id);
      const sumBudget = colDeals.reduce((sum, d) => sum + (parseFloat(d.estimated_budget) || 0), 0);
      stats[col.id] = {
        count: colDeals.length,
        value: sumBudget
      };
    });
    return stats;
  }, [filteredDeals]);

  // General metrics for visual widgets
  const pipelineMetrics = useMemo(() => {
    const totalPipelineVal = filteredDeals.reduce((sum, d) => d.status !== 'lost' ? sum + (parseFloat(d.estimated_budget) || 0) : sum, 0);
    const winRate = filteredDeals.length > 0 
      ? ((filteredDeals.filter(d => d.status === 'won').length / filteredDeals.filter(d => d.status === 'won' || d.status === 'lost').length || 0) * 100).toFixed(0)
      : 0;
    
    const avgScore = filteredDeals.length > 0
      ? (filteredDeals.reduce((sum, d) => sum + (d.lead_score || 0), 0) / filteredDeals.length).toFixed(0)
      : 0;

    return {
      totalValue: totalPipelineVal,
      activeCount: filteredDeals.filter(d => d.status !== 'won' && d.status !== 'lost').length,
      winRate: isNaN(winRate) ? 0 : winRate,
      averageScore: avgScore
    };
  }, [filteredDeals]);

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 p-4 md:p-8 font-sans">
      
      {/* Dynamic Toast feedback panel */}
      {toastMessage && (
        <div className="fixed bottom-6 right-6 z-50 bg-slate-900 border border-amber-500/30 text-amber-400 p-3.5 rounded-xl shadow-2xl flex items-center gap-2 text-xs font-semibold backdrop-blur-md animate-bounce">
          <Sparkles className="w-4 h-4 text-amber-500 animate-pulse" />
          <span>{toastMessage}</span>
        </div>
      )}

      {/* Top Navigation Frame */}
      <div className="flex flex-col lg:flex-row lg:items-center justify-between border-b border-slate-850 pb-6 mb-8 gap-4">
        <div className="flex flex-col md:flex-row md:items-center gap-4">
          {/* Company Logo requested by the user */}
          <div className="flex-shrink-0 flex items-center justify-start md:justify-center bg-slate-900/40 p-2 rounded-xl border border-slate-800">
            <img 
              src="https://chatgpt.com/s/m_6a347ae4ef54819197e20f8e18418993" 
              alt="Bekansi Logo" 
              className="h-10 w-auto object-contain"
              style={{ height: '40px' }}
            />
          </div>
          <div>
            <div className="flex items-center gap-2 mb-2">
              <span className="bg-amber-500/10 text-amber-500 text-[10px] uppercase font-bold tracking-widest px-2.5 py-1 rounded-md border border-amber-500/15">
                Interactive Kanban Control
              </span>
              <span className={`flex items-center gap-1.5 text-[11px] font-bold ${isApiConnected ? 'text-emerald-400' : 'text-slate-500'}`}>
                <Circle className={`w-2 h-2 ${isApiConnected ? 'fill-emerald-400 stroke-none animate-pulse' : 'fill-slate-600 stroke-none'}`} />
                {isApiConnected ? 'CONNECTED TO LIVE POSTGRESQL' : 'OFFLINE DESIGN SANDBOX CACHE'}
              </span>
            </div>
            <h1 className="text-2xl md:text-3xl font-black text-slate-50 tracking-tight">
              CRM Deals Progression Board
            </h1>
            <p className="text-slate-400 text-sm mt-1">
              Visualize customer conversions dynamically. Drag card items across pipeline nodes to trigger multi-tenant RLS updates.
            </p>
          </div>
        </div>

        {/* Action Header pane */}
        <div className="flex items-center gap-3">
          <button 
            onClick={loadLeads} 
            disabled={isLoading}
            className="p-2.5 bg-slate-900 border border-slate-800 rounded-xl hover:bg-slate-850 text-slate-400 hover:text-amber-500 transition-all flex items-center gap-1.5 text-xs font-semibold"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin text-amber-500' : ''}`} />
            Sync Database
          </button>
          <button 
            onClick={() => setIsAddOpen(true)}
            className="bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold px-4 py-2.5 rounded-xl transition-all shadow-lg flex items-center gap-1.5 text-xs"
          >
            <Plus className="w-4 h-4 font-black" />
            Add New Deal
          </button>
        </div>
      </div>

      {/* High Fidelity Performance metrics cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5 mb-8">
        <div className="bg-slate-900 border border-slate-853 rounded-2xl p-4 md:p-5 relative overflow-hidden shadow-inner">
          <div className="absolute top-0 right-0 w-24 h-24 bg-gradient-to-br from-amber-500/5 to-transparent rounded-full blur-2xl" />
          <p className="text-[10.5px] text-slate-400 font-semibold uppercase tracking-wider">Weighted Deal Volume</p>
          <h3 className="text-2xl md:text-3xl font-extrabold text-white mt-1.5">
            {pipelineMetrics.totalValue.toLocaleString('en-US')} <span className="text-xs text-amber-500 font-medium font-sans">ETB</span>
          </h3>
          <p className="text-[11.2px] text-slate-400 mt-2 flex items-center gap-1 text-[11px] text-slate-500">
            <DollarSign className="w-3.5 h-3.5 text-amber-400" /> Excluding closed-lost accounts
          </p>
        </div>

        <div className="bg-slate-900 border border-slate-853 rounded-2xl p-4 md:p-5 relative overflow-hidden">
          <p className="text-[10.5px] text-slate-400 font-semibold uppercase tracking-wider">Active Deals in Flight</p>
          <h3 className="text-2xl md:text-3xl font-extrabold text-white mt-1.5">
            {pipelineMetrics.activeCount} <span className="text-xs text-slate-500 font-light font-sans">Customers</span>
          </h3>
          <p className="text-[11.2px] text-indigo-400 font-medium mt-2 flex items-center gap-1">
            <Activity className="w-3.5 h-3.5" /> Progression nodes active
          </p>
        </div>

        <div className="bg-slate-900 border border-slate-853 rounded-2xl p-4 md:p-5 relative overflow-hidden">
          <p className="text-[10.5px] text-slate-400 font-semibold uppercase tracking-wider">Closed Won Conversion Rate</p>
          <h3 className="text-2xl md:text-3xl font-extrabold text-emerald-400 mt-1.5">
            {pipelineMetrics.winRate}%
          </h3>
          <p className="text-[11.2px] text-slate-500 mt-2">
            Success ratio of finalized design quotes
          </p>
        </div>

        <div className="bg-slate-900 border border-slate-853 rounded-2xl p-4 md:p-5 relative overflow-hidden">
          <p className="text-[10.5px] text-slate-400 font-semibold uppercase tracking-wider">Average Inbound Lead Score</p>
          <h3 className="text-2xl md:text-3xl font-extrabold text-amber-400 mt-1.5">
            {pipelineMetrics.averageScore} <span className="text-xs text-slate-500 font-sans">/ 100</span>
          </h3>
          <p className="text-[11.2px] text-slate-500 mt-2 flex items-center gap-1">
            <TrendingUp className="w-3 text-emerald-400" /> High score priorities
          </p>
        </div>
      </div>

      {/* Granular Filtration bar */}
      <div className="bg-slate-900 border border-slate-850 p-4 rounded-2xl mb-8 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="relative flex-1">
          <Search className="w-4 h-4 ml-3.5 absolute top-1/2 -translate-y-1/2 text-slate-500" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Filter deals by name, telephone, specifications..."
            className="w-full bg-slate-950 border border-slate-800 rounded-xl py-2 pl-10 pr-4 text-xs text-slate-200 placeholder-slate-500 focus:outline-none focus:border-amber-500/50 focus:ring-1 focus:ring-amber-500/25 transition-all text-xs"
          />
        </div>

        {/* Filter Dropdowns options */}
        <div className="flex flex-wrap items-center gap-3">
          <div className="flex items-center bg-slate-950 border border-slate-800 rounded-xl px-2.5 py-1.5 text-xs text-slate-300 gap-2">
            <Globe className="w-3.5 h-3.5 text-slate-500" />
            <span className="text-slate-500 font-semibold text-[11px]">Lang:</span>
            <select 
              value={selectedLanguage}
              onChange={(e) => setSelectedLanguage(e.target.value)}
              className="bg-transparent border-none text-slate-200 focus:outline-none cursor-pointer text-xs"
            >
              <option value="all">All Languages</option>
              <option value="am">Amharic (አማርኛ)</option>
              <option value="om">Afaan Oromo</option>
              <option value="en">English (US)</option>
            </select>
          </div>

          <div className="flex items-center bg-slate-950 border border-slate-800 rounded-xl px-2.5 py-1.5 text-xs text-slate-300 gap-2">
            <Sliders className="w-3.5 h-3.5 text-slate-500" />
            <span className="text-slate-500 font-semibold text-[11px]">Channel:</span>
            <select 
              value={selectedSource}
              onChange={(e) => setSelectedSource(e.target.value)}
              className="bg-transparent border-none text-slate-200 focus:outline-none cursor-pointer text-xs"
            >
              <option value="all">All Intake Channels</option>
              <option value="whatsapp">WhatsApp</option>
              <option value="telegram">Telegram</option>
              <option value="facebook_messenger">Messenger</option>
              <option value="web_chat">Web Chat</option>
              <option value="mobile_app">Mobile App</option>
              <option value="voice">Voice Call</option>
            </select>
          </div>
        </div>
      </div>

      {/* Main Kanban Board Layout Grid (Horizontal scroll for small screens) */}
      <div className="flex overflow-x-auto pb-6 gap-5 scrollbar-thin scrollbar-thumb-slate-800 scrollbar-track-transparent">
        
        {PIPELINE_COLUMNS.map((col) => {
          const colDeals = filteredDeals.filter(d => d.status === col.id);
          const colStats = totalsByStage[col.id] || { count: 0, value: 0 };
          const isOver = dragOverColumn === col.id;

          return (
            <div 
              key={col.id}
              onDragOver={(e) => handleDragOver(e, col.id)}
              onDrop={(e) => handleDrop(e, col.id)}
              className={`flex-shrink-0 w-80 rounded-2xl p-3.5 flex flex-col transition-all min-h-[550px] ${
                isOver ? 'bg-slate-900 border-2 border-dashed border-amber-500/40 relative' : 'bg-slate-900/40 border border-slate-850'
              }`}
            >
              {/* Column Title Header */}
              <div className="flex items-center justify-between mb-3 border-b border-slate-850 pb-2">
                <div className="flex items-center gap-2">
                  <span className={`w-2.5 h-2.5 rounded-full ${
                    col.id === 'won' ? 'bg-emerald-400' : 
                    col.id === 'lost' ? 'bg-rose-500' : 
                    col.id === 'negotiation' ? 'bg-yellow-400' : 'bg-amber-400'
                  }`} />
                  <h4 className="text-xs font-extrabold uppercase tracking-wider text-slate-200">
                    {col.label}
                  </h4>
                </div>
                <span className="bg-slate-950 text-slate-400 font-bold text-[10px] px-2 py-0.5 rounded-md border border-slate-800">
                  {colStats.count}
                </span>
              </div>

              {/* Column aggregate value */}
              <div className="flex items-center justify-between text-[11px] text-slate-400 mb-4 bg-slate-950/55 p-1 px-2.5 rounded-lg border border-slate-850">
                <span className="font-medium">Combined Pool:</span>
                <span className="font-extrabold text-amber-500/90">{colStats.value.toLocaleString('en-US')} ETB</span>
              </div>

              {/* Deal Cards Container */}
              <div className="space-y-3.5 overflow-y-auto max-h-[700px] flex-1 pb-16">
                {colDeals.length === 0 ? (
                  <div className="text-center py-10 border border-dashed border-slate-850/40 rounded-xl bg-slate-950/20 text-slate-600 flex flex-col items-center gap-1.5">
                    <Briefcase className="w-5 h-5 text-slate-700" />
                    <p className="text-[11px] font-semibold">Drop deal cards here</p>
                  </div>
                ) : (
                  colDeals.map((deal) => {
                    const clientName = `${deal.customer_first_name || ''} ${deal.customer_last_name || ''}`.trim() || 'Handshake User';
                    
                    return (
                      <div
                        key={deal.id}
                        draggable="true"
                        onDragStart={(e) => handleDragStart(e, deal.id)}
                        onDragEnd={handleDragEnd}
                        onClick={() => setSelectedLeadForEdit({ ...deal })}
                        className={`bg-slate-900 border border-slate-850 hover:border-slate-800 p-4 rounded-xl cursor-grab active:cursor-grabbing hover:shadow-2xl transition-all relative group overflow-hidden ${
                          activeCardId === deal.id ? 'opacity-40 border-amber-500/50' : ''
                        }`}
                      >
                        {/* Drag and Drop hover border glow */}
                        <div className="absolute top-0 left-0 w-1.5 h-full bg-gradient-to-b from-amber-500 to-amber-700/80 opacity-0 group-hover:opacity-100 transition-opacity" />

                        {/* Card metadata Header details */}
                        <div className="flex items-start justify-between gap-1 mb-2">
                          <span className={`text-[9.5px] font-extrabold tracking-wider uppercase px-2 py-0.5 rounded border ${
                            deal.source === 'whatsapp' ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' :
                            deal.source === 'telegram' ? 'bg-blue-500/10 text-blue-400 border-blue-500/20' :
                            deal.source === 'voice' ? 'bg-pink-500/10 text-pink-400 border-pink-500/20' :
                            'bg-amber-500/10 text-amber-400 border-amber-500/20'
                          }`}>
                            {deal.source ? deal.source.replace('_', ' ') : 'SaaS Inbound'}
                          </span>

                          {/* Language visual node indicator */}
                          <span className="text-[10px] text-slate-400 font-extrabold bg-slate-950 px-1.5 py-0.5 border border-slate-850 rounded">
                            {deal.customer_lang === 'am' || deal.preferred_language === 'am' ? 'አማ' : 
                             deal.customer_lang === 'om' || deal.preferred_language === 'om' ? 'OM' : 'EN'}
                          </span>
                        </div>

                        {/* Customer identity details Name */}
                        <h5 className="font-extrabold text-slate-100 text-sm group-hover:text-amber-500 transition-colors">
                          {clientName}
                        </h5>
                        
                        {/* Contact Phone details */}
                        <div className="flex items-center gap-1.5 text-slate-500 text-[10.5px] mt-1">
                          <Phone className="w-3 h-3" />
                          <span>{deal.customer_phone || deal.phone_number || 'No Registered Phone'}</span>
                        </div>

                        {/* Requirements content text snippet */}
                        <p className="text-[11.2px] text-slate-400 mt-2 line-clamp-2 leading-relaxed bg-slate-950/40 p-2 border border-slate-855 rounded-lg italic">
                          "{deal.requirements || 'Handcrafted mahogany custom cabinets request.'}"
                        </p>

                        {/* Budget quotation pricing */}
                        <div className="flex items-center justify-between mt-3 pt-2.5 border-t border-slate-850">
                          <div>
                            <span className="text-[9.2px] text-slate-500 uppercase font-semibold block">Deal Valuation:</span>
                            <span className="text-xs font-black text-amber-500">
                              {(parseFloat(deal.estimated_budget) || 0).toLocaleString('en-US')} ETB
                            </span>
                          </div>

                          {/* Lead Score hot state tracking */}
                          <div className="text-right">
                            <span className="text-[9.2px] text-slate-500 uppercase font-semibold block">Lead Score:</span>
                            <span className={`text-[11px] font-black ${
                              deal.lead_score >= 80 ? 'text-emerald-400 shadow-emerald-400/20' :
                              deal.lead_score >= 50 ? 'text-amber-400' : 'text-slate-400'
                            }`}>
                              🔥 {deal.lead_score}
                            </span>
                          </div>
                        </div>

                        {/* Fast Shift Column Controls (Alternative accessibility / mobile layout arrow) */}
                        <div className="flex justify-between items-center mt-3 pt-1.5 opacity-0 group-hover:opacity-100 transition-opacity">
                          <button 
                            title="Shift Status Left"
                            onClick={(e) => {
                              e.stopPropagation();
                              const currentIndex = PIPELINE_COLUMNS.findIndex(col => col.id === deal.status);
                              if (currentIndex > 0) {
                                handleUpdateStatus(deal.id, PIPELINE_COLUMNS[currentIndex - 1].id);
                              }
                            }}
                            className="p-1 px-1.5 bg-slate-950 border border-slate-800 text-slate-400 hover:text-amber-500 hover:border-amber-500/40 rounded transition-all"
                          >
                            <ChevronLeft className="w-3.5 h-3.5" />
                          </button>
                          <span className="text-[9.5px] font-medium text-slate-500">Quick Shift Node</span>
                          <button 
                            title="Shift Status Right"
                            onClick={(e) => {
                              e.stopPropagation();
                              const currentIndex = PIPELINE_COLUMNS.findIndex(col => col.id === deal.status);
                              if (currentIndex < PIPELINE_COLUMNS.length - 1) {
                                handleUpdateStatus(deal.id, PIPELINE_COLUMNS[currentIndex + 1].id);
                              }
                            }}
                            className="p-1 px-1.5 bg-slate-950 border border-slate-800 text-slate-400 hover:text-amber-500 hover:border-amber-500/40 rounded transition-all"
                          >
                            <ChevronRight className="w-3.5 h-3.5" />
                          </button>
                        </div>

                      </div>
                    );
                  })
                )}
              </div>

            </div>
          );
        })}
      </div>

      {/* MODAL WINDOW Dialog: Edit/Detail Lead */}
      {selectedLeadForEdit && (
        <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-4 z-50">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 w-full max-w-lg shadow-2xl relative">
            <button 
              onClick={() => setSelectedLeadForEdit(null)}
              className="absolute top-5 right-5 text-slate-500 hover:text-white p-1 rounded-full hover:bg-slate-800 transition-colors"
            >
              <X className="w-5 h-5" />
            </button>

            <h3 className="text-xl font-black text-slate-100 mb-1 flex items-center gap-1.5">
              <Briefcase className="w-5 h-5 text-amber-500" /> Modify Deal Parameters
            </h3>
            <p className="text-xs text-slate-400 mb-6">
              Handshake CRM client pipeline properties isolation system.
            </p>

            <form onSubmit={handleSaveEditSubmit} className="space-y-4">
              <div>
                <label className="text-[10px] font-extrabold uppercase text-slate-400 tracking-wider block mb-1">
                  Customer Profile
                </label>
                <div className="bg-slate-950 border border-slate-850 p-3 rounded-xl flex items-center justify-between text-xs text-slate-300">
                  <div>
                    <p className="font-extrabold text-white text-sm">
                      {selectedLeadForEdit.customer_first_name} {selectedLeadForEdit.customer_last_name || ''}
                    </p>
                    <p className="text-slate-500 mt-0.5">{selectedLeadForEdit.customer_phone || selectedLeadForEdit.phone_number}</p>
                  </div>
                  <UserCheck className="w-4 h-4 text-emerald-400" />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-[10px] font-extrabold uppercase text-slate-400 tracking-wider block mb-1.5">
                    Quotation Budget (ETB)
                  </label>
                  <input 
                    type="number"
                    value={selectedLeadForEdit.estimated_budget || ''}
                    onChange={(e) => setSelectedLeadForEdit({ ...selectedLeadForEdit, estimated_budget: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-850 text-slate-200 text-xs px-3.5 py-2.5 rounded-xl focus:outline-none focus:border-amber-500/50"
                  />
                </div>

                <div>
                  <label className="text-[10px] font-extrabold uppercase text-slate-400 tracking-wider block mb-1.5">
                    Workflow Node Status
                  </label>
                  <select 
                    value={selectedLeadForEdit.status}
                    onChange={(e) => setSelectedLeadForEdit({ ...selectedLeadForEdit, status: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-850 text-slate-200 text-xs px-3.5 py-2.5 rounded-xl focus:outline-none focus:border-amber-500/50"
                  >
                    {PIPELINE_COLUMNS.map(col => (
                      <option key={col.id} value={col.id}>{col.label}</option>
                    ))}
                  </select>
                </div>
              </div>

              <div>
                <label className="text-[10px] font-extrabold uppercase text-slate-400 tracking-wider block mb-1.5 font-sans">
                  Current Lead Score Priorities (🔥 {selectedLeadForEdit.lead_score})
                </label>
                <div className="flex items-center gap-3 bg-slate-950 p-2.5 rounded-xl border border-slate-850">
                  <input 
                    type="range"
                    min="1"
                    max="100"
                    value={selectedLeadForEdit.lead_score || 50}
                    onChange={(e) => setSelectedLeadForEdit({ ...selectedLeadForEdit, lead_score: e.target.value })}
                    className="flex-1 accent-amber-500 bg-slate-800 rounded-lg height-1.5"
                  />
                  <span className="text-xs font-black text-amber-500 bg-amber-500/10 px-2 py-0.5 rounded border border-amber-500/15">
                    {selectedLeadForEdit.lead_score}
                  </span>
                </div>
              </div>

              <div>
                <label className="text-[10px] font-extrabold uppercase text-slate-400 tracking-wider block mb-1.5">
                  Bespoke Handcrafted Design Specifications
                </label>
                <textarea 
                  rows="2"
                  value={selectedLeadForEdit.requirements || ''}
                  onChange={(e) => setSelectedLeadForEdit({ ...selectedLeadForEdit, requirements: e.target.value })}
                  placeholder="Design requirements (e.g. Dimensions, Timber type, fabric finishes...)"
                  className="w-full bg-slate-950 border border-slate-850 text-slate-200 text-xs p-3 rounded-xl focus:outline-none focus:border-amber-500/50 resize-none font-sans"
                />
              </div>

              <div>
                <label className="text-[10px] font-extrabold uppercase text-slate-400 tracking-wider block mb-1.5">
                  Internal Sales Agent Dialog Notes / History
                </label>
                <textarea 
                  rows="2"
                  value={selectedLeadForEdit.notes || ''}
                  onChange={(e) => setSelectedLeadForEdit({ ...selectedLeadForEdit, notes: e.target.value })}
                  placeholder="Logs or contact history..."
                  className="w-full bg-slate-950 border border-slate-850 text-slate-200 text-xs p-3 rounded-xl focus:outline-none focus:border-amber-500/50 resize-none font-sans"
                />
              </div>

              <div className="flex justify-end gap-3 pt-4">
                <button 
                  type="button"
                  onClick={() => setSelectedLeadForEdit(null)}
                  className="px-4 py-2.5 rounded-xl bg-slate-950 hover:bg-slate-850 border border-slate-800 text-xs font-bold text-slate-400 transition-colors"
                >
                  Cancel
                </button>
                <button 
                  type="submit"
                  className="px-5 py-2.5 rounded-xl bg-amber-500 hover:bg-amber-400 text-slate-950 text-xs font-black transition-colors"
                >
                  Save Changes
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL WINDOW Dialog: Add New Lead */}
      {isAddOpen && (
        <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-4 z-50">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 w-full max-w-lg shadow-2xl relative">
            <button 
              onClick={() => setIsAddOpen(false)}
              className="absolute top-5 right-5 text-slate-500 hover:text-white p-1 rounded-full hover:bg-slate-800 transition-colors"
            >
              <X className="w-5 h-5" />
            </button>

            <h3 className="text-xl font-black text-slate-100 mb-1 flex items-center gap-1.5">
              <Plus className="w-5 h-5 text-amber-500" /> Create New Deal Record
            </h3>
            <p className="text-xs text-slate-400 mb-6">
              Enter customer identity credentials and project design specifications for real-time tracking inside our CRM ecosystem.
            </p>

            <form onSubmit={handleCreateLeadSubmit} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-[10px] font-extrabold uppercase text-slate-400 tracking-wider block mb-1.5">
                    First Name *
                  </label>
                  <input 
                    type="text"
                    required
                    value={newLeadForm.first_name}
                    onChange={(e) => setNewLeadForm({ ...newLeadForm, first_name: e.target.value })}
                    placeholder="e.g. Abebe"
                    className="w-full bg-slate-950 border border-slate-850 text-slate-200 text-xs px-3.5 py-2.5 rounded-xl focus:outline-none focus:border-amber-500/50"
                  />
                </div>

                <div>
                  <label className="text-[10px] font-extrabold uppercase text-slate-400 tracking-wider block mb-1.5">
                    Last Name
                  </label>
                  <input 
                    type="text"
                    value={newLeadForm.last_name}
                    onChange={(e) => setNewLeadForm({ ...newLeadForm, last_name: e.target.value })}
                    placeholder="e.g. Kebede"
                    className="w-full bg-slate-950 border border-slate-850 text-slate-200 text-xs px-3.5 py-2.5 rounded-xl focus:outline-none focus:border-amber-500/50"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-[10px] font-extrabold uppercase text-slate-400 tracking-wider block mb-1.5">
                    Phone Number (+251) *
                  </label>
                  <input 
                    type="text"
                    required
                    value={newLeadForm.phone_number}
                    onChange={(e) => setNewLeadForm({ ...newLeadForm, phone_number: e.target.value })}
                    placeholder="+251 91 122 3344"
                    className="w-full bg-slate-950 border border-slate-850 text-slate-200 text-xs px-3.5 py-2.5 rounded-xl focus:outline-none focus:border-amber-500/50"
                  />
                </div>

                <div>
                  <label className="text-[10px] font-extrabold uppercase text-slate-400 tracking-wider block mb-1.5">
                    Email Address
                  </label>
                  <input 
                    type="email"
                    value={newLeadForm.email}
                    onChange={(e) => setNewLeadForm({ ...newLeadForm, email: e.target.value })}
                    placeholder="example@gmail.com"
                    className="w-full bg-slate-950 border border-slate-850 text-slate-200 text-xs px-3.5 py-2.5 rounded-xl focus:outline-none focus:border-amber-500/50"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-[10px] font-extrabold uppercase text-slate-400 tracking-wider block mb-1.5">
                    Preferred Language
                  </label>
                  <select 
                    value={newLeadForm.preferred_language}
                    onChange={(e) => setNewLeadForm({ ...newLeadForm, preferred_language: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-850 text-slate-200 text-xs px-3 py-2 rounded-xl focus:outline-none"
                  >
                    <option value="am">Amharic (አማርኛ)</option>
                    <option value="om">Afaan Oromo</option>
                    <option value="en">English (US)</option>
                  </select>
                </div>

                <div>
                  <label className="text-[10px] font-extrabold uppercase text-slate-400 tracking-wider block mb-1.5">
                    Deal Entry Cost (ETB)
                  </label>
                  <input 
                    type="number"
                    value={newLeadForm.estimated_budget}
                    onChange={(e) => setNewLeadForm({ ...newLeadForm, estimated_budget: e.target.value })}
                    placeholder="SLA Budget"
                    className="w-full bg-slate-950 border border-slate-850 text-slate-200 text-xs px-3.5 py-2.5 rounded-xl focus:outline-none focus:border-amber-500/50"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-[10px] font-extrabold uppercase text-slate-400 tracking-wider block mb-1.5">
                    Lead Intake Channel
                  </label>
                  <select 
                    value={newLeadForm.source}
                    onChange={(e) => setNewLeadForm({ ...newLeadForm, source: e.target.value })}
                    className="w-full bg-slate-950 border border-slate-855 text-slate-200 text-xs px-3 py-2 rounded-xl focus:outline-none"
                  >
                    <option value="whatsapp">WhatsApp</option>
                    <option value="telegram">Telegram</option>
                    <option value="facebook_messenger">Messenger</option>
                    <option value="web_chat">Web Chat</option>
                    <option value="mobile_app">Mobile App</option>
                    <option value="voice">Voice Call</option>
                  </select>
                </div>

                <div>
                  <label className="text-[10px] font-extrabold uppercase text-slate-400 tracking-wider block mb-1">
                    Priority Score: {newLeadForm.lead_score}
                  </label>
                  <input 
                    type="range"
                    min="1"
                    max="100"
                    value={newLeadForm.lead_score}
                    onChange={(e) => setNewLeadForm({ ...newLeadForm, lead_score: e.target.value })}
                    className="w-full accent-amber-500 bg-slate-850 mt-2 h-1.5 rounded"
                  />
                </div>
              </div>

              <div>
                <label className="text-[10px] font-extrabold uppercase text-slate-400 tracking-wider block mb-1.5">
                  Initial Design Specifications
                </label>
                <textarea 
                  rows="2"
                  value={newLeadForm.requirements}
                  onChange={(e) => setNewLeadForm({ ...newLeadForm, requirements: e.target.value })}
                  placeholder="Indicate sizing requirements, wood type preferences (Wanza, Zigba, Mahogany) etc..."
                  className="w-full bg-slate-950 border border-slate-850 text-slate-200 text-xs p-3 rounded-xl focus:outline-none focus:border-amber-500/50 resize-none font-sans"
                />
              </div>

              <div className="flex justify-end gap-3 pt-2">
                <button 
                  type="button"
                  onClick={() => setIsAddOpen(false)}
                  className="px-4 py-2.5 rounded-xl bg-slate-950 hover:bg-slate-850 border border-slate-800 text-xs font-bold text-slate-400 transition-colors"
                >
                  Cancel
                </button>
                <button 
                  type="submit"
                  className="px-5 py-2.5 rounded-xl bg-amber-500 hover:bg-amber-400 text-slate-950 text-xs font-black transition-colors"
                >
                  Add to Board
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

    </div>
  );
}
