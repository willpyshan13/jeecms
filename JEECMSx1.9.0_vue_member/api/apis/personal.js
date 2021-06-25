import axios from '../axios'

export default {
	// 个人信息
	fetchMemberinfo: (params) => axios.get('/member/memberinfo', params),
	// 修改个人资料
	fetchMemberinfoPut: (params) => axios.put('/member/memberinfo', params),
	// 修改扩展信息
	fetchMemberinfoCustom: (params) => axios.get('/member/memberinfo/pic', params),
	// 发送验证码
	fetchsendEmailMsg: (params) => axios.post('/register/sendEmailMsg', params),
	// 发送验证码
	fetchMailUnique: (params) => axios.get('/register/mail/unique', params),
	// 修改注册邮箱
	fetchMemberinfoEmail: (params) => axios.post('/member/memberinfo/email', params),
	// 修改密码
	fetchMemberinfoPstr: (params) => axios.post('/member/memberinfo/pstr', params),
	//验证码
	fetchSendEmailMsg: (params) => axios.post('/register/sendEmailMsg', params),
	// 验证码
	fetchAuthcode: (params) => axios.get('/register/mobile/authcode', params),
	// 邮箱验证验证码
	verifyEmailCode: (params) => axios.get('/register/mail/authcode', params),
	// 根据用户名或邮箱找回密码
	findpswd: (params) => axios.post('/register/rectrieve/key', params),
	
	// 我的信件
	//我的信件分页
	getLetter: (params) => axios.get('member/letter/page', params),
	//删除信件
	letterDel: (params) => axios.post('/member/letter/delete', params),
	// 全部清空信件
	letterClear: (params) => axios.post('member/letter/clear', params),
	// 信件详情
	letterDetail: (params) => axios.get('/letter/'+params),
	// 评分
	letterScore: (params) => axios.post('/member/letter/score',params),
	
	
	// 我的投稿
	// 获取我的投稿列表
	getTougao: (params) => axios.get('/member/memberinfo/mobile/contribute/page', params),
	// 是否允许投稿
	allowTougao: (params) => axios.get('/member/memberinfo/isContribute', params),
	// 可以投稿的栏目树形
	getChannelTree: (params) => axios.get('/channel/tree', params),
	// 新建一个投稿
	addNewTougao: (params) => axios.post('/member/memberinfo/contribute', params),
	// 查询单个投稿详情
	getTougaoDetails: (params) => axios.get('/member/memberinfo/contribute/' + params),
	// 修改投稿内容
	modifyTougao: (params) => axios.put('/member/memberinfo/contribute', params),
	// 删除单个投稿
	deleteTougao: (params) => axios.post('/member/memberinfo/contribute/delete', params),

	// 我的收藏
	// 获取我的收藏列表
	getCollections: (params) => axios.get('/member/usercollections/mobile/page', params),
	// 单个取消收藏
	cancelCollection: (params) => axios.get('/member/usercollections/cancel', params),
	// 批量取消收藏
	cancelCollections: (params) => axios.post('/member/usercollections/delete/delete', params),
	// 一键清空
	deleteAllCollections: (params) => axios.post('/member/usercollections/deleteAll/delete', params),


	// 我的点赞
	getMyLikes: (params) => axios.get('/member/memberinfo/moblielike', params),
	// 取消点赞
	cancelLike: (params) => axios.get('/member/memberinfo/quitlike', params),
	// 我的互动
	getMyInteraction: (params) => axios.get('/member/interaction/mobilePage', params),
	// 删除互动
	cancelInteraction: (params) => axios.get('/member/interaction/' + params),
	// 批量删除互动
	cancelInteractions: (params) => axios.post('/member/interaction/delete', params),
	// 一键清空互动
	deleteAllInteraction: (params) => axios.get('/member/interaction/clear', params),
	// 点赞
	upInteractions: (params) => axios.post('/usercomment/up', params),
	// 取消点赞
	cancelUpInteractions: (params) => axios.post('/usercomment/cancel/up', params),
	
	//我的资产
	getAssetDisplay: (params) => axios.get('/member/memberAssetDisplay/getAssetDisplayData',params),
	//收益统计
	getProfitCount: (params) => axios.get('/member/memberAssetDisplay/getProfitCount',params),
	//购买/打赏记录
	getPayRecordPage: (params) => axios.get('/member/memberAssetDisplay/getPayRecordPage',params),
	//收支明细
	getPayIncomDetailPage: (params) => axios.get('/member/memberAssetDisplay/getPayIncomDetailPage',params),
	//提现记录
	getWithdrawOrder: (params) => axios.get('/member/memberAssetDisplay/getWithdrawOrder',params),
	//获取默认绑定的微信和支付宝;
	accountInfo: (params) => axios.get('/member/withdraw/accountInfo',params),
	//点击提现
	applyWithdraw: (params) => axios.post('/member/withdraw/applyWithdraw',params),
	//获取提现的默认配置
	getPayConfig:(params) => axios.get('/member/memberAssetDisplay/getPayConfig',params),
}