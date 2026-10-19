import Link from 'next/link';
import { Receipt, Monitor, Smartphone, Globe, Cloud } from 'lucide-react';

export default function HomePage() {
  const modules = [
    {
      name: 'Dinevique POS',
      role: 'Master Terminal Orchestrator',
      icon: Receipt,
      accent: 'border-brand-amber text-brand-amber',
      tag: 'Native Android',
      status: 'Core Active',
    },
    {
      name: 'Dinevique Kitchen',
      role: 'Kitchen Display Board (KDB)',
      icon: Monitor,
      accent: 'border-status-info text-status-info',
      tag: 'Native Android',
      status: 'Real-Time LAN',
    },
    {
      name: 'Dinevique Handheld',
      role: 'Mobile Tableside Ordering',
      icon: Smartphone,
      accent: 'border-status-pending text-status-pending',
      tag: 'Sunmi V2',
      status: '3-Tap Ordering',
    },
    {
      name: 'Dinevique Online',
      role: 'Direct Consumer Web Storefront',
      icon: Globe,
      accent: 'border-status-success text-status-success',
      tag: 'Next.js SSR',
      status: 'Live Status Tracker',
    },
    {
      name: 'Dinevique Cloud',
      role: 'Centralized Merchant Admin',
      icon: Cloud,
      accent: 'border-gray-400 text-gray-300',
      tag: 'Next.js & Node.js',
      status: 'Multi-Location Analytics',
    },
  ];

  return (
    <main className="max-w-6xl mx-auto px-6 py-16">
      {/* Brand Hero */}
      <header className="text-center mb-16">
        <span className="text-xs font-semibold tracking-widest uppercase text-brand-amber mb-3 block">
          Unified Hospitality Operating System
        </span>
        <h1 className="text-4xl md:text-5xl font-extrabold tracking-tight mb-4 font-display">
          Dinevique
        </h1>
        <p className="text-xl italic text-gray-300 font-display">
          The Art of Smarter Dining.
        </p>
      </header>

      {/* Module Architecture Cards */}
      <section className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-16">
        {modules.map((mod) => {
          const Icon = mod.icon;
          return (
            <div
              key={mod.name}
              className={`p-6 rounded-lg bg-brand-slate border ${mod.accent} flex flex-col justify-between`}
            >
              <div>
                <div className="flex items-center justify-between mb-4">
                  <Icon className="w-8 h-8" />
                  <span className="text-xs px-2.5 py-1 rounded bg-brand-onyx border border-brand-linen text-gray-300">
                    {mod.tag}
                  </span>
                </div>
                <h2 className="text-xl font-bold text-brand-white mb-1">
                  {mod.name}
                </h2>
                <p className="text-sm text-gray-400 mb-4">{mod.role}</p>
              </div>
              <div className="pt-4 border-t border-brand-linen/20 flex items-center justify-between">
                <span className="text-xs font-medium text-gray-400">
                  {mod.status}
                </span>
                <span className="h-2 w-2 rounded-full bg-status-success inline-block"></span>
              </div>
            </div>
          );
        })}
      </section>

      {/* Quick Launch CTA Navigation */}
      <section className="flex flex-col sm:flex-row items-center justify-center gap-4">
        <Link href="/menu" className="btn-primary w-full sm:w-auto">
          Explore Dinevique Online Storefront
        </Link>
        <Link
          href="/track/ord_live_demo"
          className="w-full sm:w-auto px-6 py-3 rounded bg-brand-slate border border-brand-amber/50 hover:bg-brand-slate/80 text-brand-amber text-center font-medium transition"
        >
          Customer Live Order Tracker
        </Link>
        <Link
          href="/admin"
          className="w-full sm:w-auto px-6 py-3 rounded bg-brand-slate border border-brand-linen hover:bg-brand-slate/80 text-center font-medium transition"
        >
          Open Dinevique Cloud Admin
        </Link>
      </section>
    </main>
  );
}
