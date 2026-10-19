import express, { Request, Response, Express } from 'express';
import { createServer } from 'http';
import { WebSocketServer, WebSocket } from 'ws';
import cors from 'cors';
import { fileURLToPath } from 'url';
import { Config } from './config.js';
import { connectDatabase } from './db.js';
import { globalTunnelManager } from './tunnel/EdgeTunnelManager.js';
import { handleBatchOrderIngest } from './controllers/IngestController.js';
import { getMenuHandler, upsertMenuHandler, publishMenuHandler } from './controllers/MenuController.js';
import { streamOrderStatus, updateOrderStatus, getOrder } from './controllers/OrderTrackerController.js';
import { recordTerminalHeartbeat, getTerminals, pingTerminalsFleet } from './controllers/TerminalHeartbeatController.js';
import { getAnalyticsSummary } from './controllers/AnalyticsController.js';
import { processPayment, batchSettlePayments, getPaymentStats } from './controllers/PaymentController.js';
import { sendDigitalReceipt, getDigitalReceipt } from './controllers/DigitalReceiptController.js';
import { WebSocketEnvelope } from '@dinevique/contracts';

export const app: Express = express();
app.use(cors({ origin: Config.corsOrigin }));
app.use(express.json({ limit: '10mb' }));

// Health check endpoint
app.get('/health', (_req: Request, res: Response) => {
  res.status(200).json({
    status: 'UP',
    tier: 'DINEVIQUE_CLOUD_GATEWAY',
    timestamp: new Date().toISOString(),
    environment: Config.environment,
    activeEdgeTunnels: globalTunnelManager.getConnectedRestaurantCount(),
  });
});

// Idempotent Order Ingestion Endpoint (Store-and-forward batch ingest)
app.post('/api/v1/sync/orders', handleBatchOrderIngest);

// Omnichannel Menu Management Endpoints
app.get('/api/v1/menu', getMenuHandler);
app.post('/api/v1/menu', upsertMenuHandler);
app.post('/api/v1/menu/publish', publishMenuHandler);

// Real-Time Customer Order Tracking & SSE Stream Endpoints
app.get('/api/v1/orders/:orderUuid/stream', streamOrderStatus);
app.post('/api/v1/orders/:orderUuid/status', updateOrderStatus);
app.get('/api/v1/orders/:orderUuid', getOrder);

// Edge Terminal Mesh Telemetry & Fleet Management Endpoints
app.post('/api/v1/terminals/heartbeat', recordTerminalHeartbeat);
app.get('/api/v1/terminals', getTerminals);
app.post('/api/v1/terminals/ping', pingTerminalsFleet);

// Cloud Analytics & Performance Aggregation Endpoints
app.get('/api/v1/analytics/summary', getAnalyticsSummary);

// Mission-Critical Payment Processing & Offline Settlement Endpoints
app.post('/api/v1/payments/process', processPayment);
app.post('/api/v1/payments/batch-settle', batchSettlePayments);
app.get('/api/v1/payments/stats', getPaymentStats);

// Omnichannel Digital Receipt Dispatch & Web Portal Endpoints
app.post('/api/v1/receipts/send', sendDigitalReceipt);
app.get('/api/v1/receipts/:receiptUuid', getDigitalReceipt);

export const httpServer = createServer(app);

// WebSocket Server for persistent Edge Master Terminal WSS Tunnels
export const wss = new WebSocketServer({ server: httpServer, path: '/ws/edge-tunnel' });

wss.on('connection', (ws: WebSocket, req) => {
  const restaurantId = (req.headers['x-restaurant-id'] as string) || 'rest_default';
  const terminalId = (req.headers['x-terminal-id'] as string) || `term_${Date.now()}`;

  globalTunnelManager.registerTerminal(restaurantId, terminalId, ws);

  ws.on('message', (data) => {
    try {
      const envelope = JSON.parse(data.toString()) as WebSocketEnvelope;
      if (envelope.eventType === 'HEARTBEAT') {
        globalTunnelManager.recordHeartbeat(restaurantId);
        ws.send(JSON.stringify({
          eventType: 'HEARTBEAT_ACK',
          timestampEpochMs: Date.now(),
        }));
      }
    } catch {
      // Ignore unparseable frames
    }
  });
});

if (process.argv[1] && fileURLToPath(import.meta.url) === process.argv[1]) {
  connectDatabase().then(() => {
    httpServer.listen(Config.port, () => {
      console.log(`[Dinevique Cloud Gateway] Listening on port ${Config.port}`);
    });
  });
}
