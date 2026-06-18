import React, { useState, useMemo } from 'react';
import { 
  Users, 
  Search, 
  Filter, 
  Globe, 
  Calendar, 
  ChevronDown, 
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
  AlertCircle
} from 'lucide-react';

/**
 * BEKANSI AI SALES ASSISTANT PLATFORM
 * ENTERPRISE ACTIVE CUSTOMER DASHBOARD (REACT + TAILWIND CSS)
 * 
 * Provides interactive CRM interfaces for filtering, searching, and managing
 * omnichannel customer profiles segmented by Ethiopian local language and recent touchpoints.
 */

// Enterprise seed records containing local Ethiopian names, custom language settings, and interactive states
const INITIAL_CUSTOMERS = [
  {
    id: "cust-0f119e7a-9cb2",
    firstName: "Tewodros",
    lastName: "Alene",
    email: "tewodros.alene@gmail.com",
    phoneNumber: "+251 98 882 8861",
    preferredLanguage: "am", // Amharic
    telegramId: "@tewodros_alene",
    facebookPsid: null,
    channel: "telegram",
    segmentTags: ["Premium Customized", "Wanza Series"],
    lastInteractionDate: "2026-06-17",
    lastMessage: "Inquiry about Wanza Curved L-Sofa 'Gara' cushion options.",
    status: "active",
    pipelineValue: 135000,
    avatarColor: "bg-amber-500"
  },
  {
    id: "cust-ebd04192-124b",
    firstName: "Chala",
    lastName: "Kebede",
    email: "chala.kebede@yahoo.com",
    phoneNumber: "+251 91 082 4534",
    preferredLanguage: "om", // Afaan Oromo
    telegramId: null,
    facebookPsid: "fb-psid-99120485a",
    channel: "whatsapp",
    segmentTags: ["Mahogany Furniture", "Living Layouts"],
    lastInteractionDate: "2026-06-16",
    lastMessage: "Expresses interest in premium custom living layouts and mahogany finishes.",
    status: "active",
    pipelineValue: 245000,
    avatarColor: "bg-emerald-500"
  },
  {
    id: "cust-aac12480-abdc",
    firstName: "Helen",
    lastName: "Yohannes",
    email: "helen.y@interioraddis.com",
    phoneNumber: "+251 90 234 1188",
    preferredLanguage: "en", // English
    telegramId: "@helen_yohannes",
    facebookPsid: null,
    channel: "web_chat",
    segmentTags: ["Corporate Office", "B2B Contract"],
    lastInteractionDate: "2026-06-12",
    lastMessage: "Requested bespoke quotes for 12 modular workstation layouts for office extension.",
    status: "active",
    pipelineValue: 620000,
    avatarColor: "bg-blue-500"
  },
  {
    id: "cust-31eef90a-cda3",
    firstName: "Aster",
    lastName: "Tolossa",
    email: "aster.t@gmail.com",
    phoneNumber: "+251 91 140 1290",
    preferredLanguage: "am", // Amharic
    telegramId: null,
    facebookPsid: null,
    channel: "whatsapp",
    segmentTags: ["Kitchen Cabinets"],
    lastInteractionDate: "2026-06-05",
    lastMessage: "Confirmed design layouts. Waiting for final solid oak mockups from the design team.",
    status: "pending_quote",
    pipelineValue: 380000,
    avatarColor: "bg-purple-500"
  },
  {
    id: "cust-bf7710ea-7362",
    firstName: "Kedir",
    lastName: "Mohammed",
    email: "kedir.m@gmail.com",
    phoneNumber: "+251 94 488 2211",
    preferredLanguage: "om", // Afaan Oromo
    telegramId: null,
    facebookPsid: "fb-psid-22910405c",
    channel: "facebook_messenger",
    segmentTags: ["Bedroom Sets", "Retail Wardrobes"],
    lastInteractionDate: "2026-05-28",
    lastMessage: "Asked about warranty terms for modular bedroom sliding-door wardrobes.",
    status: "inactive",
    pipelineValue: 95000,
    avatarColor: "bg-rose-500"
  },
  {
    id: "cust-940af48c-799d",
    firstName: "Ephrem",
    lastName: "Getachew",
    email: "ephrem.get@innovateethiopia.com",
    phoneNumber: "+251 92 334 5566",
    preferredLanguage: "en", // English
    telegramId: "@ephrem_get",
    facebookPsid: null,
    channel: "mobile_app",
    segmentTags: ["Enterprise Lobby", "Wanza Series"],
    lastInteractionDate: "2026-06-14",
    lastMessage: "Configured multi-desk layouts on his account dashboard.",
    status: "active",
    pipelineValue: 450000,
    avatarColor: "bg-cyan-500"
  }
];

export default function CustomerDashboard() {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedLanguage, setSelectedLanguage] = useState('all'); // 'all' | 'am' | 'om' | 'en'
  const [dateFilter, setDateFilter] = useState('all'); // 'all' | '7_days' | '30_days' | 'older'
  const [activeSegment, setActiveSegment] = useState('all'); // Filter by specific custom product affinity segments
  const [selectedCustomerId, setSelectedCustomerId] = useState(null);

  // Computed summary stats
  const metrics = useMemo(() => {
    const totalCount = INITIAL_CUSTOMERS.length;
    const activeThisWeek = INITIAL_CUSTOMERS.filter(c => {
      const diffTime = Math.abs(new Date('2026-06-17') - new Date(c.lastInteractionDate));
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      return diffDays <= 7;
    }).length;
    
    const totalPipeline = INITIAL_CUSTOMERS.reduce((acc, curr) => acc + curr.pipelineValue, 0);
    const avgPipelineValue = (totalPipeline / totalCount).toFixed(0);

    return {
      total: totalCount,
      activeWeek: activeThisWeek,
      pipeline: totalPipeline.toLocaleString('en-US'),
      avgValue: parseInt(avgPipelineValue, 10).toLocaleString('en-US')
    };
  }, []);

  // Filter pipeline execution
  const filteredCustomers = useMemo(() => {
    return INITIAL_CUSTOMERS.filter(customer => {
      // 1. Search filter matching Name, Email, or Telephone
      const fullName = `${customer.firstName} ${customer.lastName}`.toLowerCase();
      const matchesSearch = 
        fullName.includes(searchQuery.toLowerCase()) ||
        customer.email.toLowerCase().includes(searchQuery.toLowerCase()) ||
        customer.phoneNumber.includes(searchQuery);

      // 2. Language localization filter
      const matchesLanguage = selectedLanguage === 'all' || customer.preferredLanguage === selectedLanguage;

      // 3. Time-window filters
      let matchesDate = true;
      if (dateFilter !== 'all') {
        const diffTime = Math.abs(new Date('2026-06-17') - new Date(customer.lastInteractionDate));
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        
        if (dateFilter === '7_days') matchesDate = diffDays <= 7;
        else if (dateFilter === '30_days') matchesDate = diffDays <= 30;
        else if (dateFilter === 'older') matchesDate = diffDays > 30;
      }

      // 4. Segment Tag Filter
      const matchesSegment = activeSegment === 'all' || customer.segmentTags.includes(activeSegment);

      return matchesSearch && matchesLanguage && matchesDate && matchesSegment;
    });
  }, [searchQuery, selectedLanguage, dateFilter, activeSegment]);

  // Handle setting focused customer for detailed slide-out preview panel
  const focusedCustomer = useMemo(() => {
    return INITIAL_CUSTOMERS.find(c => c.id === selectedCustomerId) || null;
  }, [selectedCustomerId]);

  return (
    <div className="w-full bg-slate-950 text-white min-h-screen p-6 font-sans">
      
      {/* Top Navigation Frame */}
      <div className="flex flex-col lg:flex-row lg:items-center justify-between border-b border-slate-850 pb-6 mb-8 gap-4">
        <div>
          <div className="flex items-center gap-2 mb-2">
            <span className="bg-amber-500/10 text-amber-500 text-[10px] uppercase font-bold tracking-widest px-2.5 py-1 rounded-md border border-amber-500/15">
              Enterprise CRM Hub
            </span>
            <span className="flex items-center gap-1 text-[11px] text-slate-500">
              <Circle className="w-2 h-2 fill-emerald-500 stroke-none" /> Connected to ILILI ERP
            </span>
          </div>
          <h1 className="text-2xl md:text-3xl font-black text-slate-50 tracking-tight">
            Omnichannel Customers
          </h1>
          <p className="text-slate-400 text-sm mt-1">
            Real-time isolation dashboard displaying localized profiles and interaction metrics.
          </p>
        </div>

        {/* Global actions */}
        <div className="flex items-center gap-3">
          <button className="bg-slate-900 hover:bg-slate-850 p-2.5 rounded-xl border border-slate-800 transition-all text-slate-400 hover:text-white">
            <Settings className="w-4 h-4" />
          </button>
          <button className="bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold px-4 py-2.5 rounded-xl transition-all shadow-lg flex items-center gap-2 text-sm">
            <Users className="w-4 h-4" />
            Add Customer Profile
          </button>
        </div>
      </div>

      {/* KPI Stats Stripes */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5 mb-8">
        <div className="bg-slate-900 border border-slate-850 rounded-2xl p-5 relative overflow-hidden">
          <p className="text-xs text-slate-400 font-semibold uppercase tracking-wider">Total CRM Reach</p>
          <h3 className="text-3xl font-extrabold text-white mt-1.5">{metrics.total}</h3>
          <p className="text-[11.5px] text-slate-400 mt-2 flex items-center gap-1">
            <ArrowUpRight className="w-3.5 h-3.5 text-emerald-400" /> Including live sync agents
          </p>
        </div>
        
        <div className="bg-slate-900 border border-slate-850 rounded-2xl p-5 relative overflow-hidden">
          <p className="text-xs text-slate-400 font-semibold uppercase tracking-wider">Active This Week</p>
          <h3 className="text-3xl font-extrabold text-white mt-1.5">{metrics.activeWeek}</h3>
          <p className="text-[11.5px] text-amber-500 font-medium mt-2 flex items-center gap-1">
            {((metrics.activeWeek / metrics.total) * 100).toFixed(0)}% engagement rate
          </p>
        </div>

        <div className="bg-slate-900 border border-slate-850 rounded-2xl p-5 relative overflow-hidden">
          <p className="text-xs text-slate-400 font-semibold uppercase tracking-wider">Estimated Pipeline</p>
          <h3 className="text-3xl font-extrabold text-white mt-1.5">{metrics.pipeline} <span className="text-xs font-medium text-slate-400">ETB</span></h3>
          <p className="text-[11.5px] text-emerald-400 mt-2 font-semibold">Active Design Quotes</p>
        </div>

        <div className="bg-slate-900 border border-slate-850 rounded-2xl p-5 relative overflow-hidden">
          <p className="text-xs text-slate-400 font-semibold uppercase tracking-wider">Average Deal Size</p>
          <h3 className="text-3xl font-extrabold text-white mt-1.5">{metrics.avgValue} <span className="text-xs font-medium text-slate-400">ETB</span></h3>
          <p className="text-[11.5px] text-slate-400 mt-2">Premium custom wood orders</p>
        </div>
      </div>

      {/* Main Board Area Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
        
        {/* Filtering & Table Section Column */}
        <div className="lg:col-span-3 space-y-4">
          
          {/* Filtering Control Bar */}
          <div className="bg-slate-900 border border-slate-850 p-4 rounded-2xl flex flex-col md:flex-row md:items-center justify-between gap-4">
            
            {/* Search Input */}
            <div className="relative flex-1">
              <Search className="w-4 h-4 ml-3.5 absolute top-1/2 -translate-y-1/2 text-slate-500" />
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Search by name, email, or +251 phone number..."
                className="w-full bg-slate-950 border border-slate-800 rounded-xl py-2.5 pl-10 pr-4 text-xs text-slate-200 placeholder-slate-500 focus:outline-none focus:border-amber-500/50 focus:ring-1 focus:ring-amber-500/25 transition-all"
              />
            </div>

            {/* Selector Filters */}
            <div className="flex flex-wrap items-center gap-3">
              
              {/* Language filter selector */}
              <div className="flex items-center bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-300 gap-2">
                <Globe className="w-3.5 h-3.5 text-slate-500" />
                <span className="text-slate-500 font-medium">Language:</span>
                <select 
                  value={selectedLanguage}
                  onChange={(e) => setSelectedLanguage(e.target.value)}
                  className="bg-transparent border-none text-slate-200 focus:outline-none cursor-pointer pr-1"
                >
                  <option value="all">All Languages</option>
                  <option value="am">Amharic (አማርኛ)</option>
                  <option value="om">Afaan Oromo</option>
                  <option value="en">English</option>
                </select>
              </div>

              {/* Date Filter Selector */}
              <div className="flex items-center bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-300 gap-2">
                <Calendar className="w-3.5 h-3.5 text-slate-500" />
                <span className="text-slate-500 font-medium">Touchpoint:</span>
                <select 
                  value={dateFilter}
                  onChange={(e) => setDateFilter(e.target.value)}
                  className="bg-transparent border-none text-slate-200 focus:outline-none cursor-pointer pr-1"
                >
                  <option value="all">Any Date</option>
                  <option value="7_days">Within 7 Days</option>
                  <option value="30_days">Within 30 Days</option>
                  <option value="older">Older than 30 Days</option>
                </select>
              </div>

            </div>
          </div>

          {/* Quick Segment Pill Filters */}
          <div className="flex items-center gap-1.5 flex-wrap">
            <span className="text-xs text-slate-500 font-medium mr-2 flex items-center gap-1">
              <Filter className="w-3 h-3" /> Quick Filter Segment:
            </span>
            {['all', 'Wanza Series', 'Mahogany Furniture', 'Kitchen Cabinets', 'Corporate Office'].map((seg) => (
              <button
                key={seg}
                onClick={() => setActiveSegment(seg)}
                className={`px-3 py-1.5 rounded-full text-xs font-semibold border transition-all ${
                  activeSegment === seg
                    ? 'bg-amber-500/10 border-amber-500 text-amber-500 font-black'
                    : 'bg-slate-900 border-slate-850 hover:bg-slate-850 text-slate-400 hover:text-slate-200'
                }`}
              >
                {seg === 'all' ? 'All Segments' : seg}
              </button>
            ))}
          </div>

          {/* Table Container card */}
          <div className="bg-slate-900 border border-slate-855 rounded-2xl overflow-hidden shadow-2xl">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs text-slate-300 border-collapse">
                <thead>
                  <tr className="border-b border-slate-850 bg-slate-900/60 uppercase text-[10.5px] font-bold tracking-wider text-slate-400">
                    <th scope="col" className="py-4 px-5">Customer Identity</th>
                    <th scope="col" className="py-4 px-4">Contact Phone</th>
                    <th scope="col" className="py-4 px-4 text-center">Language</th>
                    <th scope="col" className="py-4 px-4">Preference Segment</th>
                    <th scope="col" className="py-4 px-4">Pipeline Value</th>
                    <th scope="col" className="py-4 px-4">Last Active</th>
                    <th scope="col" className="py-4 px-4 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-850">
                  {filteredCustomers.length === 0 ? (
                    <tr>
                      <td colSpan="7" className="text-center py-12 text-slate-500">
                        <AlertCircle className="w-8 h-8 text-slate-600 mx-auto mb-3" />
                        <p className="font-semibold text-slate-400">No customers found</p>
                        <p className="text-xs text-slate-600 mt-1">Try resetting selected filters or adjusting your search params.</p>
                      </td>
                    </tr>
                  ) : (
                    filteredCustomers.map((customer) => (
                      <tr 
                        key={customer.id} 
                        onClick={() => setSelectedCustomerId(customer.id)}
                        className={`hover:bg-slate-850 transition-colors cursor-pointer group ${
                          selectedCustomerId === customer.id ? 'bg-slate-850' : ''
                        }`}
                      >
                        {/* Profile Info */}
                        <td className="py-4 px-5 flex items-center gap-3">
                          <div className={`w-8 h-8 rounded-full ${customer.avatarColor} text-slate-950 flex items-center justify-center font-bold text-xs uppercase shadow-inner`}>
                            {customer.firstName[0]}{customer.lastName[0]}
                          </div>
                          <div>
                            <p className="font-bold text-slate-100 group-hover:text-amber-500 transition-colors">
                              {customer.firstName} {customer.lastName}
                            </p>
                            <p className="text-[11px] text-slate-500 mt-0.5">{customer.email}</p>
                          </div>
                        </td>

                        {/* Phone and Channel Icon */}
                        <td className="py-4 px-4">
                          <div className="flex items-center gap-1.5 font-medium">
                            <span className="w-2.5 h-2.5 rounded-full bg-emerald-500/10 flex items-center justify-center text-[8px] border border-emerald-500/25">
                              ●
                            </span>
                            {customer.phoneNumber}
                          </div>
                          <span className="text-[10px] text-slate-500 lowercase mt-0.5 block">
                            via {customer.channel.replace('_', ' ')}
                          </span>
                        </td>

                        {/* Localized Language Identifier */}
                        <td className="py-4 px-4 text-center">
                          <span className={`px-2.5 py-1 text-[11px] rounded-md font-bold uppercase transition-colors ${
                            customer.preferredLanguage === 'am'
                              ? 'bg-amber-500/10 text-amber-500 border border-amber-500/20'
                              : customer.preferredLanguage === 'om'
                              ? 'bg-emerald-500/10 text-emerald-500 border border-emerald-500/20'
                              : 'bg-blue-500/10 text-blue-500 border border-blue-500/20'
                          }`}>
                            {customer.preferredLanguage === 'am' ? 'አማ' : customer.preferredLanguage === 'om' ? 'OM' : 'EN'}
                          </span>
                        </td>

                        {/* Custom Segment Badges */}
                        <td className="py-4 px-4">
                          <div className="flex flex-wrap gap-1 max-w-[200px]">
                            {customer.segmentTags.slice(0, 1).map((tag) => (
                              <span key={tag} className="bg-slate-950 border border-slate-800 text-[10px] px-2 py-0.5 rounded-md text-slate-300">
                                {tag}
                              </span>
                            ))}
                            {customer.segmentTags.length > 1 && (
                              <span className="text-[10px] text-slate-500 pl-1">
                                +{customer.segmentTags.length - 1}
                              </span>
                            )}
                          </div>
                        </td>

                        {/* Potential Quotation Value */}
                        <td className="py-4 px-4 font-bold text-slate-200">
                          {customer.pipelineValue.toLocaleString('en-US')} <span className="text-[10px] text-slate-500 uppercase">Birr</span>
                        </td>

                        {/* Last active dates */}
                        <td className="py-4 px-4">
                          <div className="flex flex-col">
                            <span className="font-semibold text-slate-300">
                              {new Date(customer.lastInteractionDate).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}
                            </span>
                            <span className="text-[10px] text-slate-500">
                              Touchpoint
                            </span>
                          </div>
                        </td>

                        {/* Fast View details Trigger */}
                        <td className="py-4 px-4 text-right">
                          <button className="p-1 px-2 hover:bg-slate-800 rounded-md transition-colors text-slate-400 hover:text-amber-500 inline-flex items-center gap-1.5 font-semibold text-xs">
                            <Eye className="w-3.5 h-3.5" />
                            View
                          </button>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>

            {/* Custom Interactive Table Footer */}
            <div className="bg-slate-900/40 border-t border-slate-850 p-4 px-5 flex items-center justify-between text-xs text-slate-500">
              <p>Displaying <span className="font-bold text-slate-300">{filteredCustomers.length}</span> of <span className="font-bold text-slate-300">{INITIAL_CUSTOMERS.length}</span> active organizations accounts</p>
              <div className="flex items-center gap-1.5">
                <button disabled className="px-3 py-1 bg-slate-950 hover:bg-slate-850 rounded-md border border-slate-800 disabled:opacity-30 disabled:hover:bg-slate-950 text-[11px] font-bold text-slate-400">Prev</button>
                <button disabled className="px-3 py-1 bg-slate-950 hover:bg-slate-850 rounded-md border border-slate-800 disabled:opacity-30 disabled:hover:bg-slate-950 text-[11px] font-bold text-slate-400">Next</button>
              </div>
            </div>

          </div>

        </div>

        {/* Selected Customer Detailed Workspace Column */}
        <div className="lg:col-span-1">
          {focusedCustomer ? (
            <div className="bg-slate-900 border border-slate-855 p-5 rounded-2xl space-y-5 shadow-2xl relative sticky top-6">
              
              {/* Card Meta Header */}
              <div className="flex items-center justify-between border-b border-slate-850 pb-4">
                <h4 className="text-sm font-extrabold text-slate-200 uppercase tracking-wider flex items-center gap-1.5">
                  <span className="w-2 h-2 rounded-full bg-emerald-500" /> Active Workspace
                </h4>
                <button 
                  onClick={() => setSelectedCustomerId(null)}
                  className="text-xs text-slate-500 hover:text-white hover:bg-slate-800 p-1 px-2 rounded-md transition-colors"
                >
                  Close
                </button>
              </div>

              {/* Profile Spot */}
              <div className="text-center space-y-2">
                <div className={`w-14 h-14 rounded-full ${focusedCustomer.avatarColor} text-slate-950 flex items-center justify-center font-black text-xl mx-auto shadow-xl`}>
                  {focusedCustomer.firstName[0]}{focusedCustomer.lastName[0]}
                </div>
                <div>
                  <h3 className="font-black text-lg text-slate-100">{focusedCustomer.firstName} {focusedCustomer.lastName}</h3>
                  <p className="text-xs text-slate-400 font-medium lowercase flex items-center justify-center gap-1">
                    via <span className="text-amber-500 font-bold bg-amber-500/10 px-1.5 py-0.5 rounded text-[10px] uppercase">{focusedCustomer.channel}</span>
                  </p>
                </div>
              </div>

              {/* CRM Contact Actions block */}
              <div className="bg-slate-950 p-3 rounded-xl border border-slate-850 space-y-2.5 text-[11.5px]">
                <div className="flex items-center gap-2.5 text-slate-300">
                  <Phone className="w-3.5 h-3.5 text-slate-500" />
                  <span>{focusedCustomer.phoneNumber}</span>
                </div>
                <div className="flex items-center gap-2.5 text-slate-300 break-all">
                  <Mail className="w-3.5 h-3.5 text-slate-500" />
                  <span>{focusedCustomer.email}</span>
                </div>
                {focusedCustomer.telegramId && (
                  <div className="flex items-center gap-2.5 text-slate-300">
                    <MessageSquare className="w-3.5 h-3.5 text-slate-500" />
                    <span className="font-medium text-amber-500">{focusedCustomer.telegramId}</span>
                  </div>
                )}
              </div>

              {/* Language detail banner */}
              <div className="space-y-1.5">
                <span className="text-[10px] text-slate-500 font-extrabold uppercase tracking-wider block">Customer Language Option</span>
                <div className="flex items-center justify-between bg-slate-950 border border-slate-850 p-2.5 rounded-xl">
                  <span className="text-xs font-bold text-slate-300">
                    {focusedCustomer.preferredLanguage === 'am' ? 'Amharic (አማርኛ)' : focusedCustomer.preferredLanguage === 'om' ? 'Afaan Oromo' : 'English (US)'}
                  </span>
                  <Globe className="w-4 h-4 text-amber-500" />
                </div>
              </div>

              {/* AI Transcribed Logs */}
              <div className="space-y-2">
                <span className="text-[10px] text-slate-500 font-extrabold uppercase tracking-wider block">Last Omnichannel Dialog Message</span>
                <div className="bg-slate-950/80 border border-slate-850 p-3.5 rounded-xl">
                  <p className="text-xs text-slate-300 italic leading-relaxed">
                    "{focusedCustomer.lastMessage}"
                  </p>
                  <span className="text-[9.5px] text-slate-500 block text-right mt-2 font-semibold">
                    Captured {focusedCustomer.lastInteractionDate}
                  </span>
                </div>
              </div>

              {/* Segment Tags block */}
              <div className="space-y-1.5">
                <span className="text-[10px] text-slate-500 font-extrabold uppercase tracking-wider block">Customer Affinity Segments</span>
                <div className="flex flex-wrap gap-1.5">
                  {focusedCustomer.segmentTags.map(tag => (
                    <span key={tag} className="bg-slate-950 border border-slate-800 text-[10.5px] font-medium px-2.5 py-1 rounded-md text-amber-500/90">
                      {tag}
                    </span>
                  ))}
                </div>
              </div>

              {/* Pipeline Quotes summary inside Workspace detail */}
              <div className="bg-amber-500/10 border border-amber-500/15 p-3 rounded-xl flex items-center justify-between">
                <div>
                  <span className="text-[10px] text-slate-400 font-semibold block">Design Pipeline</span>
                  <span className="text-base font-extrabold text-amber-500">{focusedCustomer.pipelineValue.toLocaleString('en-US')} ETB</span>
                </div>
                <button className="bg-amber-500 hover:bg-amber-400 text-slate-950 p-1.5 px-3.5 rounded-lg text-xs font-bold transition-all flex items-center gap-1">
                  Quote <ArrowUpRight className="w-3 h-3 stroke-[2.5]" />
                </button>
              </div>

            </div>
          ) : (
            <div className="bg-slate-900/60 border border-dashed border-slate-800 rounded-2xl p-8 py-14 text-center text-slate-500 space-y-3 sticky top-6">
              <Users className="w-10 h-10 text-slate-700 mx-auto" />
              <div>
                <p className="font-bold text-slate-400 text-sm">No workspace active</p>
                <p className="text-xs text-slate-600 mt-1 max-w-[200px] mx-auto leading-relaxed">
                  Select any customer row in the sidebar table to explore interaction logs and configure quotations.
                </p>
              </div>
            </div>
          )}
        </div>

      </div>

    </div>
  );
}
