import axios from '../axios'

export default {
	//登录
	fetchLogin: (params) => axios.post('/login',params),
	//退出登录
	fetchLogout: (params) => axios.post('/logout',params),
	//获取站点配置信息
	fetchGlobalInfo: (params) => axios.get('/globalInfo',params),
	// 获取第三方登录信息
	fetchThirdPartyInfo: (params) => axios.get('/thirdParty/info',params),
	// 第三方登录绑定会员或直接登录
	fetchThirdPartyBind: (params) => axios.post('/thirdParty/bind',params),
	// 判断是否是国密算法 cmsmanager cmsadmin
	fetchLoginTypeData: (params) => axios.get('/cmsmanager/config/global/isSmEncrypt',params),
}