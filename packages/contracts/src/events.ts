/**
 * Real-Time WebSocket Event Envelopes & Frame Payloads
 * Source: docs/SOURCES/SYSTEM_PRD.md §3, §4
 */

import { Order, TicketStatus } from './order.js';
import { CanonicalMenu } from './menu.js';

export type EventType =
  | 'SYS_DISCOVERY'
  | 'ORDER_CREATE'
  | 'ORDER_CREATED'
  | 'TICKET_RECEIVED'
  | 'TICKET_BUMPED'
  | 'TICKET_RECALLED'
  | 'ITEM_86ED'
  | 'MENU_SYNC'
  | 'ORDER_CLOUD_INGEST'
  | 'ORDER_STATUS_UPDATE'
  | 'HEARTBEAT';

export interface WebSocketEnvelope<T = unknown> {
  eventId: string; // RFC 4122 v4 UUID
  eventType: EventType;
  timestampEpochMs: number;
  restaurantId: string;
  sourceTerminalId: string;
  payload: T;
}

export interface OrderCreatePayload {
  order: Order;
}

export interface TicketReceivedPayload {
  ticketNumber: string;
  orderUuid: string;
  tableLabel?: string;
  serverName?: string;
  prepStation: string;
  items: Order['items'];
  createdAtEpochMs: number;
}

export interface TicketBumpedPayload {
  ticketNumber: string;
  orderUuid: string;
  newStatus: TicketStatus;
  bumpedAtEpochMs: number;
  bumpedByEmployeeId?: string;
}

export interface Item86Payload {
  itemId: string;
  isAvailable: boolean;
  markedByEmployeeId: string;
  reason?: string;
}

export interface MenuSyncPayload {
  menu: CanonicalMenu;
  version: number;
}

export interface OrderStatusUpdatePayload {
  orderUuid: string;
  status: TicketStatus;
  estimatedReadyEpochMs?: number;
}

export interface HeartbeatPayload {
  terminalId: string;
  terminalRole: 'MASTER_POS' | 'KDB' | 'HANDHELD';
  ipAddress: string;
  batteryPercent?: number;
  uptimeSeconds: number;
}
