import type { Config } from 'tailwindcss';

const config: Config = {
  content: [
    './src/pages/**/*.{js,ts,jsx,tsx,mdx}',
    './src/components/**/*.{js,ts,jsx,tsx,mdx}',
    './src/app/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          onyx: '#0D0D0D', // Primary dark canvas
          slate: '#1E2A38', // Secondary dark surface / card frames
          ivory: '#F7F5F1', // Light background
          linen: '#E8E4DC', // Structural borders / dividers
          amber: '#D4820A', // Primary CTA fill & interactive accent
          ink: '#1A1A1A', // Primary high-contrast text on light & amber fills
          white: '#FFFFFF',
        },
        status: {
          success: '#2D7D46', // NEW TICKET pill, sync confirmed
          alert: '#C0392B', // WAN interruption, error
          info: '#2563EB', // Info Blue (Kitchen module chip)
          pending: '#B8860B', // Pending Ochre (Handheld module chip)
        },
      },
      fontFamily: {
        sans: ['var(--font-inter)', 'system-ui', 'sans-serif'],
        display: ['var(--font-playfair)', 'Georgia', 'serif'],
      },
    },
  },
  plugins: [],
};

export default config;
