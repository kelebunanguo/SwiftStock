/**
 * 时间格式化工具函数
 */

/**
 * 格式化日期时间为 YYYY/MM/DD HH:mm:ss 格式
 * @param {string|Date} dateTime - 日期时间字符串或Date对象
 * @returns {string} 格式化后的时间字符串
 */
export function formatDateTime(dateTime) {
  if (!dateTime) return ''

  const date = new Date(dateTime)

  // 检查日期是否有效
  if (isNaN(date.getTime())) return ''

  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')

  return `${year}/${month}/${day} ${hours}:${minutes}:${seconds}`
}

/**
 * 格式化日期为 YYYY/MM/DD HH:mm 格式（不含秒）
 * @param {string|Date} dateTime - 日期时间字符串或Date对象
 * @returns {string} 格式化后的时间字符串
 */
export function formatDate(dateTime) {
  if (!dateTime) return ''

  const date = new Date(dateTime)

  // 检查日期是否有效
  if (isNaN(date.getTime())) return ''

  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')

  return `${year}/${month}/${day} ${hours}:${minutes}`
}