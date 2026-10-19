'use client';

import { useState } from 'react';
import Link from 'next/link';
import { ShoppingBag, ArrowLeft, Check, Plus, Minus, X } from 'lucide-react';
import { CanonicalMenu, MenuItem, ModifierOption, ModifierGroup } from '@dinevique/contracts';

const mockMenu: CanonicalMenu = {
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
};

interface CartItem {
  cartItemId: string;
  item: MenuItem;
  quantity: number;
  selectedModifiers: ModifierOption[];
  totalPriceCents: number;
}

function formatCents(cents: number): string {
  return `$${(cents / 100).toFixed(2)}`;
}

export default function MenuPage() {
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [cart, setCart] = useState<CartItem[]>([]);
  const [isCartOpen, setIsCartOpen] = useState<boolean>(false);
  const [modalItem, setModalItem] = useState<MenuItem | null>(null);
  const [selectedModifiersMap, setSelectedModifiersMap] = useState<Record<string, ModifierOption[]>>({});
  const [checkoutSuccess, setCheckoutSuccess] = useState<boolean>(false);

  const totalItemsCount = cart.reduce((acc, it) => acc + it.quantity, 0);
  const cartSubtotalCents = cart.reduce((acc, it) => acc + it.totalPriceCents, 0);
  const cartTaxCents = Math.round(cartSubtotalCents * 0.08875);
  const cartTotalCents = cartSubtotalCents + cartTaxCents;

  const handleOpenItem = (item: MenuItem) => {
    if (item.modifierGroups.length === 0) {
      // Add directly
      addToCart(item, []);
    } else {
      // Open modal & preselect defaults
      const defaults: Record<string, ModifierOption[]> = {};
      item.modifierGroups.forEach((grp) => {
        if (grp.isRequired && grp.options.length > 0) {
          defaults[grp.groupId] = [grp.options[0]];
        }
      });
      setSelectedModifiersMap(defaults);
      setModalItem(item);
    }
  };

  const toggleOption = (group: ModifierGroup, option: ModifierOption) => {
    const current = selectedModifiersMap[group.groupId] || [];
    if (group.maxSelection === 1) {
      setSelectedModifiersMap({ ...selectedModifiersMap, [group.groupId]: [option] });
    } else {
      const exists = current.some((o) => o.optionId === option.optionId);
      if (exists) {
        setSelectedModifiersMap({
          ...selectedModifiersMap,
          [group.groupId]: current.filter((o) => o.optionId !== option.optionId),
        });
      } else {
        if (current.length < group.maxSelection) {
          setSelectedModifiersMap({
            ...selectedModifiersMap,
            [group.groupId]: [...current, option],
          });
        }
      }
    }
  };

  const isModalValid = (): boolean => {
    if (!modalItem) return false;
    for (const grp of modalItem.modifierGroups) {
      const count = (selectedModifiersMap[grp.groupId] || []).length;
      if (grp.isRequired && count < grp.minSelection) return false;
      if (count > grp.maxSelection) return false;
    }
    return true;
  };

  const handleConfirmModal = () => {
    if (!modalItem || !isModalValid()) return;
    const allSelected = Object.values(selectedModifiersMap).flat();
    addToCart(modalItem, allSelected);
    setModalItem(null);
  };

  const addToCart = (item: MenuItem, modifiers: ModifierOption[]) => {
    const unitPrice = item.basePriceCents + modifiers.reduce((acc, m) => acc + m.priceDeltaCents, 0);
    const newCartItem: CartItem = {
      cartItemId: `${item.itemId}-${Date.now()}`,
      item,
      quantity: 1,
      selectedModifiers: modifiers,
      totalPriceCents: unitPrice,
    };
    setCart((prev) => [...prev, newCartItem]);
    setIsCartOpen(true);
  };

  const updateQuantity = (cartItemId: string, delta: number) => {
    setCart((prev) =>
      prev
        .map((ci) => {
          if (ci.cartItemId === cartItemId) {
            const nextQty = ci.quantity + delta;
            if (nextQty <= 0) return null;
            const unitPrice = ci.totalPriceCents / ci.quantity;
            return {
              ...ci,
              quantity: nextQty,
              totalPriceCents: unitPrice * nextQty,
            };
          }
          return ci;
        })
        .filter(Boolean) as CartItem[]
    );
  };

  const filteredCategories =
    selectedCategory === 'all'
      ? mockMenu.categories
      : mockMenu.categories.filter((c) => c.categoryId === selectedCategory);

  return (
    <div className="min-h-screen bg-brand-onyx text-brand-white">
      {/* Sticky Header */}
      <header className="sticky top-0 z-30 bg-brand-onyx/90 backdrop-blur-md border-b border-brand-slate px-6 py-4">
        <div className="max-w-6xl mx-auto flex items-center justify-between">
          <Link href="/" className="flex items-center gap-2 text-sm text-gray-400 hover:text-brand-white transition">
            <ArrowLeft className="w-4 h-4" />
            Home
          </Link>
          <div className="text-center">
            <span className="font-bold text-xl text-brand-amber font-display tracking-wide block">
              DINEVIQUE ONLINE
            </span>
            <span className="text-xs text-gray-400">Metropolitan Manhattan • Pickup & Delivery</span>
          </div>
          <button
            onClick={() => setIsCartOpen(true)}
            className="flex items-center gap-2.5 px-4 py-2 rounded-lg bg-brand-amber text-brand-ink text-sm font-bold shadow-lg shadow-brand-amber/10 hover:opacity-95 transition"
          >
            <ShoppingBag className="w-4 h-4" />
            <span>Bag ({totalItemsCount})</span>
          </button>
        </div>
      </header>

      {/* Main Container */}
      <main className="max-w-6xl mx-auto px-6 py-8">
        {/* Category Navigation Pills */}
        <div className="flex items-center gap-2 overflow-x-auto pb-4 mb-8 border-b border-brand-slate">
          <button
            onClick={() => setSelectedCategory('all')}
            className={`px-5 py-2 rounded-full text-sm font-semibold transition ${
              selectedCategory === 'all'
                ? 'bg-brand-amber text-brand-ink shadow-sm'
                : 'bg-brand-slate text-brand-white hover:bg-brand-slate/80'
            }`}
          >
            All Items
          </button>
          {mockMenu.categories.map((c) => (
            <button
              key={c.categoryId}
              onClick={() => setSelectedCategory(c.categoryId)}
              className={`px-5 py-2 rounded-full text-sm font-semibold whitespace-nowrap transition ${
                selectedCategory === c.categoryId
                  ? 'bg-brand-amber text-brand-ink shadow-sm'
                  : 'bg-brand-slate text-brand-white hover:bg-brand-slate/80'
              }`}
            >
              {c.name}
            </button>
          ))}
        </div>

        {/* Categories & Product Cards */}
        {filteredCategories.map((cat) => (
          <section key={cat.categoryId} className="mb-12">
            <h2 className="text-2xl font-bold font-display text-brand-white mb-6 border-b border-brand-slate pb-2">
              {cat.name}
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {cat.items.map((item) => (
                <div
                  key={item.itemId}
                  className="p-6 rounded-xl bg-brand-slate border border-brand-slate hover:border-brand-amber/40 transition flex flex-col justify-between"
                >
                  <div>
                    <div className="flex justify-between items-start mb-2">
                      <h3 className="text-lg font-bold text-brand-white font-display">{item.name}</h3>
                      <span className="text-lg font-bold text-brand-amber font-mono">
                        {formatCents(item.basePriceCents)}
                      </span>
                    </div>
                    {item.description && (
                      <p className="text-sm text-gray-400 mb-4 leading-relaxed">{item.description}</p>
                    )}
                  </div>
                  <div className="pt-4 border-t border-brand-linen/10 flex justify-between items-center">
                    <span className="text-xs uppercase tracking-wider text-gray-400 bg-brand-onyx/50 px-2.5 py-1 rounded border border-brand-slate">
                      Station: {item.prepStation}
                    </span>
                    <button
                      onClick={() => handleOpenItem(item)}
                      className="btn-primary text-xs py-2 px-4 shadow-sm"
                    >
                      {item.modifierGroups.length > 0 ? 'Customize & Add' : 'Add to Order'}
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </section>
        ))}
      </main>

      {/* Item Customization Modal */}
      {modalItem && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4">
          <div className="bg-brand-slate border border-brand-slate rounded-2xl max-w-lg w-full p-6 text-brand-white shadow-2xl animate-in fade-in zoom-in-95 duration-200">
            <div className="flex justify-between items-start mb-4 pb-3 border-b border-brand-linen/10">
              <div>
                <h3 className="text-xl font-bold font-display">{modalItem.name}</h3>
                <span className="text-brand-amber font-bold font-mono">{formatCents(modalItem.basePriceCents)}</span>
              </div>
              <button
                onClick={() => setModalItem(null)}
                className="p-1.5 rounded-full hover:bg-brand-onyx text-gray-400 hover:text-white"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="max-h-[60vh] overflow-y-auto pr-1 space-y-6">
              {modalItem.modifierGroups.map((grp) => {
                const selectedInGrp = selectedModifiersMap[grp.groupId] || [];
                return (
                  <div key={grp.groupId} className="space-y-3">
                    <div className="flex justify-between items-center">
                      <span className="font-semibold text-sm">{grp.name}</span>
                      <span className="text-xs text-gray-400">
                        {grp.isRequired ? 'Required • Pick 1' : `Optional • Max ${grp.maxSelection}`}
                      </span>
                    </div>
                    <div className="space-y-2">
                      {grp.options.map((opt) => {
                        const isSelected = selectedInGrp.some((o) => o.optionId === opt.optionId);
                        return (
                          <div
                            key={opt.optionId}
                            onClick={() => toggleOption(grp, opt)}
                            className={`p-3 rounded-lg border cursor-pointer flex justify-between items-center transition ${
                              isSelected
                                ? 'bg-brand-onyx border-brand-amber'
                                : 'bg-brand-onyx/40 border-brand-slate hover:border-gray-600'
                            }`}
                          >
                            <span className="text-sm font-medium">{opt.name}</span>
                            <span className="text-xs font-mono text-brand-amber">
                              {opt.priceDeltaCents > 0 ? `+${formatCents(opt.priceDeltaCents)}` : 'Included'}
                            </span>
                          </div>
                        );
                      })}
                    </div>
                  </div>
                );
              })}
            </div>

            <div className="pt-6 mt-6 border-t border-brand-linen/10 flex justify-between items-center">
              <div>
                <span className="text-xs text-gray-400 uppercase tracking-wide block">Total Price</span>
                <span className="text-xl font-bold font-mono text-brand-amber">
                  {formatCents(
                    modalItem.basePriceCents +
                      Object.values(selectedModifiersMap)
                        .flat()
                        .reduce((acc, m) => acc + m.priceDeltaCents, 0)
                  )}
                </span>
              </div>
              <button
                disabled={!isModalValid()}
                onClick={handleConfirmModal}
                className="btn-primary text-sm py-2.5 px-6 disabled:opacity-40 disabled:cursor-not-allowed"
              >
                Add to Bag
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Sliding Cart Drawer */}
      {isCartOpen && (
        <div className="fixed inset-0 z-50 flex justify-end bg-black/60 backdrop-blur-sm animate-in fade-in duration-200">
          <div className="bg-brand-slate border-l border-brand-slate w-full max-w-md h-full flex flex-col justify-between p-6 shadow-2xl">
            <div>
              <div className="flex justify-between items-center pb-4 border-b border-brand-linen/10">
                <div className="flex items-center gap-2">
                  <ShoppingBag className="w-5 h-5 text-brand-amber" />
                  <h3 className="font-bold text-lg font-display">Your Order</h3>
                </div>
                <button
                  onClick={() => setIsCartOpen(false)}
                  className="p-1.5 rounded-full hover:bg-brand-onyx text-gray-400 hover:text-white"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>

              {checkoutSuccess ? (
                <div className="py-16 text-center space-y-4">
                  <div className="w-12 h-12 rounded-full bg-brand-amber text-brand-ink flex items-center justify-center mx-auto">
                    <Check className="w-6 h-6 stroke-[3]" />
                  </div>
                  <h4 className="text-xl font-bold font-display">Order Transmitted</h4>
                  <p className="text-sm text-gray-400">
                    Your order has been routed to the kitchen for preparation.
                  </p>
                  <button
                    onClick={() => {
                      setCart([]);
                      setCheckoutSuccess(false);
                      setIsCartOpen(false);
                    }}
                    className="btn-primary text-sm py-2 px-6"
                  >
                    Start New Order
                  </button>
                </div>
              ) : cart.length === 0 ? (
                <div className="py-24 text-center text-gray-400">
                  <ShoppingBag className="w-12 h-12 mx-auto mb-3 opacity-30" />
                  <p className="text-sm">Your dining bag is currently empty.</p>
                </div>
              ) : (
                <div className="max-h-[60vh] overflow-y-auto py-4 space-y-4 pr-1">
                  {cart.map((ci) => (
                    <div
                      key={ci.cartItemId}
                      className="p-4 rounded-lg bg-brand-onyx/50 border border-brand-slate space-y-2"
                    >
                      <div className="flex justify-between items-start">
                        <div>
                          <span className="font-semibold text-sm block">{ci.item.name}</span>
                          {ci.selectedModifiers.length > 0 && (
                            <span className="text-xs text-gray-400 block">
                              {ci.selectedModifiers.map((m) => m.name).join(', ')}
                            </span>
                          )}
                        </div>
                        <span className="font-mono text-sm font-semibold text-brand-amber">
                          {formatCents(ci.totalPriceCents)}
                        </span>
                      </div>
                      <div className="flex items-center gap-3 pt-2">
                        <button
                          onClick={() => updateQuantity(ci.cartItemId, -1)}
                          className="w-7 h-7 rounded bg-brand-slate flex items-center justify-center hover:bg-gray-700 transition"
                        >
                          <Minus className="w-3.5 h-3.5" />
                        </button>
                        <span className="font-mono text-sm">{ci.quantity}</span>
                        <button
                          onClick={() => updateQuantity(ci.cartItemId, 1)}
                          className="w-7 h-7 rounded bg-brand-slate flex items-center justify-center hover:bg-gray-700 transition"
                        >
                          <Plus className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {!checkoutSuccess && cart.length > 0 && (
              <div className="pt-6 border-t border-brand-linen/10 space-y-3">
                <div className="flex justify-between text-sm text-gray-400">
                  <span>Subtotal</span>
                  <span className="font-mono text-white">{formatCents(cartSubtotalCents)}</span>
                </div>
                <div className="flex justify-between text-sm text-gray-400">
                  <span>Estimated Tax (8.875%)</span>
                  <span className="font-mono text-white">{formatCents(cartTaxCents)}</span>
                </div>
                <div className="flex justify-between text-base font-bold pt-2 border-t border-brand-slate">
                  <span>Total</span>
                  <span className="font-mono text-brand-amber text-lg">{formatCents(cartTotalCents)}</span>
                </div>
                <button
                  onClick={() => setCheckoutSuccess(true)}
                  className="btn-primary w-full py-3 text-base shadow-xl"
                >
                  Confirm & Place Order
                </button>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
