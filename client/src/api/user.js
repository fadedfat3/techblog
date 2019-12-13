import request from '@/utils/request'
import api from '@/constant/api'
import qs from 'qs'
export function login (data) {
  return request({
    url: api.login,
    method: 'post',
    data: qs.stringify(data)
  })
}

export function registry (data) {
  return request({
    url: api.registry,
    method: 'post',
    data: qs.stringify(data)
  })
}

export function getInfo () {
  return request({
    url: api.userInfo,
    method: 'get'
  })
}

export function logout () {
  return request({
    url: api.logout,
    method: 'post'
  })
}

export function sendActivatedEmail (data) {
  return request({
    url: api.sendActivatedEmail,
    method: 'post',
    data: qs.stringify(data)
  })
}

export function activateEmail (data) {
  return request({
    url: '/user/email/activate?' + qs.stringify(data),
    method: 'get'
  })
}