<template>
	<view class="content index">
		<web-view :src="$baseUrl" @message="handleMessage"></web-view>
	</view>
</template>

<script>
	export default {
		name: 'index',
		data() {
			return {
				url:''
			};
		},
		methods:{
			// getUrlArg(name) {
			// 	//key存在先通过search取值如果取不到就通过hash来取
			// 	var url = window.location.hash.split("/")[1];
			// 	// 正则筛选地址栏
			// 	var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)")
			// 	// 匹配目标参数
			// 	var result = url.substr(1).match(reg);
			// 	if(result){
			// 		return decodeURIComponent(result[2])
			// 	}else{
			// 		return false
			// 	}
			// }
		},
		props: {
			
		},
		onShow() {
			// if(this.getUrlArg('parentId')){
			// 	this.url= this.$baseUrl+'/content-commentDetails.htm?contentId='+this.getUrlArg('contentId')+'&parentId='+this.getUrlArg('parentId')
			// }else if(this.getUrlArg('contentId')){
			// 	this.url= this.$baseUrl+'/content-commentList.htm?contentId='+this.getUrlArg('contentId')
			// }else if(this.getUrlArg('urll')){
			// 	this.url=this.getUrlArg('urll')
			// }else{
			// 	this.url=this.$baseUrl
			// }
		}
	};
</script>

<style lang="scss">
	.content.index {
		.title {
			height: 1000upx;
		}

		uni-video {
			width: 100%;
		}

		.top {
			width: 100upx;
			height: 100upx;
			background: #00BFFF;
			position: fixed;
			right: 20upx;
			top: 80%;
			color: #FFFFFF;
			text-align: center;
		}
	}
</style>
