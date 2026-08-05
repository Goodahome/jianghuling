import request from './request'
import type { ApiResponse } from '@/types/api'

export async function uploadFile(file: File) {
  const form = new FormData()
  form.append('file', file)
  const res = await request.post('/files/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return (res.data as ApiResponse<{ url: string; fileId: string }>).data
}
