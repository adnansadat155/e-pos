/**
 * Canonical Order, Ticket, Split Check & Payment Schemas
 * Source: docs/SOURCES/SYSTEM_PRD.md §6.2
 */

export type SourceChannel = 'POS' | 'HANDHELD' | 'ONLINE';
export type TicketStatus = 'NEW_TICKET' | 'IN_PROGRESS' | 'READY' | 'PAID' | 'BUMPED' | 'CANCELLED';
export type CourseHoldState = 'HOLD' | 'FIRE';
export type TenderType = 'CASH' | 'CARD' | 'OFFLINE_CARD' | 'GIFT_CARD';

export interface SelectedModifier {
  groupId: string;
  groupName: string;
  optionId: string;
  optionName: string;
  priceDeltaCents: number;
}

export interface OrderItem {
  itemInstanceUuid: string;
  itemId: string;
  name: string;
  basePriceCents: number;
  quantity: number;
  seatNumber?: number;
  courseHold: CourseHoldState;
  prepStation: string;
  specialInstructions?: string;
  selectedModifiers: SelectedModifier[];
  itemTotalCents: number;
}

export interface SplitCheck {
  checkUuid: string;
  seatNumber?: number;
  itemInstanceUuids: string[];
  subtotalCents: number;
  taxCents: number;
  tipCents: number;
  totalCents: number;
  isPaid: boolean;
  tenderType?: TenderType;
}

export interface PaymentRecord {
  paymentUuid: string;
  clientOrderUuid: string;
  amountCents: number;
  tipCents: number;
  tenderType: TenderType;
  cardLast4?: string;
  cardBrand?: string;
  authCode?: string;
  isOfflineAuthorized: boolean;
  capturedAtEpochMs: number;
}

export interface Order {
  clientOrderUuid: string; // RFC 4122 v4 UUID generated at edge
  restaurantId: string;
  tableLabel?: string;
  serverEmployeeId?: string;
  serverName?: string;
  guestCount?: number;
  sourceChannel: SourceChannel;
  ticketStatus: TicketStatus;
  items: OrderItem[];
  splitChecks?: SplitCheck[];
  payments: PaymentRecord[];
  subtotalCents: number;
  taxCents: number;
  tipCents: number;
  totalCents: number;
  createdAtEpochMs: number;
  updatedAtEpochMs: number;
}
