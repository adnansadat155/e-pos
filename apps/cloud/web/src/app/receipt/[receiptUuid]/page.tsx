'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { useParams } from 'next/navigation';
import { 
  ArrowLeft, 
  Printer, 
  Download, 
  Mail, 
  CheckCircle2, 
  CreditCard, 
  Receipt,
  Share2
} from 'lucide-react';

interface ReceiptItem {
  name: string;
  quantity: number;
  priceCents: number;
  modifiers?: string[];
}

interface DigitalReceiptData {
  receiptUuid: string;
  orderUuid: string;
  restaurantName: string;
  locationName: string;
  tableLabel: string;
  serverName: string;
  items: ReceiptItem[];
  subtotalCents: number;
  taxCents: number;
  tipCents: number;
  totalCents: number;
  paymentTender: {
    brand: string;
    last4: string;
    authCode: string;
    cvm: string;
    timestamp: string;
  };
  deliveryMethod: string;
  createdAt: string;
}

export default function DigitalReceiptPage() {
  const params = useParams();
  const receiptUuid = (params?.receiptUuid as string) || 'rcpt_live_demo';

  const [receipt, setReceipt] = useState<DigitalReceiptData>({
    receiptUuid,
    orderUuid: 'ord_sample_9821',
    restaurantName: 'Dinevique',
    locationName: 'Manhattan Flagship (#01)',
    tableLabel: 'Table 14',
    serverName: 'Julian C.',
    items: [
      {
        name: 'Prime Dry-Aged Ribeye',
        quantity: 1,
        priceCents: 4800,
        modifiers: ['Medium Rare', 'Truffle Butter Glaze'],
      },
      {
        name: 'Charred Broccolini',
        quantity: 1,
        priceCents: 1400,
        modifiers: ['Garlic Confit', 'Toasted Almonds'],
      },
      {
        name: 'Sparkling Mineral Water',
        quantity: 2,
        priceCents: 1000,
      },
    ],
    subtotalCents: 7200,
    taxCents: 612,
    tipCents: 1400,
    totalCents: 9212,
    paymentTender: {
      brand: 'Visa',
      last4: '4242',
      authCode: 'AUTH-892104',
      cvm: 'Contactless EMV (Quick Chip)',
      timestamp: new Date().toISOString(),
    },
    deliveryMethod: 'WEB',
    createdAt: new Date().toISOString(),
  });

  const [copied, setCopied] = useState(false);

  useEffect(() => {
    const gatewayBase = process.env.NEXT_PUBLIC_GATEWAY_URL || 'http://localhost:4000';
    fetch(`${gatewayBase}/api/v1/receipts/${receiptUuid}`)
      .then((res) => (res.ok ? res.json() : null))
      .then((data) => {
        if (data) setReceipt(data);
      })
      .catch(() => {});
  }, [receiptUuid]);

  const handlePrint = () => {
    if (typeof window !== 'undefined') {
      window.print();
    }
  };

  const handleShare = () => {
    if (typeof navigator !== 'undefined' && navigator.clipboard) {
      navigator.clipboard.writeText(window.location.href);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  return (
    <div className="min-h-screen bg-brand-onyx text-brand-white py-10 px-4">
      {/* Top Navigation */}
      <div className="max-w-md mx-auto mb-6 flex items-center justify-between print:hidden">
        <Link href="/menu" className="flex items-center gap-1.5 text-xs text-gray-400 hover:text-brand-white transition-colors">
          <ArrowLeft className="w-3.5 h-3.5" />
          Back to Menu
        </Link>
        <span className="text-xs font-mono text-gray-500">Ref: {receipt.receiptUuid}</span>
      </div>

      {/* Main Digital Bill Card */}
      <div className="max-w-md mx-auto bg-brand-slate border border-brand-linen/15 rounded-2xl p-6 sm:p-8 shadow-2xl relative overflow-hidden">
        {/* Subtle decorative top bar */}
        <div className="absolute top-0 left-0 right-0 h-1.5 bg-gradient-to-r from-brand-amber via-brand-amber/80 to-brand-amber"></div>

        {/* Restaurant Brand Header */}
        <div className="text-center pb-6 border-b border-gray-800">
          <span className="text-xs font-semibold uppercase tracking-widest text-brand-amber">
            Official Guest Bill
          </span>
          <h1 className="text-2xl font-bold font-display text-brand-white mt-1">
            {receipt.restaurantName}
          </h1>
          <p className="text-xs text-gray-400 mt-0.5">{receipt.locationName}</p>
          <div className="flex items-center justify-center gap-3 text-xs text-gray-500 mt-3 font-mono">
            <span>{receipt.tableLabel}</span>
            <span>•</span>
            <span>Server: {receipt.serverName}</span>
            <span>•</span>
            <span>{new Date(receipt.createdAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}</span>
          </div>
        </div>

        {/* Itemized Order Section */}
        <div className="py-6 space-y-4 border-b border-gray-800">
          {receipt.items.map((item, idx) => (
            <div key={idx} className="flex justify-between items-start text-xs">
              <div>
                <div className="font-medium text-brand-white">
                  {item.quantity}x {item.name}
                </div>
                {item.modifiers && item.modifiers.length > 0 && (
                  <div className="text-gray-400 mt-0.5 pl-3 border-l border-brand-amber/40">
                    {item.modifiers.join(', ')}
                  </div>
                )}
              </div>
              <span className="font-mono text-gray-300 font-medium">
                ${(item.priceCents / 100).toFixed(2)}
              </span>
            </div>
          ))}
        </div>

        {/* Financial Totals */}
        <div className="py-4 space-y-2 border-b border-gray-800 text-xs text-gray-400 font-mono">
          <div className="flex justify-between">
            <span>Subtotal</span>
            <span>${(receipt.subtotalCents / 100).toFixed(2)}</span>
          </div>
          <div className="flex justify-between">
            <span>Sales Tax (8.5%)</span>
            <span>${(receipt.taxCents / 100).toFixed(2)}</span>
          </div>
          {receipt.tipCents > 0 && (
            <div className="flex justify-between text-brand-amber font-medium">
              <span>Staff Gratuity</span>
              <span>${(receipt.tipCents / 100).toFixed(2)}</span>
            </div>
          )}
          <div className="flex justify-between text-base font-bold text-brand-white pt-2 border-t border-brand-onyx">
            <span>Total Paid</span>
            <span>${(receipt.totalCents / 100).toFixed(2)}</span>
          </div>
        </div>

        {/* Payment Tender & Card Authorization Details */}
        <div className="py-5 text-xs text-gray-400 space-y-1.5 font-mono">
          <div className="flex items-center gap-2 text-brand-white font-medium mb-2">
            <CreditCard className="w-4 h-4 text-brand-amber" />
            <span>{receipt.paymentTender.brand} ending in {receipt.paymentTender.last4}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-500">Method</span>
            <span>{receipt.paymentTender.cvm}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-500">Auth Code</span>
            <span>{receipt.paymentTender.authCode}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-500">Status</span>
            <span className="text-status-success font-semibold">APPROVED & SETTLED</span>
          </div>
        </div>

        {/* Footer Note */}
        <div className="text-center pt-4 border-t border-gray-800 text-xs text-gray-500 font-display italic">
          Thank you for dining with Dinevique.
        </div>

        {/* Interactive Action Bar (Print / Share / Download) */}
        <div className="mt-8 pt-4 border-t border-gray-800 flex flex-col gap-2.5 print:hidden">
          <button
            onClick={handlePrint}
            className="w-full py-2.5 rounded-lg bg-brand-amber text-brand-ink font-bold text-xs flex items-center justify-center gap-2 hover:opacity-90 transition-opacity"
          >
            <Printer className="w-3.5 h-3.5" />
            Print or Save as PDF
          </button>

          <button
            onClick={handleShare}
            className="w-full py-2.5 rounded-lg bg-brand-onyx border border-gray-800 text-gray-300 hover:text-brand-white font-medium text-xs flex items-center justify-center gap-2 transition-colors"
          >
            <Share2 className="w-3.5 h-3.5" />
            {copied ? 'Link Copied to Clipboard' : 'Share Receipt Link'}
          </button>
        </div>
      </div>
    </div>
  );
}
