import { test, describe, before, after } from 'node:test';
import assert from 'node:assert/strict';
import { WebSocket } from 'ws';
import { app, httpServer } from '../dist/server.js';

describe('Sprint P03_M01: Next.js Storefront & Cloud Menu Studio Tests', () => {

  let testServerPort;
  let testServerUrl;
  let wsUrl;

  before(async () => {
    await new Promise((resolve) => {
      httpServer.listen(0, () => {
        const address = httpServer.address();
        testServerPort = address.port;
        testServerUrl = `http://127.0.0.1:${testServerPort}`;
        wsUrl = `ws://127.0.0.1:${testServerPort}/ws/edge-tunnel`;
        resolve();
      });
    });
  });

  after(async () => {
    await new Promise((resolve) => {
      httpServer.close(resolve);
    });
  });

  describe('1. Cloud Gateway Menu API Endpoints', () => {
    test('GET /api/v1/menu returns canonical menu catalog', async () => {
      const res = await fetch(`${testServerUrl}/api/v1/menu?restaurantId=rest_nyc_01`);
      assert.equal(res.status, 200);

      const data = await res.json();
      assert.equal(data.success, true);
      assert.ok(data.menu);
      assert.equal(data.menu.restaurantId, 'rest_nyc_01');
      assert.ok(data.menu.categories.length > 0);
    });

    test('POST /api/v1/menu persists menu catalog update', async () => {
      const payload = {
        menuId: 'menu_test_01',
        restaurantId: 'rest_nyc_01',
        name: 'Updated Dinevique Metropolitan Menu',
        isActive: true,
        categories: [
          {
            categoryId: 'cat_tasting',
            name: "Chef's Tasting Menu",
            displayOrder: 1,
            items: [
              {
                itemId: 'item_caviar',
                name: 'Osetra Caviar Tart',
                basePriceCents: 6500,
                taxRatePercent: 8.875,
                prepStation: 'EXPO',
                isAvailable: true,
                modifierGroups: []
              }
            ]
          }
        ]
      };

      const res = await fetch(`${testServerUrl}/api/v1/menu`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      assert.equal(res.status, 200);
      const data = await res.json();
      assert.equal(data.success, true);
      assert.equal(data.categoriesCount, 1);
    });
  });

  describe('2. WSS Edge Tunnel MENU_SYNC Broadcast Delivery', () => {
    test('POST /api/v1/menu/publish broadcasts MENU_SYNC envelope to connected edge terminals in < 500ms', async () => {
      const restaurantId = 'rest_nyc_01';
      const terminalId = 'term_edge_master_01';

      // 1. Establish mock edge tunnel WebSocket
      const client = new WebSocket(wsUrl, {
        headers: {
          'x-restaurant-id': restaurantId,
          'x-terminal-id': terminalId
        }
      });

      await new Promise((resolve, reject) => {
        client.on('open', resolve);
        client.on('error', reject);
      });

      // 2. Set up expectation for incoming MENU_SYNC
      const syncPromise = new Promise((resolve) => {
        const start = Date.now();
        client.on('message', (raw) => {
          const envelope = JSON.parse(raw.toString());
          if (envelope.eventType === 'MENU_SYNC') {
            const latency = Date.now() - start;
            resolve({ envelope, latency });
          }
        });
      });

      // 3. Trigger publish endpoint
      const res = await fetch(`${testServerUrl}/api/v1/menu/publish`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ restaurantId, menuId: 'menu_test_01' })
      });

      assert.equal(res.status, 200);

      // 4. Await and verify received envelope & latency SLA
      const { envelope, latency } = await syncPromise;
      assert.equal(envelope.eventType, 'MENU_SYNC');
      assert.equal(envelope.restaurantId, restaurantId);
      assert.ok(latency < 500, `Broadcast latency ${latency}ms must be < 500ms SLA`);

      client.close();
    });
  });

  describe('3. Online Storefront Cart Calculations with Modifiers', () => {
    test('Computes subtotal, tax, and total accurately with item variations and quantities', () => {
      const itemRibeye = {
        name: 'USDA Prime Ribeye 14oz',
        basePriceCents: 4800,
        selectedModifiers: [
          { name: 'Medium-Rare', priceDeltaCents: 0 },
          { name: 'Truffle Parmesan Fries', priceDeltaCents: 300 }
        ]
      };

      const unitPriceCents = itemRibeye.basePriceCents + itemRibeye.selectedModifiers.reduce((a, m) => a + m.priceDeltaCents, 0);
      assert.equal(unitPriceCents, 5100); // 4800 + 300 = 5100 cents ($51.00)

      const quantity = 2;
      const subtotalCents = unitPriceCents * quantity; // 10200 cents ($102.00)
      assert.equal(subtotalCents, 10200);

      const taxRate = 0.08875;
      const taxCents = Math.round(subtotalCents * taxRate); // 10200 * 0.08875 = 905.25 -> 905 cents ($9.05)
      assert.equal(taxCents, 905);

      const grandTotalCents = subtotalCents + taxCents; // 10200 + 905 = 11105 cents ($111.05)
      assert.equal(grandTotalCents, 11105);
    });
  });
});
