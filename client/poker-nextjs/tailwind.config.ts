import type { Config } from "tailwindcss";

export default {
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        background: "var(--background)",
        foreground: "var(--foreground)",
        poker: {
          green: {
            900: "#1a472a",
            950: "#0d2818",
          },
          gold: "#ffd700",
          amber: {
            900: "#78350f",
          },
        },
      },
      fontFamily: {
        sans: ["Arial", "sans-serif"],
      },
      keyframes: {
        'bounce-in': {
          '0%': { transform: 'scale(0.3)', opacity: '0' },
          '50%': { transform: 'scale(1.05)' },
          '70%': { transform: 'scale(0.9)' },
          '100%': { transform: 'scale(1)', opacity: '1' },
        },
        'slide-in-right': {
          '0%': { transform: 'translateX(100%)', opacity: '0' },
          '100%': { transform: 'translateX(0)', opacity: '1' },
        },
        'wiggle': {
          '0%, 100%': { transform: 'rotate(-10deg)' },
          '50%': { transform: 'rotate(10deg)' },
        },
        'spin-slow': {
          '0%': { transform: 'rotate(0deg)' },
          '100%': { transform: 'rotate(360deg)' },
        },
        'spin-slow-reverse': {
          '0%': { transform: 'rotate(360deg)' },
          '100%': { transform: 'rotate(0deg)' },
        },
      },
      animation: {
        'bounce-in': 'bounce-in 0.6s ease-out',
        'slide-in-right': 'slide-in-right 0.3s ease-out',
        'wiggle': 'wiggle 1s ease-in-out infinite',
        'spin-slow': 'spin-slow 3s linear infinite',
        'spin-slow-reverse': 'spin-slow-reverse 3s linear infinite',
      },
    },
  },
  plugins: [],
} satisfies Config;
