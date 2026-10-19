'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { useParams } from 'next/navigation';
import { 
  ArrowLeft, 
  CheckCircle2, 
  Clock, 
  Flame, 
  ShoppingBag, 
  Sparkles, 
  Receipt, 
  ChevronDown, 
  ChevronUp,
  Radio,
  RefreshCw,
  Bell
} from 'lucide-react';

interface OrderItem {
  name: string;
  quantity: number;
  modifiers?: string[];
  priceCents: number;
}

interface OrderTrackerData {
  orderUuid: string;
  pickupCode: string;
  status: 'ORDER_PLACED' | 'IN_PREPARATION' | 'READY_FOR_PICKUP' | 'COMPLETED';
  etaMinutes: number;
  tableLabel?: string;
  sourceChannel: string;
  items: OrderItem[];
  subtotalCents: number;
  taxCents: number;
  tipCents: number;
  totalCents: number;
  createdAt: string;
  updatedAt: string;
}

const STAGES = [
  { id: 'ORDER_PLACED', label: 'Order Received', desc: 'Sent to the kitchen ticket rail', icon: Clock },
  { id: 'IN_PREPARATION', label: 'In the Kitchen', desc: 'Line chefs are firing hot items', icon: Flame },
  { id: 'READY_FOR_PICKUP', label: 'Ready for Pickup', desc: 'Order plated and ready at counter', icon: ShoppingBag },
  { id: 'COMPLETED', label: 'Completed', desc: 'Order picked up and fulfilled', icon: CheckCircle2 },
];

export default function OrderTrackerPage() {
  const params = useParams();
  const orderUuid = (params?.orderUuid as string) || 'ord_live_demo';

  const [order, setOrder] = useState<OrderTrackerData>({
    orderUuid,
    pickupCode: 'B-42',
    status: 'IN_PREPARATION',
    etaMinutes: 12,
    tableLabel: 'Online Pickup',
    sourceChannel: 'ONLINE',
    items: [
      { name: 'Prime Dry-Aged Ribeye', quantity: 1, modifiers: ['Medium Rare', 'Truffle Butter Glaze'], priceCents: 4800 },
      { name: 'Charred Broccolini', quantity: 1, modifiers: ['Garlic Confit', 'Toasted Almonds'], priceCents: 1400 },
      { name: 'Sparkling Mineral Water', quantity: 2, priceCents: 1000 },
    ],
    subtotalCents: 7200,
    taxCents: 612,
    tipCents: 1200,
    totalCents: 9012,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  });

  const [connectionStatus, setConnectionStatus] = useState<'CONNECTING' | 'CONNECTED' | 'FALLBACK'>('CONNECTING');
  const [receiptOpen, setReceiptOpen] = useState(false);
  const [isUpdating, setIsUpdating] = useState(false);
  const [lastNotification, setLastNotification] = useState<string | null>(null);

  // SSE Stream Connection
  useEffect(() => {
    const gatewayBase = process.env.NEXT_PUBLIC_GATEWAY_URL || 'http://localhost:4000';
    const streamUrl = `${gatewayBase}/api/v1/orders/${orderUuid}/stream`;

    let eventSource: EventSource | null = null;
    let fallbackInterval: NodeJS.Timeout | null = null;

    try {
      eventSource = new EventSource(streamUrl);

      eventSource.onopen = () => {
        setConnectionStatus('CONNECTED');
      };

      eventSource.addEventListener('INITIAL_STATE', (e: MessageEvent) => {
        try {
          const data = JSON.parse(e.data);
          setOrder(data);
          setConnectionStatus('CONNECTED');
        } catch {
          // Parse error
        }
      });

      eventSource.addEventListener('ORDER_UPDATED', (e: MessageEvent) => {
        try {
          const data = JSON.parse(e.data);
          setOrder(data);
          setConnectionStatus('CONNECTED');
          setLastNotification(`Status updated to ${data.status.replace(/_/g, ' ')}`);
        } catch {
          // Parse error
        }
      });

      eventSource.onerror = () => {
        setConnectionStatus('FALLBACK');
        if (eventSource) {
          eventSource.close();
        }
        // Start polling fallback every 5 seconds
        fallbackInterval = setInterval(async () => {
          try {
            const res = await fetch(`${gatewayBase}/api/v1/orders/${orderUuid}`);
            if (res.ok) {
              const data = await res.json();
              setOrder(data);
            }
          } catch {
            // Offline polling fail
          }
        }, 5000);
      };
    } catch {
      setConnectionStatus('FALLBACK');
    }

    return () => {
      if (eventSource) eventSource.close();
      if (fallbackInterval) clearInterval(fallbackInterval);
    };
  }, [orderUuid]);

  // Stage index calculation
  const currentStageIndex = STAGES.findIndex((s) => s.id === order.status);

  // Simulated kitchen bump action for demonstration and verification testing
  const simulateStatusTransition = async (nextStatus: OrderTrackerData['status']) => {
    setIsUpdating(true);
    const gatewayBase = process.env.NEXT_PUBLIC_GATEWAY_URL || 'http://localhost:4000';
    try {
      const res = await fetch(`${gatewayBase}/api/v1/orders/${orderUuid}/status`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: nextStatus }),
      });
      if (res.ok) {
        const data = await res.json();
        setOrder(data.order);
      } else {
        // Local simulation fallback
        setOrder((prev) => ({
          ...prev,
          status: nextStatus,
          etaMinutes: nextStatus === 'READY_FOR_PICKUP' ? 0 : nextStatus === 'COMPLETED' ? 0 : 8,
          updatedAt: new Date().toISOString(),
        }));
      }
    } catch {
      setOrder((prev) => ({
        ...prev,
        status: nextStatus,
        etaMinutes: nextStatus === 'READY_FOR_PICKUP' ? 0 : nextStatus === 'COMPLETED' ? 0 : 8,
        updatedAt: new Date().toISOString(),
      }));
    } finally {
      setIsUpdating(false);
    }
  };

  return (
    <div className="min-h-screen bg-brand-onyx text-brand-white">
      {/* Top Header */}
      <header className="border-b border-brand-slate px-6 py-4 flex items-center justify-between max-w-4xl mx-auto">
        <Link href="/menu" className="flex items-center gap-2 text-sm text-gray-400 hover:text-brand-white transition-colors">
          <ArrowLeft className="w-4 h-4" />
          Back to Menu
        </Link>
        <span className="font-bold text-lg text-brand-white font-display">
          Dinevique <span className="text-brand-amber text-xs px-2 py-0.5 rounded bg-brand-slate font-sans">Live Tracker</span>
        </span>
        <div className="flex items-center gap-2">
          {connectionStatus === 'CONNECTED' ? (
            <span className="flex items-center gap-1.5 text-xs text-status-success font-medium bg-status-success/10 px-2.5 py-1 rounded-full border border-status-success/20">
              <span className="w-2 h-2 rounded-full bg-status-success animate-pulse"></span>
              Live Sync Active
            </span>
          ) : (
            <span className="flex items-center gap-1.5 text-xs text-brand-amber font-medium bg-brand-amber/10 px-2.5 py-1 rounded-full border border-brand-amber/20">
              <Radio className="w-3 h-3 animate-spin" />
              Polling Stream
            </span>
          )}
        </div>
      </header>

      <main className="max-w-4xl mx-auto px-6 py-8">
        {/* Toast alert if status updated */}
        {lastNotification && (
          <div className="mb-6 p-3 rounded-lg bg-brand-amber/15 border border-brand-amber/30 text-xs text-brand-amber flex items-center justify-between">
            <span className="flex items-center gap-2">
              <Bell className="w-4 h-4" />
              {lastNotification}
            </span>
            <button 
              onClick={() => setLastNotification(null)}
              className="text-gray-400 hover:text-brand-white font-medium ml-4"
            >
              Dismiss
            </button>
          </div>
        )}

        {/* Hero Pickup Card */}
        <div className="rounded-xl bg-brand-slate border border-brand-linen/10 p-6 md:p-8 mb-8 relative overflow-hidden">
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
            <div>
              <span className="text-xs uppercase tracking-widest text-gray-400 font-semibold">Pickup Code</span>
              <div className="text-5xl md:text-6xl font-black text-brand-amber font-mono mt-1 tracking-tight">
                {order.pickupCode}
              </div>
              <p className="text-xs text-gray-400 mt-2">
                Order reference: <span className="font-mono text-gray-300">{order.orderUuid}</span>
              </p>
            </div>

            <div className="md:text-right">
              <span className="text-xs uppercase tracking-widest text-gray-400 font-semibold">Estimated Preparation</span>
              <div className="text-3xl md:text-4xl font-bold text-brand-white mt-1">
                {order.status === 'READY_FOR_PICKUP' ? (
                  <span className="text-status-success flex items-center md:justify-end gap-2">
                    <Sparkles className="w-7 h-7" /> Ready Now
                  </span>
                ) : order.status === 'COMPLETED' ? (
                  <span className="text-gray-400">Fulfilled</span>
                ) : (
                  <span>{order.etaMinutes} minutes</span>
                )}
              </div>
              <p className="text-xs text-gray-400 mt-2">
                Collection Point: Manhattan Flagship Expo Counter
              </p>
            </div>
          </div>
        </div>

        {/* 4-Stage Stepper Progress Tracker */}
        <div className="rounded-xl bg-brand-slate/60 border border-brand-linen/10 p-6 md:p-8 mb-8">
          <h2 className="text-sm font-semibold text-gray-300 uppercase tracking-wider mb-6">
            Preparation Progress
          </h2>

          <div className="relative">
            {/* Connecting line */}
            <div className="absolute top-5 left-4 right-4 h-0.5 bg-gray-800 -z-0 hidden md:block">
              <div 
                className="h-full bg-brand-amber transition-all duration-700 ease-out"
                style={{ width: `${(Math.max(0, currentStageIndex) / (STAGES.length - 1)) * 100}%` }}
              ></div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-4 gap-6 relative z-10">
              {STAGES.map((stage, idx) => {
                const isComplete = idx < currentStageIndex;
                const isCurrent = idx === currentStageIndex;
                const Icon = stage.icon;

                return (
                  <div key={stage.id} className="flex md:flex-col items-center md:items-center text-left md:text-center gap-4 md:gap-3">
                    <div 
                      className={`w-10 h-10 rounded-full flex items-center justify-center transition-all ${
                        isComplete
                          ? 'bg-status-success text-brand-white shadow-lg shadow-status-success/20'
                          : isCurrent
                          ? 'bg-brand-amber text-brand-ink ring-4 ring-brand-amber/30 animate-pulse'
                          : 'bg-brand-onyx border border-gray-700 text-gray-500'
                      }`}
                    >
                      {isComplete ? <CheckCircle2 className="w-5 h-5" /> : <Icon className="w-5 h-5" />}
                    </div>

                    <div>
                      <div className={`text-sm font-bold ${isCurrent ? 'text-brand-amber' : isComplete ? 'text-brand-white' : 'text-gray-400'}`}>
                        {stage.label}
                      </div>
                      <div className="text-xs text-gray-400 mt-0.5">
                        {stage.desc}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>

        {/* Interactive Simulation Controls (For Operational Testing & Demonstrations) */}
        <div className="rounded-xl bg-brand-slate/40 border border-brand-linen/10 p-5 mb-8">
          <div className="flex items-center justify-between mb-3">
            <span className="text-xs uppercase tracking-wider text-brand-amber font-semibold flex items-center gap-1.5">
              <RefreshCw className={`w-3.5 h-3.5 ${isUpdating ? 'animate-spin' : ''}`} />
              Simulate Kitchen Bump Stream Actions
            </span>
            <span className="text-xs text-gray-400">Triggers SSE Event</span>
          </div>
          <div className="flex flex-wrap gap-2">
            <button
              onClick={() => simulateStatusTransition('ORDER_PLACED')}
              disabled={isUpdating}
              className={`text-xs px-3 py-1.5 rounded transition-all font-medium ${
                order.status === 'ORDER_PLACED' 
                  ? 'bg-brand-amber text-brand-ink font-bold' 
                  : 'bg-brand-onyx text-gray-300 hover:bg-gray-800'
              }`}
            >
              1. Order Received
            </button>
            <button
              onClick={() => simulateStatusTransition('IN_PREPARATION')}
              disabled={isUpdating}
              className={`text-xs px-3 py-1.5 rounded transition-all font-medium ${
                order.status === 'IN_PREPARATION' 
                  ? 'bg-brand-amber text-brand-ink font-bold' 
                  : 'bg-brand-onyx text-gray-300 hover:bg-gray-800'
              }`}
            >
              2. Firing in Kitchen
            </button>
            <button
              onClick={() => simulateStatusTransition('READY_FOR_PICKUP')}
              disabled={isUpdating}
              className={`text-xs px-3 py-1.5 rounded transition-all font-medium ${
                order.status === 'READY_FOR_PICKUP' 
                  ? 'bg-brand-amber text-brand-ink font-bold' 
                  : 'bg-brand-onyx text-gray-300 hover:bg-gray-800'
              }`}
            >
              3. Ready at Counter
            </button>
            <button
              onClick={() => simulateStatusTransition('COMPLETED')}
              disabled={isUpdating}
              className={`text-xs px-3 py-1.5 rounded transition-all font-medium ${
                order.status === 'COMPLETED' 
                  ? 'bg-brand-amber text-brand-ink font-bold' 
                  : 'bg-brand-onyx text-gray-300 hover:bg-gray-800'
              }`}
            >
              4. Complete Order
            </button>
          </div>
        </div>

        {/* Itemized Receipt Breakdown */}
        <div className="rounded-xl bg-brand-slate/60 border border-brand-linen/10 overflow-hidden">
          <button
            onClick={() => setReceiptOpen(!receiptOpen)}
            className="w-full px-6 py-4 flex items-center justify-between text-left hover:bg-brand-slate transition-colors"
          >
            <div className="flex items-center gap-3">
              <Receipt className="w-5 h-5 text-brand-amber" />
              <div>
                <span className="text-sm font-bold text-brand-white">Itemized Receipt</span>
                <span className="text-xs text-gray-400 block">{order.items.length} items • ${(order.totalCents / 100).toFixed(2)}</span>
              </div>
            </div>
            {receiptOpen ? <ChevronUp className="w-5 h-5 text-gray-400" /> : <ChevronDown className="w-5 h-5 text-gray-400" />}
          </button>

          {receiptOpen && (
            <div className="px-6 pb-6 pt-2 border-t border-brand-onyx space-y-4">
              <div className="divide-y divide-brand-onyx">
                {order.items.map((item, idx) => (
                  <div key={idx} className="py-3 flex justify-between items-start">
                    <div>
                      <div className="text-sm font-medium text-brand-white">
                        {item.quantity}x {item.name}
                      </div>
                      {item.modifiers && item.modifiers.length > 0 && (
                        <div className="text-xs text-gray-400 mt-1 pl-4 border-l border-brand-amber/40">
                          {item.modifiers.join(', ')}
                        </div>
                      )}
                    </div>
                    <div className="text-sm font-mono text-gray-300">
                      ${(item.priceCents / 100).toFixed(2)}
                    </div>
                  </div>
                ))}
              </div>

              {/* Totals Summary */}
              <div className="pt-3 border-t border-gray-800 text-xs text-gray-400 space-y-1.5 font-mono">
                <div className="flex justify-between">
                  <span>Subtotal</span>
                  <span>${(order.subtotalCents / 100).toFixed(2)}</span>
                </div>
                <div className="flex justify-between">
                  <span>Sales Tax (8.5%)</span>
                  <span>${(order.taxCents / 100).toFixed(2)}</span>
                </div>
                {order.tipCents > 0 && (
                  <div className="flex justify-between text-brand-amber">
                    <span>Staff Gratuity</span>
                    <span>${(order.tipCents / 100).toFixed(2)}</span>
                  </div>
                )}
                <div className="flex justify-between text-sm font-bold text-brand-white pt-2 border-t border-brand-onyx">
                  <span>Total Paid</span>
                  <span>${(order.totalCents / 100).toFixed(2)}</span>
                </div>
              </div>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
