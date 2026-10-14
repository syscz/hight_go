/**
 * 媒体资源 URL 统一处理
 * 后端返回相对路径（/uploads/...）：开发环境由 vite 代理转发，生产环境 nginx 同源直出，
 * 前端一律使用相对地址，避免把部署地址硬编码进页面
 */
export function resolveMediaUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return url.startsWith('/') ? url : '/' + url
}

/**
 * 网格卡片展示图：图片用缩略图封面，视频优先封面帧，其次转码后 mp4（可用 #t= 取帧），最后原文件
 */
export function tileImageSrc(item) {
  if (!item) return ''
  if (item.mediaType === 'video') {
    return resolveMediaUrl(item.coverImage)
  }
  return resolveMediaUrl(item.coverImage || item.filePath)
}

/**
 * 无封面视频的取帧地址（#t=1 让浏览器直接渲染 1 秒处画面，preload=metadata 时开销很小）
 */
export function videoFrameSrc(item) {
  const base = item.videoMp4Path || item.filePath
  return resolveMediaUrl(base) ? resolveMediaUrl(base) + '#t=1' : ''
}

/**
 * 视频播放地址：优先转码后的 mp4（faststart，边下边播），回落原文件
 */
export function videoPlaySrc(item) {
  return resolveMediaUrl(item.videoMp4Path || item.filePath)
}

export function formatDuration(seconds) {
  if (seconds == null || seconds === '' || isNaN(seconds) || seconds < 0) return ''
  const s = Math.round(Number(seconds))
  const h = Math.floor(s / 3600)
  const m = Math.floor((s % 3600) / 60)
  const sec = s % 60
  const mm = String(m).padStart(2, '0')
  const ss = String(sec).padStart(2, '0')
  return h > 0 ? `${h}:${mm}:${ss}` : `${m}:${ss}`
}
