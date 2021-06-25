<template>
    <view class="content login">
		<!-- 防止密码自动填写 -->
		<input type="password" style="position: absolute;z-index: -1;width:0;opacity:0;"/>
		<input type="text" style="position: absolute;z-index: -1;width:0;opacity:0;"/>
		<page-head :title="register" @click="handleReg"></page-head>
		<view class="uni-form-wrap">
			<view class="uni-title-h1 Medium">登录</view>
			<view class="box">
				<input class="inputbox Regular" focus
					placeholder="输入用户名"
					autocomplete='off'
					maxlength="50"
					v-model="username"
				/>
				<view class="uni-icon" v-if="username" @click="clearIcon">
					<img src="../../static/img/icon/qingchu.png" alt="">
				</view>
			</view>
			<view class="box">
				<input class="inputbox Regular"
					:password="showPassword"
					placeholder="输入密码"
					autocomplete='off'
					maxlength="50"
					v-model="password"
				/>

			</view>
			<view class="box" v-if="errorNumber>=3">
				<input class="inputbox Regular"
					placeholder="输入验证码"
					autocomplete='off'
					maxlength="4"
					v-model="captcha"
				/>
				<img class="code-img" :src="'data:image/png;base64,'+codeImage" @click="genCapatch()"/>
			</view>
			<button class="uni-button"
				:disabled="!username || !password"
				@click="handleSubmit">立即登录
			</button>
			<view @click="find()" class="forget-pass uni-text-sm uni-text"
				v-if="showForgetPass"
			>
				忘记密码？ 点击
				<text class="uni-link-a uni-text-66">找回</text>
			</view>
		</view>
		<view class="fast-login-wrap" v-if="speediness">
			<view class="fast-login-title uni-layout-center">
				<view class="line"></view>
				<text class="uni-text uni-text-md">快速登录</text>
				<view class="line"></view>
			</view>
			<view class="oauth-row uni-layout-center">
				<!-- <view v-if="wechatOpen" class="uni-layout-center oauth-image" >
					<uni-icons
						class='iconfont iconwechat-fill'
						color='#46C961'
						size='50'
						@click="oauth()"
					/>
				</view> -->
				<view v-if="qqOpen" class="uni-layout-center oauth-image">
					<uni-icons
						class='iconfont iconqq-fill'
						color='#41C0F6'
						size='20'
						@click="qqQauth()"
					/>
				</view>
				<view v-if="weiboOpen" class="uni-layout-center oauth-image" >
					<uni-icons
						class="iconfont iconxinlang"
						color='#FF5D5D'
						size='20'
						@click="sinaQauth()"
					/>
				</view>
			</view>
			<text class="uni-text-sm uni-text-b3">
				登录即代表您已阅读并同意
				<text @click="gotoAgreement()" class="uni-link">《JEECMS用户协议》</text>
			</text>
		</view>
    </view>
</template>

<script>
	import {desEncrypt} from '../../common/js/util.js'
    export default {
		name: 'login',
        data() {
            return {
							speediness:true,
							username: '',
							password: '',
							positionTop: 0,
							showPassword: true,
							showForgetPass: false,
							qqOpen:false,
							wechatOpen:false,
							weiboOpen:false,
							memberRegisterOpen:false,
							register:'',
							errorNumber:0,
							codeImage:'',
							sessionId:''
            }
        },
        methods: {
			clearIcon() {
				this.username = '';
			},
			changePassword () {
				this.showPassword = !this.showPassword;
			},
			oauth (key) {
				console.log(key)
			},
			async handleSubmit () {
				this.$loading('登录中...')
				var password = JSON.stringify({pStr:this.password});
				let desStr = await desEncrypt(password);
				this.$request.fetchLogin({
					identity: this.username,
					desStr: desStr,
					sessionId: this.sessionId,
					captcha: this.captcha
				}).then(res => {
					if (res.code == 200) {
						localStorage.setItem('JEECMS-Auth-Token', res.data['JEECMS-Auth-Token'])
						uni.navigateTo({
							url: '/pages/personal/index/index'
						})
					} else {
						this.errorNumber+=1
						this.showForgetPass=true
						this.$message(res.message)
						if(this.errorNumber>=3){
							this.genCapatch()
						}
					}
				})
			},
			handleReg () {
				if(this.memberRegisterOpen){
					this.$nav('../register/register')
				}
			},
			find(){
				uni.navigateTo({
					url: '/pages/login/findpswd'
				})
			},
			fetchGlobalInfo(){
				this.$request.fetchGlobalInfo().then(res => {
					if (res.code == 200) {
						this.wechatOpen = res.data.wechatOpen
						this.weiboOpen = res.data.weiboOpen
						this.qqOpen = res.data.qqOpen
						if(!this.wechatOpen&&!this.weiboOpen&&!this.qqOpen){
							this.speediness = false
						}
					} else {
						this.$message(res.message)
					}
				})
			},
			gotoAgreement(){
				uni.navigateTo({
					url: '/pages/register/agreement'
				})
			},
			qqQauth(){
				location.href= this.$baseUrl+'/thirdParty/qq/mobile'
			},
			sinaQauth(){
				location.href= this.$baseUrl+'/thirdParty/sina/mobile'
			},
			judgeCapatch(){
				this.$request.judgeCapatch().then(res => {
					if(res.code == 200){
						this.memberRegisterOpen = res.data.memberRegisterOpen
						if(this.memberRegisterOpen){
							this.register = '注册'
						}else{
							this.register = ''
						}
					}
				})
			},
			genCapatch(){
				this.$request.genCapatch().then(res => {
					if(res.code == 200){
						this.codeImage=res.data.img
						this.sessionId=res.data.sessionId
					}
				})
			}
        },
		mounted() {
			this.judgeCapatch()
			this.fetchGlobalInfo()
		}
    }
</script>

<style lang="scss" scoped>
	.content.login{
		width: 100%;
		min-height: 100vh;
		position: relative;
		.uni-form-wrap{
			.uni-title-h1{
				padding: 60upx 0 26upx;
				font-size:50upx;
				font-family:PingFang SC;
			color:rgba(51,51,51,1);
			}
			.box{
				height: 122upx;
				position: relative;
				border-bottom: 1px solid rgba(240,240,240,1);
				.uni-icon{
					position: absolute;
					top: 49upx;
					right: 0;
					img{
						width: 32upx;
						height: 32upx;
					}
				}
				.inputbox{
					font-size:28upx;
					position: absolute;
					top: 54upx;
					left: 0;
					font-family:PingFang SC;
					color:rgba(51,51,51,1);
					line-height: 1;
				}
			}
			.uni-button{
				margin-top: 60upx;
			}
			.forget-pass{
				padding-top: 40upx;
				text-align: center;
			}
		}
		.fast-login-wrap{
			margin-top: 250upx;
			width: 100%;
			text-align: center;
			.fast-login-title{
				.line{
					width: 30upx;
					height: 1upx;
					background: #E6E6E6;
				}
				.uni-text-md{
					padding: 0 28upx;
				}
			}
			.oauth-row {
				padding: 60upx 0;
				.oauth-image {
					width: 80upx;
					height: 80upx;
					border-radius: 50%;
					margin: 0 35upx;
					background-color: #F5F5F5;
					&:hover{
						background-color: #F2F2F2;
					}
					image {
						width: 40upx;
						height: 40upx;
						margin: 20upx;
					}
				}
			}
		}
		.code-img{
			position: absolute;
			top: 30upx;
			right: 10upx;
			height: 70upx;
			width: 250upx;
		}
	}

</style>
