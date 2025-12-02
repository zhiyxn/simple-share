import { ref, computed } from 'vue'
import { 
  getMasterFileConfig, 
  parseFileConfig, 
  buildFileUrl as utilBuildFileUrl,
  buildPreviewUrl as utilBuildPreviewUrl,
  clearFileConfigCache
} from '@/utils/file'

interface FileConfig {
  id: number
  name: string
  storage: number
  master: boolean
  config: string
  remark: string
  // 可选的domain字段，从后端直接返回的顶层域名配置
  domain?: string
}

interface ParsedConfig {
  basePath?: string
  domain?: string
  pathPrefix?: string
  endpoint?: string
  bucket?: string
  accessKey?: string
  accessSecret?: string
}

const masterConfig = ref<FileConfig | null>(null)
const parsedConfig = ref<ParsedConfig>({})
const loading = ref(false)

export function useFileConfig() {
  // 获取主配置
  const getMasterConfig = async () => {
    if (masterConfig.value) {
      return masterConfig.value
    }

    try {
      loading.value = true
      const config = await getMasterFileConfig()
      if (config) {
        masterConfig.value = config
        // 解析配置JSON
        let configFromJson: ParsedConfig = {}
        if (config.config) {
          configFromJson = parseFileConfig(config.config)
        }
        
        // 优先使用后端返回的顶层 domain 字段，如果没有则使用 JSON 配置中的 domain
        parsedConfig.value = {
          ...configFromJson,
          domain: config.domain || configFromJson.domain
        }
        
        console.log('解析后的配置:', {
          topLevelDomain: config.domain,
          configJsonDomain: configFromJson.domain,
          finalDomain: parsedConfig.value.domain
        })
      }
      return masterConfig.value
    } catch (error) {
      console.error('获取主配置失败:', error)
      return null
    } finally {
      loading.value = false
    }
  }

  // 构建文件访问URL
  const buildFileUrl = async (filePath: string) => {
    return await utilBuildFileUrl(filePath, parsedConfig.value.domain)
  }

  // 构建文件预览URL（用于图片等）
  const buildPreviewUrl = async (fileId: string | number, filePath?: string) => {
    return await utilBuildPreviewUrl(fileId, filePath, parsedConfig.value.domain)
  }

  // 计算属性
  const domain = computed(() => parsedConfig.value.domain || '')
  const isLocalStorage = computed(() => masterConfig.value?.storage === 1)
  const isCloudStorage = computed(() => masterConfig.value?.storage && masterConfig.value.storage >= 10)

  // 初始化时获取配置
  const init = async () => {
    await getMasterConfig()
  }

  // 清除缓存
  const clearCache = () => {
    masterConfig.value = null
    parsedConfig.value = {}
    clearFileConfigCache()
  }

  return {
    masterConfig: computed(() => masterConfig.value),
    parsedConfig: computed(() => parsedConfig.value),
    loading: computed(() => loading.value),
    domain,
    isLocalStorage,
    isCloudStorage,
    getMasterConfig,
    buildFileUrl,
    buildPreviewUrl,
    init,
    clearCache
  }
}