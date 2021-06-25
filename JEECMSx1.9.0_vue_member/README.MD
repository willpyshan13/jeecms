# 目录说明

## 目录结构

┌─apis            	    接口目录
├─common            		公共目录
├─components            uni-app组件目录
│  └─comp-a.vue         可复用的a组件
├─hybrid                存放本地网页的目录，详见
├─platforms             存放各平台专用页面的目录，详见
├─pages                 业务页面文件存放的目录
│  ├─index
│  │  └─index.vue       index页面
│  └─list
│     └─list.vue        list页面
├─static                存放应用引用静态资源（如图片、视频等）的目录，注意：静态资源只能存放于此
├─store                	vuex存储目录
├─wxcomponents          存放小程序组件的目录，详见
├─main.js               Vue初始化入口文件
├─App.vue               应用配置，用来配置App全局样式以及监听 应用生命周期
├─manifest.json         配置应用名称、appid、logo、版本等打包信息，详见
└─pages.json            配置页面路由、导航条、选项卡等页面类信息，详见

## hybrid目录

```
	<web-view src="/hybrid/html/local.html"></web-view>
```

[webView](https://uniapp.dcloud.io/component/web-view)
[webView调用uniapi](https://ask.dcloud.net.cn/article/35083)

## 条件编译

	APP-PLUS、APP-PLUS-NVUE、MP-WEIXIN、H5、MP、MP-BAIDU、MP-ALIPAY
	
```
	//#ifdef H5  
	this.titleHeight = 44  
	//#endif  
```

```
	// #ifndef H5  
	console.log("这段代码编译到非H5平台");  
	// #endif  
```

## css变量

.fixed{
    bottom:var(--window-bottom)
}

## 第三方登录

[微博](https://open.weibo.com/wiki/Weibo-JS_V2)
[微信](https://open.weixin.qq.com/cgi-bin/frame?t=home/web_tmpl)
[QQ](https://open.qq.com/reg)