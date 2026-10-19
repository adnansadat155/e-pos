/**
 * Store-and-Forward Offline Queue Types
 * Source: docs/SOURCES/SYSTEM_PRD.md §6.2
 */

export type SyncStatus = 'PENDING' | 'IN_FLIGHT' | 'ACKED' | 'FAILED';
export type SettlementStatus = 'QUEUED' | 'SETTLED' | 'DECLINED' | 'FAILED';

export interface OutboundEventQueueItem {
  eventUuid: string;
  eventType: 'ORDER_CREATE' | 'TICKET_BUMPED' | 'ORDER_PAID' | 'ITEM_86ED';
  entityUuid: string;
  payloadJson: string;
  retryCount: number;
  syncStatus: SyncStatus;
  createdAtEpochMs: number;
}

export interface OfflinePaymentRecord {
  paymentUuid: string;
  clientOrderUuid: string;
  amountCents: number;
  tipCents: number;
  encryptedCardPayload: string; // AES-256 GCM encrypted ciphertext + tag
  cardLast4: string;
  cardBrand: string;
  capturedAtEpochMs: number;
  settlementStatus: SettlementStatus;
}
