import { Request, Response } from 'express';
import { CanonicalMenu, WebSocketEnvelope } from '@dinevique/contracts';
import { MenuModel } from '../models/MenuModel.js';
import { globalTunnelManager } from '../tunnel/EdgeTunnelManager.js';
import mongoose from 'mongoose';

// In-memory fallback catalog when MongoDB is not connected
let memoryMenuCatalog: Map<string, CanonicalMenu> = new Map([
  [
    'rest_nyc_01',
    {
      menuId: 'menu_001',
      restaurantId: 'rest_nyc_01',
      name: 'Dinevique Metropolitan Menu',
      isActive: true,
      updatedAtIso: new Date().toISOString(),
      categories: [
        {
          categoryId: 'cat_mains',
          name: 'Entrees & Steaks',
          displayOrder: 1,
          items: [
            {
              itemId: 'item_ribeye',
              name: 'USDA Prime Ribeye 14oz',
              description: 'Cast-iron seared prime beef, roasted garlic herb butter, demi-glace.',
              basePriceCents: 4800,
              taxRatePercent: 8.875,
              prepStation: 'GRILL',
              isAvailable: true,
              modifierGroups: [
                {
                  groupId: 'grp_temp',
                  name: 'Meat Temperature',
                  minSelection: 1,
                  maxSelection: 1,
                  isRequired: true,
                  options: [
                    { optionId: 'opt_rare', name: 'Rare', priceDeltaCents: 0, isAvailable: true },
                    { optionId: 'opt_mr', name: 'Medium-Rare', priceDeltaCents: 0, isAvailable: true },
                    { optionId: 'opt_med', name: 'Medium', priceDeltaCents: 0, isAvailable: true },
                    { optionId: 'opt_well', name: 'Well Done', priceDeltaCents: 0, isAvailable: true },
                  ],
                },
                {
                  groupId: 'grp_sides',
                  name: 'Choice of Side',
                  minSelection: 1,
                  maxSelection: 1,
                  isRequired: true,
                  options: [
                    { optionId: 'opt_fries', name: 'Truffle Parmesan Fries', priceDeltaCents: 300, isAvailable: true },
                    { optionId: 'opt_mash', name: 'Yukon Gold Mashed Potatoes', priceDeltaCents: 0, isAvailable: true },
                  ],
                },
              ],
            },
            {
              itemId: 'item_salmon',
              name: 'Pan-Roasted King Salmon',
              description: 'Wild Alaskan salmon, saffron fennel broth, baby fingerling potatoes.',
              basePriceCents: 3800,
              taxRatePercent: 8.875,
              prepStation: 'GRILL',
              isAvailable: true,
              modifierGroups: [],
            },
          ],
        },
        {
          categoryId: 'cat_pasta',
          name: 'Handmade Pastas',
          displayOrder: 2,
          items: [
            {
              itemId: 'item_tagliatelle',
              name: 'Tagliatelle Bolognese',
              description: '12-hour braised veal & pork ragu, aged pecorino.',
              basePriceCents: 3200,
              taxRatePercent: 8.875,
              prepStation: 'EXPO',
              isAvailable: true,
              modifierGroups: [],
            },
          ],
        },
        {
          categoryId: 'cat_desserts',
          name: 'Desserts',
          displayOrder: 3,
          items: [
            {
              itemId: 'item_fondant',
              name: 'Warm Valrhona Chocolate Fondant',
              description: 'Madagascar vanilla bean gelato, salted caramel brittle.',
              basePriceCents: 1600,
              taxRatePercent: 8.875,
              prepStation: 'PASTRY',
              isAvailable: true,
              modifierGroups: [],
            },
          ],
        },
      ],
    },
  ],
]);

/**
 * GET /api/v1/menu
 * Returns active canonical menu for a given restaurant
 */
export async function getMenuHandler(req: Request, res: Response): Promise<void> {
  const restaurantId = (req.query.restaurantId as string) || 'rest_nyc_01';

  try {
    if (mongoose.connection.readyState === 1) {
      const doc = await MenuModel.findOne({ restaurantId, isActive: true }).lean();
      if (doc) {
        res.status(200).json({ success: true, menu: doc });
        return;
      }
    }

    const fallback = memoryMenuCatalog.get(restaurantId) || memoryMenuCatalog.get('rest_nyc_01');
    res.status(200).json({ success: true, menu: fallback });
  } catch (error) {
    res.status(500).json({ success: false, error: (error as Error).message });
  }
}

/**
 * POST /api/v1/menu
 * Creates or updates canonical menu in database
 */
export async function upsertMenuHandler(req: Request, res: Response): Promise<void> {
  const menu = req.body as CanonicalMenu;

  if (!menu || !menu.restaurantId || !menu.categories) {
    res.status(400).json({ success: false, error: 'Invalid menu payload. restaurantId and categories required.' });
    return;
  }

  menu.updatedAtIso = new Date().toISOString();

  try {
    if (mongoose.connection.readyState === 1) {
      await MenuModel.findOneAndUpdate(
        { restaurantId: menu.restaurantId, menuId: menu.menuId },
        { $set: menu },
        { upsert: true, new: true }
      );
    }

    // Always update in-memory catalog
    memoryMenuCatalog.set(menu.restaurantId, menu);

    res.status(200).json({
      success: true,
      menuId: menu.menuId,
      updatedAtIso: menu.updatedAtIso,
      categoriesCount: menu.categories.length,
    });
  } catch (error) {
    res.status(500).json({ success: false, error: (error as Error).message });
  }
}

/**
 * POST /api/v1/menu/publish
 * Publishes menu revision and broadcasts MENU_SYNC envelope over WSS edge tunnels
 */
export async function publishMenuHandler(req: Request, res: Response): Promise<void> {
  const { restaurantId, menuId } = req.body;

  const targetRestaurantId = restaurantId || 'rest_nyc_01';
  const menu = memoryMenuCatalog.get(targetRestaurantId) || memoryMenuCatalog.get('rest_nyc_01');

  if (!menu) {
    res.status(404).json({ success: false, error: 'Menu not found for publication' });
    return;
  }

  const syncPayload = {
    menuId: menuId || menu.menuId,
    name: menu.name,
    updatedAtIso: new Date().toISOString(),
    categoriesCount: menu.categories.length,
    categories: menu.categories,
  };

  const syncEnvelope: WebSocketEnvelope<typeof syncPayload> = {
    eventId: `evt-sync-${Date.now()}`,
    eventType: 'MENU_SYNC',
    restaurantId: targetRestaurantId,
    sourceTerminalId: 'cloud_gateway_01',
    timestampEpochMs: Date.now(),
    payload: syncPayload,
  };

  // Broadcast over active edge tunnel
  globalTunnelManager.sendToRestaurant(targetRestaurantId, syncEnvelope);

  res.status(200).json({
    success: true,
    publishedRevision: syncPayload.updatedAtIso,
    restaurantId: targetRestaurantId,
    eventType: 'MENU_SYNC',
    message: 'Menu synchronized to edge terminals',
  });
}
