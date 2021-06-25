<template>
	<view class="content email">
		<page-head pageTitle="邮箱"></page-head>
		<view class="email-box">
			<text class="email-title">当前注册邮箱</text>
			<view class="email-message">
				<view class="message-content">
					<image src="../../../static/img/icon/youxiang.png" mode=""></image>
					<view class="content-text">
						<text class="Medium">邮箱</text>
						<view class="Medium" v-text="email"></view>
					</view>
				</view>
				<view class="message-operate Medium" @click="gotoAmend()">
					修改
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		name: "personal",
		data() {
			return {
				email:""
			}
		},
		mounted() {
			this.fetchMemberinfo()
		},
		methods:{
			fetchMemberinfo(){
				this.$request.fetchMemberinfo().then(res => {
					if(res.code == 200){
						this.email = res.data.dataField.email
					}
				})
			},
			gotoAmend(){
				uni.navigateTo({
					url:'/pages/personal/email/amend',
				})
			}
		}
	}
</script>

<style lang="scss" scoped>
	.content.email {
		width: 100%;
		.email-box{
			padding: 0 40upx;
			padding-top: 60upx;
			.email-title{
				color: #666666;
				font-size: 28upx;
			}
			.email-message{
				display: flex;
				margin-top: 40upx;
				justify-content: space-between;
				.message-content{
					display: flex;
					image{
						width: 70upx;
						height: 70upx;
						margin-right: 30upx;
					}
					.content-text{
						text{
							color: #333333;
							font-size: 28upx;
						}
						view{
							color: #999999;
							font-size: 24upx;
							margin-top: 18upx;
						}
					}
				}
				.message-operate{
					color: #666666;
					background-color: #F6F6F7;
					width: 108upx;
					height: 48upx;
					line-height: 48upx;
					text-align: center;
					font-size: 24upx;
					border-radius: 24upx;
					margin-top: 10upx;
				}
			}
		}
	}
</style>
