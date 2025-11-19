/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_WALLET_API_URL: string
  // more env variables...
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
