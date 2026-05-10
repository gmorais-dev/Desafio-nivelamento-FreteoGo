import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";
import path from "node:path";

export default defineConfig({
  plugins: [react(), tailwindcss()],
  base: "/SISTEMA-FRETES/dashboard-fretes/",
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "src"),
    },
  },
  build: {
    outDir: "../../src/main/webapp/dashboard-fretes",
    emptyOutDir: true,
    cssCodeSplit: false,
    rollupOptions: {
      output: {
        entryFileNames: "dashboard-fretes.js",
        chunkFileNames: "dashboard-fretes-[hash].js",
        assetFileNames: (assetInfo) => {
          if (assetInfo.name && assetInfo.name.endsWith(".css")) {
            return "dashboard-fretes.css";
          }
          return "assets/[name]-[hash][extname]";
        },
      },
    },
  },
});
