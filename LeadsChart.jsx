import React, { useState, useMemo } from 'react';
import { 
  AreaChart, 
  Area, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  Legend, 
  ResponsiveContainer,
  BarChart,
  Bar
} from 'recharts';
import { 
  TrendingUp, 
  Users, 
  Globe, 
  Calendar, 
  ArrowUpRight,
  Filter,
  BarChart2,
  Activity
} from 'lucide-react';

/**
 * BEKANSI AI SALES ASSISTANT PLATFORM
 * ENTERPRISE LEADS ANALYTICS VISUALIZER (REACT + RECHARTS)
 * 
 * Provides interactive time-series visualizations of multilingual lead ingestion.
 * Features:
 * - Real-time metrics breakdown cards (Total, Growth rates, Language ratios).
 * - Multi-dimensional area charts showing cumulative or daily acquisition trends.
 * - Granular tooltips highlighting Ethiopian localizations (Amharic, Afaan Oromo, English).
 */

// Production mock data reflecting local sales activity in Addis Ababa and Oromia regions
const INITIAL_LEAD_DATA = [
  { date: 'Jan 2026', amharic: 12, afaanOromo: 8, english: 5 },
  { date: 'Feb 2026', amharic: 28, afaanOromo: 19, english: 12 },
  { date: 'Mar 2026', amharic: 47, afaanOromo: 35, english: 22 },
  { date: 'Apr 2026', amharic: 74, afaanOromo: 52, english: 31 },
  { date: 'May 2026', amharic: 110, afaanOromo: 84, english: 48 },
  { date: 'Jun 2026', amharic: 165, afaanOromo: 121, english: 64 }, // Current cycle
];

export default function LeadsChart() {
  const [chartType, setChartType] = useState('cumulative'); // 'cumulative' | 'bar_breakdown'
  const [timeRange, setTimeRange] = useState('6M'); // For future filtration expansion

  // Calculate high-fidelity KPI aggregates for dashboard cards
  const stats = useMemo(() => {
    const currentMonth = INITIAL_LEAD_DATA[INITIAL_LEAD_DATA.length - 1];
    const prevMonth = INITIAL_LEAD_DATA[INITIAL_LEAD_DATA.length - 2];
    
    const amTotal = currentMonth.amharic;
    const omTotal = currentMonth.afaanOromo;
    const enTotal = currentMonth.english;
    const grandTotal = amTotal + omTotal + enTotal;

    const prevGrandTotal = prevMonth.amharic + prevMonth.afaanOromo + prevMonth.english;
    const netGrowth = grandTotal - prevGrandTotal;
    const percentageGrowth = ((netGrowth / prevGrandTotal) * 100).toFixed(1);

    return {
      total: grandTotal,
      growth: percentageGrowth,
      channels: [
        { name: 'Amharic (አማርኛ)', count: amTotal, color: '#D4AF37', pct: ((amTotal / grandTotal) * 100).toFixed(0) }, // Golden Oak
        { name: 'Afaan Oromo', count: omTotal, color: '#10B981', pct: ((omTotal / grandTotal) * 100).toFixed(0) },    // Emerald Spruce
        { name: 'English', count: enTotal, color: '#3B82F6', pct: ((enTotal / grandTotal) * 100).toFixed(0) }        // Royal Sapphire
      ]
    };
  }, []);

  // Custom high-fidelity Tooltip styling for luxury design aesthetics
  const CustomTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
      const sum = payload.reduce((acc, curr) => acc + curr.value, 0);
      return (
        <div className="bg-slate-900 border border-slate-700 p-4 rounded-xl shadow-2xl backdrop-blur-md">
          <p className="text-slate-400 text-xs font-semibold mb-2 flex items-center gap-1">
            <Calendar className="w-3.5 h-3.5" /> {label}
          </p>
          <div className="space-y-1.5">
            {payload.map((item) => (
              <div key={item.name} className="flex items-center justify-between gap-8">
                <span className="flex items-center gap-2 text-sm text-slate-300">
                  <span className="w-2 h-2 rounded-full" style={{ backgroundColor: item.color }} />
                  {item.name === 'amharic' ? 'Amharic (አማርኛ)' : item.name === 'afaanOromo' ? 'Afaan Oromo' : 'English'}
                </span>
                <span className="text-sm font-bold text-white">{item.value} leads</span>
              </div>
            ))}
          </div>
          <div className="border-t border-slate-700 mt-2.5 pt-2 flex justify-between text-xs font-semibold text-slate-200">
            <span>Total Combined:</span>
            <span>{sum} Leads</span>
          </div>
        </div>
      );
    }
    return null;
  };

  return (
    <div className="w-full bg-slate-950 text-white p-6 rounded-2xl border border-slate-800 shadow-xl space-y-6">
      
      {/* Header Section */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <span className="text-xs font-bold uppercase tracking-wider text-amber-500 flex items-center gap-1.5 mb-1">
            <Activity className="w-3.5 h-3.5 text-amber-500" />
            SaaS Analytics Engine
          </span>
          <h2 className="text-xl md:text-2xl font-extrabold text-slate-50 tracking-tight">
            Lead Acquisition & Demographics
          </h2>
          <p className="text-slate-400 text-sm">
            Tracking multi-tenant lead growth across Ethiopian localized language preferences.
          </p>
        </div>

        {/* Dynamic Filter Controls */}
        <div className="flex items-center gap-2 self-start md:self-center">
          <div className="flex bg-slate-900 p-1 rounded-lg border border-slate-800">
            <button
              onClick={() => setChartType('cumulative')}
              className={`px-3 py-1.5 rounded-md text-xs font-medium transition-all flex items-center gap-1.5 ${
                chartType === 'cumulative'
                  ? 'bg-amber-500 text-slate-950 shadow-md font-bold'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              <TrendingUp className="w-3.5 h-3.5" />
              Cumulative Flow
            </button>
            <button
              onClick={() => setChartType('bar_breakdown')}
              className={`px-3 py-1.5 rounded-md text-xs font-medium transition-all flex items-center gap-1.5 ${
                chartType === 'bar_breakdown'
                  ? 'bg-amber-500 text-slate-950 shadow-md font-bold'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              <BarChart2 className="w-3.5 h-3.5" />
              Bar Distribution
            </button>
          </div>
        </div>
      </div>

      {/* KPI Cards Strip */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        {/* Total Leads Card */}
        <div className="bg-slate-900 border border-slate-850 p-4 rounded-xl flex items-center justify-between">
          <div>
            <p className="text-xs text-slate-400 font-medium uppercase tracking-wider">Total Active Leads</p>
            <h3 className="text-2xl font-extrabold text-slate-50 mt-1">{stats.total}</h3>
            <span className="text-xs text-emerald-400 font-medium flex items-center gap-1 mt-0.5">
              <ArrowUpRight className="w-3 h-3" />
              +{stats.growth}% this month
            </span>
          </div>
          <div className="bg-slate-800 p-3 rounded-lg text-amber-500">
            <Users className="w-5 h-5" />
          </div>
        </div>

        {/* Language Segment Summaries */}
        {stats.channels.map((ch) => (
          <div key={ch.name} className="bg-slate-900 border border-slate-850 p-4 rounded-xl space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-xs text-slate-400 font-semibold">{ch.name}</span>
              <span className="text-xs font-bold px-2 py-0.5 rounded-full" style={{ backgroundColor: `${ch.color}20`, color: ch.color }}>
                {ch.pct}% share
              </span>
            </div>
            <div className="flex items-baseline justify-between">
              <span className="text-xl font-bold text-slate-100">{ch.count}</span>
              <span className="text-[10px] text-slate-500">Inbound Leads</span>
            </div>
            <div className="w-full bg-slate-820 h-1.5 rounded-full overflow-hidden">
              <div 
                className="h-full rounded-full" 
                style={{ width: `${ch.pct}%`, backgroundColor: ch.color }}
              />
            </div>
          </div>
        ))}
      </div>

      {/* Chart Canvas Area */}
      <div className="bg-slate-900 border border-slate-850 p-4 rounded-xl h-[320px] w-full">
        <ResponsiveContainer width="100%" height="100%">
          {chartType === 'cumulative' ? (
            <AreaChart
              data={INITIAL_LEAD_DATA}
              margin={{ top: 10, right: 10, left: -20, bottom: 0 }}
            >
              <defs>
                <linearGradient id="colorAmharic" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#D4AF37" stopOpacity={0.4}/>
                  <stop offset="95%" stopColor="#D4AF37" stopOpacity={0}/>
                </linearGradient>
                <linearGradient id="colorOromo" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#10B981" stopOpacity={0.4}/>
                  <stop offset="95%" stopColor="#10B981" stopOpacity={0}/>
                </linearGradient>
                <linearGradient id="colorEnglish" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#3B82F6" stopOpacity={0.4}/>
                  <stop offset="95%" stopColor="#3B82F6" stopOpacity={0}/>
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#1E293B" />
              <XAxis 
                dataKey="date" 
                stroke="#64748B" 
                fontSize={11} 
                tickLine={false} 
              />
              <YAxis 
                stroke="#64748B" 
                fontSize={11} 
                tickLine={false}
              />
              <Tooltip content={<CustomTooltip />} />
              <Legend 
                verticalAlign="top" 
                height={36} 
                iconType="circle"
                formatter={(value) => {
                  if (value === 'amharic') return <span className="text-xs text-slate-300">Amharic (አማርኛ)</span>;
                  if (value === 'afaanOromo') return <span className="text-xs text-slate-300">Afaan Oromo</span>;
                  return <span className="text-xs text-slate-300">English</span>;
                }}
              />
              <Area 
                type="monotone" 
                dataKey="amharic" 
                stroke="#D4AF37" 
                strokeWidth={2}
                fillOpacity={1} 
                fill="url(#colorAmharic)" 
              />
              <Area 
                type="monotone" 
                dataKey="afaanOromo" 
                stroke="#10B981" 
                strokeWidth={2}
                fillOpacity={1} 
                fill="url(#colorOromo)" 
              />
              <Area 
                type="monotone" 
                dataKey="english" 
                stroke="#3B82F6" 
                strokeWidth={2}
                fillOpacity={1} 
                fill="url(#colorEnglish)" 
              />
            </AreaChart>
          ) : (
            <BarChart
              data={INITIAL_LEAD_DATA}
              margin={{ top: 10, right: 10, left: -20, bottom: 0 }}
            >
              <CartesianGrid strokeDasharray="3 3" stroke="#1E293B" />
              <XAxis 
                dataKey="date" 
                stroke="#64748B" 
                fontSize={11} 
                tickLine={false} 
              />
              <YAxis 
                stroke="#64748B" 
                fontSize={11} 
                tickLine={false}
              />
              <Tooltip content={<CustomTooltip />} />
              <Legend 
                verticalAlign="top" 
                height={36} 
                iconType="rect"
                formatter={(value) => {
                  if (value === 'amharic') return <span className="text-xs text-slate-300">Amharic (አማርኛ)</span>;
                  if (value === 'afaanOromo') return <span className="text-xs text-slate-300">Afaan Oromo</span>;
                  return <span className="text-xs text-slate-300">English</span>;
                }}
              />
              <Bar dataKey="amharic" fill="#D4AF37" radius={[4, 4, 0, 0]} />
              <Bar dataKey="afaanOromo" fill="#10B981" radius={[4, 4, 0, 0]} />
              <Bar dataKey="english" fill="#3B82F6" radius={[4, 4, 0, 0]} />
            </BarChart>
          )}
        </ResponsiveContainer>
      </div>

      {/* Enterprise Audit Footer */}
      <div className="flex items-center justify-between text-xs text-slate-500 pt-2 border-t border-slate-900">
        <span className="flex items-center gap-1">
          <Globe className="w-3.5 h-3.5" />
          Timezone: Africa/Addis_Ababa
        </span>
        <span>Last synced: Just now</span>
      </div>
    </div>
  );
}
