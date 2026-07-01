import React, { useState, useMemo } from 'react';
import {
  PieChart,
  Pie,
  Cell,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  AreaChart,
  Area
} from 'recharts';
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
  MapPin,
  TrendingUp,
  DollarSign,
  X,
  Plus,
  AlertCircle,
  CheckCircle,
  Clock,
  ChevronRight,
  Sliders,
  Briefcase
} from 'lucide-react';

/**
 * ============================================================================
 * BEKANSI AI SALES ASSISTANT PLATFORM
 * LEAD STATUSES & SALES PIPELINE OBSERVABILITY DASHBOARD
 * ============================================================================
 * 
 * An executive observability dashboard designed for Bekansi Furniture (Ethiopia).
 * Centrally manages, parses, and visualizes:
 * 1. Distribution of Omnichannel Leads by Status (Hot, Warm, Cold) using glowing Pie/Donut charts.
 * 2. Absolute Sales Pipeline Valuations (ETB Birr) mapped across Status categories.
 * 3. Dynamic multi-variable filters (Preferred localized language, Source Channel, Furniture Type).
 * 4. Interactive CRM Prospect Grid with detailed workspace drawer.
 */

// Production-grade Seed Leads Database containing local Ethiopian profiles, locations, languages, and pipeline values.
const INITIAL_LEADS = [
  {
    id: "lead-291a-4c21",
    fullName: "Abebe Balcha",
    phoneNumber: "+251 91 124 5588",
    location: "Addis Ababa (Bole Atlas)",
    productInterest: "Wanza Premium L-Sofa Gara Set",
    productCategory: "Living Room",
    budgetRange: 280000,
    deliveryTimeline: "Urgent (Within 10 Days)",
    leadStatus: "Hot",
    preferredLanguage: "am", // Amharic
    sourceChannel: "Telegram",
    notes: "Requires custom high-gloss lacquer coating. Ready to transfer deposit immediately.",
    dateCreated: "2026-06-15"
  },
  {
    id: "lead-ebd0-128c",
    fullName: "Chaltu Lamessa",
    phoneNumber: "+251 92 344 1162",
    location: "Adama (Oromia)",
    productInterest: "Imperial Mahogany Bedroom Set",
    productCategory: "Bedroom",
    budgetRange: 420000,
    deliveryTimeline: "Flexible (30 Days)",
    leadStatus: "Warm",
    preferredLanguage: "om", // Afaan Oromo
    sourceChannel: "WhatsApp",
    notes: "Requesting free shipping options for complete collection to Adama city center.",
    dateCreated: "2026-06-14"
  },
  {
    id: "lead-fa09-882d",
    fullName: "Dawit Wolde",
    phoneNumber: "+251 90 411 9931",
    location: "Addis Ababa (Kazanchis)",
    productInterest: "Executive Modular Office desks (L-Shaped)",
    productCategory: "Office",
    budgetRange: 650000,
    deliveryTimeline: "Within 2 Weeks",
    leadStatus: "Hot",
    preferredLanguage: "en", // English
    sourceChannel: "Web Chat",
    notes: "Corporate contract for expansion. Requires multi-tenant isolation and invoice alignment.",
    dateCreated: "2026-06-16"
  },
  {
    id: "lead-119c-8a21",
    fullName: "Makeda Kassa",
    phoneNumber: "+251 91 140 2201",
    location: "Addis Ababa (CMC Compound)",
    productInterest: "Bespoke Solid Oak Kitchen Cabinetry",
    productCategory: "Kitchen Cabinets",
    budgetRange: 580000,
    deliveryTimeline: "45 Days (Renovation)",
    leadStatus: "Warm",
    preferredLanguage: "am", // Amharic
    sourceChannel: "Telegram",
    notes: "Kitchen dimensions shared. Waiting on 3D CAD design renderings from Bekansi design team.",
    dateCreated: "2026-06-12"
  },
  {
    id: "lead-3a9d-5192",
    fullName: "Dr. Kenenisa Dibaba",
    phoneNumber: "+251 94 488 4591",
    location: "Bishoftu (Debre Zeyit)",
    productInterest: "Royal Wanza Dining Table (10-Seater)",
    productCategory: "Dining Room",
    budgetRange: 195000,
    deliveryTimeline: "Within 3 Weeks",
    leadStatus: "Hot",
    preferredLanguage: "om", // Afaan Oromo
    sourceChannel: "WhatsApp",
    notes: "Requested traditional hand-carved coffee cup holder inserts and matte lacquer coat.",
    dateCreated: "2026-06-16"
  },
  {
    id: "lead-940b-ac89",
    fullName: "Yohannes Tekle",
    phoneNumber: "+251 92 511 6622",
    location: "Addis Ababa (Sarbet)",
    productInterest: "Heritage Accent Credenza & TV Stand",
    productCategory: "Heritage Accent",
    budgetRange: 125000,
    deliveryTimeline: "Immediate (Ready Product)",
    leadStatus: "Cold",
    preferredLanguage: "am", // Amharic
    sourceChannel: "Facebook",
    notes: "Browsing and comparing wood durability. Asked if Zigba wood is cheaper than Wanza core.",
    dateCreated: "2026-06-08"
  },
  {
    id: "lead-8bc4-712d",
    fullName: "Semira Kemal",
    phoneNumber: "+251 90 882 1104",
    location: "Hawassa (Sidama)",
    productInterest: "Luxury Tufted Headboard King Bed",
    productCategory: "Bedroom",
    budgetRange: 220000,
    deliveryTimeline: "Flexible",
    leadStatus: "Warm",
    preferredLanguage: "en", // English
    sourceChannel: "Web Chat",
    notes: "Comparing fabric swatches. Interested in Plush Royal Velvet upgrade (+8%).",
    dateCreated: "2026-06-11"
  },
  {
    id: "lead-01f2-9ccd",
    fullName: "Abdi Biya",
    phoneNumber: "0988828861",
    location: "Bishoftu City, Dukem Subcity",
    productInterest: "Custom Mahogany Lounge Seating Set",
    productCategory: "Living Room",
    budgetRange: 340000,
    deliveryTimeline: "Within 1 Month",
    leadStatus: "Hot",
    preferredLanguage: "am", // Amharic
    sourceChannel: "Telegram",
    notes: "High interest in Mahogany durability. Confirmed pricing matches high-gloss varnish.",
    dateCreated: "2026-06-17"
  },
  {
    id: "lead-771c-acdf",
    fullName: "Girma Tolossa",
    phoneNumber: "+251 91 155 0041",
    location: "Addis Ababa (Kazanchis)",
    productInterest: "Modular Executive Conference Room Suite",
    productCategory: "Office",
    budgetRange: 780000,
    deliveryTimeline: "60 Days (Contract)",
    leadStatus: "Warm",
    preferredLanguage: "en", // English
    sourceChannel: "Facebook",
    notes: "Requesting B2B volume discount structure. Waiting for manager review on corporate credit.",
    dateCreated: "2026-06-05"
  },
  {
    id: "lead-50ac-bcde",
    fullName: "Almaz Kebede",
    phoneNumber: "+251 91 002 9911",
    location: "Bishoftu City, Dukem Subcity",
    productInterest: "L-Shape Cozy Sofa (Misty Spruce)",
    productCategory: "Living Room",
    budgetRange: 160000,
    deliveryTimeline: "No Rush",
    leadStatus: "Cold",
    preferredLanguage: "am", // Amharic
    sourceChannel: "WhatsApp",
    notes: "Just comparing options. Visited Bishoftu showroom, noted layout size adjustments option.",
    dateCreated: "2026-06-01"
  },
  {
    id: "lead-9cf2-bbcf",
    fullName: "Obbo Tollera Desta",
    phoneNumber: "+251 92 110 9044",
    location: "Jimma (Oromia)",
    productInterest: "Zigba Timber School / Hall Benches",
    productCategory: "Heritage Accent",
    budgetRange: 290000,
    deliveryTimeline: "Before School Term Start",
    leadStatus: "Warm",
    preferredLanguage: "om", // Afaan Oromo
    sourceChannel: "Telegram",
    notes: "Requires durable kiln-dried preparation. Standard sizes confirmed with Jimma agents.",
    dateCreated: "2026-06-13"
  }
];

export default function LeadStatusPipelineChart() {
  const [leads, setLeads] = useState(INITIAL_LEADS);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('All'); // All, Hot, Warm, Cold
  const [channelFilter, setChannelFilter] = useState('All');
  const [languageFilter, setLanguageFilter] = useState('All');
  const [categoryFilter, setCategoryFilter] = useState('All');
  const [selectedLeadId, setSelectedLeadId] = useState(null);

  // New Lead Creation states
  const [showAddModal, setShowAddModal] = useState(false);
  const [newLeadName, setNewLeadName] = useState('');
  const [newLeadPhone, setNewLeadPhone] = useState('');
  const [newLeadLocation, setNewLeadLocation] = useState('Bishoftu City, Dukem Subcity');
  const [newLeadInterest, setNewLeadInterest] = useState('');
  const [newLeadCategory, setNewLeadCategory] = useState('Living Room');
  const [newLeadBudget, setNewLeadBudget] = useState('180000');
  const [newLeadTimeline, setNewLeadTimeline] = useState('Urgent');
  const [newLeadStatus, setNewLeadStatus] = useState('Hot');
  const [newLeadLanguage, setNewLeadLanguage] = useState('am');
  const [newLeadChannel, setNewLeadChannel] = useState('Telegram');
  const [newLeadNotes, setNewLeadNotes] = useState('');

  // Dropdown list configurations
  const CHANNELS = ['All', 'Telegram', 'WhatsApp', 'Web Chat', 'Facebook'];
  const LANGUAGES = [
    { id: 'All', label: 'All Languages' },
    { id: 'am', label: 'Amharic (አማርኛ)' },
    { id: 'om', label: 'Afaan Oromo' },
    { id: 'en', label: 'English' }
  ];
  const CATEGORIES = ['All', 'Living Room', 'Bedroom', 'Office', 'Dining Room', 'Kitchen Cabinets', 'Heritage Accent'];

  // Handle addition of new leads dynamically for visual prototyping
  const handleCreateLead = (e) => {
    e.preventDefault();
    if (!newLeadName || !newLeadPhone || !newLeadInterest) {
      alert('Please fill out Name, Phone and Product Interest.');
      return;
    }

    const created = {
      id: "lead-" + Math.random().toString(36).substr(2, 9),
      fullName: newLeadName,
      phoneNumber: newLeadPhone,
      location: newLeadLocation,
      productInterest: newLeadInterest,
      productCategory: newLeadCategory,
      budgetRange: parseInt(newLeadBudget) || 120000,
      deliveryTimeline: newLeadTimeline,
      leadStatus: newLeadStatus,
      preferredLanguage: newLeadLanguage,
      sourceChannel: newLeadChannel,
      notes: newLeadNotes || "Lead entered via administrative panel.",
      dateCreated: new Date().toISOString().split('T')[0]
    };

    setLeads([created, ...leads]);
    setShowAddModal(false);

    // Reset Form Fields state
    setNewLeadName('');
    setNewLeadPhone('');
    setNewLeadInterest('');
    setNewLeadNotes('');
  };

  // Filter application pipeline
  const filteredLeads = useMemo(() => {
    return leads.filter(lead => {
      const nmLower = lead.fullName.toLowerCase();
      const prLower = lead.productInterest.toLowerCase();
      const lcLower = lead.location.toLowerCase();
      const sqLower = searchQuery.toLowerCase();

      const matchesSearch = 
        nmLower.includes(sqLower) || 
        prLower.includes(sqLower) || 
        lcLower.includes(sqLower) || 
        lead.phoneNumber.includes(searchQuery);

      const matchesStatus = statusFilter === 'All' || lead.leadStatus === statusFilter;
      const matchesChannel = channelFilter === 'All' || lead.sourceChannel === channelFilter;
      const matchesLanguage = languageFilter === 'All' || lead.preferredLanguage === languageFilter;
      const matchesCategory = categoryFilter === 'All' || lead.productCategory === categoryFilter;

      return matchesSearch && matchesStatus && matchesChannel && matchesLanguage && matchesCategory;
    });
  }, [leads, searchQuery, statusFilter, channelFilter, languageFilter, categoryFilter]);

  // Aggregate high-fidelity metrics
  const stats = useMemo(() => {
    const totalLeadsCount = filteredLeads.length;
    const totalPipelineValue = filteredLeads.reduce((acc, lead) => acc + lead.budgetRange, 0);
    const averageLeadBudget = totalLeadsCount > 0 ? Math.round(totalPipelineValue / totalLeadsCount) : 0;

    // Direct counts per status
    const hotCount = filteredLeads.filter(l => l.leadStatus === 'Hot').length;
    const warmCount = filteredLeads.filter(l => l.leadStatus === 'Warm').length;
    const coldCount = filteredLeads.filter(l => l.leadStatus === 'Cold').length;

    // Values per status
    const hotValue = filteredLeads.filter(l => l.leadStatus === 'Hot').reduce((acc, curr) => acc + curr.budgetRange, 0);
    const warmValue = filteredLeads.filter(l => l.leadStatus === 'Warm').reduce((acc, curr) => acc + curr.budgetRange, 0);
    const coldValue = filteredLeads.filter(l => l.leadStatus === 'Cold').reduce((acc, curr) => acc + curr.budgetRange, 0);

    const hotPct = totalLeadsCount > 0 ? Math.round((hotCount / totalLeadsCount) * 100) : 0;
    const warmPct = totalLeadsCount > 0 ? Math.round((warmCount / totalLeadsCount) * 100) : 0;
    const coldPct = totalLeadsCount > 0 ? Math.round((coldCount / totalLeadsCount) * 100) : 0;

    return {
      count: totalLeadsCount,
      pipeline: totalPipelineValue,
      avgValue: averageLeadBudget,
      hotCount,
      warmCount,
      coldCount,
      hotValue,
      warmValue,
      coldValue,
      hotPct,
      warmPct,
      coldPct
    };
  }, [filteredLeads]);

  // Format Recharts status distribution data
  const chartDataStatusDistribution = useMemo(() => {
    return [
      { name: '🔥 Hot Lead', value: stats.hotCount, rawValue: stats.hotValue, color: '#F43F5E' },    // Rose
      { name: '🟡 Warm Lead', value: stats.warmCount, rawValue: stats.warmValue, color: '#10B981' }, // Emerald
      { name: '⚪ Cold Lead', value: stats.coldCount, rawValue: stats.coldValue, color: '#3B82F6' } // Royal Blue
    ];
  }, [stats]);

  // Format Recharts pipeline status values data
  const chartDataPipelineValue = useMemo(() => {
    return [
      { status: 'Hot', value: Math.round(stats.hotValue / 1000), leads: stats.hotCount, fill: '#F43F5E' },
      { status: 'Warm', value: Math.round(stats.warmValue / 1000), leads: stats.warmCount, fill: '#10B981' },
      { status: 'Cold', value: Math.round(stats.coldValue / 1000), leads: stats.coldCount, fill: '#3B82F6' }
    ];
  }, [stats]);

  const focusedLead = useMemo(() => {
    return leads.find(l => l.id === selectedLeadId) || null;
  }, [leads, selectedLeadId]);

  // Design standard Recharts custom renderers
  const CustomPieTooltip = ({ active, payload }) => {
    if (active && payload && payload.length) {
      const data = payload[0].payload;
      return (
        <div className="bg-slate-900 border border-slate-800 p-3.5 rounded-xl shadow-2xl backdrop-blur-md">
          <p className="text-xs font-bold text-slate-100 flex items-center gap-1.5 mb-1.5">
            <Circle className="w-2.5 h-2.5 fill-current border-none" style={{ color: data.color }} />
            {data.name}
          </p>
          <div className="text-[11.5px] text-slate-300 space-y-1">
            <div className="flex justify-between gap-6">
              <span>Leads volume:</span>
              <span className="font-bold text-white">{data.value}</span>
            </div>
            <div className="flex justify-between gap-6 border-t border-slate-800 mt-1 pt-1">
              <span>Cumulative value:</span>
              <span className="font-extrabold text-amber-500">{data.rawValue.toLocaleString()} ETB</span>
            </div>
          </div>
        </div>
      );
    }
    return null;
  };

  const CustomBarTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
      const data = payload[0].payload;
      return (
        <div className="bg-slate-900 border border-slate-800 p-3.5 rounded-xl shadow-2xl backdrop-blur-md">
          <p className="text-xs font-extrabold text-slate-100 mb-2">
            Status Category: {label}
          </p>
          <div className="text-[11.5px] text-slate-300 space-y-1">
            <div className="flex justify-between gap-6">
              <span>Active Pipeline:</span>
              <span className="font-black text-amber-500">{(data.value * 1000).toLocaleString()} ETB</span>
            </div>
            <div className="flex justify-between gap-6">
              <span>Lead counts:</span>
              <span className="font-semibold text-slate-200">{data.leads} Leads</span>
            </div>
          </div>
        </div>
      );
    }
    return null;
  };

  return (
    <div className="w-full bg-slate-950 text-white min-h-screen p-6 font-sans">
      
      {/* Executive Header Segment */}
      <div className="flex flex-col xl:flex-row xl:items-center justify-between border-b border-slate-900 pb-6 mb-6 gap-4">
        <div>
          <div className="flex items-center gap-2 mb-2">
            <span className="bg-amber-500/10 text-amber-500 text-[10px] uppercase font-bold tracking-widest px-2.5 py-1 rounded-md border border-amber-500/15 flex items-center gap-1">
              <Activity className="w-3 h-3 text-amber-500 animate-pulse" />
              SaaS Sales Observability
            </span>
            <span className="flex items-center gap-1.5 text-[11px] text-slate-500 font-medium">
              <Plus className="w-3.5 h-3.5" /> Synchronized with Bekansi Sales Core
            </span>
          </div>
          <h1 className="text-2xl md:text-3xl font-black text-slate-50 tracking-tight">
            Lead Pipelines & Conversion
          </h1>
          <p className="text-slate-400 text-sm mt-1">
            Analyze the absolute distribution of qualified customer leads and corresponding sales workspace values (ETB).
          </p>
        </div>

        {/* Global Admin Interactive Handlers */}
        <div className="flex items-center gap-3">
          <button 
            onClick={() => {
              setSearchQuery('');
              setStatusFilter('All');
              setChannelFilter('All');
              setLanguageFilter('All');
              setCategoryFilter('All');
            }}
            className="bg-slate-900 hover:bg-slate-850 p-2.5 px-3.5 rounded-xl border border-slate-800 transition-all text-slate-400 hover:text-white text-xs font-semibold flex items-center gap-1.5"
          >
            Reset Filters
          </button>
          
          <button 
            onClick={() => setShowAddModal(true)}
            className="bg-amber-500 hover:bg-amber-400 text-slate-950 font-black px-4.5 py-2.5 rounded-xl transition-all shadow-lg flex items-center gap-1.5 text-xs"
          >
            <Plus className="w-4 h-4 stroke-[2.5]" />
            Register New Lead
          </button>
        </div>
      </div>

      {/* Structured Executive KPI Cards Strip */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5 mb-6">
        
        {/* Total Pipeline value Card */}
        <div className="bg-slate-900 border border-slate-900 p-5 rounded-2xl relative overflow-hidden flex flex-col justify-between">
          <div className="space-y-1">
            <span className="text-[10px] text-slate-400 font-bold uppercase tracking-wider block">Total Pipeline Value</span>
            <h3 className="text-2xl font-black text-amber-500">
              {stats.pipeline.toLocaleString()} <span className="text-xs font-semibold text-slate-400 uppercase">Birr</span>
            </h3>
          </div>
          <p className="text-[11px] text-slate-500 mt-4 flex items-center gap-1">
            <TrendingUp className="w-3.5 h-3.5 text-emerald-500" /> Active quotes across {stats.count} prospects
          </p>
        </div>

        {/* Total Leads Card */}
        <div className="bg-slate-900 border border-slate-900 p-5 rounded-2xl relative overflow-hidden flex flex-col justify-between">
          <div className="space-y-1">
            <span className="text-[10px] text-slate-400 font-bold uppercase tracking-wider block">Active Leads Volume</span>
            <h3 className="text-2xl font-black text-white">
              {stats.count} <span className="text-xs font-semibold text-slate-500">Organizations</span>
            </h3>
          </div>
          <div className="flex gap-2.5 mt-3 text-[11px]">
            <span className="text-rose-400 font-bold flex items-center gap-0.5">🔥 {stats.hotCount} Hot</span>
            <span className="text-emerald-400 font-bold flex items-center gap-0.5">● {stats.warmCount} Warm</span>
            <span className="text-blue-400 font-bold flex items-center gap-0.5">○ {stats.coldCount} Cold</span>
          </div>
        </div>

        {/* Average budget Lead potential Card */}
        <div className="bg-slate-900 border border-slate-900 p-5 rounded-2xl relative overflow-hidden flex flex-col justify-between">
          <div className="space-y-1">
            <span className="text-[10px] text-slate-400 font-bold uppercase tracking-wider block">Average Deal Sizing</span>
            <h3 className="text-2xl font-black text-slate-100">
              {stats.avgValue.toLocaleString()} <span className="text-xs font-semibold text-slate-500">ETB</span>
            </h3>
          </div>
          <p className="text-[11px] text-slate-400 mt-4">
            Custom wood and residential orders
          </p>
        </div>

        {/* Hot lead ratios velocity card */}
        <div className="bg-slate-900 border border-slate-900 p-5 rounded-2xl relative overflow-hidden flex flex-col justify-between">
          <div className="space-y-1">
            <span className="text-[10px] text-slate-400 font-bold uppercase tracking-wider block">Lead Status Distribution</span>
            <div className="flex items-baseline gap-2 mt-1">
              <span className="text-2xl font-black text-rose-500">{stats.hotPct}%</span>
              <span className="text-xs text-slate-400 font-medium font-serif leading-none">Hot / Urgent Intent</span>
            </div>
          </div>
          <div className="w-full bg-slate-950 h-1.5 rounded-full overflow-hidden mt-3.5 flex">
            <div className="bg-rose-500 h-full" style={{ width: `${stats.hotPct}%` }} />
            <div className="bg-emerald-500 h-full" style={{ width: `${stats.warmPct}%` }} />
            <div className="bg-blue-500 h-full" style={{ width: `${stats.coldPct}%` }} />
          </div>
        </div>

      </div>

      {/* Interactive Multi-facets Filters Area */}
      <div className="bg-slate-900 border border-slate-900 p-4.5 rounded-2xl flex flex-col xl:flex-row xl:items-center justify-between gap-4 mb-6">
        
        {/* Keyword Search input */}
        <div className="relative flex-1 max-w-sm">
          <Search className="w-3.5 h-3.5 ml-3 absolute top-1/2 -translate-y-1/2 text-slate-500" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search leads by name, product interest, city..."
            className="w-full bg-slate-950 border border-slate-800 rounded-xl py-2 pl-9 pr-3 text-xs text-slate-200 placeholder-slate-500 focus:outline-none focus:border-amber-500/50 focus:ring-1 focus:ring-amber-500/25 transition-all text-ellipsis"
          />
        </div>

        {/* Dropdown selectors */}
        <div className="flex flex-wrap items-center gap-3">
          
          {/* Status selector */}
          <div className="flex items-center bg-slate-950 border border-slate-800 rounded-xl px-2.5 py-1.5 text-xs text-slate-300 gap-1.5">
            <Sliders className="w-3.5 h-3.5 text-slate-500" />
            <span className="text-slate-500 font-semibold text-xs">Status:</span>
            <select 
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="bg-transparent border-none text-slate-200 font-bold focus:outline-none cursor-pointer text-xs"
            >
              <option value="All">All statuses</option>
              <option value="Hot">🔥 Hot leads</option>
              <option value="Warm">🟡 Warm leads</option>
              <option value="Cold">⚪ Cold leads</option>
            </select>
          </div>

          {/* Channel selector */}
          <div className="flex items-center bg-slate-950 border border-slate-800 rounded-xl px-2.5 py-1.5 text-xs text-slate-300 gap-1.5">
            <MessageSquare className="w-3.5 h-3.5 text-slate-500" />
            <span className="text-slate-500 font-semibold text-xs text-ellipsis">Channel:</span>
            <select 
              value={channelFilter}
              onChange={(e) => setChannelFilter(e.target.value)}
              className="bg-transparent border-none text-slate-200 font-bold focus:outline-none cursor-pointer text-xs"
            >
              {CHANNELS.map(ch => (
                <option key={ch} value={ch}>{ch === 'All' ? 'All Channels' : ch}</option>
              ))}
            </select>
          </div>

          {/* Category selector */}
          <div className="flex items-center bg-slate-950 border border-slate-800 rounded-xl px-2.5 py-1.5 text-xs text-slate-300 gap-1.5">
            <Briefcase className="w-3.5 h-3.5 text-slate-500" />
            <span className="text-slate-500 font-semibold text-xs">Category:</span>
            <select 
              value={categoryFilter}
              onChange={(e) => setCategoryFilter(e.target.value)}
              className="bg-transparent border-none text-slate-200 font-bold focus:outline-none cursor-pointer text-xs"
            >
              {CATEGORIES.map(cat => (
                <option key={cat} value={cat}>{cat === 'All' ? 'All Furniture' : cat}</option>
              ))}
            </select>
          </div>

          {/* Language selector */}
          <div className="flex items-center bg-slate-950 border border-slate-800 rounded-xl px-2.5 py-1.5 text-xs text-slate-300 gap-1.5">
            <Globe className="w-3.5 h-3.5 text-slate-500" />
            <span className="text-slate-500 font-semibold text-xs">Client Lang:</span>
            <select 
              value={languageFilter}
              onChange={(e) => setLanguageFilter(e.target.value)}
              className="bg-transparent border-none text-slate-200 font-bold focus:outline-none cursor-pointer text-xs border-r pr-1"
            >
              {LANGUAGES.map(lang => (
                <option key={lang.id} value={lang.id}>{lang.label}</option>
              ))}
            </select>
          </div>

        </div>

      </div>

      {/* Dual Charts Visualization Section */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
        
        {/* Pie/Donut Chart: Lead Status Volume Distribution */}
        <div className="bg-slate-900 border border-slate-900 rounded-2xl p-5 space-y-4">
          <div>
            <h3 className="text-base font-extrabold text-slate-50 flex items-center gap-1.5">
              <Circle className="w-2.5 h-2.5 fill-amber-500 stroke-none" />
              Lead Status Count Distribution
            </h3>
            <p className="text-xs text-slate-500 mt-1">
              Visualizes the current volume proportion of leads in Hot, Warm, and Cold categories.
            </p>
          </div>

          {totalLeadsCountRaw() === 0 ? (
            <div className="h-[260px] flex items-center justify-center text-slate-500 text-xs italic">
              No matching data inside current filtered parameters.
            </div>
          ) : (
            <div className="h-[260px] flex flex-col sm:flex-row items-center gap-2">
              <div className="flex-1 w-full h-full relative">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Tooltip content={<CustomPieTooltip />} />
                    <Pie
                      data={chartDataStatusDistribution}
                      cx="50%"
                      cy="50%"
                      innerRadius={65}
                      outerRadius={85}
                      paddingAngle={5}
                      dataKey="value"
                    >
                      {chartDataStatusDistribution.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={entry.color} stroke="#0f172a" strokeWidth={2} />
                      ))}
                    </Pie>
                  </PieChart>
                </ResponsiveContainer>
                {/* Embedded absolute totals counter */}
                <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 text-center select-none">
                  <span className="text-[10px] uppercase font-bold text-slate-500 block leading-none">Filtered</span>
                  <span className="text-2xl font-black text-white leading-none block mt-1">{stats.count}</span>
                  <span className="text-[10px] text-slate-400 block leading-none mt-1">Leads</span>
                </div>
              </div>

              {/* High-fidelity responsive legends block */}
              <div className="space-y-2.5 w-full sm:w-48 shrink-0 border-l border-slate-850 pl-4 py-2">
                {chartDataStatusDistribution.map((entry) => (
                  <div key={entry.name} className="space-y-1">
                    <div className="flex items-center justify-between text-xs">
                      <span className="flex items-center gap-1.5 text-slate-300 leading-none">
                        <span className="w-2 h-2 rounded-full" style={{ backgroundColor: entry.color }} />
                        {entry.name}
                      </span>
                      <span className="font-bold text-white">
                        {entry.value} ({stats.count > 0 ? Math.round((entry.value / stats.count) * 100) : 0}%)
                      </span>
                    </div>
                    <span className="text-[10px] font-mono text-slate-500 block pl-3.5">
                      Value: {entry.rawValue.toLocaleString()} Birr
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Bar Chart: Sales Pipeline Value by Lead Status */}
        <div className="bg-slate-900 border border-slate-900 rounded-2xl p-5 space-y-4">
          <div>
            <h3 className="text-base font-extrabold text-slate-50 flex items-center gap-1.5">
              <TrendingUp className="w-4 h-4 text-emerald-500" />
              Cumulative Pipeline Value by Status
            </h3>
            <p className="text-xs text-slate-500 mt-1">
              Visualizes the economic weight of prospective customer pipelines grouped by lead intent status.
            </p>
          </div>

          {totalLeadsCountRaw() === 0 ? (
            <div className="h-[260px] flex items-center justify-center text-slate-500 text-xs italic">
              No matching data inside current filtered parameters.
            </div>
          ) : (
            <div className="h-[260px] w-full">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart
                  data={chartDataPipelineValue}
                  margin={{ top: 10, right: 10, left: -15, bottom: 0 }}
                >
                  <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
                  <XAxis dataKey="status" stroke="#64748b" fontSize={11} tickLine={false} />
                  <YAxis 
                    stroke="#64748b" 
                    fontSize={11} 
                    tickLine={false}
                    tickFormatter={(v) => `${v}k`}
                  />
                  <Tooltip content={<CustomBarTooltip />} />
                  <Bar dataKey="value" radius={[5, 5, 0, 0]} barSize={55}>
                    {chartDataPipelineValue.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.fill} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          )}
        </div>

      </div>

      {/* Lead Prospect Grid & Details Sidebar Area */}
      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
        
        {/* Interactive Prospect Grid Table */}
        <div className="lg:col-span-3 space-y-4">
          
          <div className="flex items-center justify-between px-1">
            <h2 className="text-lg font-black text-slate-100 flex items-center gap-1.5">
              <Users className="w-4 h-4 text-slate-400" /> Current Prospective Leads Pool
            </h2>
            <span className="text-xs text-slate-500 font-medium font-mono">
              Showing {filteredLeads.length} of {leads.length} recorded leads
            </span>
          </div>

          <div className="bg-slate-900 border border-slate-900 rounded-2xl overflow-hidden shadow-2xl">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs text-slate-300 border-collapse">
                <thead>
                  <tr className="border-b border-slate-850 bg-slate-900/60 uppercase text-[10.5px] font-bold tracking-wider text-slate-400">
                    <th scope="col" className="py-4 px-5">Lead Identity</th>
                    <th scope="col" className="py-4 px-4 text-center">Status</th>
                    <th scope="col" className="py-4 px-4">Product of Interest</th>
                    <th scope="col" className="py-4 px-4">Pipeline Value</th>
                    <th scope="col" className="py-4 px-4 text-center">Language</th>
                    <th scope="col" className="py-4 px-4">Channel Origin</th>
                    <th scope="col" className="py-4 px-5 text-right">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-850">
                  {filteredLeads.length === 0 ? (
                    <tr>
                      <td colSpan="7" className="text-center py-12 text-slate-500">
                        <AlertCircle className="w-8 h-8 text-slate-600 mx-auto mb-3" />
                        <p className="font-semibold text-slate-400">No leads match filters</p>
                        <p className="text-xs text-slate-600 mt-1">Try resetting search or adjusting categories selection parameters.</p>
                      </td>
                    </tr>
                  ) : (
                    filteredLeads.map((lead) => {
                      const isSelected = lead.id === selectedLeadId;
                      return (
                        <tr
                          key={lead.id}
                          onClick={() => setSelectedLeadId(lead.id)}
                          className={`hover:bg-slate-850/80 transition-colors cursor-pointer group ${
                            isSelected ? 'bg-slate-850' : ''
                          }`}
                        >
                          {/* Identity block */}
                          <td className="py-4 px-5">
                            <div className="font-bold text-slate-100 group-hover:text-amber-500 transition-colors">{lead.fullName}</div>
                            <div className="text-[10px] text-slate-500 mt-0.5">{lead.location}</div>
                          </td>

                          {/* Status Badge */}
                          <td className="py-4 px-4 text-center">
                            <span className={`px-2.5 py-0.5 text-[10px] rounded-full font-bold uppercase ${
                              lead.leadStatus === 'Hot'
                                ? 'bg-rose-500/15 text-rose-400 border border-rose-500/20'
                                : lead.leadStatus === 'Warm'
                                ? 'bg-emerald-500/15 text-emerald-400 border border-emerald-500/20'
                                : 'bg-blue-500/15 text-blue-400 border border-blue-500/20'
                            }`}>
                              {lead.leadStatus}
                            </span>
                          </td>

                          {/* Product of interest */}
                          <td className="py-4 px-4 max-w-[180px] truncate">
                            <span className="font-medium text-slate-200 block truncate">{lead.productInterest}</span>
                            <span className="text-[10px] text-slate-500 uppercase block">{lead.productCategory}</span>
                          </td>

                          {/* Budget / Pipeline Value */}
                          <td className="py-4 px-4 font-bold text-slate-200">
                            {lead.budgetRange.toLocaleString()} <span className="text-[9.5px] text-slate-500 tracking-wider">ETB</span>
                          </td>

                          {/* Localized Language setting */}
                          <td className="py-4 px-4 text-center font-bold">
                            <span className={`px-2 py-0.5 text-[11px] rounded uppercase ${
                              lead.preferredLanguage === 'am'
                                ? 'bg-amber-500/10 text-amber-500'
                                : lead.preferredLanguage === 'om'
                                ? 'bg-emerald-500/10 text-emerald-500'
                                : 'bg-sky-500/10 text-sky-500'
                            }`}>
                              {lead.preferredLanguage === 'am' ? 'አማ' : lead.preferredLanguage === 'om' ? 'OM' : 'EN'}
                            </span>
                          </td>

                          {/* Channel Badge */}
                          <td className="py-4 px-4 font-medium text-slate-300">
                            {lead.sourceChannel}
                          </td>

                          {/* Action view triggers */}
                          <td className="py-4 px-5 text-right">
                            <button className="p-1 px-2.5 bg-slate-950 border border-slate-800 hover:bg-slate-800 rounded-lg text-slate-400 group-hover:text-amber-500 transition-colors inline-flex items-center gap-1 text-[11px] font-bold">
                              Explore <ChevronRight className="w-3.5 h-3.5" />
                            </button>
                          </td>
                        </tr>
                      );
                    })
                  )}
                </tbody>
              </table>
            </div>

            {/* Custom Interactive table footer */}
            <div className="bg-slate-900/40 border-t border-slate-850 p-4 px-5 flex items-center justify-between text-xs text-slate-500">
              <span>Displaying <b>{filteredLeads.length}</b> leads. Total potential of <b>{filteredLeads.reduce((a, b) => a + b.budgetRange, 0).toLocaleString()} Birr</b></span>
              <span>Timezone: East Africa (Addis Ababa)</span>
            </div>
          </div>

        </div>

        {/* Selected Lead Workspace details Column */}
        <div className="lg:col-span-1">
          {focusedLead ? (
            <div className="bg-slate-900 border border-slate-900 p-5 rounded-2xl space-y-4 shadow-2xl sticky top-6">
              
              {/* Header block */}
              <div className="flex items-center justify-between border-b border-slate-850 pb-3">
                <span className="text-xs font-black text-rose-500 uppercase tracking-widest flex items-center gap-1.5">
                  <span className="w-2.5 h-2.5 rounded-full bg-rose-5 animate-pulse" style={{ backgroundColor: focusedLead.leadStatus === 'Hot' ? '#F43F5E' : focusedLead.leadStatus === 'Warm' ? '#10B981' : '#3B82F6' }} />
                  {focusedLead.leadStatus} Workspace
                </span>
                <button 
                  onClick={() => setSelectedLeadId(null)}
                  className="text-xs text-slate-500 hover:text-white"
                >
                  Close
                </button>
              </div>

              {/* Identity Spot */}
              <div className="space-y-1.5">
                <h3 className="font-extrabold text-white text-lg leading-tight">{focusedLead.fullName}</h3>
                <div className="flex items-center gap-1.5 text-xs text-slate-400">
                  <MapPin className="w-3.5 h-3.5 text-slate-500 flex-shrink-0" />
                  <span>{focusedLead.location}</span>
                </div>
              </div>

              {/* Direct channels parameters */}
              <div className="bg-slate-950 p-3 rounded-xl border border-slate-850 space-y-2.5 text-[11.5px]">
                <div className="flex items-center gap-2.5 text-slate-300">
                  <Phone className="w-3.5 h-3.5 text-slate-500" />
                  <span className="font-medium">{focusedLead.phoneNumber}</span>
                </div>
                <div className="flex items-center gap-2.5 text-slate-300">
                  <MessageSquare className="w-3.5 h-3.5 text-slate-500" />
                  <span>Origin: <span className="text-amber-500 font-bold font-mono">{focusedLead.sourceChannel}</span></span>
                </div>
                <div className="flex items-center gap-2.5 text-slate-300">
                  <Globe className="w-3.5 h-3.5 text-slate-500" />
                  <span>Language: <span className="font-bold">{focusedLead.preferredLanguage === 'am' ? 'Amharic (አማርኛ)' : focusedLead.preferredLanguage === 'om' ? 'Afaan Oromo' : 'English'}</span></span>
                </div>
              </div>

              {/* Customer design interest card */}
              <div className="space-y-1.5">
                <span className="text-[10px] text-slate-500 font-black uppercase tracking-wider block">Product of Interest</span>
                <div className="bg-slate-950/70 border border-slate-850 p-3 rounded-xl">
                  <span className="text-xs font-extrabold text-slate-200 block">{focusedLead.productInterest}</span>
                  <span className="text-[10px] text-slate-400 font-mono tracking-widest mt-1 block">{focusedLead.productCategory.toUpperCase()}</span>
                </div>
              </div>

              {/* Live transcript logs */}
              <div className="space-y-1.5">
                <span className="text-[10px] text-slate-500 font-black uppercase tracking-wider block">Captured Client Message Notes</span>
                <div className="bg-slate-950 p-3.5 rounded-xl border border-slate-850">
                  <p className="text-xs text-slate-300 italic leading-relaxed">
                    "{focusedLead.notes}"
                  </p>
                </div>
              </div>

              {/* Detailed schedule metrics */}
              <div className="grid grid-cols-2 gap-3 text-[11px] pt-1.5">
                <div>
                  <span className="text-slate-500 block">Lead Pipeline Value:</span>
                  <span className="text-sm font-black text-amber-500">{focusedLead.budgetRange.toLocaleString()} ETB</span>
                </div>
                <div>
                  <span className="text-slate-500 block">Requested Delivery:</span>
                  <span className="text-xs text-slate-300 font-semibold">{focusedLead.deliveryTimeline}</span>
                </div>
              </div>

              {/* Follow-up triggers */}
              <div className="bg-slate-950 px-3.5 py-3 rounded-xl border border-dashed border-slate-800">
                <span className="text-[10px] text-slate-500 font-bold block mb-1">Recommended Follow-up Steps:</span>
                <p className="text-xs text-amber-500 font-bold flex items-center gap-1">
                  <Sparkles className="w-3.5 h-3.5 text-amber-500" /> 
                  Contact via {focusedLead.sourceChannel} with custom photo and RLS quotation.
                </p>
              </div>

              <div className="flex gap-2">
                <button 
                  onClick={() => {
                    const statusCycle = focusedLead.leadStatus === 'Hot' ? 'Warm' : focusedLead.leadStatus === 'Warm' ? 'Cold' : 'Hot';
                    setLeads(leads.map(lead => lead.id === focusedLead.id ? { ...lead, leadStatus: statusCycle } : lead));
                  }}
                  className="bg-slate-950 border border-slate-800 hover:bg-slate-850 text-slate-300 py-2 rounded-xl text-[11px] font-bold flex-1 transition-all"
                >
                  Adjust Status
                </button>
                <a 
                  href={`tel:${focusedLead.phoneNumber}`}
                  className="bg-amber-500 hover:bg-amber-400 text-slate-950 py-2 rounded-xl text-[11px] font-black text-center flex-1 transition-all"
                >
                  Initiate Call
                </a>
              </div>

            </div>
          ) : (
            <div className="bg-slate-900/40 border border-dashed border-slate-850 rounded-2xl p-8 py-14 text-center text-slate-500 space-y-3 sticky top-6">
              <Plus className="w-10 h-10 text-slate-850 mx-auto" />
              <div>
                <p className="font-bold text-slate-400 text-sm">No Prospect Workspace Active</p>
                <p className="text-xs text-slate-600 mt-1 max-w-[200px] mx-auto leading-relaxed">
                  Click on any row in the prospective leads list to view full profile details and dispatch callbacks.
                </p>
              </div>
            </div>
          )}
        </div>

      </div>

      {/* Dynamic Lead Registration Modal popup */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/80 backdrop-blur-md p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl w-full max-w-xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
            
            {/* Modal Header */}
            <div className="p-5 border-b border-slate-850 flex items-center justify-between bg-slate-900/60">
              <h3 className="text-lg font-black text-white flex items-center gap-1.5">
                <Users className="w-5 h-5 text-amber-500" />
                Register New Omnichannel Prospect Lead
              </h3>
              <button 
                onClick={() => setShowAddModal(false)}
                className="p-1 text-slate-500 hover:text-white hover:bg-slate-800 rounded-lg transition-colors"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {/* Modal Form Scroll Area */}
            <form onSubmit={handleCreateLead} className="p-6 space-y-4 overflow-y-auto flex-1">
              
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                
                {/* Full name input */}
                <div className="space-y-1">
                  <label className="text-xs text-slate-400 font-semibold block">Prospect Full Name:</label>
                  <input 
                    type="text" 
                    required
                    value={newLeadName}
                    onChange={(e) => setNewLeadName(e.target.value)}
                    placeholder="e.g. Obbo Kedir Mohammed"
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl p-2.5 text-xs text-slate-200 focus:outline-none focus:border-amber-500"
                  />
                </div>

                {/* Telephone */}
                <div className="space-y-1">
                  <label className="text-xs text-slate-400 font-semibold block">Phone Number (+251):</label>
                  <input 
                    type="text" 
                    required
                    value={newLeadPhone}
                    onChange={(e) => setNewLeadPhone(e.target.value)}
                    placeholder="+251 91 100 0000"
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl p-2.5 text-xs text-slate-200 focus:outline-none focus:border-amber-500 font-mono"
                  />
                </div>

                {/* Local Area location */}
                <div className="space-y-1">
                  <label className="text-xs text-slate-400 font-semibold block">Local Location / City:</label>
                  <input 
                    type="text" 
                    value={newLeadLocation}
                    onChange={(e) => setNewLeadLocation(e.target.value)}
                    placeholder="e.g. Addis Ababa (Bole Atlas) or Hawassa"
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl p-2.5 text-xs text-slate-200 focus:outline-none focus:border-amber-500"
                  />
                </div>

                {/* Status selection */}
                <div className="space-y-1">
                  <label className="text-xs text-slate-400 font-semibold block">Lead Status classification:</label>
                  <select 
                    value={newLeadStatus}
                    onChange={(e) => setNewLeadStatus(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl p-2.5 text-xs text-slate-200 focus:outline-none focus:border-amber-500"
                  >
                    <option value="Hot">🔥 Hot lead (Ready to purchase / Urgent)</option>
                    <option value="Warm">🟡 Warm lead (Interested / Deciding)</option>
                    <option value="Cold">⚪ Cold lead (Browsing / Inquiry grid)</option>
                  </select>
                </div>

                {/* Specific product interest */}
                <div className="space-y-1">
                  <label className="text-xs text-slate-400 font-semibold block">Product Interest:</label>
                  <input 
                    type="text" 
                    required
                    value={newLeadInterest}
                    onChange={(e) => setNewLeadInterest(e.target.value)}
                    placeholder="e.g. Solid Mahogany Dining Table & Chairs"
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl p-2.5 text-xs text-slate-200 focus:outline-none focus:border-amber-500"
                  />
                </div>

                {/* Category select */}
                <div className="space-y-1">
                  <label className="text-xs text-slate-400 font-semibold block">Furniture Album Category:</label>
                  <select 
                    value={newLeadCategory}
                    onChange={(e) => setNewLeadCategory(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl p-2.5 text-xs text-slate-200 focus:outline-none focus:border-amber-500"
                  >
                    {CATEGORIES.filter(c => c !== 'All').map(cat => (
                      <option key={cat} value={cat}>{cat}</option>
                    ))}
                  </select>
                </div>

                {/* Potential budget range */}
                <div className="space-y-1">
                  <label className="text-xs text-slate-400 font-semibold block">Prospect Budget Value (ETB Birr):</label>
                  <input 
                    type="number" 
                    value={newLeadBudget}
                    onChange={(e) => setNewLeadBudget(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl p-2.5 text-xs text-slate-200 focus:outline-none focus:border-amber-500 font-mono"
                  />
                </div>

                {/* Requested delivery timeline */}
                <div className="space-y-1">
                  <label className="text-xs text-slate-400 font-semibold block">Delivery Timeline:</label>
                  <input 
                    type="text" 
                    value={newLeadTimeline}
                    onChange={(e) => setNewLeadTimeline(e.target.value)}
                    placeholder="e.g. Within 15 Days or Flexible"
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl p-2.5 text-xs text-slate-200 focus:outline-none focus:border-amber-500"
                  />
                </div>

                {/* Preferred Language for consultations */}
                <div className="space-y-1">
                  <label className="text-xs text-slate-400 font-semibold block">Consultation Language:</label>
                  <select 
                    value={newLeadLanguage}
                    onChange={(e) => setNewLeadLanguage(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl p-2.5 text-xs text-slate-200 focus:outline-none focus:border-amber-500"
                  >
                    <option value="am">Amharic (አማርኛ)</option>
                    <option value="om">Afaan Oromo</option>
                    <option value="en">English (B2B)</option>
                  </select>
                </div>

                {/* Source marketing channel */}
                <div className="space-y-1">
                  <label className="text-xs text-slate-400 font-semibold block">Source Channel Origin:</label>
                  <select 
                    value={newLeadChannel}
                    onChange={(e) => setNewLeadChannel(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl p-2.5 text-xs text-slate-200 focus:outline-none focus:border-amber-500"
                  >
                    {CHANNELS.filter(c => c !== 'All').map(ch => (
                      <option key={ch} value={ch}>{ch}</option>
                    ))}
                  </select>
                </div>

              </div>

              {/* Consultation logs detailed notes */}
              <div className="space-y-1">
                <label className="text-xs text-slate-400 font-semibold block">Omnichannel transcript notes & requests:</label>
                <textarea 
                  rows="3"
                  value={newLeadNotes}
                  onChange={(e) => setNewLeadNotes(e.target.value)}
                  placeholder="Insert any detailed notes from dialog conversation. E.g. likes natural wood finishing styles, requests 15% discount for Dukem region..."
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl p-2.5 text-xs text-slate-200 focus:outline-none focus:border-amber-500"
                />
              </div>

              {/* Action commands */}
              <div className="flex items-center gap-3 pt-4 border-t border-slate-850">
                <button 
                  type="button"
                  onClick={() => setShowAddModal(false)}
                  className="bg-slate-950 border border-slate-800 hover:bg-slate-850 text-slate-300 py-2.5 rounded-xl text-xs font-bold flex-1 transition-all"
                >
                  Cancel Registration
                </button>
                <button 
                  type="submit"
                  className="bg-amber-500 hover:bg-amber-400 text-slate-950 py-2.5 rounded-xl text-xs font-black flex-1 transition-all"
                >
                  Confirm Lead Entry
                </button>
              </div>

            </form>
          </div>
        </div>
      )}

    </div>
  );

  // Helper calculation function
  function totalLeadsCountRaw() {
    return filteredLeads.length;
  }
}
