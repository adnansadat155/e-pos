import { test } from 'node:test';
import assert from 'node:assert';
import { app } from '../dist/server.js';

test('Cloud Gateway /health route returns 200 and UP status', async () => {
  // Use http request on listening server instance or mock
  const server = app.listen(0);
  const address = server.address();
  const port = address.port;

  try {
    const res = await fetch(`http://127.0.0.1:${port}/health`);
    assert.strictEqual(res.status, 200);
    const data = await res.json();
    assert.strictEqual(data.status, 'UP');
    assert.strictEqual(data.tier, 'DINEVIQUE_CLOUD_GATEWAY');
  } finally {
    server.close();
  }
});

test('Cloud Gateway /api/v1/sync/orders rejects missing restaurantId', async () => {
  const server = app.listen(0);
  const address = server.address();
  const port = address.port;

  try {
    const res = await fetch(`http://127.0.0.1:${port}/api/v1/sync/orders`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ orders: [] }),
    });
    assert.strictEqual(res.status, 400);
  } finally {
    server.close();
  }
});
