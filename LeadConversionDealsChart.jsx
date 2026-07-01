import React, { useState, useMemo } from 'react';
import {
  ComposedChart,
  Bar,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  AreaChart,
  Area,
  PieChart,
  Pie,
  Cell
} from 'recharts';
import {
  TrendingUp,
  Percent,
  CheckCircle,
  Clock,
  Briefcase,
  Layers,
  Sparkles,
  Award,
  DollarSign,
  Heart,
  ChevronRight,
  Filter,
  Activity,
  ArrowUpRight,
  Phone,
  MessageSquare,
  Globe,
  Sliders
} from 'lucide-react';

/**
 * ============================================================================
 * BEKANSI AI SALES ASSISTANT PLATFORM
 * LEAD CONVERSION RATES & CLOSED DEALS OBSERVABILITY VISUALIZER
 * ============================================================================
 * 
 * An executive-grade analytical dashboard React component styled with high-fidelity
 * dark luxury aesthetics matching the gold/amber theme of Bekansi Furniture (Ethiopia).
 * 
 * Features:
 * 1. KPI Cards Strip showing Overall Conversion Ratio, Total Deals Won, Revenue, and Closing Velocity.
 * 2. Composed Chart: Visualizes the relationship between Deals Won (Bar Chart) and Conversion % (Line Chart).
 * 3. Step-by-Step Funnel Stages: Shows conversion drop-offs across the sales pipeline steps.
 * 4. Advanced Interactive Filters: Allows segmenting analysis by Wood Type, Sales Source Channel, and Branch Location.
 * 5. Interactive Recent Victories Grid: Browse recent high-value deals closed.
 */

// Production mock data representing local sales activities of Bekansi Furniture
const RECENT_WON_DEALS = [
  {
    id: "deal-091a",
    clientName: "Abebe Balcha",
    location: "Bishoftu City, Dukem Subcity",
    product: "Wanza Premium L-Sofa Gara Set",
    woodType: "Wanza",
    value: 280000,
    channel: "Telegram Bot",
    leadTimeDays: 12,
    dateWon: "2026-06-15",
    status: "Won"
  },
  {
    id: "deal-092b",
    clientName: "Bekansi",
    location: "Bishoftu City, Dukem Subcity",
    product: "Imperial Mahogany Bedroom Set",
    woodType: "Mahogany",
    value: 245000,
    channel: "WhatsApp API",
    leadTimeDays: 19,
    dateWon: "2026-06-16",
    status: "Won"
  },
  {
    id: "deal-093c",
    clientName: "Helen Yohannes",
    location: "Addis Ababa (CMC Compound)",
    product: "Corporate Modular Conferencing Suites",
    woodType: "Solid Oak",
    value: 620000,
    channel: "Web Chat",
    leadTimeDays: 24,
    dateWon: "2026-06-12",
    status: "Won"
  },
  {
    id: "deal-094d",
    clientName: "Dawit Wolde",
    location: "Addis Ababa (Kazanchis)",
    product: "Executive L-Shaped Solid Desk",
    woodType: "Mahogany",
    value: 650000,
    channel: "Web Chat",
    leadTimeDays: 14,
    dateWon: "2026-06-16",
    status: "Won"
  },
  {
    id: "deal-095e",
    clientName: "Makeda Kassa",
    location: "Bishoftu City, Dukem Subcity",
    product: "Bespoke Royal Kitchen Cabinetry",
    woodType: "Solid Oak",
    value: 580000,
    channel: "Telegram Bot",
    leadTimeDays: 29,
    dateWon: "2026-06-11",
    status: "Won"
  },
  {
    id: "deal-096f",
    clientName: "Dr. Kenenisa Dibaba",
    location: "Adama Branch",
    product: "Royal Wanza Dining Table (10-Seater)",
    woodType: "Wanza",
    value: 195000,
    channel: "WhatsApp API",
    leadTimeDays: 8,
    dateWon: "2026-05-28",
    status: "Won"
  },
  {
    id: "deal-097g",
    clientName: "Semira Kemal",
    location: "Hawassa Showroom",
    product: "Luxury Tufted King Sized Bed Frame",
    woodType: "Walnut",
    value: 220000,
    channel: "Showroom",
    leadTimeDays: 16,
    dateWon: "2026-06-10",
    status: "Won"
  },
  {
    id: "deal-098h",
    clientName: "ATO Yohannes Tekle",
    location: "Addis Ababa (Sarbet)",
    product: "Heritage Carved Accent Credenza",
    woodType: "Zigba",
    value: 125000,
    channel: "Facebook Messenger",
    leadTimeDays: 22,
    dateWon: "2026-06-08",
    status: "Won"
  }
];

// Historical monthly trend of conversions & deals won
const MONTHLY_TRENDS = [
  { month: 'Jan 2026', leadsReceived: 35, dealsWon: 8, conversionRate: 22.8, revenueThousands: 1680 },
  { month: 'Feb 2026', leadsReceived: 59, dealsWon: 14, conversionRate: 23.7, revenueThousands: 2840 },
  { month: 'Mar 2026', leadsReceived: 104, dealsWon: 27, conversionRate: 26.0, revenueThousands: 5210 },
  { month: 'Apr 2026', leadsReceived: 157, dealsWon: 41, conversionRate: 26.1, revenueThousands: 8150 },
  { month: 'May 2026', leadsReceived: 242, dealsWon: 68, conversionRate: 28.1, revenueThousands: 13920 },
  { month: 'Jun 2026', leadsReceived: 350, dealsWon: 99, conversionRate: 28.3, revenueThousands: 21830 },
];

export default function LeadConversionDealsChart() {
  const [woodFilter, setWoodFilter] = useState('All'); // All, Wanza, Mahogany, Solid Oak, Zigba, Walnut
  const [channelFilter, setChannelFilter] = useState('All'); // All, WhatsApp, Telegram, Web, Showroom
  const [branchFilter, setBranchFilter] = useState('All'); // All, Bishoftu, Adama, Hawassa, CMC
  const [selectedDealId, setSelectedDealId] = useState(null);
  const [activeTab, setActiveTab] = useState('composed'); // 'composed' | 'area_revenue'

  const WOOD_TYPES = ['All', 'Wanza', 'Mahogany', 'Solid Oak', 'Zigba', 'Walnut'];
  const CHANNELS = ['All', 'WhatsApp API', 'Telegram Bot', 'Web Chat', 'Facebook Messenger', 'Showroom'];
  const BRANCHES = ['All', 'Bishoftu City (Dukem Subcity)', 'Adama Branch', 'Addis Ababa (CMC Compound)', 'Addis Ababa (Kazanchis)', 'Hawassa Showroom'];

  // Filter application pipeline
  const filteredDeals = useMemo(() => {
    return RECENT_WON_DEALS.filter(deal => {
      const matchesWood = woodFilter === 'All' || deal.woodType === woodFilter;
      const matchesChannel = channelFilter === 'All' || deal.channel === channelFilter;
      const matchesBranch = branchFilter === 'All' || deal.location === branchFilter;
      return matchesWood && matchesChannel && matchesBranch;
    });
  }, [woodFilter, channelFilter, branchFilter]);

  // Aggregate stats based on current filters
  const aggregates = useMemo(() => {
    const dealsCount = filteredDeals.length;
    const totalWonRevenue = filteredDeals.reduce((sum, deal) => sum + deal.value, 0);
    const avgClosingTime = dealsCount > 0 ? (filteredDeals.reduce((sum, deal) => sum + deal.leadTimeDays, 0) / dealsCount).toFixed(0) : 0;
    
    // Static baseline comparison references
    const platformConversionRatio = dealsCount > 0 ? (28.3).toFixed(1) : "0.0";
    
    return {
      count: dealsCount,
      revenue: totalWonRevenue,
      avgVelocity: avgClosingTime,
      convRate: platformConversionRatio
    };
  }, [filteredDeals]);

  const selectedDeal = useMemo(() => {
    return RECENT_WON_DEALS.find(d => d.id === selectedDealId) || null;
  }, [selectedDealId]);

  // Funnel Stages drop-off data (based on baseline platforms average)
  const funnelStagesData = [
    { stage: '1. Inbound Leads', volume: 350, percentage: 100, color: '#64748B' },
    { stage: '2. Qualified (AI Check)', volume: 242, percentage: 69.1, color: '#3B82F6' },
    { stage: '3. Custom Quotation Sent', volume: 157, percentage: 44.8, color: '#EC4899' },
    { stage: '4. Price Negotiated', volume: 118, percentage: 33.7, color: '#D4AF37' },
    { stage: '5. Signed (Deal Won)', volume: 99, percentage: 28.3, color: '#10B981' }
  ];

  // Composed Chart Tooltip Formatter
  const CustomComposedTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
      return (
        <div className="bg-slate-900 border border-slate-800 p-4 rounded-xl shadow-2xl backdrop-blur-md">
          <p className="text-slate-400 text-xs font-bold mb-2.5 flex items-center gap-1.5 border-b border-slate-800 pb-1.5">
            <Clock className="w-3.5 h-3.5 text-slate-400" />
            {label} Core Performance
          </p>
          <div className="space-y-2 text-[12px]">
            <div className="flex justify-between gap-10">
              <span className="text-slate-300">Total Leads Inbound:</span>
              <span className="font-bold text-white">{payload[0].payload.leadsReceived} Leads</span>
            </div>
            <div className="flex justify-between gap-10">
              <span className="text-amber-500 font-semibold flex items-center gap-1">🏆 Closed Deals Won:</span>
              <span className="font-extrabold text-amber-500">{payload[0].value} Deals</span>
            </div>
            <div className="flex justify-between gap-10 border-t border-slate-800/80 pt-1.5 mt-1">
              <span className="text-emerald-400 font-semibold flex items-center gap-1">⚡ Conversion Ratio:</span>
              <span className="font-extrabold text-emerald-400">{payload[1].value}%</span>
            </div>
            <div className="flex justify-between gap-10 text-[11px] text-slate-500">
              <span>Estimated Revenue:</span>
              <span className="font-mono font-bold">{(payload[0].payload.revenueThousands * 1000).toLocaleString()} ETB</span>
            </div>
          </div>
        </div>
      );
    }
    return null;
  };

  return (
    <div className="w-full bg-slate-950 text-white p-6 rounded-2xl border border-slate-900 shadow-2xl space-y-6">
      
      {/* Header Visual Anchor Block */}
      <div className="flex flex-col lg:flex-row lg:items-center justify-between border-b border-slate-900 pb-6 gap-4">
        <div>
          <div className="flex items-center gap-2 mb-2">
            <span className="bg-amber-500/10 text-amber-500 text-[10px] uppercase font-bold tracking-widest px-2.5 py-1 rounded-md border border-amber-500/15 flex items-center gap-1">
              <Award className="w-3.5 h-3.5 text-amber-500 animate-pulse" />
              SaaS Conversion Engine
            </span>
            <span className="flex items-center gap-1.5 text-[11px] text-slate-500 font-medium font-mono">
              ★ Active Multi-Tenant Analytics
            </span>
          </div>
          <h2 className="text-xl md:text-2xl font-black text-slate-50 tracking-tight">
            Conversion Velocity & Deals Dashboard
          </h2>
          <p className="text-slate-400 text-sm">
            Interactive metrics monitoring lead flow conversion ratios and signed luxury wood design deals (ETB).
          </p>
        </div>

        {/* Global Chart Toggle */}
        <div className="flex items-center gap-2">
          <div className="flex bg-slate-900 p-1 rounded-lg border border-slate-800">
            <button
              onClick={() => setActiveTab('composed')}
              className={`px-3.5 py-1.5 rounded-md text-xs font-semibold transition-all flex items-center gap-1.5 ${
                activeTab === 'composed'
                  ? 'bg-amber-500 text-slate-950 shadow-md font-black'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              <TrendingUp className="w-3.5 h-3.5" />
              Performance Matrix
            </button>
            <button
              onClick={() => setActiveTab('area_revenue')}
              className={`px-3.5 py-1.5 rounded-md text-xs font-semibold transition-all flex items-center gap-1.5 ${
                activeTab === 'area_revenue'
                  ? 'bg-amber-500 text-slate-950 shadow-md font-black'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              <DollarSign className="w-3.5 h-3.5" />
              Revenue Flow
            </button>
          </div>
        </div>
      </div>

      {/* KPI Cards Strip */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        
        {/* Deal Conversion Rate */}
        <div className="bg-slate-900 border border-slate-900 p-4.5 rounded-xl flex items-center justify-between relative overflow-hidden group hover:border-slate-800 transition-all">
          <div>
            <p className="text-[10px] text-slate-400 font-bold uppercase tracking-wider">Overall Conversion Rate</p>
            <h3 className="text-3xl font-black text-emerald-400 mt-1.5">
              {aggregates.convRate}%
            </h3>
            <span className="text-[11px] text-slate-500 font-medium flex items-center gap-1 mt-1">
              <ArrowUpRight className="w-3.5 h-3.5 text-emerald-400" />
              +2.1% since Q1 2026
            </span>
          </div>
          <div className="bg-slate-950/40 p-3 rounded-lg border border-slate-850 text-emerald-400">
            <Percent className="w-5 h-5" />
          </div>
        </div>

        {/* Total Deals Won */}
        <div className="bg-slate-900 border border-slate-900 p-4.5 rounded-xl flex items-center justify-between relative overflow-hidden group hover:border-slate-800 transition-all">
          <div>
            <p className="text-[10px] text-slate-400 font-bold uppercase tracking-wider">Total Deals Won</p>
            <h3 className="text-3xl font-black text-amber-500 mt-1.5">
              {aggregates.count} <span className="text-xs font-semibold text-slate-400 uppercase">Clients</span>
            </h3>
            <span className="text-[11px] text-slate-500 mt-1 flex items-center gap-1">
              Currently visible under filters
            </span>
          </div>
          <div className="bg-slate-950/40 p-3 rounded-lg border border-slate-850 text-amber-500">
            <CheckCircle className="w-5 h-5" />
          </div>
        </div>

        {/* Closed Won Revenue */}
        <div className="bg-slate-900 border border-slate-900 p-4.5 rounded-xl flex items-center justify-between relative overflow-hidden group hover:border-slate-800 transition-all">
          <div>
            <p className="text-[10px] text-slate-400 font-bold uppercase tracking-wider">Closed Won Revenue</p>
            <h3 className="text-3xl font-black text-slate-100 mt-1.5">
              {aggregates.revenue.toLocaleString()} <span className="text-xs font-semibold text-slate-500">ETB</span>
            </h3>
            <span className="text-[11px] text-slate-400 font-semibold flex items-center gap-0.5 mt-1 text-emerald-400">
              100% committed contracts billing
            </span>
          </div>
          <div className="bg-slate-950/40 p-3 rounded-lg border border-slate-850 text-slate-100">
            <DollarSign className="w-5 h-5" />
          </div>
        </div>

        {/* Avg Closing Velocity */}
        <div className="bg-slate-900 border border-slate-900 p-4.5 rounded-xl flex items-center justify-between relative overflow-hidden group hover:border-slate-800 transition-all">
          <div>
            <p className="text-[10px] text-slate-400 font-bold uppercase tracking-wider">Avg. Closing Velocity</p>
            <h3 className="text-3xl font-black text-slate-100 mt-1.5">
              {aggregates.avgVelocity} <span className="text-xs font-semibold text-slate-400 uppercase">Days</span>
            </h3>
            <span className="text-[11px] text-amber-500 font-bold flex items-center gap-1 mt-1">
              <Sparkles className="w-3.5 h-3.5 text-amber-500" />
              AI auto-qualification boost
            </span>
          </div>
          <div className="bg-slate-950/40 p-3 rounded-lg border border-slate-850 text-slate-400">
            <Clock className="w-5 h-5" />
          </div>
        </div>

      </div>

      {/* Interactive Micro-Filters Panel */}
      <div className="bg-slate-900 border border-slate-900 p-4 rounded-xl flex flex-col xl:flex-row xl:items-center justify-between gap-4">
        <div className="flex items-center gap-2 text-slate-400 text-xs font-bold">
          <Filter className="w-4 h-4 text-emerald-400" />
          <span>Segment filters applied:</span>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          
          {/* Wood Type Core */}
          <div className="flex items-center bg-slate-950 border border-slate-800 rounded-lg px-2.5 py-1.5 text-xs text-slate-300 gap-1.5">
            <Layers className="w-3.5 h-3.5 text-slate-500" />
            <span className="text-slate-500 font-semibold">Timber Core:</span>
            <select
              value={woodFilter}
              onChange={(e) => setWoodFilter(e.target.value)}
              className="bg-transparent border-none text-slate-200 font-extrabold focus:outline-none cursor-pointer pr-1"
            >
              {WOOD_TYPES.map(wood => (
                <option key={wood} value={wood}>{wood === 'All' ? 'All woods' : `${wood} Core`}</option>
              ))}
            </select>
          </div>

          {/* Marketing Source Channel */}
          <div className="flex items-center bg-slate-950 border border-slate-800 rounded-lg px-2.5 py-1.5 text-xs text-slate-300 gap-1.5 font-mono">
            <MessageSquare className="w-3.5 h-3.5 text-slate-500" />
            <span className="text-slate-500 font-semibold">Origin:</span>
            <select
              value={channelFilter}
              onChange={(e) => setChannelFilter(e.target.value)}
              className="bg-transparent border-none text-slate-200 font-extrabold focus:outline-none cursor-pointer pr-1"
            >
              {CHANNELS.map(ch => (
                <option key={ch} value={ch}>{ch === 'All' ? 'All Channels' : ch}</option>
              ))}
            </select>
          </div>

          {/* Regional Branch Locations */}
          <div className="flex items-center bg-slate-950 border border-slate-800 rounded-lg px-2.5 py-1.5 text-xs text-slate-300 gap-1.5 font-serif">
            <Globe className="w-3.5 h-3.5 text-slate-500" />
            <span className="text-slate-500 font-semibold text-ellipsis">Branch:</span>
            <select
              value={branchFilter}
              onChange={(e) => setBranchFilter(e.target.value)}
              className="bg-transparent border-none text-slate-200 font-extrabold focus:outline-none cursor-pointer pr-1"
            >
              {BRANCHES.map(b => (
                <option key={b} value={b}>{b === 'All' ? 'All Branches' : b.replace('Addis Ababa ', '')}</option>
              ))}
            </select>
          </div>

          {/* Clear Button */}
          {(woodFilter !== 'All' || channelFilter !== 'All' || branchFilter !== 'All') && (
            <button
              onClick={() => {
                setWoodFilter('All');
                setChannelFilter('All');
                setBranchFilter('All');
              }}
              className="text-xs text-red-400 hover:text-red-300 font-black px-2 py-1"
            >
              Reset Filters
            </button>
          )}

        </div>
      </div>

      {/* Main Visualizations Strip */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* Core Recharts Canvas Column */}
        <div className="lg:col-span-2 bg-slate-900 border border-slate-900 rounded-xl p-5 space-y-4">
          <div className="flex items-center justify-between pb-1">
            <div>
              <h3 className="text-sm font-black text-slate-100 flex items-center gap-1.5">
                <Sliders className="w-4 h-4 text-emerald-400" />
                {activeTab === 'composed' ? 'Sales Pipeline Velocity & Success Ratio' : 'Cumulative Closed Deal Volume (ETB)'}
              </h3>
              <p className="text-xs text-slate-500 mt-0.5">
                {activeTab === 'composed' 
                  ? 'Displays the comparison trends between monthly deals finalized (Bar) and qualified conversions % (Line).' 
                  : 'Displays cumulative contract revenues earned from signed furniture designs since Jan 2026.'
                }
              </p>
            </div>
          </div>

          <div className="h-[280px] w-full">
            <ResponsiveContainer width="100%" height="100%">
              {activeTab === 'composed' ? (
                <ComposedChart
                  data={MONTHLY_TRENDS}
                  margin={{ top: 10, right: -10, left: -20, bottom: 0 }}
                >
                  <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
                  <XAxis dataKey="month" stroke="#64748b" fontSize={11} tickLine={false} />
                  
                  {/* Left YAxis for Deals Won Counts */}
                  <YAxis 
                    yAxisId="left"
                    stroke="#D4AF37" 
                    fontSize={11} 
                    tickLine={false} 
                  />
                  
                  {/* Right YAxis for Conversion Rate Percentage */}
                  <YAxis 
                    yAxisId="right"
                    orientation="right"
                    stroke="#10B981" 
                    fontSize={11} 
                    tickLine={false}
                    tickFormatter={(v) => `${v}%`}
                  />
                  
                  <Tooltip content={<CustomComposedTooltip />} />
                  <Legend 
                    verticalAlign="top" 
                    height={36} 
                    iconType="circle"
                    formatter={(value) => {
                      if (value === 'dealsWon') return <span className="text-xs font-bold text-slate-300 mr-2">🥇 Signed Deals (Bar)</span>;
                      return <span className="text-xs font-bold text-slate-300">⚡ Conversion % (Line)</span>;
                    }}
                  />

                  {/* Deals Won (Bar Chart in Amber Gold) */}
                  <Bar 
                    yAxisId="left"
                    dataKey="dealsWon" 
                    fill="#D4AF37" 
                    radius={[4, 4, 0, 0]} 
                    barSize={40} 
                  />

                  {/* Conversion Ratio (Line Chart in Emerald Green) */}
                  <Line 
                    yAxisId="right"
                    type="monotone" 
                    dataKey="conversionRate" 
                    stroke="#10B981" 
                    strokeWidth={3} 
                    activeDot={{ r: 6 }} 
                  />

                </ComposedChart>
              ) : (
                <AreaChart
                  data={MONTHLY_TRENDS}
                  margin={{ top: 15, right: 10, left: -15, bottom: 0 }}
                >
                  <defs>
                    <linearGradient id="colorRevenue" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#D4AF37" stopOpacity={0.35}/>
                      <stop offset="95%" stopColor="#D4AF37" stopOpacity={0}/>
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
                  <XAxis dataKey="month" stroke="#64748b" fontSize={11} tickLine={false} />
                  <YAxis 
                    stroke="#64748b" 
                    fontSize={11} 
                    tickLine={false} 
                    tickFormatter={(v) => `${(v / 1000).toFixed(1)}M`}
                  />
                  <Tooltip 
                    formatter={(value) => [`${(value * 1000).toLocaleString()} ETB`, 'Cumulative Revenue']}
                    contentStyle={{ backgroundColor: '#0f172a', borderColor: '#1e293b', borderRadius: '12px' }}
                    labelStyle={{ color: '#94a3b8', fontSize: '11px', fontWeight: 'bold' }}
                  />
                  <Area 
                    type="monotone" 
                    dataKey="revenueThousands" 
                    stroke="#D4AF37" 
                    strokeWidth={2.5} 
                    fillOpacity={1} 
                    fill="url(#colorRevenue)" 
                  />
                </AreaChart>
              )}
            </ResponsiveContainer>
          </div>
        </div>

        {/* Lead drop-off Funnel Analysis */}
        <div className="lg:col-span-1 bg-slate-900 border border-slate-900 rounded-xl p-5 flex flex-col justify-between">
          <div>
            <h3 className="text-sm font-black text-slate-100 flex items-center gap-1.5">
              <Layers className="w-4 h-4 text-emerald-400" />
              Platform Funnel Drop-offs
            </h3>
            <p className="text-xs text-slate-500 mt-0.5">
              Reflects lead conversion rates progression stages down the pipeline.
            </p>
          </div>

          <div className="space-y-3.5 my-4">
            {funnelStagesData.map((stage) => (
              <div key={stage.stage} className="space-y-1">
                <div className="flex items-center justify-between text-xs">
                  <span className="font-semibold text-slate-300">{stage.stage}</span>
                  <span className="font-mono font-bold text-slate-200">{stage.volume} ({stage.percentage}%)</span>
                </div>
                {/* Visual progression bar indicator */}
                <div className="w-full bg-slate-950 h-2 rounded-full overflow-hidden">
                  <div 
                    className="h-full rounded-full transition-all duration-500"
                    style={{ 
                      width: `${stage.percentage}%`, 
                      backgroundColor: stage.color 
                    }}
                  />
                </div>
              </div>
            ))}
          </div>

          <div className="bg-slate-950 p-2.5 rounded-xl border border-slate-850 border-dashed text-center text-[11px] text-slate-400">
            Current stage drop-off analysis dictates an outstanding <b>12% optimization</b> in Lead-to-Qualified stages post AI integration.
          </div>
        </div>

      </div>

      {/* Won Deals Workspace & Victories Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
        
        {/* Workspace details slideout sidebar */}
        <div className="lg:col-span-1 order-last lg:order-first">
          {selectedDeal ? (
            <div className="bg-slate-900 border border-slate-905 p-5 rounded-xl space-y-4">
              <div className="flex items-center justify-between border-b border-slate-800 pb-3">
                <span className="text-[10px] text-emerald-400 font-extrabold uppercase tracking-widest flex items-center gap-1">
                  <span className="w-2 h-2 rounded-full bg-emerald-500 text-xs inline-block animate-ping" />
                  Victory Workspace active
                </span>
                <button
                  onClick={() => setSelectedDealId(null)}
                  className="text-xs text-slate-500 hover:text-white"
                >
                  Clear
                </button>
              </div>

              <div>
                <span className="text-[9.5px] text-slate-500 uppercase font-black block">Client Name</span>
                <h4 className="font-black text-white text-base mt-0.5">{selectedDeal.clientName}</h4>
                <p className="text-[11px] text-slate-400 mt-0.5 flex items-center gap-1">
                  <Globe className="w-3.5 h-3.5 text-slate-500" />
                  {selectedDeal.location}
                </p>
              </div>

              <div className="bg-slate-950 p-3 rounded-xl border border-slate-850 space-y-2 text-[11px] text-slate-300">
                <div className="flex justify-between">
                  <span className="text-slate-500">Finished Product:</span>
                  <span className="font-bold text-slate-100">{selectedDeal.product}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-500">Timber Core Type:</span>
                  <span className="font-mono text-amber-500 font-bold">{selectedDeal.woodType}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-500">Marketing Source:</span>
                  <span>{selectedDeal.channel}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-500">Time-To-Close:</span>
                  <span className="font-bold text-slate-100">{selectedDeal.leadTimeDays} Days</span>
                </div>
              </div>

              <div className="bg-amber-500/10 p-3 rounded-xl border border-amber-500/15 flex items-center justify-between">
                <div>
                  <span className="text-[9.5px] text-slate-400 uppercase font-bold block">Deal Sizing Price</span>
                  <span className="text-lg font-black text-amber-500">{selectedDeal.value.toLocaleString()} ETB</span>
                </div>
                <button className="bg-amber-500 hover:bg-amber-400 text-slate-950 p-1.5 px-3 rounded-lg text-xs font-black">
                  Invoiced
                </button>
              </div>
            </div>
          ) : (
            <div className="bg-slate-900/60 border border-dashed border-slate-800 rounded-xl p-6 py-9 text-center text-slate-500 flex flex-col justify-center items-center gap-2">
              <Award className="w-8 h-8 text-slate-700" />
              <div>
                <p className="font-bold text-slate-400 text-xs">No Workspace Selection</p>
                <p className="text-[10px] text-slate-600 mt-0.5 max-w-[200px] leading-relaxed">
                  Select any deal in the victories grid to explore product timber cores, locations, and close cycle days.
                </p>
              </div>
            </div>
          )}
        </div>

        {/* Victories Grid Table */}
        <div className="lg:col-span-3 space-y-3.5">
          <div className="flex items-center justify-between px-1">
            <h3 className="text-base font-black text-slate-100 flex items-center gap-1.5">
              <CheckCircle className="w-4 h-4 text-emerald-400" />
              Recent Victories Pool (Signed Accounts)
            </h3>
            <span className="text-xs text-slate-500 font-mono">
              Displaying {filteredDeals.length} Won Deals
            </span>
          </div>

          <div className="bg-slate-900 border border-slate-900 rounded-xl overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs text-slate-300 border-collapse">
                <thead>
                  <tr className="border-b border-slate-850 bg-slate-900/60 font-bold uppercase text-[10.5px] tracking-wider text-slate-400">
                    <th scope="col" className="py-3.5 px-4">Client Identity</th>
                    <th scope="col" className="py-3.5 px-4">Branch Location</th>
                    <th scope="col" className="py-3.5 px-4">Finished Product</th>
                    <th scope="col" className="py-3.5 px-4 text-center">Core wood</th>
                    <th scope="col" className="py-3.5 px-4">Deal Valuation</th>
                    <th scope="col" className="py-3.5 px-4">Close Cycle</th>
                    <th scope="col" className="py-3.5 px-4 text-right">Details</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-850">
                  {filteredDeals.length === 0 ? (
                    <tr>
                      <td colSpan="7" className="text-center py-10 text-slate-500">
                        No final deals match selected filter parameters.
                      </td>
                    </tr>
                  ) : (
                    filteredDeals.map((deal) => {
                      const isSelected = feelMatchesId(deal.id);
                      return (
                        <tr
                          key={deal.id}
                          onClick={() => setSelectedDealId(deal.id)}
                          className={`hover:bg-slate-850 transition-colors cursor-pointer group ${
                            isSelected ? 'bg-slate-800' : ''
                          }`}
                        >
                          <td className="py-3 px-4 font-bold text-slate-100 group-hover:text-amber-500 transition-colors">
                            {deal.clientName}
                          </td>
                          <td className="py-3 px-4 text-slate-400">
                            {deal.location.replace('Addis Ababa ', '')}
                          </td>
                          <td className="py-3 px-4 max-w-[150px] truncate md:font-medium text-slate-200">
                            {deal.product}
                          </td>
                          <td className="py-3 px-4 text-center">
                            <span className="px-2 py-0.5 bg-amber-500/10 text-amber-500 rounded font-bold uppercase text-[9.5px]">
                              {deal.woodType}
                            </span>
                          </td>
                          <td className="py-3 px-4 font-extrabold text-slate-200">
                            {deal.value.toLocaleString()} <span className="text-[10px] text-slate-500">ETB</span>
                          </td>
                          <td className="py-3 px-4 font-medium">
                            {deal.leadTimeDays} Days
                          </td>
                          <td className="py-3 px-4 text-right">
                            <button className="p-1 px-2.5 bg-slate-950 border border-slate-800 hover:bg-slate-800 rounded-md text-slate-400 group-hover:text-amber-500 inline-flex items-center gap-1 text-[11px] font-bold transition-all">
                              Manage <ChevronRight className="w-3.5 h-3.5" />
                            </button>
                          </td>
                        </tr>
                      );
                    })
                  )}
                </tbody>
              </table>
            </div>

            {/* Custom Interactive Table Footer */}
            <div className="bg-slate-900/40 border-t border-slate-850 p-3.5 px-4 flex items-center justify-between text-xs text-slate-500">
              <span>Displaying <b>{filteredDeals.length}</b> records. Cumulative value of <b>{filteredDeals.reduce((a, b) => a + b.value, 0).toLocaleString()} Birr</b></span>
              <span>Regional Timezone (EAT)</span>
            </div>

          </div>
        </div>

      </div>

    </div>
  );

  function totalLeadsCountRaw() {
    return MONTHLY_TRENDS.reduce((acc, curr) => acc + curr.leadsReceived, 0);
  }

  function feelMatchesId(id) {
    return selectedDealId === id;
  }
}
