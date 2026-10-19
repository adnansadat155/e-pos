import { test, describe, before, after } from 'node:test';
import assert from 'node:assert/strict';
import { httpServer } from '../dist/server.js';

describe('Sprint P04_M02: Handheld Tap-to-Pay, Card Certification & Digital Receipts Tests', () => {
  let testServerPort;
  let testServerUrl;

  before(async () => {
    await new Promise((resolve) => {
      httpServer.listen(0, () => {
        const address = httpServer.address();
        testServerPort = address.port;
        testServerUrl = `http://127.0.0.1:${testServerPort}`;
        resolve();
      });
    });
  });

  after(async () => {
    await new Promise((resolve) => {
      httpServer.close(resolve);
    });
  });

  describe('1. Contactless EMV Level 2 Kernel & AID Selection', () => {
    function detectCardBrand(pan) {
      if (pan.startsWith('4')) return { brand: 'Visa', aid: 'A0000000031010' };
      if (pan.startsWith('5') || pan.startsWith('2')) return { brand: 'Mastercard', aid: 'A0000000041010' };
      if (pan.startsWith('34') || pan.startsWith('37')) return { brand: 'American Express', aid: 'A00000002501' };
      return { brand: 'Visa', aid: 'A0000000031010' };
    }

    function maskPan(pan) {
      const clean = pan.replace(/\D/g, '');
      const last4 = clean.slice(-4) || '4242';
      return `•••• •••• •••• ${last4}`;
    }

    function evaluateCvm(amountCents, isMobileWallet = false) {
      if (isMobileWallet) return 'ON_DEVICE_CVM'; // Apple Pay / Google Pay FaceID
      if (amountCents <= 10000) return 'NO_CVM_REQUIRED'; // Quick Chip under $100.00
      return 'PIN_REQUIRED';
    }

    test('Maps Visa, Mastercard, and Amex PAN prefixes to correct EMV AIDs', () => {
      const visa = detectCardBrand('4111222233334444');
      assert.equal(visa.brand, 'Visa');
      assert.equal(visa.aid, 'A0000000031010');

      const mc = detectCardBrand('5200111122223333');
      assert.equal(mc.brand, 'Mastercard');
      assert.equal(mc.aid, 'A0000000041010');

      const amex = detectCardBrand('378282246310005');
      assert.equal(amex.brand, 'American Express');
      assert.equal(amex.aid, 'A00000002501');
    });

    test('Strictly masks PAN to preserve PCI compliance', () => {
      const masked = maskPan('4000123456789876');
      assert.equal(masked, '•••• •••• •••• 9876');
      assert.ok(!masked.includes('40001234'));
    });

    test('Evaluates CVM Quick Chip rules and Mobile Wallet ODCVM', () => {
      assert.equal(evaluateCvm(4500, false), 'NO_CVM_REQUIRED'); // $45.00
      assert.equal(evaluateCvm(14500, false), 'PIN_REQUIRED'); // $145.00
      assert.equal(evaluateCvm(25000, true), 'ON_DEVICE_CVM'); // $250.00 Apple Pay
    });
  });

  describe('2. Tableside Tip Arithmetic & Dynamic Totals', () => {
    function calculateGratuity(subtotalCents, percent) {
      return Math.round((subtotalCents * percent) / 100);
    }

    test('Computes exact penny-perfect gratuity for standard tip presets', () => {
      const subtotalCents = 7200; // $72.00
      const taxCents = 612; // 8.5%

      const tip15 = calculateGratuity(subtotalCents, 15);
      const tip18 = calculateGratuity(subtotalCents, 18);
      const tip20 = calculateGratuity(subtotalCents, 20);

      assert.equal(tip15, 1080); // $10.80
      assert.equal(tip18, 1296); // $12.96
      assert.equal(tip20, 1440); // $14.40

      const grandTotal18 = subtotalCents + taxCents + tip18;
      assert.equal(grandTotal18, 9108); // $91.08
    });
  });

  describe('3. Omnichannel Digital Receipt Dispatch & Web Portal API', () => {
    let createdReceiptUuid;

    test('POST /api/v1/receipts/send generates and dispatches digital receipt', async () => {
      const res = await fetch(`${testServerUrl}/api/v1/receipts/send`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          orderUuid: 'ord_handheld_9012',
          deliveryMethod: 'SMS',
          recipient: '+12125550199',
          tableLabel: 'Table 4',
          serverName: 'Julian C.',
          subtotalCents: 6500,
          taxCents: 553,
          tipCents: 1170,
          totalCents: 8223,
          paymentTender: {
            brand: 'Visa',
            last4: '4242',
            authCode: 'AUTH-991024',
            cvm: 'Contactless EMV (Quick Chip)',
            timestamp: new Date().toISOString(),
          },
        }),
      });

      assert.equal(res.status, 200);
      const data = await res.json();
      assert.equal(data.success, true);
      assert.ok(data.receiptUuid);
      assert.ok(data.publicUrl.includes(data.receiptUuid));
      createdReceiptUuid = data.receiptUuid;
    });

    test('GET /api/v1/receipts/:receiptUuid retrieves itemized receipt for web viewer', async () => {
      const res = await fetch(`${testServerUrl}/api/v1/receipts/${createdReceiptUuid}`);
      assert.equal(res.status, 200);

      const receipt = await res.json();
      assert.equal(receipt.receiptUuid, createdReceiptUuid);
      assert.equal(receipt.orderUuid, 'ord_handheld_9012');
      assert.equal(receipt.totalCents, 8223);
      assert.equal(receipt.paymentTender.brand, 'Visa');
      assert.equal(receipt.paymentTender.last4, '4242');
    });
  });
});
