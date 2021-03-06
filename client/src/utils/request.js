import { Message } from 'element-ui'
import axios from 'axios'
import store from '@/store'
import constant from '@/constant'
axios.defaults.baseURL = '/api'

// create an axios instance
const service = axios.create({
  // baseURL: '/api', // url = base url + request url
  // withCredentials: true,
  // send cookies when cross-domain requests
  timeout: 60000 // request timeout
})
service.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded'
// request interceptor
service.interceptors.request.use(
  config => {
    // do something before request is sent

    if (store.getters.token) {
      // let each request carry token
      // ['X-Token'] is a custom headers key
      // please modify it according to the actual situation
      config.headers[constant.token.HEADER_SESSION_TOKEN] = store.getters.token
    }

    return config
  },
  error => {
    // do something with request error
    console.log(error) // for debug
    return Promise.reject(error)
  }
)

// response interceptor
service.interceptors.response.use(
  /**
   * If you want to get http information such as headers or status
   * Please return  response => response
  */

  /**
   * Determine the request status by custom code
   * Here is just an example
   * You can also judge the status by HTTP Status Code
   */
  response => {
    const res = response.data

    // if the custom code is not 20000, it is judged as an error.
    if (res.code !== 20000) {
      Message({
        message: res.message || 'Error',
        type: 'error',
        duration: 5 * 1000
      })

      // 50000: invalid token;
      if (res.code === 50000) {
        // to re-login
        store.dispatch('user/relogin', true)
      }
      // 40100: user does not login
      if (res.code === 40100) {
        store.dispatch('user/relogin', false)
      }
      return Promise.reject(new Error(res.message || 'Error'))
    } else {
      return res
    }
  },
  error => {
    console.log('err' + error) // for debug
    Message({
      message: error.message,
      type: 'error',
      duration: 5 * 1000
    })
    return Promise.reject(error)
  }
)

export default service
