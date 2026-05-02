import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      "/velog/graphql": {
        target: "https://v2.velog.io",
        changeOrigin: true,
        secure: true,
        rewrite: () => "/graphql",
      },
    },
  },
});
