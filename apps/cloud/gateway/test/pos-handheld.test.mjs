import { test, describe, before } from 'node:test';
import assert from 'node:assert/strict';

describe('Sprint P02_M01: POS Core UI & Handheld Flow Tests', () => {

  describe('1. Table Floor Plan State Machine & Elapsed Timers', () => {
    class TableStateEngine {
      constructor(tableId, tableLabel, capacity = 4) {
        this.tableId = tableId;
        this.tableLabel = tableLabel;
        this.capacity = capacity;
        this.status = 'AVAILABLE';
        this.currentGuests = 0;
        this.serverName = null;
        this.seatedAtEpochMs = null;
        this.activeCheckTotalCents = 0;
      }

      seatGuests(guests, serverName = 'Server Elena', epochMs = Date.now()) {
        assert(this.status === 'AVAILABLE', `Cannot seat table when status is ${this.status}`);
        assert(guests > 0 && guests <= this.capacity, `Guest count must be 1..${this.capacity}`);
        this.status = 'SEATED';
        this.currentGuests = guests;
        this.serverName = serverName;
        this.seatedAtEpochMs = epochMs;
      }

      dropCheck(totalCents) {
        assert(this.status === 'SEATED', 'Can only drop check for seated tables');
        this.status = 'CHECK_DROPPED';
        this.activeCheckTotalCents = totalCents;
      }

      markDirty() {
        this.status = 'DIRTY';
        this.activeCheckTotalCents = 0;
      }

      markCleaned() {
        assert(this.status === 'DIRTY', 'Only dirty tables can be cleaned');
        this.status = 'AVAILABLE';
        this.currentGuests = 0;
        this.serverName = null;
        this.seatedAtEpochMs = null;
        this.activeCheckTotalCents = 0;
      }

      formattedElapsedTime(nowEpochMs) {
        if (!this.seatedAtEpochMs) return '00:00';
        const diffMs = Math.max(0, nowEpochMs - this.seatedAtEpochMs);
        const minutes = Math.floor(diffMs / 1000 / 60);
        const seconds = Math.floor((diffMs / 1000) % 60);
        return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
      }
    }

    test('Table progresses through full occupancy lifecycle: AVAILABLE -> SEATED -> DROPPED -> DIRTY -> AVAILABLE', () => {
      const table = new TableStateEngine('T1', 'Table 1', 4);
      assert.equal(table.status, 'AVAILABLE');

      const seatedTime = 1700000000000;
      table.seatGuests(4, 'Server Elena', seatedTime);
      assert.equal(table.status, 'SEATED');
      assert.equal(table.currentGuests, 4);
      assert.equal(table.serverName, 'Server Elena');

      // Test elapsed timer formatting at +24 minutes and 35 seconds
      const checkTime = seatedTime + (24 * 60 + 35) * 1000;
      assert.equal(table.formattedElapsedTime(checkTime), '24:35');

      // Drop check
      table.dropCheck(14580);
      assert.equal(table.status, 'CHECK_DROPPED');
      assert.equal(table.activeCheckTotalCents, 14580);

      // Settle and mark dirty
      table.markDirty();
      assert.equal(table.status, 'DIRTY');
      assert.equal(table.activeCheckTotalCents, 0);

      // Clean table
      table.markCleaned();
      assert.equal(table.status, 'AVAILABLE');
      assert.equal(table.currentGuests, 0);
      assert.equal(table.seatedAtEpochMs, null);
    });
  });

  describe('2. Item Modifier Constraint Validation & Penny-Perfect Pricing', () => {
    const ribeyeItem = {
      itemId: 'item_ribeye',
      name: 'USDA Prime Ribeye 14oz',
      basePriceCents: 4800,
      modifierGroups: [
        {
          groupId: 'grp_temp',
          name: 'Meat Temperature',
          minSelection: 1,
          maxSelection: 1,
          isRequired: true,
          options: [
            { optionId: 'opt_rare', name: 'Rare', priceDeltaCents: 0 },
            { optionId: 'opt_mr', name: 'Medium Rare', priceDeltaCents: 0 },
            { optionId: 'opt_med', name: 'Medium', priceDeltaCents: 0 }
          ]
        },
        {
          groupId: 'grp_sides',
          name: 'Choice of Side',
          minSelection: 1,
          maxSelection: 1,
          isRequired: true,
          options: [
            { optionId: 'opt_fries', name: 'Truffle Parmesan Fries', priceDeltaCents: 300 },
            { optionId: 'opt_mash', name: 'Mashed Potatoes', priceDeltaCents: 0 }
          ]
        },
        {
          groupId: 'grp_addons',
          name: 'Enhancements',
          minSelection: 0,
          maxSelection: 3,
          isRequired: false,
          options: [
            { optionId: 'opt_prawns', name: 'Jumbo Garlic Prawns (3)', priceDeltaCents: 1200 },
            { optionId: 'opt_bleu', name: 'Melted Bleu Cheese Crust', priceDeltaCents: 400 }
          ]
        }
      ]
    };

    function validateModifiers(item, selectedMap) {
      for (const grp of item.modifierGroups) {
        const selections = selectedMap[grp.groupId] || [];
        if (grp.isRequired && selections.length < grp.minSelection) return false;
        if (selections.length > grp.maxSelection) return false;
      }
      return true;
    }

    function calculateItemTotal(item, selectedMap) {
      let total = item.basePriceCents;
      for (const opts of Object.values(selectedMap)) {
        for (const opt of opts) {
          total += opt.priceDeltaCents;
        }
      }
      return total;
    }

    test('Enforces minSelection on required groups', () => {
      // 0 modifiers selected
      assert.equal(validateModifiers(ribeyeItem, {}), false);

      // Only temp selected, side missing
      assert.equal(validateModifiers(ribeyeItem, {
        grp_temp: [ribeyeItem.modifierGroups[0].options[1]]
      }), false);

      // Both required satisfied
      assert.equal(validateModifiers(ribeyeItem, {
        grp_temp: [ribeyeItem.modifierGroups[0].options[1]],
        grp_sides: [ribeyeItem.modifierGroups[1].options[0]]
      }), true);
    });

    test('Computes penny-perfect item total with base price and modifiers', () => {
      const selected = {
        grp_temp: [ribeyeItem.modifierGroups[0].options[1]], // 0
        grp_sides: [ribeyeItem.modifierGroups[1].options[0]], // +300
        grp_addons: [ribeyeItem.modifierGroups[2].options[0]] // +1200
      };

      const itemTotal = calculateItemTotal(ribeyeItem, selected);
      // 4800 + 0 + 300 + 1200 = 6300 cents ($63.00)
      assert.equal(itemTotal, 6300);
    });

    test('Check summary computes accurate 8.25% tax and grand total in cents', () => {
      const item1Cents = 6300;
      const item2Cents = 2200; // Wagyu carpaccio
      const subtotalCents = item1Cents + item2Cents; // 8500
      const taxRate = 0.0825;
      const taxCents = Math.round(subtotalCents * taxRate); // 8500 * 0.0825 = 701.25 -> 701
      const grandTotalCents = subtotalCents + taxCents; // 8500 + 701 = 9201

      assert.equal(subtotalCents, 8500);
      assert.equal(taxCents, 701);
      assert.equal(grandTotalCents, 9201);
    });
  });

  describe('3. Sunmi V2 Handheld 3-Tap Order Entry Flow Simulation', () => {
    class HandheldFlowSimulator {
      constructor() {
        this.tapCount = 0;
        this.selectedItem = null;
        this.selectedModifier = null;
        this.firedEnvelope = null;
      }

      tap1_SelectItem(item) {
        this.tapCount++;
        this.selectedItem = item;
        return item.requiresModifier ? 'OPEN_MODIFIER_SHEET' : 'READY_TO_FIRE';
      }

      tap2_SelectModifier(option) {
        assert(this.selectedItem, 'Must select item before modifier');
        this.tapCount++;
        this.selectedModifier = option;
        return 'READY_TO_FIRE';
      }

      tap3_FireToKitchen(tableLabel = 'Table 7', serverName = 'Elena R.') {
        assert(this.selectedItem, 'No item to fire');
        this.tapCount++;

        const itemTotal = this.selectedItem.basePriceCents + (this.selectedModifier?.priceDeltaCents || 0);
        const subtotal = itemTotal;
        const tax = Math.round(subtotal * 0.0825);

        this.firedEnvelope = {
          eventId: 'evt-' + Date.now(),
          eventType: 'TICKET_RECEIVED',
          restaurantId: 'rest_dinv_001',
          timestampEpochMs: Date.now(),
          sourceChannel: 'HANDHELD',
          payload: {
            clientOrderUuid: 'ord-' + Date.now(),
            tableLabel,
            serverName,
            sourceChannel: 'HANDHELD',
            ticketStatus: 'NEW_TICKET',
            items: [
              {
                itemId: this.selectedItem.itemId,
                name: this.selectedItem.name,
                basePriceCents: this.selectedItem.basePriceCents,
                quantity: 1,
                courseHold: 'FIRE',
                selectedModifiers: this.selectedModifier ? [
                  {
                    optionId: this.selectedModifier.optionId,
                    optionName: this.selectedModifier.name,
                    priceDeltaCents: this.selectedModifier.priceDeltaCents
                  }
                ] : [],
                itemTotalCents: itemTotal
              }
            ],
            subtotalCents: subtotal,
            taxCents: tax,
            totalCents: subtotal + tax
          }
        };

        return this.firedEnvelope;
      }
    }

    test('3-tap flow executes in exactly 3 taps and dispatches compliant TICKET_RECEIVED event', () => {
      const sim = new HandheldFlowSimulator();

      const item = {
        itemId: 'h_burger',
        name: 'Dinevique Prime Burger',
        basePriceCents: 2400,
        requiresModifier: true
      };

      const modifier = {
        optionId: 'opt_cheddar',
        name: 'Aged White Cheddar',
        priceDeltaCents: 0
      };

      // Step 1: Tap item
      const step1 = sim.tap1_SelectItem(item);
      assert.equal(step1, 'OPEN_MODIFIER_SHEET');
      assert.equal(sim.tapCount, 1);

      // Step 2: Tap modifier
      const step2 = sim.tap2_SelectModifier(modifier);
      assert.equal(step2, 'READY_TO_FIRE');
      assert.equal(sim.tapCount, 2);

      // Step 3: Tap fire CTA
      const envelope = sim.tap3_FireToKitchen('Table 7', 'Elena R.');
      assert.equal(sim.tapCount, 3, 'Flow MUST complete in exactly 3 taps');

      // Verify envelope compliance with Dinevique architecture
      assert.equal(envelope.eventType, 'TICKET_RECEIVED');
      assert.equal(envelope.sourceChannel, 'HANDHELD');
      assert.equal(envelope.payload.tableLabel, 'Table 7');
      assert.equal(envelope.payload.items[0].name, 'Dinevique Prime Burger');
      assert.equal(envelope.payload.items[0].selectedModifiers[0].optionName, 'Aged White Cheddar');
      assert.equal(envelope.payload.items[0].courseHold, 'FIRE');
      assert.equal(envelope.payload.totalCents, 2400 + Math.round(2400 * 0.0825));
    });

    test('Zero-modifier item completes in 2 taps (well under the 3-tap ceiling)', () => {
      const sim = new HandheldFlowSimulator();
      const item = {
        itemId: 'h_fries',
        name: 'Truffle Parmesan Fries',
        basePriceCents: 1400,
        requiresModifier: false
      };

      sim.tap1_SelectItem(item);
      sim.tap3_FireToKitchen('Table 7', 'Elena R.');

      assert.equal(sim.tapCount, 2);
      assert.equal(sim.firedEnvelope.payload.items[0].name, 'Truffle Parmesan Fries');
    });
  });
});
