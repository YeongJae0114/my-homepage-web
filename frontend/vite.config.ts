import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      "/api": {
        target: "https://api.zerojae175-dev.shop",
        changeOrigin: true,
        secure: true,
      },
      "/velog/graphql": {
        target: "https://v2.velog.io",
        changeOrigin: true,
        secure: true,
        rewrite: () => "/graphql",
      },
    },
  },
});
