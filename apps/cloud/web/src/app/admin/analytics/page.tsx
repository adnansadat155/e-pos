'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { 
  ArrowLeft, 
  DollarSign, 
  TrendingUp, 
  ShoppingBag, 
  Clock, 
  PieChart, 
  BarChart3, 
  Users, 
  RefreshCw,
  Calendar
} from 'lucide-react';

interface CategoryMetric {
  categoryId: string;
  categoryName: string;
  itemCount: number;
  revenueCents: number;
  percentageOfGross: number;
}

interface HourlyBucket {
  hour: number;
  displayHour: string;
  orderCount: number;
  volumeCents: number;
}

interface AnalyticsData {
  range: string;
  dateFormatted: string;
  grossSalesCents: number;
  netRevenueCents: number;
  orderCount: number;
  averageCheckCents: number;
  laborCostRatioPercent: number;
  averageTableTurnMinutes: number;
  categories: CategoryMetric[];
  hourlyDistribution: HourlyBucket[];
  channelDistribution: {
    pos: number;
    online: number;
    handheld: number;
  };
}

export default function AdminAnalyticsPage() {
  const [range, setRange] = useState<'today' | 'yesterday' | 'week' | 'month'>('today');
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<AnalyticsData>({
    range: 'today',
    dateFormatted: 'Monday, Oct 20, 2026',
    grossSalesCents: 482150,
    netRevenueCents: 467950,
    orderCount: 68,
    averageCheckCents: 7090,
    laborCostRatioPercent: 23.8,
    averageTableTurnMinutes: 46.5,
    categories: [
      { categoryId: 'cat_mains', categoryName: 'Steaks & Wood-Fired Mains', itemCount: 56, revenueCents: 250718, percentageOfGross: 52.0 },
      { categoryId: 'cat_starters', categoryName: 'Appetizers & Crudo', itemCount: 38, revenueCents: 86787, percentageOfGross: 18.0 },
      { categoryId: 'cat_drinks', categoryName: 'Craft Cocktails & Wine', itemCount: 44, revenueCents: 67501, percentageOfGross: 14.0 },
      { categoryId: 'cat_sides', categoryName: 'Market Sides & Greens', itemCount: 28, revenueCents: 57858, percentageOfGross: 12.0 },
      { categoryId: 'cat_desserts', categoryName: 'Artisanal Desserts', itemCount: 14, revenueCents: 19286, percentageOfGross: 4.0 },
    ],
    hourlyDistribution: [
      { hour: 11, displayHour: '11:00 AM', orderCount: 3, volumeCents: 19286 },
      { hour: 12, displayHour: '12:00 PM', orderCount: 8, volumeCents: 57858 },
      { hour: 13, displayHour: '1:00 PM', orderCount: 10, volumeCents: 67501 },
      { hour: 14, displayHour: '2:00 PM', orderCount: 4, volumeCents: 28929 },
      { hour: 17, displayHour: '5:00 PM', orderCount: 4, volumeCents: 28929 },
      { hour: 18, displayHour: '6:00 PM', orderCount: 11, volumeCents: 77144 },
      { hour: 19, displayHour: '7:00 PM', orderCount: 15, volumeCents: 106073 },
      { hour: 20, displayHour: '8:00 PM', orderCount: 8, volumeCents: 57858 },
      { hour: 21, displayHour: '9:00 PM', orderCount: 4, volumeCents: 28929 },
    ],
    channelDistribution: {
      pos: 39,
      online: 19,
      handheld: 10,
    },
  });

  const fetchAnalytics = async (selectedRange: string) => {
    setLoading(true);
    const gatewayBase = process.env.NEXT_PUBLIC_GATEWAY_URL || 'http://localhost:4000';
    try {
      const res = await fetch(`${gatewayBase}/api/v1/analytics/summary?range=${selectedRange}`);
      if (res.ok) {
        const result = await res.json();
        setData(result);
      }
    } catch {
      // Retain fallback data
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAnalytics(range);
  }, [range]);

  const maxHourlyVolume = Math.max(...data.hourlyDistribution.map((h) => h.volumeCents), 1);

  return (
    <div className="min-h-screen bg-brand-onyx text-brand-white">
      {/* Header */}
      <header className="border-b border-brand-slate px-6 py-4 flex items-center justify-between max-w-6xl mx-auto">
        <Link href="/admin" className="flex items-center gap-2 text-sm text-gray-400 hover:text-brand-white transition-colors">
          <ArrowLeft className="w-4 h-4" />
          Back to Admin
        </Link>
        <span className="font-bold text-lg text-brand-white font-display">
          Dinevique <span className="text-brand-amber text-xs px-2 py-0.5 rounded bg-brand-slate font-sans">Analytics</span>
        </span>
        <button
          onClick={() => fetchAnalytics(range)}
          disabled={loading}
          className="flex items-center gap-1.5 text-xs text-gray-400 hover:text-brand-white transition-colors"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin text-brand-amber' : ''}`} />
          Refresh
        </button>
      </header>

      <main className="max-w-6xl mx-auto px-6 py-8">
        {/* Title & Date Filter Bar */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
          <div>
            <h1 className="text-3xl font-bold font-display text-brand-white">Operational & Financial Intelligence</h1>
            <p className="text-xs text-gray-400 mt-1 flex items-center gap-1.5">
              <Calendar className="w-3.5 h-3.5 text-brand-amber" />
              Reporting Period: {data.dateFormatted} • Manhattan Flagship (#01)
            </p>
          </div>

          <div className="flex items-center bg-brand-slate p-1 rounded-lg border border-brand-linen/10">
            {(['today', 'yesterday', 'week', 'month'] as const).map((r) => (
              <button
                key={r}
                onClick={() => setRange(r)}
                className={`text-xs px-3 py-1.5 rounded-md font-medium capitalize transition-all ${
                  range === r
                    ? 'bg-brand-amber text-brand-ink font-bold shadow-sm'
                    : 'text-gray-400 hover:text-brand-white'
                }`}
              >
                {r === 'week' ? 'Last 7 Days' : r === 'month' ? 'Month to Date' : r}
              </button>
            ))}
          </div>
        </div>

        {/* Core KPI Cards */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div className="p-5 rounded-xl bg-brand-slate border border-brand-linen/10">
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs text-gray-400 uppercase tracking-wider">Gross Sales</span>
              <DollarSign className="w-5 h-5 text-brand-amber" />
            </div>
            <div className="text-2xl font-bold text-brand-white font-mono">
              ${(data.grossSalesCents / 100).toLocaleString('en-US', { minimumFractionDigits: 2 })}
            </div>
            <div className="text-xs text-status-success mt-2 flex items-center gap-1">
              <TrendingUp className="w-3.5 h-3.5" /> +14.2% vs previous period
            </div>
          </div>

          <div className="p-5 rounded-xl bg-brand-slate border border-brand-linen/10">
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs text-gray-400 uppercase tracking-wider">Net Revenue</span>
              <DollarSign className="w-5 h-5 text-brand-amber" />
            </div>
            <div className="text-2xl font-bold text-brand-white font-mono">
              ${(data.netRevenueCents / 100).toLocaleString('en-US', { minimumFractionDigits: 2 })}
            </div>
            <div className="text-xs text-gray-400 mt-2">
              After comps, discounts & refunds
            </div>
          </div>

          <div className="p-5 rounded-xl bg-brand-slate border border-brand-linen/10">
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs text-gray-400 uppercase tracking-wider">Average Check</span>
              <ShoppingBag className="w-5 h-5 text-brand-amber" />
            </div>
            <div className="text-2xl font-bold text-brand-white font-mono">
              ${(data.averageCheckCents / 100).toFixed(2)}
            </div>
            <div className="text-xs text-gray-400 mt-2">
              Across {data.orderCount} fulfilled tickets
            </div>
          </div>

          <div className="p-5 rounded-xl bg-brand-slate border border-brand-linen/10">
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs text-gray-400 uppercase tracking-wider">Labor Cost Ratio</span>
              <Users className="w-5 h-5 text-brand-amber" />
            </div>
            <div className="text-2xl font-bold text-brand-white font-mono">
              {data.laborCostRatioPercent}%
            </div>
            <div className="text-xs text-status-success mt-2">
              Optimal benchmark (&lt; 28.0%)
            </div>
          </div>
        </div>

        {/* Charts & Breakdown Row */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 mb-8">
          {/* Category Revenue Distribution (2 cols) */}
          <div className="lg:col-span-2 p-6 rounded-xl bg-brand-slate border border-brand-linen/10">
            <h2 className="text-base font-bold text-brand-white mb-6 flex items-center gap-2">
              <BarChart3 className="w-4 h-4 text-brand-amber" />
              Category Revenue Share
            </h2>

            <div className="space-y-5">
              {data.categories.map((cat) => (
                <div key={cat.categoryId}>
                  <div className="flex justify-between items-center text-xs mb-1.5">
                    <span className="font-medium text-gray-300">{cat.categoryName}</span>
                    <span className="font-mono text-gray-400">
                      ${(cat.revenueCents / 100).toLocaleString('en-US', { minimumFractionDigits: 2 })} ({cat.percentageOfGross}%)
                    </span>
                  </div>
                  <div className="w-full h-2 rounded-full bg-brand-onyx overflow-hidden">
                    <div
                      className="h-full bg-brand-amber rounded-full transition-all duration-700 ease-out"
                      style={{ width: `${cat.percentageOfGross}%` }}
                    ></div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Omnichannel Channel Share (1 col) */}
          <div className="p-6 rounded-xl bg-brand-slate border border-brand-linen/10">
            <h2 className="text-base font-bold text-brand-white mb-6 flex items-center gap-2">
              <PieChart className="w-4 h-4 text-brand-amber" />
              Order Source Channels
            </h2>

            <div className="space-y-4">
              <div className="p-3 rounded-lg bg-brand-onyx border border-gray-800">
                <div className="flex justify-between text-xs mb-1">
                  <span className="text-gray-300 font-medium">In-Store Master POS</span>
                  <span className="font-mono text-brand-white font-bold">{data.channelDistribution.pos} orders</span>
                </div>
                <div className="text-xs text-gray-500">58% of total volume</div>
              </div>

              <div className="p-3 rounded-lg bg-brand-onyx border border-gray-800">
                <div className="flex justify-between text-xs mb-1">
                  <span className="text-gray-300 font-medium">Online Omnichannel Storefront</span>
                  <span className="font-mono text-brand-white font-bold">{data.channelDistribution.online} orders</span>
                </div>
                <div className="text-xs text-gray-500">28% of total volume</div>
              </div>

              <div className="p-3 rounded-lg bg-brand-onyx border border-gray-800">
                <div className="flex justify-between text-xs mb-1">
                  <span className="text-gray-300 font-medium">Sunmi V2 Handheld Terminals</span>
                  <span className="font-mono text-brand-white font-bold">{data.channelDistribution.handheld} orders</span>
                </div>
                <div className="text-xs text-gray-500">14% of total volume</div>
              </div>

              <div className="pt-2 text-xs text-gray-400 flex items-center justify-between border-t border-gray-800">
                <span className="flex items-center gap-1.5">
                  <Clock className="w-3.5 h-3.5 text-brand-amber" />
                  Table Turn Duration
                </span>
                <span className="font-bold text-brand-white font-mono">{data.averageTableTurnMinutes} mins</span>
              </div>
            </div>
          </div>
        </div>

        {/* Hourly Volume Distribution Matrix */}
        <div className="p-6 rounded-xl bg-brand-slate border border-brand-linen/10">
          <h2 className="text-base font-bold text-brand-white mb-6 flex items-center justify-between">
            <span className="flex items-center gap-2">
              <Clock className="w-4 h-4 text-brand-amber" />
              Hourly Sales Velocity Heatmap
            </span>
            <span className="text-xs font-normal text-gray-400">Peak dining periods: 12-2 PM & 6-9 PM</span>
          </h2>

          <div className="grid grid-cols-3 sm:grid-cols-5 md:grid-cols-9 gap-3">
            {data.hourlyDistribution.map((slot) => {
              const heightPercent = Math.max(12, Math.round((slot.volumeCents / maxHourlyVolume) * 100));
              const isPeak = slot.orderCount >= 8;

              return (
                <div key={slot.hour} className="flex flex-col items-center">
                  <div className="text-xs font-mono text-gray-400 mb-2">${(slot.volumeCents / 100).toFixed(0)}</div>
                  <div className="w-full h-24 bg-brand-onyx rounded-lg p-1 flex items-end">
                    <div
                      className={`w-full rounded-md transition-all duration-500 ${
                        isPeak ? 'bg-brand-amber' : 'bg-brand-slate border border-gray-700'
                      }`}
                      style={{ height: `${heightPercent}%` }}
                    ></div>
                  </div>
                  <div className="text-xs text-gray-400 mt-2 font-mono">{slot.displayHour.split(' ')[0]}</div>
                  <div className="text-xs text-gray-500">{slot.orderCount} ord</div>
                </div>
              );
            })}
          </div>
        </div>
      </main>
    </div>
  );
}
