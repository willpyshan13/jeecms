<template>
	<view class="content thirdLogin">
		<view class="back">
			<uni-icons class="iconfont iconfanhui" @click='backLogin()'></uni-icons>
		</view>
		<view class="third-cont">
			<view class="third-header" v-if="isCelerity">
				<text @click="onRapid()" class="pitch-on">快速登录</text>
				<text @click="onNoRapid()" class="heder-two">绑定账户</text>
			</view>
			<view class="third-header" v-else>
				<text @click="onRapid()">快速登录</text>
				<text @click="onNoRapid()" class="heder-two pitch-on">绑定账户</text>
			</view>
			<view v-if="isCelerity" class="celerity">
				<view class="Regular celerity-header">您在JEECMS新闻网的用户名为</view>
				<input type="text" placeholder="请输入昵称" v-model="rapidUsername" class="Medium celerity-input"/>
				<button class="uni-button third-btn" :disabled="!rapidUsername" @click="rapidGo">直接进入</button>
			</view>
			<view v-if="!isCelerity" class="bound">
				<input type="text" v-model="boundUsername" class="Medium bound-input"/>
				<input type="password" v-model="boundPsw" class="Medium bound-input"/>
				<button class="uni-button bound-btn" :disabled="!boundUsername||!boundPsw" @click="boundGo">绑定并登录</button>
			</view>
		</view>
	</view>
</template>

<script>
	import {
		desEncrypt
	} from '../../common/js/util.js'
	export default {
		name: 'thirdLogin',
		data() {
			return {
				thirdId: '',
				isCelerity: true,
				thirdData: {},
				rapidUsername: '',
				boundPsw: '',
				boundUsername: ''
			};
		},
		methods: {
			getUrlArg(name) {
				// console.log("search:"+window.location.search)
				// console.log("search /:"+window.location.hash.split("/")[1])
				// console.log("search ?:"+window.location.hash.split("?")[1])
				//key存在先通过search取值如果取不到就通过hash来取
				var url = window.location.hash.split("?")[1];
				// 正则筛选地址栏
				// var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)")
				// 匹配目标参数

				var index = url.indexOf("=");
				var result = url.substr(index + 1,url.length);
				if (result) {
					// this.thirdId = decodeURIComponent(result[2])
					this.thirdId = result
					this.fetchThirdPartyInfo()
				} else {
					this.$message('获取用户信息失败')
				}
			},
			fetchThirdPartyInfo() {
				this.$request.fetchThirdPartyInfo({
					thidId: this.thirdId
				}).then(res => {
					if (res.code == 200) {
						this.thirdData = JSON.parse(res.data)
						this.rapidUsername = this.thirdData.nickname
						this.judge()
					} else {
						this.$message(res.message)
					}
				})
			},
			judge() {
				if (this.thirdData.bind) {
					localStorage.setItem('JEECMS-Auth-Token', this.thirdData['JEECMS-Auth-Token'])
					uni.navigateTo({
						url: '/pages/personal/index/index'
					})
				}
			},
			//直接登录
			rapidGo() {
				this.$request.fetchThirdPartyBind({
					loginWay: 1,
					loginType: this.thirdData.loginType,
					thirdId: this.thirdData.uid,
					nickname: this.thirdData.nickname,
					username: this.rapidUsername,
					psw: ''
				}).then(res => {
					if (res.code == 200) {
						localStorage.setItem('JEECMS-Auth-Token',res.data['JEECMS-Auth-Token'])
						uni.navigateTo({
							url: '/pages/personal/index/index'
						})
					} else {
						this.$message(res.message)
					}
				})
			},
			// 绑定并登录
			async boundGo() {
				let boundPsw = await desEncrypt(this.boundPsw)
				this.$request.fetchThirdPartyBind({
					loginWay: 2,
					loginType: this.thirdData.loginType,
					thirdId: this.thirdData.uid,
					nickname: this.thirdData.nickname,
					username: this.boundUsername,
					psw: boundPsw
				}).then(res => {
					if (res.code == 200) {
						localStorage.setItem('JEECMS-Auth-Token',res.data['JEECMS-Auth-Token'])
						uni.navigateTo({
							url: '/pages/personal/index/index'
						})
					} else {
						this.$message(res.message)
					}
				})
			},
			onRapid(){
				this.isCelerity = true
			},
			onNoRapid(){
				this.isCelerity = false
			},
			backLogin(){
				uni.navigateTo({
					url: '/pages/login/login'
				})
			}
		},
		onShow() {
			this.getUrlArg('thirdId')
		}
	};
</script>

<style lang="scss" scoped>
	.content.thirdLogin {
		.back {
			padding-left: 40upx;
			height: 88upx;
			line-height: 88upx;
			font-size: 30upx;
			color: #999999;
		}

		.third-cont {
			padding: 60upx 110upx;

			.third-header {
				display: flex;
				height: 50upx;
				line-height: 0;
				color: #666666;
				font-size: 40upx;
				font-weight: 450;

				.pitch-on {
					font-size: 50upx;
					color: #333333;
					font-weight: 500;
				}
				.heder-two{
					margin-left: 60upx;
				}
			}

			.celerity {
				width: 100%;

				.celerity-header {
					margin-top: 79upx;
					font-size: 28upx;
					color: 28upx;
					margin-bottom: 6upx;
				}

				.celerity-input {
					width: 100%;
					height: 100upx;
					line-height: 100upx;
					border-bottom: 1upx solid #F0F0F0;
					color: #333333;
					font-size: 28upx;
				}

				.third-btn {
					width: 458upx;
					height: 86upx;
					line-height: 86upx;
					font-size: 30upx;
					color: #fff;
					margin: 0 auto;
					margin-top: 60upx;
					border-radius: 43upx;
				}
			}

			.bound {
				padding: 27upx 0;
				width: 100%;

				.bound-input {
					width: 100%;
					height: 120upx;
					line-height: 120upx;
					border-bottom: 1upx solid #F0F0F0;
					color: #333333;
					font-size: 28upx;
				}

				.bound-btn {
					width: 458upx;
					height: 86upx;
					line-height: 86upx;
					font-size: 30upx;
					color: #fff;
					margin: 0 auto;
					margin-top: 60upx;
					border-radius: 43upx;
				}
			}
		}
	}
</style>
