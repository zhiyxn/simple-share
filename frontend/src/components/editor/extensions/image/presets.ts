export type ImageBorderStyle =
  | 'none'
  | 'rounded'
  | 'card'
  | 'shadow'
  | 'polaroid'
  | 'glass'

export interface ImageBorderStyleConfig {
  wrapper: Record<string, string>
  image: Record<string, string>
}

const BORDER_STYLE_MAP: Record<ImageBorderStyle, ImageBorderStyleConfig> = {
  none: {
    wrapper: {
      border: 'none',
      borderRadius: '0',
      padding: '0',
      background: 'transparent',
      boxShadow: 'none',
    },
    image: {
      borderRadius: '12px',
    },
  },
  rounded: {
    wrapper: {
      border: '1px solid rgba(148, 163, 184, 0.45)',
      borderRadius: '18px',
      padding: '8px',
      background: 'rgba(255, 255, 255, 0.9)',
      boxShadow: '0 12px 30px rgba(15, 23, 42, 0.08)',
    },
    image: {
      borderRadius: '14px',
    },
  },
  card: {
    wrapper: {
      border: '1px solid rgba(59, 130, 246, 0.35)',
      borderRadius: '22px',
      padding: '12px',
      background: '#ffffff',
      boxShadow: '0 18px 40px rgba(59, 130, 246, 0.18)',
    },
    image: {
      borderRadius: '18px',
    },
  },
  shadow: {
    wrapper: {
      border: 'none',
      borderRadius: '18px',
      padding: '0',
      background: 'transparent',
      boxShadow: '0 25px 55px -30px rgba(15, 23, 42, 0.45)',
    },
    image: {
      borderRadius: '18px',
    },
  },
  polaroid: {
    wrapper: {
      border: '1px solid rgba(15, 23, 42, 0.12)',
      borderRadius: '6px',
      padding: '12px 12px 24px 12px',
      background: '#fffdf7',
      boxShadow: '0 18px 50px rgba(15, 23, 42, 0.18)',
    },
    image: {
      borderRadius: '4px',
      boxShadow: '0 14px 28px rgba(15, 23, 42, 0.12)',
    },
  },
  glass: {
    wrapper: {
      border: '1px solid rgba(255, 255, 255, 0.4)',
      borderRadius: '24px',
      padding: '10px',
      background: 'linear-gradient(135deg, rgba(255,255,255,0.45), rgba(209, 233, 255, 0.35))',
      boxShadow: '0 25px 60px -35px rgba(59, 130, 246, 0.55)',
      backdropFilter: 'blur(16px)',
    },
    image: {
      borderRadius: '18px',
    },
  },
}

export const imageBorderStyleOptions: Array<{ label: string; value: ImageBorderStyle }> = [
  { label: '无边框', value: 'none' },
  { label: '柔和圆角', value: 'rounded' },
  { label: '卡片投影', value: 'card' },
  { label: '炫酷阴影', value: 'shadow' },
  { label: '复古拍立得', value: 'polaroid' },
  { label: '玻璃质感', value: 'glass' },
]

export const getImageBorderStyleConfig = (style?: ImageBorderStyle): ImageBorderStyleConfig => {
  return BORDER_STYLE_MAP[style ?? 'none']
}
