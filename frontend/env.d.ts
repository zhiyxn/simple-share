/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_APP_TITLE: string
  readonly VITE_APP_DESCRIPTION: string
  readonly VITE_APP_WATERMARK_TEXT: string
  readonly VITE_API_BASE_URL: string
  readonly VITE_DEBUG_MODE: string
  readonly VITE_SHOW_DEBUG_TOOLS: string
  readonly VITE_LOG_LEVEL: string
  readonly VITE_PROXY_TARGET: string
  readonly VITE_APP_FOOTER_COPYRIGHT: string
  readonly VITE_APP_FOOTER_ICP: string
  readonly VITE_ENABLE_USER_REGISTRATION: string
  readonly VITE_FILE_BASE_URL: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
