import mongoose, { Schema, Document } from 'mongoose';

export interface IPaymentDocument extends Document {
  paymentUuid: string;
  clientOrderUuid: string;
  restaurantId: string;
  amountCents: number;
  tipCents: number;
  cardLast4: string;
  cardBrand: string;
  isOfflineAuthorized: boolean;
  settlementStatus: 'AUTHORIZED' | 'SETTLED' | 'FAILED';
  authCode: string;
  capturedAtEpochMs: number;
  settledAtEpochMs: number;
}

const PaymentSchema = new Schema({
  paymentUuid: { type: String, required: true, unique: true, index: true },
  clientOrderUuid: { type: String, required: true, index: true },
  restaurantId: { type: String, required: true, index: true },
  amountCents: { type: Number, required: true },
  tipCents: { type: Number, default: 0 },
  cardLast4: { type: String, required: true },
  cardBrand: { type: String, required: true },
  isOfflineAuthorized: { type: Boolean, default: false },
  settlementStatus: {
    type: String,
    enum: ['AUTHORIZED', 'SETTLED', 'FAILED'],
    default: 'SETTLED',
  },
  authCode: { type: String, required: true },
  capturedAtEpochMs: { type: Number, required: true },
  settledAtEpochMs: { type: Number, required: true },
}, {
  timestamps: false,
  bufferCommands: false,
});

PaymentSchema.index({ restaurantId: 1, paymentUuid: 1 }, { unique: true });

export const PaymentModel = mongoose.model<IPaymentDocument>('Payment', PaymentSchema);
