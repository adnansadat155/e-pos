import mongoose, { Schema, Document } from 'mongoose';
import { Order } from '@dinevique/contracts';

export interface IOrderDocument extends Document, Omit<Order, 'clientOrderUuid'> {
  clientOrderUuid: string;
}

const OrderItemSchema = new Schema({
  itemInstanceUuid: { type: String, required: true },
  itemId: { type: String, required: true },
  name: { type: String, required: true },
  basePriceCents: { type: Number, required: true },
  quantity: { type: Number, required: true, default: 1 },
  seatNumber: { type: Number },
  courseHold: { type: String, enum: ['HOLD', 'FIRE'], default: 'FIRE' },
  prepStation: { type: String, required: true },
  specialInstructions: { type: String },
  selectedModifiers: [{
    groupId: { type: String, required: true },
    groupName: { type: String, required: true },
    optionId: { type: String, required: true },
    optionName: { type: String, required: true },
    priceDeltaCents: { type: Number, required: true, default: 0 },
  }],
  itemTotalCents: { type: Number, required: true },
}, { _id: false });

const PaymentRecordSchema = new Schema({
  paymentUuid: { type: String, required: true },
  clientOrderUuid: { type: String, required: true },
  amountCents: { type: Number, required: true },
  tipCents: { type: Number, default: 0 },
  tenderType: { type: String, required: true },
  cardLast4: { type: String },
  cardBrand: { type: String },
  authCode: { type: String },
  isOfflineAuthorized: { type: Boolean, default: false },
  capturedAtEpochMs: { type: Number, required: true },
}, { _id: false });

const OrderSchema = new Schema({
  clientOrderUuid: { type: String, required: true, unique: true, index: true },
  restaurantId: { type: String, required: true, index: true },
  tableLabel: { type: String },
  serverEmployeeId: { type: String },
  serverName: { type: String },
  guestCount: { type: Number },
  sourceChannel: { type: String, enum: ['POS', 'HANDHELD', 'ONLINE'], required: true },
  ticketStatus: {
    type: String,
    enum: ['NEW_TICKET', 'IN_PROGRESS', 'READY', 'PAID', 'BUMPED', 'CANCELLED'],
    required: true,
    default: 'NEW_TICKET',
    index: true,
  },
  items: [OrderItemSchema],
  payments: [PaymentRecordSchema],
  subtotalCents: { type: Number, required: true },
  taxCents: { type: Number, required: true },
  tipCents: { type: Number, default: 0 },
  totalCents: { type: Number, required: true },
  createdAtEpochMs: { type: Number, required: true, index: true },
  updatedAtEpochMs: { type: Number, required: true },
}, {
  timestamps: false,
  bufferCommands: false,
});

// Idempotent upsert compound index
OrderSchema.index({ restaurantId: 1, clientOrderUuid: 1 }, { unique: true });

export const OrderModel = mongoose.model<IOrderDocument>('Order', OrderSchema);
