import { ref, watch, type Ref } from 'vue'
import { debounce } from 'lodash-es'
import { articleApi } from '@/api/article'
import type { Article, ArticleCreateData, ArticleUpdateData } from '@/types/article'
import type { Router } from 'vue-router'

export interface AutoSaveOptions {
  interval?: number
  debounceDelay?: number
  enabled?: boolean
  minLength?: number
  getData?: () => any
  maxSaveFrequency?: number
  router?: Router
}

export interface AutoSaveState {
  status: 'idle' | 'saving' | 'saved' | 'error'
  lastSavedAt: Date | null
  error: string | null
}

export function useAutoSave(options: AutoSaveOptions = {}) {
  const {
    interval = 30000,
    debounceDelay = 2000,
    enabled = true,
    minLength = 10,
    getData,
    maxSaveFrequency = 5000,
    router: providedRouter
  } = options

  const currentArticleId = ref<string>('')
  const autoSaveState = ref<AutoSaveState>({
    status: 'idle',
    lastSavedAt: null,
    error: null
  })

  const updateRouteWithArticleId = (articleId: string) => {
    if (!providedRouter) {
      return
    }

    const currentRoute = providedRouter.currentRoute.value
    const query = { ...currentRoute.query }
    const hash = currentRoute.hash
    let targetPath: string | null = null

    if (currentRoute.path === '/post-editor') {
      targetPath = `/advanced-editor/${articleId}`
    } else if (currentRoute.path === '/profile/articles/create') {
      targetPath = `/profile/articles/${articleId}/edit`
    } else if (currentRoute.path === '/admin/articles/create') {
      targetPath = `/admin/articles/${articleId}/edit`
    }

    if (targetPath && targetPath !== currentRoute.path) {
      providedRouter.replace({
        path: targetPath,
        query,
        hash
      })
    }
  }

  let autoSaveTimer: number | null = null
  let stopWatcher: (() => void) | null = null

  let lastSavedHash = ''
  let lastSaveTime = 0
  let isSaving = false

  const stableStringify = (value: any): string => {
    const seen = new WeakSet<any>()

    const serialize = (input: any): any => {
      if (input === null || input === undefined) {
        return input
      }

      if (input instanceof Date) {
        return input.toISOString()
      }

      const type = typeof input
      if (type === 'string' || type === 'number' || type === 'boolean') {
        return input
      }

      if (type !== 'object') {
        return String(input)
      }

      if (seen.has(input)) {
        return null
      }
      seen.add(input)

      if (Array.isArray(input)) {
        return input.map(item => serialize(item))
      }

      const keys = Object.keys(input).sort()
      const result: Record<string, any> = {}
      for (const key of keys) {
        result[key] = serialize(input[key])
      }
      return result
    }

    return JSON.stringify(serialize(value))
  }

  const generateContentHash = (data: Record<string, any>): string => {
    return btoa(encodeURIComponent(stableStringify(data)))
  }

  const extractArticleId = (payload: Article | undefined): string | null => {
    if (!payload) {
      return null
    }

    const candidates = [
      (payload as any)?.articleId,
      (payload as any)?.id
    ]

    const detected = candidates.find(value => value !== undefined && value !== null)
    return detected !== undefined && detected !== null ? detected.toString() : null
  }

  const isContentValid = (title: string, content: string): boolean => {
    const totalLength = (title?.trim().length || 0) + (content?.trim().length || 0)
    return totalLength >= minLength
  }

  const performAutoSave = async (
    data: {
      title: string
      content: string
      [key: string]: any
    },
    options: {
      force?: boolean
    } = {}
  ): Promise<Article | undefined> => {
    if (!enabled) {
      return undefined
    }

    if (isSaving) {
      console.log('Auto-save already in progress, skipping request')
      return undefined
    }

    const { title, content, ...otherData } = data

    if (!isContentValid(title, content)) {
      return undefined
    }

    const hashPayload = {
      title,
      content,
      ...otherData
    }
    const currentHash = generateContentHash(hashPayload)

    if (!options.force && currentHash === lastSavedHash) {
      console.log('Auto-save skipped because content hash is unchanged')
      return undefined
    }

    const now = Date.now()
    if (!options.force && now - lastSaveTime < maxSaveFrequency) {
      console.log('Auto-save throttled to avoid frequent requests')
      return undefined
    }

    try {
      isSaving = true
      autoSaveState.value.status = 'saving'
      autoSaveState.value.error = null
      lastSaveTime = now

      let result: Article | undefined

      if (currentArticleId.value) {
        const updateData: Partial<ArticleUpdateData> = {
          title,
          content,
          ...otherData
        }

        result = await articleApi.autosave.saveDraft(currentArticleId.value, updateData)
      } else {
        const createData: Partial<ArticleCreateData> = {
          title,
          content,
          status: 'draft' as any,
          tags: [],
          allowCopy: true,
          enableWatermark: false,
          visibility: 'public' as any,
          ...otherData
        }
        result = await articleApi.autosave.createDraft(createData)
      }

      const detectedId = extractArticleId(result)
      if (detectedId) {
        const previousId = currentArticleId.value
        currentArticleId.value = detectedId
        if (!previousId) {
          updateRouteWithArticleId(detectedId)
        }
      } else {
        console.warn('Auto-save did not return an article ID', result)
      }

      autoSaveState.value.status = 'saved'
      autoSaveState.value.lastSavedAt = new Date()
      lastSavedHash = currentHash

      console.log('Auto-save succeeded', {
        articleId: currentArticleId.value,
        title,
        contentLength: content.length
      })

      setTimeout(() => {
        if (autoSaveState.value.status === 'saved') {
          autoSaveState.value.status = 'idle'
        }
      }, 3000)

      return result
    } catch (error: any) {
      console.error('Auto-save failed', error)
      autoSaveState.value.status = 'error'

      if (error.response?.status === 401) {
        autoSaveState.value.error = 'Session expired, please sign in again'
      } else {
        autoSaveState.value.error = error?.message || 'Unable to save changes'
      }

      lastSaveTime = 0
    } finally {
      isSaving = false
    }

    return undefined
  }

  const debouncedSave = debounce(async () => {
    const data = getData ? getData() : null
    if (data) {
      await performAutoSave(data)
    }
  }, debounceDelay)

  const startAutoSave = (dataRef?: Ref<any>) => {
    if (!enabled) {
      return
    }

    stopAutoSave()

    if (dataRef) {
      stopWatcher = watch(
        dataRef,
        () => {
          debouncedSave()
        },
        { deep: true }
      )
    }

    autoSaveTimer = window.setInterval(async () => {
      const data = getData ? getData() : (dataRef?.value || null)
      if (data && autoSaveState.value.status !== 'saving') {
        await performAutoSave(data)
      }
    }, interval)
  }

  const stopAutoSave = () => {
    if (stopWatcher) {
      stopWatcher()
      stopWatcher = null
    }
    if (autoSaveTimer) {
      clearInterval(autoSaveTimer)
      autoSaveTimer = null
    }
  }

  const saveImmediately = async (data?: any) => {
    const dataToSave = data || (getData ? getData() : null)
    if (dataToSave) {
      return await performAutoSave(dataToSave, { force: true })
    }
    return undefined
  }

  const setArticleId = (id: string | null) => {
    currentArticleId.value = id || ''
  }

  return {
    autoSaveState,
    currentArticleId,
    setArticleId,
    startAutoSave,
    stopAutoSave,
    saveImmediately
  }
}
