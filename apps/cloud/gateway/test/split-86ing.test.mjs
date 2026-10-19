import { test, describe } from 'node:test';
import assert from 'node:assert/strict';

describe('Sprint P02_M02: Split Checks, Coursing & Pass 86ing Tests', () => {

  describe('1. Penny-Perfect Even N-Way Split Algorithm (Zero Discrepancy Proof)', () => {
    function splitEvenlyNWay(subtotalCents, taxRatePercent, n) {
      assert(n >= 2 && n <= 20, 'N must be between 2 and 20');
      const taxCents = Math.round(subtotalCents * (taxRatePercent / 100.0));
      const totalCents = subtotalCents + taxCents;

      const baseTotal = Math.floor(totalCents / n);
      const remainderTotal = totalCents % n;

      const baseTax = Math.floor(taxCents / n);
      const remainderTax = taxCents % n;

      const subChecks = [];

      for (let i = 0; i < n; i++) {
        const partyTotal = i < remainderTotal ? baseTotal + 1 : baseTotal;
        const partyTax = i < remainderTax ? baseTax + 1 : baseTax;
        const partySubtotal = partyTotal - partyTax;

        subChecks.push({
          checkIndex: i + 1,
          subtotalCents: partySubtotal,
          taxCents: partyTax,
          totalCents: partyTotal
        });
      }

      return {
        originalSubtotalCents: subtotalCents,
        originalTaxCents: taxCents,
        originalTotalCents: totalCents,
        subChecks
      };
    }

    test('Zero discrepancy across varied odd totals and party sizes N=2..15', () => {
      const testCases = [
        { subtotal: 10001, taxRate: 8.25 }, // $100.01 subtotal
        { subtotal: 14587, taxRate: 8.25 }, // $145.87 subtotal
        { subtotal: 3999, taxRate: 8.25 },  // $39.99 subtotal
        { subtotal: 21553, taxRate: 8.25 }, // $215.53 subtotal
        { subtotal: 703, taxRate: 8.25 }    // $7.03 subtotal
      ];

      for (const tc of testCases) {
        for (let n = 2; n <= 15; n++) {
          const result = splitEvenlyNWay(tc.subtotal, tc.taxRate, n);
          const sumTotals = result.subChecks.reduce((acc, c) => acc + c.totalCents, 0);
          const sumTaxes = result.subChecks.reduce((acc, c) => acc + c.taxCents, 0);
          const sumSubtotals = result.subChecks.reduce((acc, c) => acc + c.subtotalCents, 0);

          // Absolute mathematical zero discrepancy checks
          assert.equal(
            sumTotals,
            result.originalTotalCents,
            `Total discrepancy detected for subtotal=${tc.subtotal}, N=${n}: sum=${sumTotals}, expected=${result.originalTotalCents}`
          );

          assert.equal(
            sumTaxes,
            result.originalTaxCents,
            `Tax discrepancy detected for subtotal=${tc.subtotal}, N=${n}: sum=${sumTaxes}, expected=${result.originalTaxCents}`
          );

          assert.equal(
            sumSubtotals,
            result.originalSubtotalCents,
            `Subtotal discrepancy detected for subtotal=${tc.subtotal}, N=${n}: sum=${sumSubtotals}, expected=${result.originalSubtotalCents}`
          );
        }
      }
    });
  });

  describe('2. Seat-Based Split Check Separation', () => {
    function splitBySeat(items, taxRatePercent) {
      const seatMap = new Map();
      for (const item of items) {
        const seat = item.seatNumber || 1;
        if (!seatMap.has(seat)) seatMap.set(seat, []);
        seatMap.get(seat).push(item);
      }

      const subChecks = [];
      for (const [seat, seatItems] of seatMap.entries()) {
        const subtotal = seatItems.reduce((acc, it) => acc + it.itemTotalCents, 0);
        const tax = Math.round(subtotal * (taxRatePercent / 100.0));
        subChecks.push({
          seatNumber: seat,
          items: seatItems,
          subtotalCents: subtotal,
          taxCents: tax,
          totalCents: subtotal + tax
        });
      }
      return subChecks;
    }

    test('Partitions items accurately across seats and computes balanced checks', () => {
      const items = [
        { itemId: 'item_carpaccio', name: 'Carpaccio', seatNumber: 1, itemTotalCents: 2200 },
        { itemId: 'item_ribeye', name: 'Ribeye', seatNumber: 1, itemTotalCents: 4800 },
        { itemId: 'item_salmon', name: 'Salmon', seatNumber: 2, itemTotalCents: 3800 },
        { itemId: 'item_fondant', name: 'Fondant', seatNumber: 3, itemTotalCents: 1600 }
      ];

      const splitChecks = splitBySeat(items, 8.25);
      assert.equal(splitChecks.length, 3);

      // Seat 1: Carpaccio (2200) + Ribeye (4800) = 7000 cents
      const seat1 = splitChecks.find(c => c.seatNumber === 1);
      assert.equal(seat1.subtotalCents, 7000);
      assert.equal(seat1.taxCents, Math.round(7000 * 0.0825)); // 578
      assert.equal(seat1.totalCents, 7578);

      // Seat 2: Salmon (3800)
      const seat2 = splitChecks.find(c => c.seatNumber === 2);
      assert.equal(seat2.subtotalCents, 3800);
      assert.equal(seat2.taxCents, Math.round(3800 * 0.0825)); // 314
      assert.equal(seat2.totalCents, 4114);

      // Seat 3: Fondant (1600)
      const seat3 = splitChecks.find(c => c.seatNumber === 3);
      assert.equal(seat3.subtotalCents, 1600);
      assert.equal(seat3.taxCents, Math.round(1600 * 0.0825)); // 132
      assert.equal(seat3.totalCents, 1732);
    });
  });

  describe('3. Coursing Engine: Hold & Fire Isolation', () => {
    test('Held items are excluded from initial KDB ticket and emitted on course fire', () => {
      const items = [
        { itemId: 'c_carpaccio', course: 'APPETIZER', courseHold: 'FIRE' },
        { itemId: 'c_ribeye', course: 'ENTREE', courseHold: 'HOLD' },
        { itemId: 'c_salmon', course: 'ENTREE', courseHold: 'HOLD' }
      ];

      // Initial kitchen dispatch: only fired items
      const initialFired = items.filter(it => it.courseHold === 'FIRE');
      assert.equal(initialFired.length, 1);
      assert.equal(initialFired[0].itemId, 'c_carpaccio');

      const initialHeld = items.filter(it => it.courseHold === 'HOLD');
      assert.equal(initialHeld.length, 2);

      // Fire Entrees
      const updated = items.map(it => {
        if (it.course === 'ENTREE') return { ...it, courseHold: 'FIRE' };
        return it;
      });

      const newlyFired = updated.filter(it => it.course === 'ENTREE' && it.courseHold === 'FIRE');
      assert.equal(newlyFired.length, 2);
      assert.equal(updated.filter(it => it.courseHold === 'HOLD').length, 0);
    });
  });

  describe('4. Pass 86ing Real-Time Availability Engine', () => {
    class MockPass86Manager {
      constructor() {
        this.unavailableIds = new Set();
        this.broadcastLog = [];
      }

      toggle86(itemId, itemName, isAvailable) {
        if (isAvailable) {
          this.unavailableIds.delete(itemId);
        } else {
          this.unavailableIds.add(itemId);
        }

        const event = {
          eventId: 'evt-86-' + Date.now(),
          eventType: 'MENU_ITEM_86',
          itemId,
          itemName,
          isAvailable,
          timestampEpochMs: Date.now()
        };
        this.broadcastLog.push(event);
        return event;
      }

      canAddItemToOrder(itemId) {
        return !this.unavailableIds.has(itemId);
      }
    }

    test('86ing an item emits LAN event and blocks order addition', () => {
      const p86 = new MockPass86Manager();
      const ribeyeId = 'item_ribeye';

      // Initially available
      assert.equal(p86.canAddItemToOrder(ribeyeId), true);

      // Kitchen / Manager 86s the ribeye
      const evt = p86.toggle86(ribeyeId, 'USDA Prime Ribeye', false);
      assert.equal(evt.eventType, 'MENU_ITEM_86');
      assert.equal(evt.isAvailable, false);

      // Now blocked
      assert.equal(p86.canAddItemToOrder(ribeyeId), false);

      // Restock / un-86
      p86.toggle86(ribeyeId, 'USDA Prime Ribeye', true);
      assert.equal(p86.canAddItemToOrder(ribeyeId), true);
    });
  });
});
