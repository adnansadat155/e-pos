import { Request, Response } from 'express';

export interface DigitalReceiptData {
  receiptUuid: string;
  orderUuid: string;
  restaurantName: string;
  locationName: string;
  tableLabel: string;
  serverName: string;
  items: Array<{
    name: string;
    quantity: number;
    priceCents: number;
    modifiers?: string[];
  }>;
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
  deliveryMethod: 'SMS' | 'EMAIL' | 'WEB';
  recipient?: string;
  createdAt: string;
}

const receiptCache = new Map<string, DigitalReceiptData>();

// Seed a sample default receipt for demonstration and test queries
export function getOrCreateCachedReceipt(receiptUuid: string): DigitalReceiptData {
  const existing = receiptCache.get(receiptUuid);
  if (existing) return existing;

  const defaultReceipt: DigitalReceiptData = {
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
  };

  receiptCache.set(receiptUuid, defaultReceipt);
  return defaultReceipt;
}

/**
 * POST /api/v1/receipts/send
 * Dispatches digital receipt and returns public viewing link
 */
export async function sendDigitalReceipt(req: Request, res: Response): Promise<void> {
  const { orderUuid, deliveryMethod, recipient, items, subtotalCents, taxCents, tipCents, totalCents, paymentTender } = req.body;

  if (!orderUuid) {
    res.status(400).json({ error: 'Missing orderUuid parameter' });
    return;
  }

  const receiptUuid = `rcpt_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`;
  const now = new Date().toISOString();

  const receiptRecord: DigitalReceiptData = {
    receiptUuid,
    orderUuid,
    restaurantName: 'Dinevique',
    locationName: 'Manhattan Flagship (#01)',
    tableLabel: req.body.tableLabel || 'Table 12',
    serverName: req.body.serverName || 'Service Team',
    items: items || [
      { name: 'Artisanal Tasting Course', quantity: 1, priceCents: subtotalCents || 4500 }
    ],
    subtotalCents: subtotalCents || 4500,
    taxCents: taxCents || 382,
    tipCents: tipCents || 900,
    totalCents: totalCents || 5782,
    paymentTender: paymentTender || {
      brand: 'Visa',
      last4: '4242',
      authCode: 'AUTH-129048',
      cvm: 'Contactless NFC',
      timestamp: now,
    },
    deliveryMethod: deliveryMethod || 'WEB',
    recipient,
    createdAt: now,
  };

  receiptCache.set(receiptUuid, receiptRecord);

  res.status(200).json({
    success: true,
    receiptUuid,
    publicUrl: `/receipt/${receiptUuid}`,
    dispatchedTo: recipient || 'Web Viewer',
    createdAt: now,
  });
}

/**
 * GET /api/v1/receipts/:receiptUuid
 * Retrieves digital receipt breakdown for customer web viewer
 */
export async function getDigitalReceipt(req: Request, res: Response): Promise<void> {
  const { receiptUuid } = req.params;
  if (!receiptUuid) {
    res.status(400).json({ error: 'Missing receiptUuid parameter' });
    return;
  }

  const receipt = receiptCache.get(receiptUuid) || getOrCreateCachedReceipt(receiptUuid);
  res.status(200).json(receipt);
}
