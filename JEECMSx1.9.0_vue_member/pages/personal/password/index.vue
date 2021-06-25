<template>
	<view class="content email">
		<page-head pageTitle="修改密码"></page-head>
		<view class="email-box">
			<view class="code-box">
				<view class="code-conent">
					<input type="password" class="code-input" maxlength="18" v-model="amend.oldPStr" placeholder="原始密码"/>
				</view>
				<view class="code-conent">
					<input type="password" class="code-input" maxlength="18" v-model="amend.newPStr" placeholder="新密码"/>
				</view>
				<view class="code-conent">
					<input type="password" class="code-input" maxlength="18" v-model="amend.againPStr" placeholder="确认新密码"/>
				</view>
				<view :style="{'background-color': amend.againPStr&&amend.oldPStr&&amend.newPStr ? '#D20505' : '#E6E6E6' }" class="code-next Medium" @click="complete()">完成修改</view>
			</view>
		</view>
	</view>
</template>


<script>
	import {desEncrypt} from '../../../common/js/util.js'
	export default {
		name: "password",
		data() {
			return {
				amend:{
					oldPStr:'',
					newPStr:'',
					againPStr:''
				},
				rules: {
					oldPStr: [this.$rules.required('请填写原始密码')],
					newPStr: [this.$rules.required('请填写新密码')],
					againPStr: [this.$rules.required('请确认新密码')]
				},
			}
		},
		mounted() {

		},
		methods:{
			async complete(){
				if(this.amend.newPStr != this.amend.againPStr){
					this.$message('新密码和确认密码要一致')
					return
				}
				let oldPStr = await desEncrypt(this.amend.oldPStr)
				let newPStr = await desEncrypt(this.amend.newPStr)
				let againPStr = newPStr
				this.$rules
					.validator(this.amend, this.rules)
					.then(() => {
						this.$request.fetchMemberinfoPstr({
							oldPStr: oldPStr,
							newPStr: newPStr,
							againPStr: againPStr
						}).then(res => {
							if(res.code == 200){
								this.$message('修改密码成功')
								this.$request.fetchLogout().then(res => {
									if(res.code == 200){
										localStorage.setItem('JEECMS-Auth-Token', '')
										uni.navigateTo({
											url:'/pages/login/login'
										})
									}
								})
							}else{
								this.$message(res.message)
							}
						})
					})
					.catch(this.$message);
			}
		}
	}
</script>

<style lang="scss" scoped>
	.content.email {
		.email-box{
			padding: 0 40upx;
			padding-top: 100upx;
			.code-box{
				padding: 0 74upx;
				.code-conent{
					padding-top: 30upx;
					padding-bottom: 25upx;
					border-bottom: 1upx solid #F0F0F0;
					display: flex;
					justify-content: space-between;
					.code-input{
						height: 60upx;
						line-height: 60upx;
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
