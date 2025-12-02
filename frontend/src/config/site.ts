const envSiteTitle = (import.meta.env.VITE_APP_TITLE ?? '').trim()
const envSiteDescription = (import.meta.env.VITE_APP_DESCRIPTION ?? '').trim()
const envWatermarkText = (import.meta.env.VITE_APP_WATERMARK_TEXT ?? '').trim()

const fallbackDocumentTitle =
  typeof document !== 'undefined' && typeof document.title === 'string'
    ? document.title.trim()
    : ''

const fallbackHost =
  typeof window !== 'undefined' && typeof window.location?.hostname === 'string'
    ? window.location.hostname.trim()
    : ''

const defaultSiteTitle = envSiteTitle || fallbackDocumentTitle || fallbackHost || 'Site'
const defaultSiteDescription = envSiteDescription || ''
const defaultWatermarkText = envWatermarkText || defaultSiteTitle

const sanitizedTitle = defaultSiteTitle.replace(/\s+/g, '')
const defaultInitial =
  sanitizedTitle.charAt(0) ||
  (fallbackHost ? fallbackHost.charAt(0).toUpperCase() : 'S')

export const siteDefaults = Object.freeze({
  title: defaultSiteTitle,
  description: defaultSiteDescription,
  watermarkText: defaultWatermarkText,
  initial: defaultInitial
})

export const SITE_TITLE = siteDefaults.title
export const SITE_DESCRIPTION = siteDefaults.description
export const SITE_WATERMARK_TEXT = siteDefaults.watermarkText
export const SITE_INITIAL = siteDefaults.initial
