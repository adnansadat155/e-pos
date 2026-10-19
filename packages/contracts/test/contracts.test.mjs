import { test } from 'node:test';
import assert from 'node:assert';
import { DineviqueBrandTokens } from '../src/tokens.ts';

test('Dinevique Brand Tokens are accurately defined', () => {
  assert.strictEqual(DineviqueBrandTokens.colors.brandOnyx, '#0D0D0D');
  assert.strictEqual(DineviqueBrandTokens.colors.brandAmber, '#D4820A');
  assert.strictEqual(DineviqueBrandTokens.colors.brandSlate, '#1E2A38');
  assert.strictEqual(DineviqueBrandTokens.colors.brandInk, '#1A1A1A');
  assert.strictEqual(DineviqueBrandTokens.colors.brandIvory, '#F7F5F1');
});

test('Brand Amber accessibility guardrail enforces Brand Ink pairing', () => {
  // Prohibited: amber text on white/ivory
  assert.strictEqual(DineviqueBrandTokens.accessibility.isValidAmberUsage(false, '#FFFFFF'), false);
  // Allowed: amber button fill with Brand Ink text
  assert.strictEqual(DineviqueBrandTokens.accessibility.isValidAmberUsage(true, '#1A1A1A'), true);
});
