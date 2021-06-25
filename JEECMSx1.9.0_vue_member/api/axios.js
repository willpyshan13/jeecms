import app from '../config.js'
import qs from 'qs'
import Fly from 'flyio/dist/npm/wx'
var fly = new Fly;

function initCookies() {
	var cookie = document.cookie,
		items = cookie.split(";"),
		keys = {};
		// console.log(cookie)
		items.forEach(function(item) {
			var kv = item.split('=');
			// console.log(kv)
			if(kv&&kv.length>1){
				// keys[$.trim(kv[0])] = $.trim(kv[1]);
				// keys[kv[0].trim()] = kv[1].trim();
				keys[kv[0].replace(/(^\s*)|(\s*$)/g, "")] = kv[1].replace(/(^\s*)|(\s*$)/g, "");
			}
		});
		// console.log(keys)
	return keys;
}
//配置请求基地址
fly.config.baseURL = app.baseUrl

fly.interceptors.request.use((config, promise) => {
	// initCookies()
  localStorage.needchangepassword = config.headers.needchangepassword
	var cookie = config.headers['Cookie'] || config.headers['Set-Cookie']
	if(config.url == '/login'){
		config.body = qs.stringify(config.body)
		config.headers = {
			'X-XSRF-TOKEN':initCookies()['X-XSRF-TOKEN']||initCookies()['XSRF-TOKEN']
		}
		return config;
	}
	//全局api拦截，需要做什么在这里处理
	let headersObj = {
		'JEECMS-Auth-Token': localStorage.getItem('JEECMS-Auth-Token'),
		'Redirect-Header': false,
		'X-XSRF-TOKEN':initCookies()['X-XSRF-TOKEN']||initCookies()['XSRF-TOKEN'],
		'Content-Type': 'application/json'
	}
	// Set-Cookie
	config.headers = headersObj
	return config;
})

//添加响应拦截器，响应拦截器会在then/catch处理之前执行
fly.interceptors.response.use(response => {
	// console.log(response)
		//这里拦截响应，根据具体业务定
		if (response.data.code == 302) {
			const dcRichAlert = uni.requireNativePlugin('DCloud-RichAlert')
			dcRichAlert.show({
				 position: 'center',
				 title: "提示",
				 titleColor: '#323232',
				 titleAlign:'left',
				 content: response.data.message,
				 contentAlign: 'center',
				 contentColor:'#323232',
				 buttons: [{title: '确认',titleColor: '#025bbf'}]
				}, result => {
				  switch (result.type) {
					case 'button':
						if(result.index==0){
							uni.reLaunch({
								url: '/pages/login/login'
							})
						}
						break;
					case 'backCancel':
						break;
				}
			});
		}
		return response.data
	},
	err => {
		return Promise.resolve(err)
	}
)

export default fly