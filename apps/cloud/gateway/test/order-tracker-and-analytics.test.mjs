import { test, describe, before, after } from 'node:test';
import assert from 'node:assert/strict';
import { httpServer } from '../dist/server.js';

describe('Sprint P03_M02: Live Order Status Tracker & Cloud Analytics Tests', () => {
  let testServerPort;
  let testServerUrl;

  before(async () => {
    await new Promise((resolve) => {
      httpServer.listen(0, () => {
        const address = httpServer.address();
        testServerPort = address.port;
        testServerUrl = `http://127.0.0.1:${testServerPort}`;
        resolve();
      });
    });
  });

  after(async () => {
    await new Promise((resolve) => {
      httpServer.close(resolve);
    });
  });

  describe('1. Real-Time Server-Sent Events (SSE) Live Order Tracker', () => {
    const testOrderUuid = `ord_test_track_${Date.now()}`;

    test('GET /api/v1/orders/:orderUuid/stream returns text/event-stream with INITIAL_STATE', async () => {
      const controller = new AbortController();
      const res = await fetch(`${testServerUrl}/api/v1/orders/${testOrderUuid}/stream`, {
        signal: controller.signal,
      });

      assert.equal(res.status, 200);
      assert.ok(res.headers.get('content-type')?.includes('text/event-stream'));
      assert.equal(res.headers.get('connection'), 'keep-alive');

      // Read initial SSE chunk
      const reader = res.body.getReader();
      const { value } = await reader.read();
      const chunkText = new TextDecoder().decode(value);

      assert.ok(chunkText.includes('event: INITIAL_STATE'));
      assert.ok(chunkText.includes(testOrderUuid));

      controller.abort();
    });

    test('POST /api/v1/orders/:orderUuid/status broadcasts ORDER_UPDATED to active SSE listener within < 500ms', async () => {
      const controller = new AbortController();
      const streamRes = await fetch(`${testServerUrl}/api/v1/orders/${testOrderUuid}/stream`, {
        signal: controller.signal,
      });

      const reader = streamRes.body.getReader();

      // Discard initial snapshot
      await reader.read();

      // Trigger status update
      const startTime = Date.now();
      const updateRes = await fetch(`${testServerUrl}/api/v1/orders/${testOrderUuid}/status`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          status: 'READY_FOR_PICKUP',
        }),
      });

      assert.equal(updateRes.status, 200);
      const updateData = await updateRes.json();
      assert.equal(updateData.success, true);
      assert.equal(updateData.order.status, 'READY_FOR_PICKUP');
      assert.equal(updateData.order.etaMinutes, 0);

      // Read incoming SSE broadcast
      const { value } = await reader.read();
      const elapsedMs = Date.now() - startTime;
      const broadcastText = new TextDecoder().decode(value);

      assert.ok(broadcastText.includes('event: ORDER_UPDATED'));
      assert.ok(broadcastText.includes('READY_FOR_PICKUP'));
      assert.ok(elapsedMs < 500, `Broadcast took ${elapsedMs}ms, exceeding 500ms benchmark`);

      controller.abort();
    });

    test('GET /api/v1/orders/:orderUuid serves polling fallback with accurate state', async () => {
      const res = await fetch(`${testServerUrl}/api/v1/orders/${testOrderUuid}`);
      assert.equal(res.status, 200);

      const order = await res.json();
      assert.equal(order.orderUuid, testOrderUuid);
      assert.equal(order.status, 'READY_FOR_PICKUP');
      assert.ok(order.items.length > 0);
      assert.ok(order.totalCents > 0);
    });
  });

  describe('2. Master Terminal Heartbeat & Edge Health Monitor', () => {
    test('GET /api/v1/terminals retrieves active edge fleet status', async () => {
      const res = await fetch(`${testServerUrl}/api/v1/terminals`);
      assert.equal(res.status, 200);

      const data = await res.json();
      assert.ok(data.totalCount >= 3);
      assert.ok(data.onlineCount >= 3);

      const master = data.terminals.find((t) => t.role === 'MASTER_TERMINAL');
      assert.ok(master);
      assert.equal(master.lanPort, 8080);
      assert.equal(master.peripherals.cashDrawer, 'CLOSED');
    });

    test('POST /api/v1/terminals/heartbeat registers new edge device telemetry', async () => {
      const newTerminal = {
        terminalId: 'term_patio_sunmi_99',
        role: 'HANDHELD_ORDER_PAD',
        displayName: 'Rooftop Server Sunmi V2',
        restaurantId: 'rest_flagship_01',
        lanIp: '192.168.1.145',
        lanPort: 8083,
        lanPingLatencyMs: 9,
        batteryLevelPercent: 64,
        isCharging: false,
        offlineQueueDepth: 0,
        peripherals: {
          thermalPrinter: 'PAPER_LOW',
          kitchenImpact: 'OFFLINE',
          cashDrawer: 'DISCONNECTED',
        },
      };

      const res = await fetch(`${testServerUrl}/api/v1/terminals/heartbeat`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(newTerminal),
      });

      assert.equal(res.status, 200);
      const ack = await res.json();
      assert.equal(ack.success, true);
      assert.equal(ack.terminalId, 'term_patio_sunmi_99');

      // Verify in fleet listing
      const listRes = await fetch(`${testServerUrl}/api/v1/terminals`);
      const listData = await listRes.json();
      const found = listData.terminals.find((t) => t.terminalId === 'term_patio_sunmi_99');
      assert.ok(found);
      assert.equal(found.peripherals.thermalPrinter, 'PAPER_LOW');
    });

    test('POST /api/v1/terminals/ping initiates fleet-wide ping and updates latency', async () => {
      const res = await fetch(`${testServerUrl}/api/v1/terminals/ping`, { method: 'POST' });
      assert.equal(res.status, 200);

      const data = await res.json();
      assert.equal(data.success, true);
      assert.ok(data.pingCount >= 4);
      assert.ok(data.averageLatencyMs < 20);
    });
  });

  describe('3. Restaurant Management Analytics Aggregation Engine', () => {
    test('GET /api/v1/analytics/summary computes gross, net, check size, and category distribution', async () => {
      const res = await fetch(`${testServerUrl}/api/v1/analytics/summary?range=today`);
      assert.equal(res.status, 200);

      const data = await res.json();
      assert.equal(data.range, 'today');
      assert.ok(data.grossSalesCents > 0);
      assert.ok(data.netRevenueCents > 0);
      assert.ok(data.orderCount > 0);
      assert.ok(data.averageCheckCents > 0);

      // Verify mathematical identity: Average Check * Order Count ~= Gross Sales
      const computedGross = data.averageCheckCents * data.orderCount;
      const discrepancyCents = Math.abs(computedGross - data.grossSalesCents);
      assert.ok(discrepancyCents <= data.orderCount, 'Average check rounding discrepancy exceeds penny bounds');

      // Verify Category distribution completeness
      assert.ok(data.categories.length >= 5);
      const totalCategoryShare = data.categories.reduce((sum, c) => sum + c.percentageOfGross, 0);
      assert.ok(Math.abs(totalCategoryShare - 100.0) < 0.5, `Category share total ${totalCategoryShare}% deviates from 100%`);

      // Verify 24-hour heatmap integrity
      assert.equal(data.hourlyDistribution.length, 24);
      assert.equal(data.hourlyDistribution[12].displayHour, '12:00 PM');
      assert.ok(data.hourlyDistribution[19].volumeCents > data.hourlyDistribution[4].volumeCents, 'Dinner rush volume must exceed early morning');

      // Verify labor cost ratio is within realistic operational bounds
      assert.ok(data.laborCostRatioPercent >= 15 && data.laborCostRatioPercent <= 35);
    });
  });
});
