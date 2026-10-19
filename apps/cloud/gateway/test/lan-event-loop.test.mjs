import { test } from 'node:test';
import assert from 'node:assert';
import { createServer } from 'http';
import { WebSocketServer, WebSocket } from 'ws';

test('LAN WebSocket Event Loop & Sub-400ms SLA Verification', async () => {
  // 1. Initialize Mock Master Ktor LAN WebSocket Server on an ephemeral port
  const httpServer = createServer();
  const wss = new WebSocketServer({ server: httpServer, path: '/ws/lan' });

  await new Promise((resolve) => httpServer.listen(0, resolve));
  const port = httpServer.address().port;

  const connectedClients = new Set();

  wss.on('connection', (ws) => {
    connectedClients.add(ws);

    ws.on('message', (data) => {
      const envelope = JSON.parse(data.toString());

      // Simulate Master Terminal processing & immediate broadcast to KDB
      if (envelope.eventType === 'ORDER_CREATE') {
        const ticketReceivedEnvelope = {
          eventId: 'evt_ticket_001',
          eventType: 'TICKET_RECEIVED',
          timestampEpochMs: Date.now(),
          restaurantId: envelope.restaurantId,
          sourceTerminalId: 'term_master_pos_01',
          payload: {
            ticketNumber: '#4421',
            orderUuid: envelope.payload.order.clientOrderUuid,
            tableLabel: envelope.payload.order.tableLabel,
            items: envelope.payload.order.items,
            createdAtEpochMs: Date.now(),
          },
        };

        const broadcastStr = JSON.stringify(ticketReceivedEnvelope);
        for (const client of connectedClients) {
          if (client.readyState === WebSocket.OPEN) {
            client.send(broadcastStr);
          }
        }
      } else if (envelope.eventType === 'TICKET_BUMPED') {
        // Echo bump state
        for (const client of connectedClients) {
          if (client.readyState === WebSocket.OPEN) {
            client.send(JSON.stringify(envelope));
          }
        }
      }
    });

    ws.on('close', () => connectedClients.delete(ws));
  });

  try {
    // 2. Connect Mock KDB Client
    const kdbWs = new WebSocket(`ws://127.0.0.1:${port}/ws/lan`);
    await new Promise((resolve, reject) => {
      kdbWs.on('open', resolve);
      kdbWs.on('error', reject);
    });

    // 3. Connect Mock Handheld Client
    const handheldWs = new WebSocket(`ws://127.0.0.1:${port}/ws/lan`);
    await new Promise((resolve, reject) => {
      handheldWs.on('open', resolve);
      handheldWs.on('error', reject);
    });

    // 4. Measure propagation latency
    const startEpochMs = Date.now();

    const orderPayload = {
      eventId: 'evt_order_001',
      eventType: 'ORDER_CREATE',
      timestampEpochMs: startEpochMs,
      restaurantId: 'rest_nyc_01',
      sourceTerminalId: 'term_handheld_01',
      payload: {
        order: {
          clientOrderUuid: 'ord_uuid_9921',
          restaurantId: 'rest_nyc_01',
          tableLabel: 'Table 7',
          sourceChannel: 'HANDHELD',
          ticketStatus: 'NEW_TICKET',
          items: [{ name: 'Pasta Carbonara', quantity: 2 }],
        },
      },
    };

    // Await KDB receipt of TICKET_RECEIVED
    const receivedTicket = await new Promise((resolve, reject) => {
      const timeout = setTimeout(() => reject(new Error('SLA Timeout: Exceeded 400ms')), 400);

      kdbWs.on('message', (data) => {
        const envelope = JSON.parse(data.toString());
        if (envelope.eventType === 'TICKET_RECEIVED') {
          clearTimeout(timeout);
          resolve({ envelope, latencyMs: Date.now() - startEpochMs });
        }
      });

      // Handheld fires order
      handheldWs.send(JSON.stringify(orderPayload));
    });

    // Assert SLA: Handheld-to-KDB transit must be < 400ms
    assert.ok(receivedTicket.latencyMs < 400, `Latency must be < 400ms, was ${receivedTicket.latencyMs}ms`);
    assert.strictEqual(receivedTicket.envelope.payload.ticketNumber, '#4421');
    assert.strictEqual(receivedTicket.envelope.payload.tableLabel, 'Table 7');

    // 5. Test Bump Cycle
    const bumpPromise = new Promise((resolve) => {
      handheldWs.on('message', (data) => {
        const envelope = JSON.parse(data.toString());
        if (envelope.eventType === 'TICKET_BUMPED') {
          resolve(envelope);
        }
      });
    });

    kdbWs.send(JSON.stringify({
      eventId: 'evt_bump_001',
      eventType: 'TICKET_BUMPED',
      timestampEpochMs: Date.now(),
      restaurantId: 'rest_nyc_01',
      sourceTerminalId: 'term_kdb_01',
      payload: {
        ticketNumber: '#4421',
        orderUuid: 'ord_uuid_9921',
        newStatus: 'BUMPED',
      },
    }));

    const bumpedEvent = await bumpPromise;
    assert.strictEqual(bumpedEvent.payload.newStatus, 'BUMPED');

    kdbWs.close();
    handheldWs.close();
  } finally {
    wss.close();
    await new Promise((resolve) => httpServer.close(resolve));
  }
});
