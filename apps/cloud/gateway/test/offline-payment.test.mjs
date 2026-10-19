import { test, describe, before, after } from 'node:test';
import assert from 'node:assert/strict';
import crypto from 'node:crypto';
import { httpServer } from '../dist/server.js';

describe('Sprint P04_M01: Store-and-Forward Offline Payments & AES-256 GCM Tests', () => {
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

  describe('1. AES-256 GCM Cryptographic Security Engine', () => {
    const key = crypto.randomBytes(32); // 256-bit symmetric key

    function encryptPayload(plaintext) {
      const iv = crypto.randomBytes(12); // 96-bit GCM IV
      const cipher = crypto.createCipheriv('aes-256-gcm', key, iv);
      const encrypted = Buffer.concat([cipher.update(plaintext, 'utf8'), cipher.final()]);
      const tag = cipher.getAuthTag(); // 128-bit authentication tag
      return Buffer.concat([iv, encrypted, tag]).toString('base64');
    }

    function decryptPayload(ciphertextBase64) {
      const buf = Buffer.from(ciphertextBase64, 'base64');
      const iv = buf.subarray(0, 12);
      const tag = buf.subarray(buf.length - 16);
      const ciphertext = buf.subarray(12, buf.length - 16);

      const decipher = crypto.createDecipheriv('aes-256-gcm', key, iv);
      decipher.setAuthTag(tag);
      return decipher.update(ciphertext, undefined, 'utf8') + decipher.final('utf8');
    }

    test('Encrypts and decrypts cardholder payload with byte-level fidelity', () => {
      const sensitiveCardData = JSON.stringify({
        pan: '4111111111114242',
        expiry: '12/28',
        cardholder: 'Eleanor Vance',
        track2: '4111111111114242=2812201000000',
      });

      const encrypted = encryptPayload(sensitiveCardData);
      assert.ok(encrypted.length > 0);
      assert.notEqual(encrypted, sensitiveCardData);

      const decrypted = decryptPayload(encrypted);
      assert.equal(decrypted, sensitiveCardData);
    });

    test('Detects ciphertext tampering via GCM authentication tag failure', () => {
      const sensitiveData = '4000123456789010=2901101';
      const encrypted = encryptPayload(sensitiveData);

      // Tamper with middle byte
      const rawBuf = Buffer.from(encrypted, 'base64');
      rawBuf[15] = (rawBuf[15] ^ 0xff); // flip bits
      const tamperedBase64 = rawBuf.toString('base64');

      assert.throws(() => {
        decryptPayload(tamperedBase64);
      }, /Unsupported state or unable to authenticate data/);
    });
  });

  describe('2. Risk Ceilings & Floor Limit Enforcement Logic', () => {
    const MAX_SINGLE_TRANSACTION_CENTS = 15000; // $150.00
    const MAX_CUMULATIVE_RISK_CENTS = 500000; // $5,000.00

    function evaluateOfflineRisk(amountCents, currentQueuedCents) {
      if (amountCents > MAX_SINGLE_TRANSACTION_CENTS) {
        return { approved: false, reason: 'EXCEEDS_SINGLE_TRANSACTION_LIMIT' };
      }
      if (currentQueuedCents + amountCents > MAX_CUMULATIVE_RISK_CENTS) {
        return { approved: false, reason: 'EXCEEDS_CUMULATIVE_RISK_LIMIT' };
      }
      return { approved: true, reason: 'APPROVED_OFFLINE' };
    }

    test('Approves offline transaction under single and cumulative limits', () => {
      const result = evaluateOfflineRisk(4800, 120000); // $48.00 check, $1,200 currently queued
      assert.equal(result.approved, true);
      assert.equal(result.reason, 'APPROVED_OFFLINE');
    });

    test('Rejects offline transaction exceeding single floor limit of $150.00', () => {
      const result = evaluateOfflineRisk(15001, 10000); // $150.01 check
      assert.equal(result.approved, false);
      assert.equal(result.reason, 'EXCEEDS_SINGLE_TRANSACTION_LIMIT');
    });

    test('Rejects offline transaction breaching cumulative risk ceiling of $5,000.00', () => {
      const result = evaluateOfflineRisk(5000, 498000); // $50.00 check when $4,980 already queued
      assert.equal(result.approved, false);
      assert.equal(result.reason, 'EXCEEDS_CUMULATIVE_RISK_LIMIT');
    });
  });

  describe('3. Cloud Payment Gateway Batch Settlement API', () => {
    const batchId = Date.now();
    const payments = [
      {
        paymentUuid: `pay_test_off_1_${batchId}`,
        clientOrderUuid: `ord_test_off_1_${batchId}`,
        amountCents: 4200,
        tipCents: 800,
        encryptedCardPayload: 'b64_dummy_payload_1',
        cardLast4: '4242',
        cardBrand: 'Visa',
        capturedAtEpochMs: Date.now() - 3600000,
      },
      {
        paymentUuid: `pay_test_off_2_${batchId}`,
        clientOrderUuid: `ord_test_off_2_${batchId}`,
        amountCents: 8900,
        tipCents: 1500,
        encryptedCardPayload: 'b64_dummy_payload_2',
        cardLast4: '1111',
        cardBrand: 'Mastercard',
        capturedAtEpochMs: Date.now() - 1800000,
      },
      {
        paymentUuid: `pay_test_off_3_${batchId}`,
        clientOrderUuid: `ord_test_off_3_${batchId}`,
        amountCents: 3100,
        tipCents: 500,
        encryptedCardPayload: 'b64_dummy_payload_3',
        cardLast4: '9876',
        cardBrand: 'Amex',
        capturedAtEpochMs: Date.now() - 900000,
      },
    ];

    test('POST /api/v1/payments/batch-settle processes batch replay upon WAN restore', async () => {
      const res = await fetch(`${testServerUrl}/api/v1/payments/batch-settle`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          restaurantId: 'rest_flagship_01',
          terminalId: 'term_pos_master_01',
          payments,
        }),
      });

      assert.equal(res.status, 200);
      const data = await res.json();
      assert.equal(data.success, true);
      assert.equal(data.settledCount, 3);
      assert.equal(data.duplicateCount, 0);
      assert.equal(data.totalProcessed, 3);
    });

    test('POST /api/v1/payments/batch-settle strictly deduplicates on repeated replay (0 double charges)', async () => {
      const res = await fetch(`${testServerUrl}/api/v1/payments/batch-settle`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          restaurantId: 'rest_flagship_01',
          terminalId: 'term_pos_master_01',
          payments,
        }),
      });

      assert.equal(res.status, 200);
      const data = await res.json();
      assert.equal(data.success, true);
      assert.equal(data.settledCount, 0);
      assert.equal(data.duplicateCount, 3);
      assert.equal(data.totalProcessed, 3);
    });

    test('POST /api/v1/payments/process handles instant online authorization', async () => {
      const res = await fetch(`${testServerUrl}/api/v1/payments/process`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          restaurantId: 'rest_flagship_01',
          terminalId: 'term_pos_master_01',
          clientOrderUuid: `ord_online_${Date.now()}`,
          amountCents: 6500,
          tipCents: 1200,
          cardLast4: '0005',
          cardBrand: 'Visa',
        }),
      });

      assert.equal(res.status, 200);
      const data = await res.json();
      assert.equal(data.success, true);
      assert.equal(data.payment.amountCents, 6500);
      assert.equal(data.payment.settlementStatus, 'SETTLED');
    });

    test('GET /api/v1/payments/stats returns aggregated financial telemetry', async () => {
      const res = await fetch(`${testServerUrl}/api/v1/payments/stats`);
      assert.equal(res.status, 200);

      const stats = await res.json();
      assert.ok(stats.totalPaymentsCount >= 4);
      assert.ok(stats.offlineAuthorizedCount >= 3);
      assert.ok(stats.onlineAuthorizedCount >= 1);
      assert.ok(stats.totalSettledCents > 0);
    });
  });
});
