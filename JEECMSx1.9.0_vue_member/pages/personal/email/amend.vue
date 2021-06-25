<template>
	<view class="content email">
		<page-head pageTitle="修改注册邮箱"></page-head>
		<view class="email-box">
			<view class="code-box" v-if="status==1">
				<view class="code-conent">
					<input type="text" class="code-input" v-model="code" placeholder="邮箱验证码" maxlength="6"/>
					<view class="code-btn" :style="{ 'color': time < 60 ? '#fff' : '#fff' }" @click="fetchsendEmailMsg()" v-text="codeBtn"></view>
				</view>
				<view class="code-next Medium" @click="verifyCode()" :style="{ 'background-color': code.length >= 6 ? '#D20505' : '#E6E6E6' }">下一步</view>
			</view>
			<view class="code-box" v-if="status==2">
				<view class="code-conent">
					<input type="text" class="code-input" maxlength="50" v-model="newEmail" placeholder="请输入新邮箱"/>
				</view>
				<view class="code-conent" style="margin-top: 20upx;">
					<input type="text" class="code-input" v-model="newCode" placeholder="新邮箱验证码" maxlength="6"/>
					<view class="code-btn" :style="{ 'color': newTime < 60 ? '#fff' : '#fff' }" @click="newFetchsendEmailMsg()" v-text="newCodeBtn"></view>
				</view>
				<view class="code-next Medium" @click="accomplish()" :style="{ 'background-color': newCode.length >= 6 ? '#D20505' : '#E6E6E6' }">完成修改</view>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		name: "amend",
		data() {
			return {
				email:"",
				codeBtn:'获取验证码',
				time:60,
				code:'',
				status:1,
				newEmail:'',
				isNew:false,
				newTime:60,
				newCode:'',
				newCodeBtn:'获取验证码'
			}
		},
		mounted() {
			this.fetchMemberinfo()
		},
		methods:{
			accomplish(){
				if(this.newEmail&&this.newCode){
					this.$request.fetchMemberinfoEmail({
						code:this.code,
						newEmail:this.newEmail,
						newEmailCode:this.newCode
					}).then(res => {
						if(res.code == 200){
							this.$message('完成修改')
							uni.navigateTo({
								url: '/pages/personal/index/index'
							})
						}else{
							this.$message(res.message)
						}
					})
				}
			},
			fetchsendEmailMsg(){
				if(this.time >= 60){
					this.$request.fetchsendEmailMsg({
						type:3,
						targetNumber:this.email
					}).then(res => {
						if(res.code == 200){
							this.countDown()
						}else{
							this.$message(res.message)
						}
					})
				}
			},
			newFetchsendEmailMsg(){
				const reg = new RegExp(/^([a-zA-Z]|[0-9])(\w|\-)+@[a-zA-Z0-9]+\.([a-zA-Z]{2,4})$/)
				if(reg.test(this.newEmail)){
					if(this.newTime >= 60){
						this.$request.fetchsendEmailMsg({
							type:4,
							targetNumber:this.newEmail
						}).then(res => {
							if(res.code == 200){
								this.newCountDown()
							}else{
								this.$message(res.message)
							}
						})
					}
				}else{
					this.$message('请正确输入邮箱')
				}
			},
			fetchMemberinfo(){
				this.$request.fetchMemberinfo().then(res => {
					if(res.code == 200){
						this.email = res.data.dataField.email
					}
				})
			},
			countDown(){
				let _this = this
			    var codeTmie = setInterval(function(){
					if(_this.time > 0){
						_this.codeBtn = '获取验证码('+_this.time+')'
						_this.time -= 1
					}else{
						clearTimeout(codeTmie)
						_this.time = 60
						_this.codeBtn='获取验证码'
					}
				},1000)
			},
			newCountDown(){
				let _this = this
				var newCodeTmie = setInterval(function(){
					if(_this.newTime > 0){
						_this.newCodeBtn = '获取验证码('+_this.newTime+')'
						_this.newTime -= 1
					}else{
						clearTimeout(newCodeTmie)
						_this.newTime = 60
						_this.newCodeBtn='获取验证码'
					}
				},1000)
			},
			verifyCode(){
				if(this.code){
					this.$request.verifyEmailCode({
						email:this.email,
						type:3,
						code:this.code
					}).then(res => {
						if(res.code == 200){
							this.status=2
						}else{
							this.$message(res.message)
						}
					})
				}
			}
		}
	}
</script>

<style lang="scss" scoped>
	.content.email {
		.email-box{
			padding: 0 40upx;
			padding-top: 150upx;
			.code-box{
				padding: 0 74upx;
				.code-conent{
					padding-bottom: 25upx;
					border-bottom: 1upx solid #F0F0F0;
					display: flex;
					justify-content: space-between;
					.code-input{
						height: 60upx;
						line-height: 60upx;
					}
					.code-btn{
						width: 184upx;
						height: 60upx;
						line-height: 60upx;
						text-align: center;
						font-size: 22upx;
						color: #FFFFFF;
						background-color: #3B3B3B;
						border-radius: 10upx;
					}
				}
				.code-next{
					width: 458upx;
					height: 86upx;
					font-size: 30upx;
					color: #FFFFFF;
					line-height: 86upx;
					text-align: center;
					margin: 60upx auto;
					border-radius: 43upx;
					background-color: #E6E6E6;
				}
			}
		}
	}
</style>
