import { WebSocket } from 'ws';
import { WebSocketEnvelope } from '@dinevique/contracts';

interface ConnectedTerminalSession {
  terminalId: string;
  restaurantId: string;
  socket: WebSocket;
  lastHeartbeatEpochMs: number;
}

export class EdgeTunnelManager {
  // Key: restaurantId -> Session
  private sessions = new Map<string, ConnectedTerminalSession>();

  registerTerminal(restaurantId: string, terminalId: string, socket: WebSocket): void {
    const session: ConnectedTerminalSession = {
      terminalId,
      restaurantId,
      socket,
      lastHeartbeatEpochMs: Date.now(),
    };

    this.sessions.set(restaurantId, session);

    socket.on('close', () => {
      this.sessions.delete(restaurantId);
    });

    socket.on('error', () => {
      this.sessions.delete(restaurantId);
    });
  }

  recordHeartbeat(restaurantId: string): void {
    const session = this.sessions.get(restaurantId);
    if (session) {
      session.lastHeartbeatEpochMs = Date.now();
    }
  }

  sendToRestaurant(restaurantId: string, envelope: WebSocketEnvelope): boolean {
    const session = this.sessions.get(restaurantId);
    if (!session || session.socket.readyState !== WebSocket.OPEN) {
      return false;
    }

    try {
      session.socket.send(JSON.stringify(envelope));
      return true;
    } catch {
      return false;
    }
  }

  isRestaurantConnected(restaurantId: string): boolean {
    const session = this.sessions.get(restaurantId);
    return Boolean(session && session.socket.readyState === WebSocket.OPEN);
  }

  getConnectedRestaurantCount(): number {
    return this.sessions.size;
  }
}

export const globalTunnelManager = new EdgeTunnelManager();
