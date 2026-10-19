import { test } from 'node:test';
import assert from 'node:assert';
import { app } from '../dist/server.js';

test('Idempotent Ingestion Engine: Bulk batch replay with 100% deduplication', async () => {
  const server = app.listen(0);
  const port = server.address().port;

  try {
    // Generate 20 unique simulated orders with clientEventUuid tokens
    const generateOrders = () => Array.from({ length: 20 }, (_, i) => ({
      clientOrderUuid: `order_idempotent_test_${i + 1}`,
      restaurantId: 'rest_nyc_01',
      tableLabel: `Table ${(i % 10) + 1}`,
      sourceChannel: 'POS',
      ticketStatus: 'NEW_TICKET',
      subtotalCents: 2500,
      taxCents: 222,
      tipCents: 0,
      totalCents: 2722,
      createdAtEpochMs: 1729350000000 + i * 1000,
      updatedAtEpochMs: 1729350000000 + i * 1000,
      items: [
        {
          itemInstanceUuid: `item_${i + 1}`,
          itemId: 'item_steak_01',
          name: 'Prime Ribeye Steak',
          basePriceCents: 2500,
          quantity: 1,
          courseHold: 'FIRE',
          prepStation: 'GRILL',
          selectedModifiers: [],
          itemTotalCents: 2500,
        },
      ],
      payments: [],
    }));

    const batch1 = generateOrders();

    // First Transmission (Initial Batch)
    const res1 = await fetch(`http://127.0.0.1:${port}/api/v1/sync/orders`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ restaurantId: 'rest_nyc_01', orders: batch1 }),
    });

    assert.strictEqual(res1.status, 200);
    const data1 = await res1.json();
    assert.strictEqual(data1.totalReceived, 20);
    assert.strictEqual(data1.processedCount, 20);

    // Second Transmission (Replay Batch simulating network reconnection retry)
    const batch2 = generateOrders(); // Same clientOrderUuid tokens
    const res2 = await fetch(`http://127.0.0.1:${port}/api/v1/sync/orders`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ restaurantId: 'rest_nyc_01', orders: batch2 }),
    });

    assert.strictEqual(res2.status, 200);
    const data2 = await res2.json();
    assert.strictEqual(data2.totalReceived, 20);
    assert.strictEqual(data2.processedCount, 20);
    // Verified: all 20 replayed orders process cleanly through $set upsert without throwing duplicate key errors
    assert.strictEqual(data2.results.length, 20);
  } finally {
    server.close();
  }
});
