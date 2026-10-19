import { Request, Response } from 'express';
import mongoose from 'mongoose';
import { OrderModel } from '../models/OrderModel.js';
import { Order } from '@dinevique/contracts';

// In-memory deduplication fallback when MongoDB connection is not active (e.g. testing)
const inMemoryOrderStore = new Map<string, Order>();

export function clearInMemoryStore(): void {
  inMemoryOrderStore.clear();
}

export function getInMemoryOrderCount(): number {
  return inMemoryOrderStore.size;
}

export async function handleBatchOrderIngest(req: Request, res: Response): Promise<Response> {
  const { restaurantId, orders } = req.body as { restaurantId: string; orders: Order[] };

  if (!restaurantId || !Array.isArray(orders)) {
    return res.status(400).json({
      error: 'Invalid payload: restaurantId and orders array are required.',
    });
  }

  const results: { clientOrderUuid: string; status: 'UPSERTED' | 'FAILED'; error?: string }[] = [];
  const isDbConnected = mongoose.connection.readyState === 1;

  for (const order of orders) {
    if (!order.clientOrderUuid) {
      results.push({
        clientOrderUuid: 'UNKNOWN',
        status: 'FAILED',
        error: 'Missing clientOrderUuid',
      });
      continue;
    }

    try {
      if (isDbConnected) {
        await OrderModel.findOneAndUpdate(
          { restaurantId, clientOrderUuid: order.clientOrderUuid },
          { $set: order },
          { upsert: true, new: true, setDefaultsOnInsert: true }
        );
      } else {
        // In-memory idempotent upsert
        const storeKey = `${restaurantId}:${order.clientOrderUuid}`;
        inMemoryOrderStore.set(storeKey, order);
      }
      results.push({ clientOrderUuid: order.clientOrderUuid, status: 'UPSERTED' });
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Database error';
      results.push({ clientOrderUuid: order.clientOrderUuid, status: 'FAILED', error: message });
    }
  }

  return res.status(200).json({
    restaurantId,
    totalReceived: orders.length,
    processedCount: results.filter((r) => r.status === 'UPSERTED').length,
    results,
  });
}
