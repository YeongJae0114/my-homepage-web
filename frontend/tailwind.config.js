/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      fontFamily: {
        sans: ["Inter", "Pretendard", "ui-sans-serif", "system-ui", "sans-serif"],
        mono: ["JetBrains Mono", "SFMono-Regular", "Consolas", "monospace"],
      },
      colors: {
        surface: {
          950: "#070806",
          900: "#10110d",
          850: "#151811",
          800: "#1b1e16",
        },
      },
      boxShadow: {
        glow: "0 0 0 1px rgba(125, 211, 252, 0.14), 0 24px 80px rgba(0, 0, 0, 0.42)",
      },
      backgroundImage: {
        "tech-grid":
          "linear-gradient(rgba(255,255,255,.045) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,.045) 1px, transparent 1px)",
      },
    },
  },
  plugins: [],
};
