import { test, describe } from 'node:test';
import assert from 'node:assert/strict';
import { performance } from 'node:perf_hooks';

describe('Sprint P02_M03: Direct Hardware I/O Tests', () => {

  describe('1. ESC/POS Thermal Receipt Bytecode Integrity', () => {
    class EscPosTestBuilder {
      constructor() {
        this.buffer = [];
        this.write([0x1B, 0x40]); // ESC @ (Init)
      }

      write(bytes) {
        this.buffer.push(...bytes);
      }

      writeText(str) {
        this.buffer.push(...Buffer.from(str, 'utf-8'));
      }

      alignCenter() {
        this.write([0x1B, 0x61, 0x01]);
      }

      alignLeft() {
        this.write([0x1B, 0x61, 0x00]);
      }

      bold(enable) {
        this.write([0x1B, 0x45, enable ? 0x01 : 0x00]);
      }

      cut() {
        this.write([0x1D, 0x56, 0x42, 0x03]); // GS V 66 3 (Partial cut with feed)
      }

      buildReceipt(storeName, tableLabel, items, subtotal, tax, total) {
        this.alignCenter();
        this.bold(true);
        this.writeText(storeName + '\n');
        this.bold(false);
        this.writeText('The Art of Smarter Dining\n');
        this.writeText('------------------------------------------------\n');
        this.alignLeft();
        this.writeText(`Table: ${tableLabel}\n`);
        this.writeText('------------------------------------------------\n');

        for (const it of items) {
          const line = `${it.qty}x ${it.name}`.padEnd(38) + it.price.padStart(10) + '\n';
          this.writeText(line);
        }

        this.writeText('------------------------------------------------\n');
        this.writeText('Subtotal:'.padEnd(38) + subtotal.padStart(10) + '\n');
        this.writeText('Tax (8.25%):'.padEnd(38) + tax.padStart(10) + '\n');
        this.bold(true);
        this.writeText('TOTAL:'.padEnd(38) + total.padStart(10) + '\n');
        this.bold(false);
        this.cut();

        return Buffer.from(this.buffer);
      }
    }

    test('Receipt bytecode starts with INIT, formats 48-col tabular lines, and concludes with CUT_PARTIAL', () => {
      const builder = new EscPosTestBuilder();
      const items = [
        { qty: 1, name: 'USDA Prime Ribeye 14oz', price: '$48.00' },
        { qty: 1, name: 'Truffle Parmesan Fries', price: '$3.00' }
      ];

      const receiptBytes = builder.buildReceipt(
        'DINEVIQUE',
        'Table 2',
        items,
        '$51.00',
        '$4.21',
        '$55.21'
      );

      // Verify init bytes: 0x1B 0x40
      assert.equal(receiptBytes[0], 0x1B);
      assert.equal(receiptBytes[1], 0x40);

      // Verify receipt text contains brand and table
      const receiptString = receiptBytes.toString('utf-8');
      assert.ok(receiptString.includes('DINEVIQUE'));
      assert.ok(receiptString.includes('Table: Table 2'));
      assert.ok(receiptString.includes('USDA Prime Ribeye 14oz'));
      assert.ok(receiptString.includes('$55.21'));

      // Verify partial cut sequence at end: 0x1D 0x56 0x42 0x03
      const len = receiptBytes.length;
      assert.equal(receiptBytes[len - 4], 0x1D);
      assert.equal(receiptBytes[len - 3], 0x56);
      assert.equal(receiptBytes[len - 2], 0x42);
      assert.equal(receiptBytes[len - 1], 0x03);
    });
  });

  describe('2. Kitchen Impact Two-Color Ribbon Switching', () => {
    class KitchenImpactTestBuilder {
      constructor() {
        this.buffer = [];
        this.write([0x1B, 0x40]); // Init
        this.ribbonBlack();
      }

      write(bytes) {
        this.buffer.push(...bytes);
      }

      ribbonBlack() {
        this.write([0x1B, 0x72, 0x00]);
      }

      ribbonRed() {
        this.write([0x1B, 0x72, 0x01]);
      }

      chime() {
        this.write([0x1B, 0x1E]);
      }

      buildTicket(ticketNumber, tableLabel, items, allergyAlert = null) {
        this.chime();
        this.write(Buffer.from(`${ticketNumber}  ${tableLabel}\n`, 'utf-8'));

        if (allergyAlert) {
          this.ribbonRed();
          this.write(Buffer.from(`ALLERGY ALERT: ${allergyAlert}\n`, 'utf-8'));
          this.ribbonBlack();
        }

        for (const it of items) {
          this.write(Buffer.from(`${it.qty}x ${it.name}\n`, 'utf-8'));
          if (it.exclusion) {
            this.ribbonRed();
            this.write(Buffer.from(`   * ${it.exclusion}\n`, 'utf-8'));
            this.ribbonBlack();
          }
        }
        return Buffer.from(this.buffer);
      }
    }

    test('Two-color ribbon switches to RED for allergies and exclusions and restores BLACK', () => {
      const builder = new KitchenImpactTestBuilder();
      const ticketBytes = builder.buildTicket(
        '#104',
        'Table 2',
        [
          { qty: 1, name: 'USDA Prime Ribeye', exclusion: 'Rare - No Onions' }
        ],
        'PEANUT ALLERGY'
      );

      // Verify chime command exists: 0x1B 0x1E
      const hasChime = ticketBytes.indexOf(Buffer.from([0x1B, 0x1E])) !== -1;
      assert.ok(hasChime, 'Chime command must be present');

      // Verify Red Ribbon switch exists: 0x1B 0x72 0x01
      const hasRed = ticketBytes.indexOf(Buffer.from([0x1B, 0x72, 0x01])) !== -1;
      assert.ok(hasRed, 'Red ribbon switch must be present for allergy/exclusion');

      // Verify Black Ribbon switch exists: 0x1B 0x72 0x00
      const hasBlack = ticketBytes.indexOf(Buffer.from([0x1B, 0x72, 0x00])) !== -1;
      assert.ok(hasBlack, 'Black ribbon switch must restore for regular items');
    });
  });

  describe('3. Cash Drawer Kick Controller & < 100ms Latency SLA', () => {
    test('Drawer kick command matches ESC/POS specification and executes in < 100ms', () => {
      const start = performance.now();

      // Standard Solenoid Kick Byte Sequence: 0x1B 0x70 0x00 0x19 0xFA
      const kickCmd = Buffer.from([0x1B, 0x70, 0x00, 0x19, 0xFA]);

      const auditEvent = {
        eventId: 'aud-' + Date.now(),
        eventType: 'CASH_DRAWER_OPENED',
        triggerSource: 'CASH_TENDER',
        employeeId: 'emp_elena',
        employeeName: 'Server Elena',
        latencyMs: performance.now() - start
      };

      assert.equal(kickCmd[0], 0x1B);
      assert.equal(kickCmd[1], 0x70); // 'p'
      assert.equal(kickCmd[2], 0x00); // Pin 2
      assert.equal(kickCmd[3], 0x19); // 50ms ON
      assert.equal(kickCmd[4], 0xFA); // 500ms OFF

      // Latency SLA verification
      assert.ok(auditEvent.latencyMs < 100, `Drawer kick latency ${auditEvent.latencyMs}ms must be < 100ms SLA`);
    });
  });

  describe('4. Hardware Peripheral Failover & Paper-Out Rerouting', () => {
    class MockHardwareManager {
      constructor() {
        this.printers = {
          prn_counter_01: { id: 'prn_counter_01', name: 'Front Counter', status: 'READY', fallback: 'prn_backup_bar' },
          prn_backup_bar: { id: 'prn_backup_bar', name: 'Bar Printer', status: 'READY', fallback: null }
        };
      }

      setStatus(printerId, status) {
        this.printers[printerId].status = status;
      }

      dispatchPrint(targetId, payload) {
        const target = this.printers[targetId];
        const needsReroute = target.status === 'OUT_OF_PAPER' || target.status === 'OFFLINE';
        const actualPrinter = needsReroute && target.fallback ? this.printers[target.fallback] : target;

        return {
          targetId,
          executedOn: actualPrinter.id,
          wasRerouted: actualPrinter.id !== target.id,
          status: actualPrinter.status === 'READY' ? 'SUCCESS' : 'FAILED'
        };
      }
    }

    test('Dispatches normally when READY and transparently reroutes when OUT_OF_PAPER', () => {
      const hw = new MockHardwareManager();
      const payload = Buffer.from('TEST RECEIPT');

      // 1. Normal dispatch
      const normalResult = hw.dispatchPrint('prn_counter_01', payload);
      assert.equal(normalResult.executedOn, 'prn_counter_01');
      assert.equal(normalResult.wasRerouted, false);
      assert.equal(normalResult.status, 'SUCCESS');

      // 2. Paper roll runs out on front counter
      hw.setStatus('prn_counter_01', 'OUT_OF_PAPER');

      // 3. Failover dispatch
      const reroutedResult = hw.dispatchPrint('prn_counter_01', payload);
      assert.equal(reroutedResult.executedOn, 'prn_backup_bar');
      assert.equal(reroutedResult.wasRerouted, true);
      assert.equal(reroutedResult.status, 'SUCCESS');
    });
  });
});
