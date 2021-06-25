import axios from '../axios'

export default {
  // 注册
  fetchRegister: (params) => axios.post('/register',params),
  // 邮箱注册
  fetchEmailCode:(params) => axios.post('/register/mail/unique',params),
  // 会员字段
  getModel:(params) => axios.get('/register/addition/model',params),
  // 获取邮箱验证码
  getEmailCode:(data) => axios.post('/register/sendEmailMsg',data),
  // 获取手机验证码
  getTelephoneCode:(data) => axios.post('/register/sendPhoneMsg',data),
  // 判断是否需要图形验证
  judgeCapatch:(data) => axios.get('/systemInfo',data),
  // 获取图形验证码
  genCapatch:(data) => axios.get('/common/kaptcha',data)
  
}