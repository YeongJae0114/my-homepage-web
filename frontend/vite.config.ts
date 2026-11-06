import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig(({ mode }) => {
  // Local backend by default so a dev login never reaches production asset data.
  // To browse against production, put API_PROXY_TARGET=https://api.zerojae.cloud in frontend/.env.local.
  const apiProxyTarget = loadEnv(mode, process.cwd(), "").API_PROXY_TARGET || "http://localhost:8080";

  return {
    plugins: [react()],
    server: {
      proxy: {
        "/api": {
          target: apiProxyTarget,
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
  };
});
