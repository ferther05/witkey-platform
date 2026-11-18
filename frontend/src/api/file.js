// api/file.js：接口：文件上传、元信息、后台文件管理
import request from './request'

/** 上传：bizType 决定权限（AVATAR/TASK 公开，DELIVER 只给订单双方，REPORT 只给上传人） */
export const uploadFile = (file, bizType = 'TASK', bizId) => {
  const form = new FormData()
  form.append('file', file)
  form.append('bizType', bizType)
  if (bizId != null) form.append('bizId', bizId)
  return request.post('/file/upload', form)
}

// 接口：GET /file/${id}
export const getFileMeta = (id) => request.get(`/file/${id}`)

// 接口：GET /admin/file
export const adminListFiles = (params) => request.get('/admin/file', { params })

// 接口：DELETE /admin/file/${id}
export const adminDeleteFile = (id) => request.delete(`/admin/file/${id}`)
