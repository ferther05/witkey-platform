import request from './request'

/** 上传：bizType 决定权限（AVATAR/TASK 公开，DELIVER 只给订单双方，REPORT 只给上传人） */
export const uploadFile = (file, bizType = 'TASK', bizId) => {
  const form = new FormData()
  form.append('file', file)
  form.append('bizType', bizType)
  if (bizId != null) form.append('bizId', bizId)
  return request.post('/file/upload', form)
}

export const getFileMeta = (id) => request.get(`/file/${id}`)

export const adminListFiles = (params) => request.get('/admin/file', { params })

export const adminDeleteFile = (id) => request.delete(`/admin/file/${id}`)
