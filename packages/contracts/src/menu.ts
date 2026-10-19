/**
 * Canonical Menu Domain Models & Hierarchical Schemas
 * Source: docs/SOURCES/SYSTEM_PRD.md §6.1
 */

export type PrepStation = 'GRILL' | 'FRY' | 'SALAD' | 'PASTRY' | 'EXPO' | 'BAR' | 'COUNTER';

export interface ModifierOption {
  optionId: string;
  name: string;
  priceDeltaCents: number;
  isAvailable: boolean;
  nestedModifierGroupIds?: string[];
}

export interface ModifierGroup {
  groupId: string;
  name: string;
  minSelection: number;
  maxSelection: number;
  isRequired: boolean;
  options: ModifierOption[];
}

export interface MenuItem {
  itemId: string;
  name: string;
  description?: string;
  basePriceCents: number;
  taxRatePercent: number;
  prepStation: PrepStation;
  isAvailable: boolean;
  modifierGroups: ModifierGroup[];
  imageUrl?: string;
}

export interface MenuCategory {
  categoryId: string;
  name: string;
  displayOrder: number;
  items: MenuItem[];
}

export interface CanonicalMenu {
  menuId: string;
  restaurantId: string;
  name: string;
  isActive: boolean;
  updatedAtIso: string;
  categories: MenuCategory[];
}
