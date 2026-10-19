import mongoose, { Schema, Document } from 'mongoose';
import { CanonicalMenu } from '@dinevique/contracts';

export interface IMenuDocument extends Document, Omit<CanonicalMenu, 'menuId'> {
  menuId: string;
}

const ModifierOptionSchema = new Schema({
  optionId: { type: String, required: true },
  name: { type: String, required: true },
  priceDeltaCents: { type: Number, required: true, default: 0 },
  isAvailable: { type: Boolean, default: true },
  nestedModifierGroupIds: [{ type: String }],
}, { _id: false });

const ModifierGroupSchema = new Schema({
  groupId: { type: String, required: true },
  name: { type: String, required: true },
  minSelection: { type: Number, required: true, default: 0 },
  maxSelection: { type: Number, required: true, default: 1 },
  isRequired: { type: Boolean, default: false },
  options: [ModifierOptionSchema],
}, { _id: false });

const MenuItemSchema = new Schema({
  itemId: { type: String, required: true },
  name: { type: String, required: true },
  description: { type: String },
  basePriceCents: { type: Number, required: true },
  taxRatePercent: { type: Number, required: true, default: 0 },
  prepStation: { type: String, required: true },
  isAvailable: { type: Boolean, default: true },
  modifierGroups: [ModifierGroupSchema],
  imageUrl: { type: String },
}, { _id: false });

const MenuCategorySchema = new Schema({
  categoryId: { type: String, required: true },
  name: { type: String, required: true },
  displayOrder: { type: Number, required: true, default: 0 },
  items: [MenuItemSchema],
}, { _id: false });

const MenuSchema = new Schema({
  menuId: { type: String, required: true, unique: true, index: true },
  restaurantId: { type: String, required: true, index: true },
  name: { type: String, required: true },
  isActive: { type: Boolean, default: true },
  updatedAtIso: { type: String, required: true },
  categories: [MenuCategorySchema],
}, {
  timestamps: true,
});

export const MenuModel = mongoose.model<IMenuDocument>('Menu', MenuSchema);
