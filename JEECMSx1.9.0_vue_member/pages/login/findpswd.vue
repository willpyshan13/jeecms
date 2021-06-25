<template>
	<view class="content findpswd">
		<!-- 防止密码自动填写 -->
		<input type="password" style="position: absolute;z-index: -1;width:0;opacity:0;" />
		<input type="text" style="position: absolute;z-index: -1;width:0;opacity:0;" />
		<view :class="current==0?'page show':'page'">
			<page-head></page-head>
			<view class="uni-form-wrap">
				<view class="uni-title-h1 Medium">找回密码</view>
				<view class="box">
					<input class="inputbox Regular" focus placeholder="输入用户名/注册邮箱" autocomplete='off' maxlength="50" v-model="username_email" />
				</view>
				<view class="box">
					<input class="inputbox Regular" placeholder="请输入验证码" autocomplete='off' maxlength="6" v-model="emailCode" />
					<view class="right Regular" v-text="code" @click="getEmailCode"></view>
				</view>
				<button class="uni-button" :disabled="!username_email || !emailCode" @click="handleSubmit">下一步
				</button>

			</view>
		</view>
		<view :class="current==1?'page show':'page'">
			<page-head></page-head>
			<view class="uni-form-wrap">
				<view class="uni-title-h1 Medium">重置密码</view>
				<view class="box">
					<input class="inputbox Regular" focus placeholder="新密码" autocomplete='off' maxlength="18" v-model="password" type="password" />
				</view>
				<view class="box">
					<input class="inputbox Regular" focus placeholder="重复新密码" autocomplete='off' maxlength="18" v-model="repswd" type="password" />
				</view>
				<button class="uni-button" :disabled="!password || !repswd" @click="confirmModify">确认修改
				</button>

			</view>
		</view>
		<view :class="current==2?'page show':'page'">
			<page-head></page-head>
			<view class="uni-wrap">
				<view class="page3">
					<img src="../../static/img/chenggong.png" alt="">
					<view class="h3-item Medium">找回成功</view>
					<view class="p-item Regular">
						您已成功找回您的密码，登录时请用新密码登录
					</view>
					<view class="btn Medium" @click="toLogin">现在登录</view>
				</view>

			</view>
		</view>
	</view>
</template>

<script>
	import {
		desEncrypt
	} from '../../common/js/util.js'
	export default {
		name: 'findpswd',
		data() {
			return {
				username_email: '',
				current: 0,
				code: '发送验证码',
				emailCode: '',
				time1: 60,
				password: '',
				repswd: ''
			}
		},
		methods: {
			// 获取邮箱验证码
			getEmailCode() {
				if (this.username_email) {
					if (this.time1 == 60) {
						const data = {
							type: 2,
							targetNumber: this.username_email
						}
						this.$request.getEmailCode(data).then(res => {
							if (res.code == 200) {
								this.$message('验证码发送成功')
								if (res.data) {
									if (!this.timer1) {
										this.timer1 = setInterval(() => {
											if (this.time1 > 0) {
												this.time1--;
												this.code = '重新发送(' + this.time1 + ')'
											} else {
												clearInterval(this.timer1);
												this.time1 = 60
												this.timer1 = null;
												this.code = '获取验证码'
											}
										}, 1000);
									}

								}
							} else {
								this.$message(res.message)
							}

						})
					}
				} else {
					this.$message('请输入用户名或邮箱')
				}
			},
			// 下一步
			handleSubmit() {
				this.$request.verifyEmailCode({
					email: this.username_email,
					type: 2,
					code: this.emailCode
				}).then(res => {
					if (res.code == 200) {
						if (res.data) {
							this.current = 1
						}
					} else {
						this.$message(res.message)
					}
				})
			},
			// 确认修改
			async confirmModify() {
				if (this.password.trim() == this.repswd.trim()) {
					let passwords =  await desEncrypt(this.password)
					this.$request.findpswd({
						key: this.username_email,
						validateCode: this.emailCode,
						pStr: passwords
					}).then(res => {
						if (res.code == 200) {
							this.$message('修改密码成功')
							this.current = 2

						} else {
							this.$message(res.message)
						}
					})

				} else {
					this.$message('新密码与重复新密码不同')
				}

			},
			// 现在登录
			toLogin() {
				uni.navigateTo({
					url: '/pages/login/login'
				})
			}

		},

	}
</script>

<style lang="scss" scoped>
	.page {
		display: none;
	}

	.page.show {
		display: block;
	}

	.page3 {
		padding-top: 80upx;
		text-align: center;

		img {
			width: 452upx;
			height: 379upx;
			margin: 0 auto;
		}

		.h3-item {
			font-size: 50upx;
			font-family: PingFang SC;
			color: rgba(51, 51, 51, 1);
			margin-top: 60upx;
			margin-bottom: 30upx;
			text-align: center;
		}

		.p-item {
			font-size: 26upx;
			font-family: PingFang SC;
			color: rgba(153, 153, 153, 1);
			margin-bottom: 80upx;
			text-align: center;
		}

		.btn {
			width: 458upx;
			height: 86upx;
			margin: 0 auto;
			background: rgba(227, 11, 32, 1);
			border-radius: 43upx;
			text-align: center;
			line-height: 86upx;
			font-size: 30upx;
			font-family: PingFang SC;
			color: rgba(255, 255, 255, 1);
		}
	}

	.content.findpswd {
		line-height: 1;

		.uni-form-wrap {

			.uni-title-h1 {
				padding: 60upx 0 26upx;
				font-size: 50upx;
				font-family: PingFang SC;
				color: rgba(51, 51, 51, 1);
			}

			.box {
				height: 122upx;
				position: relative;
				border-bottom: 1px solid rgba(240, 240, 240, 1);

				.right {
					width: 184upx;
					height: 60upx;
					background: rgba(59, 59, 59, 1);
					border-radius: 10upx;
					position: absolute;
					right: 0;
					top: 35upx;
					color: rgba(255, 255, 255, 1);
					text-align: center;
					line-height: 60upx;
				}

				.uni-icon {
					position: absolute;
					top: 49upx;
					right: 0;

					img {
						width: 32upx;
						height: 32upx;
					}
				}

				.inputbox {
					font-size: 28upx;
					position: absolute;
					top: 54upx;
					left: 0;
					font-family: PingFang SC;
					color: #333;
					line-height: 1;
				}
			}

			.uni-button {
				margin-top: 60upx;
			}

			.forget-pass {
				padding-top: 40upx;
				text-align: center;
			}
		}


	}

	.inputbox::-webkit-input-placeholder {
		/* WebKit browsers */

		color: #999;

	}

	.inputbox:-moz-placeholder {
		/* Mozilla Firefox 4 to 18 */

		color: #999;

	}

	.inputbox::-moz-placeholder {
		/* Mozilla Firefox 19+ */

		color: #999;

	}

	.inputbox:-ms-input-placeholder {
		/* Internet Explorer 10+ */

		color: #999;

	}
</style>
