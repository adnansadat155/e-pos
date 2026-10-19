import Link from 'next/link';
import { ArrowLeft, Activity, DollarSign, Users, Terminal } from 'lucide-react';

export default function AdminPage() {
  const stats = [
    { label: "Today's Gross Sales", value: "$4,821.50", icon: DollarSign, trend: "+12.4% vs last week" },
    { label: "Active Orders", value: "8 in-flight", icon: Activity, trend: "4 POS • 3 Online • 1 Handheld" },
    { label: "Staff on Shift", value: "6 clocked in", icon: Users, trend: "2 Servers • 2 Cooks • 2 Cashiers" },
    { label: "Connected Terminals", value: "3 / 3 Online", icon: Terminal, trend: "Master POS • KDB • Handheld" },
  ];

  return (
    <div className="min-h-screen bg-brand-onyx text-brand-white">
      {/* Admin Header */}
      <header className="border-b border-brand-slate px-6 py-4 flex items-center justify-between max-w-6xl mx-auto">
        <Link href="/" className="flex items-center gap-2 text-sm text-gray-400 hover:text-brand-white">
          <ArrowLeft className="w-4 h-4" />
          Back to Overview
        </Link>
        <span className="font-bold text-lg text-brand-white font-display">
          Dinevique Cloud <span className="text-xs px-2 py-0.5 rounded bg-brand-slate text-brand-amber font-sans">Admin</span>
        </span>
        <div className="text-xs text-gray-400">Location: Manhattan Flagship (#01)</div>
      </header>

      <main className="max-w-6xl mx-auto px-6 py-8">
        <h1 className="text-3xl font-bold mb-6 font-display">Store Operations Overview</h1>

        {/* Stats Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-10">
          {stats.map((stat) => {
            const Icon = stat.icon;
            return (
              <div key={stat.label} className="p-5 rounded-lg bg-brand-slate border border-brand-linen/10">
                <div className="flex items-center justify-between mb-3">
                  <span className="text-xs text-gray-400 uppercase tracking-wide">{stat.label}</span>
                  <Icon className="w-5 h-5 text-brand-amber" />
                </div>
                <div className="text-2xl font-bold text-brand-white mb-1">{stat.value}</div>
                <div className="text-xs text-gray-400">{stat.trend}</div>
              </div>
            );
          })}
        </div>

        {/* Quick Hub Navigation */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-10">
          <Link
            href="/admin/menu"
            className="p-5 rounded-lg bg-brand-slate/60 border border-brand-linen/10 hover:border-brand-amber/40 transition-all flex items-center justify-between group"
          >
            <div>
              <span className="text-xs font-semibold text-brand-amber uppercase tracking-wider">Catalog & Pricing</span>
              <div className="text-base font-bold text-brand-white mt-1 group-hover:text-brand-amber transition-colors">
                Omnichannel Menu Studio
              </div>
              <span className="text-xs text-gray-400 mt-1 block">Categories, items, and modifiers</span>
            </div>
            <ArrowLeft className="w-4 h-4 text-gray-500 rotate-180 group-hover:text-brand-amber group-hover:translate-x-1 transition-all" />
          </Link>

          <Link
            href="/admin/analytics"
            className="p-5 rounded-lg bg-brand-slate/60 border border-brand-linen/10 hover:border-brand-amber/40 transition-all flex items-center justify-between group"
          >
            <div>
              <span className="text-xs font-semibold text-brand-amber uppercase tracking-wider">Intelligence</span>
              <div className="text-base font-bold text-brand-white mt-1 group-hover:text-brand-amber transition-colors">
                Sales & Operations Analytics
              </div>
              <span className="text-xs text-gray-400 mt-1 block">Gross sales, net revenue, and heatmaps</span>
            </div>
            <ArrowLeft className="w-4 h-4 text-gray-500 rotate-180 group-hover:text-brand-amber group-hover:translate-x-1 transition-all" />
          </Link>

          <Link
            href="/admin/terminals"
            className="p-5 rounded-lg bg-brand-slate/60 border border-brand-linen/10 hover:border-brand-amber/40 transition-all flex items-center justify-between group"
          >
            <div>
              <span className="text-xs font-semibold text-brand-amber uppercase tracking-wider">Infrastructure</span>
              <div className="text-base font-bold text-brand-white mt-1 group-hover:text-brand-amber transition-colors">
                Edge Terminal Telemetry
              </div>
              <span className="text-xs text-gray-400 mt-1 block">Heartbeats, LAN latency, and printers</span>
            </div>
            <ArrowLeft className="w-4 h-4 text-gray-500 rotate-180 group-hover:text-brand-amber group-hover:translate-x-1 transition-all" />
          </Link>
        </div>
        <section className="p-6 rounded-lg bg-brand-slate border border-brand-linen/10">
          <h2 className="text-lg font-bold mb-4 flex items-center gap-2">
            <span className="h-2.5 w-2.5 rounded-full bg-status-success animate-pulse inline-block"></span>
            Edge Terminal Mesh Heartbeat Status
          </h2>
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm text-gray-300">
              <thead className="border-b border-brand-onyx text-xs text-gray-400 uppercase">
                <tr>
                  <th className="py-3 px-4">Terminal ID</th>
                  <th className="py-3 px-4">Role</th>
                  <th className="py-3 px-4">Subnet IP</th>
                  <th className="py-3 px-4">Last Ping</th>
                  <th className="py-3 px-4">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-brand-onyx">
                <tr>
                  <td className="py-3 px-4 font-mono text-xs">term_pos_master_01</td>
                  <td className="py-3 px-4 font-medium text-brand-amber">Master POS (Orchestrator)</td>
                  <td className="py-3 px-4 font-mono text-xs">192.168.1.100:8080</td>
                  <td className="py-3 px-4">3 seconds ago</td>
                  <td className="py-3 px-4"><span className="px-2 py-0.5 rounded bg-status-success/20 text-status-success text-xs font-semibold">ONLINE (WAN CONNECTED)</span></td>
                </tr>
                <tr>
                  <td className="py-3 px-4 font-mono text-xs">term_kdb_expo_01</td>
                  <td className="py-3 px-4 font-medium text-status-info">Dinevique Kitchen (KDB)</td>
                  <td className="py-3 px-4 font-mono text-xs">192.168.1.105</td>
                  <td className="py-3 px-4">8 seconds ago</td>
                  <td className="py-3 px-4"><span className="px-2 py-0.5 rounded bg-status-success/20 text-status-success text-xs font-semibold">CONNECTED TO MASTER</span></td>
                </tr>
                <tr>
                  <td className="py-3 px-4 font-mono text-xs">term_hnd_floor_01</td>
                  <td className="py-3 px-4 font-medium text-status-pending">Dinevique Handheld (Sunmi V2)</td>
                  <td className="py-3 px-4 font-mono text-xs">192.168.1.112</td>
                  <td className="py-3 px-4">12 seconds ago</td>
                  <td className="py-3 px-4"><span className="px-2 py-0.5 rounded bg-status-success/20 text-status-success text-xs font-semibold">CONNECTED TO MASTER</span></td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </main>
    </div>
  );
}
