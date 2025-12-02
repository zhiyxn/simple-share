/**
 * 日期格式化工具函数
 */

/**
 * 格式化日期
 * @param date 日期对象、时间戳或日期字符串
 * @param format 格式化模式，默认 'YYYY-MM-DD HH:mm:ss'
 * @returns 格式化后的日期字符串
 */
export function formatDate(
  date: Date | number | string,
  format: string = 'YYYY-MM-DD HH:mm:ss'
): string {
  const d = new Date(date)

  // 检查日期是否有效
  if (isNaN(d.getTime())) {
    return ''
  }

  const year = d.getFullYear()
  const month = d.getMonth() + 1
  const day = d.getDate()
  const hours = d.getHours()
  const minutes = d.getMinutes()
  const seconds = d.getSeconds()

  // 替换格式中的占位符
  return format
    .replace(/YYYY/g, year.toString())
    .replace(/YY/g, (year % 100).toString().padStart(2, '0'))
    .replace(/MM/g, month.toString().padStart(2, '0'))
    .replace(/M/g, month.toString())
    .replace(/DD/g, day.toString().padStart(2, '0'))
    .replace(/D/g, day.toString())
    .replace(/HH/g, hours.toString().padStart(2, '0'))
    .replace(/H/g, hours.toString())
    .replace(/mm/g, minutes.toString().padStart(2, '0'))
    .replace(/m/g, minutes.toString())
    .replace(/ss/g, seconds.toString().padStart(2, '0'))
    .replace(/s/g, seconds.toString())
}

/**
 * 获取相对时间描述
 * @param date 日期对象、时间戳或日期字符串
 * @returns 相对时间描述，如 "刚刚"、"5分钟前" 等
 */
export function getRelativeTime(date: Date | number | string): string {
  const d = new Date(date)
  const now = new Date()
  const diff = now.getTime() - d.getTime()

  const minute = 60 * 1000
  const hour = minute * 60
  const day = hour * 24
  const week = day * 7
  const month = day * 30
  const year = day * 365

  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  } else if (diff < week) {
    return `${Math.floor(diff / day)}天前`
  } else if (diff < month) {
    return `${Math.floor(diff / week)}周前`
  } else if (diff < year) {
    return `${Math.floor(diff / month)}个月前`
  } else {
    return `${Math.floor(diff / year)}年前`
  }
}

/**
 * 检查日期是否为今天
 * @param date 日期对象、时间戳或日期字符串
 * @returns 是否为今天
 */
export function isToday(date: Date | number | string): boolean {
  const d = new Date(date)
  const today = new Date()

  return d.getDate() === today.getDate() &&
         d.getMonth() === today.getMonth() &&
         d.getFullYear() === today.getFullYear()
}

/**
 * 检查日期是否为昨天
 * @param date 日期对象、时间戳或日期字符串
 * @returns 是否为昨天
 */
export function isYesterday(date: Date | number | string): boolean {
  const d = new Date(date)
  const yesterday = new Date()
  yesterday.setDate(yesterday.getDate() - 1)

  return d.getDate() === yesterday.getDate() &&
         d.getMonth() === yesterday.getMonth() &&
         d.getFullYear() === yesterday.getFullYear()
}