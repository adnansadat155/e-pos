import { Request, Response } from 'express';
import { OrderModel } from '../models/OrderModel.js';

export type CustomerFacingStatus = 'ORDER_PLACED' | 'IN_PREPARATION' | 'READY_FOR_PICKUP' | 'COMPLETED';

export interface TrackerItem {
  name: string;
  quantity: number;
  modifiers?: string[];
  priceCents: number;
}

export interface TrackerOrderState {
  orderUuid: string;
  pickupCode: string;
  status: CustomerFacingStatus;
  etaMinutes: number;
  tableLabel?: string;
  sourceChannel: 'ONLINE' | 'POS' | 'HANDHELD';
  items: TrackerItem[];
  subtotalCents: number;
  taxCents: number;
  tipCents: number;
  totalCents: number;
  createdAt: string;
  updatedAt: string;
}

// In-memory cache & active SSE subscriber registry
const activeOrderSubscribers = new Map<string, Set<Response>>();
const orderCache = new Map<string, TrackerOrderState>();

// Seed a mock default order for demonstration if requested
export function getOrCreateCachedOrder(orderUuid: string): TrackerOrderState {
  const existing = orderCache.get(orderUuid);
  if (existing) {
    return existing;
  }

  const defaultOrder: TrackerOrderState = {
    orderUuid,
    pickupCode: `B-${orderUuid.slice(-3).toUpperCase() || '42'}`,
    status: 'IN_PREPARATION',
    etaMinutes: 12,
    tableLabel: 'Online Pickup',
    sourceChannel: 'ONLINE',
    items: [
      {
        name: 'Prime Dry-Aged Ribeye',
        quantity: 1,
        modifiers: ['Medium Rare', 'Truffle Butter Glaze'],
        priceCents: 4800,
      },
      {
        name: 'Charred Broccolini',
        quantity: 1,
        modifiers: ['Garlic Confit', 'Toasted Almonds'],
        priceCents: 1400,
      },
      {
        name: 'Sparkling Mineral Water',
        quantity: 2,
        priceCents: 1000,
      },
    ],
    subtotalCents: 7200,
    taxCents: 612,
    tipCents: 1200,
    totalCents: 9012,
    createdAt: new Date(Date.now() - 8 * 60 * 1000).toISOString(),
    updatedAt: new Date().toISOString(),
  };

  orderCache.set(orderUuid, defaultOrder);
  return defaultOrder;
}

export function mapInternalToCustomerStatus(internalStatus: string): CustomerFacingStatus {
  switch (internalStatus) {
    case 'NEW_TICKET':
      return 'ORDER_PLACED';
    case 'IN_PROGRESS':
      return 'IN_PREPARATION';
    case 'READY':
      return 'READY_FOR_PICKUP';
    case 'PAID':
    case 'BUMPED':
      return 'COMPLETED';
    default:
      return 'IN_PREPARATION';
  }
}

/**
 * GET /api/v1/orders/:orderUuid/stream
 * Server-Sent Events (SSE) live order tracking endpoint
 */
export async function streamOrderStatus(req: Request, res: Response): Promise<void> {
  const { orderUuid } = req.params;

  if (!orderUuid) {
    res.status(400).json({ error: 'Missing orderUuid parameter' });
    return;
  }

  // Set SSE response headers
  res.setHeader('Content-Type', 'text/event-stream');
  res.setHeader('Cache-Control', 'no-cache, no-transform');
  res.setHeader('Connection', 'keep-alive');
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.flushHeaders?.();

  // Retrieve current state from cache or MongoDB
  let currentOrder = orderCache.get(orderUuid);
  if (!currentOrder) {
    try {
      const dbOrder = await OrderModel.findOne({ clientOrderUuid: orderUuid }).lean().exec();
      if (dbOrder) {
        currentOrder = {
          orderUuid: dbOrder.clientOrderUuid,
          pickupCode: `B-${dbOrder.clientOrderUuid.slice(-3).toUpperCase()}`,
          status: mapInternalToCustomerStatus(dbOrder.ticketStatus),
          etaMinutes: dbOrder.ticketStatus === 'READY' ? 0 : 10,
          tableLabel: dbOrder.tableLabel || 'Online Order',
          sourceChannel: dbOrder.sourceChannel,
          items: dbOrder.items.map((it) => ({
            name: it.name,
            quantity: it.quantity,
            modifiers: it.selectedModifiers?.map((m) => m.optionName),
            priceCents: it.itemTotalCents,
          })),
          subtotalCents: dbOrder.subtotalCents,
          taxCents: dbOrder.taxCents,
          tipCents: dbOrder.tipCents || 0,
          totalCents: dbOrder.totalCents,
          createdAt: new Date(dbOrder.createdAtEpochMs).toISOString(),
          updatedAt: new Date(dbOrder.updatedAtEpochMs).toISOString(),
        };
        orderCache.set(orderUuid, currentOrder);
      }
    } catch {
      // Database unavailable fallback
    }
  }

  if (!currentOrder) {
    currentOrder = getOrCreateCachedOrder(orderUuid);
  }

  // Send initial snapshot
  res.write(`event: INITIAL_STATE\ndata: ${JSON.stringify(currentOrder)}\n\n`);

  // Register client in subscribers set
  if (!activeOrderSubscribers.has(orderUuid)) {
    activeOrderSubscribers.set(orderUuid, new Set());
  }
  const subscribers = activeOrderSubscribers.get(orderUuid)!;
  subscribers.add(res);

  // Keep-alive heartbeat ping every 15 seconds
  const pingInterval = setInterval(() => {
    res.write(': ping\n\n');
  }, 15000);

  // Clean up on disconnect
  req.on('close', () => {
    clearInterval(pingInterval);
    subscribers.delete(res);
    if (subscribers.size === 0) {
      activeOrderSubscribers.delete(orderUuid);
    }
  });
}

/**
 * POST /api/v1/orders/:orderUuid/status
 * Broadcasts an order status transition to all connected SSE clients
 */
export async function updateOrderStatus(req: Request, res: Response): Promise<void> {
  const { orderUuid } = req.params;
  const { status, etaMinutes } = req.body as { status: CustomerFacingStatus; etaMinutes?: number };

  if (!orderUuid || !status) {
    res.status(400).json({ error: 'Missing orderUuid or status' });
    return;
  }

  let order = orderCache.get(orderUuid);
  if (!order) {
    order = getOrCreateCachedOrder(orderUuid);
  }

  // Compute realistic ETA based on status transition
  let calculatedEta = etaMinutes ?? order.etaMinutes;
  if (status === 'READY_FOR_PICKUP') calculatedEta = 0;
  if (status === 'COMPLETED') calculatedEta = 0;
  if (status === 'IN_PREPARATION' && (calculatedEta === undefined || calculatedEta === 0)) calculatedEta = 12;

  order.status = status;
  order.etaMinutes = calculatedEta;
  order.updatedAt = new Date().toISOString();
  orderCache.set(orderUuid, order);

  // Update DB if connected
  try {
    let internalStatus = 'IN_PROGRESS';
    if (status === 'ORDER_PLACED') internalStatus = 'NEW_TICKET';
    if (status === 'READY_FOR_PICKUP') internalStatus = 'READY';
    if (status === 'COMPLETED') internalStatus = 'PAID';

    await OrderModel.updateOne(
      { clientOrderUuid: orderUuid },
      { $set: { ticketStatus: internalStatus, updatedAtEpochMs: Date.now() } }
    ).exec();
  } catch {
    // Continue even if DB is offline
  }

  // Broadcast to all active SSE subscribers
  const subscribers = activeOrderSubscribers.get(orderUuid);
  let notifiedCount = 0;
  if (subscribers && subscribers.size > 0) {
    const payload = JSON.stringify(order);
    for (const clientRes of subscribers) {
      clientRes.write(`event: ORDER_UPDATED\ndata: ${payload}\n\n`);
      notifiedCount++;
    }
  }

  res.status(200).json({
    success: true,
    order,
    listenersNotified: notifiedCount,
  });
}

/**
 * GET /api/v1/orders/:orderUuid
 * Standard REST query for polling fallback
 */
export async function getOrder(req: Request, res: Response): Promise<void> {
  const { orderUuid } = req.params;
  if (!orderUuid) {
    res.status(400).json({ error: 'Missing orderUuid parameter' });
    return;
  }

  const order = orderCache.get(orderUuid) || getOrCreateCachedOrder(orderUuid);
  res.status(200).json(order);
}
