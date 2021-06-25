<template>
	<view class="content register">
		<!-- 防止密码自动填写 -->
		<input type="password" style="position: absolute;z-index: -1;width:0;opacity:0;"/>
		<input type="text" style="position: absolute;z-index: -1;width:0;opacity:0;"/>
		<page-head title="登录" @click="handleLogin"></page-head>
		<view class="form-wrap">
			<view class="title Medium">注册</view>
			<view v-for="(a,index) in list" :key="index" class="box">
				<view class="wrap" v-if="a.name!='头像'">
					<view class="top">
						<!-- <view class="required" v-if="a.value.isRequired">
							*
						</view> -->
						<input v-if="a.value.name == 'password'" type="password" class="inputbox" :placeholder="a.value.label" v-model="form.passwords" :id="a.value.name" :maxlength="a.value.max>=0&&a.value.isLengthLimit?a.value.max:''"/>
						<input v-else class="inputbox" type="text" :placeholder="a.value.label" v-model="form[a.value.name]" :id="a.value.name" :maxlength="a.value.max>=0&&a.value.isLengthLimit?a.value.max:''"/>
					</view>
					<view class="text Semilight" v-if="a.value.tip" v-text="a.value.tip"></view>
				</view>
				<view v-if="a.value.name === 'password'" :class="a.value.name === 'password'?'wrap more':'wrap'">
					<view class="wrap con">
						<view class="top">
							<!-- <view class="required">
								*
							</view> -->
							<input type="password" autocomplete="off" class="inputbox repassword" placeholder="重复密码" v-model="form.repassword" id="repassword" />
						</view>
						<view class="text Semilight">请再次输入密码</view>
					</view>
				</view>
				<view class="wrap" v-else-if="a.value.name=='email'&& a.value.isSmsVerification" :class="a.value.name === 'email'?'wrap more':'wrap'">

					<view class="wrap con">
						<view class="top">
							<!-- <view class="required">
								*
							</view> -->
							<input type="text" class="inputbox email" autocomplete="off" placeholder="邮箱验证码" v-model="form.emailCode" id="email" maxlength="6" />
							<view class="right emailCode Semilight code" @click="getEmailCode" v-text="code">

							</view>
						</view>
						<view class="text Semilight">请输入邮箱验证码</view>
					</view>
				</view>
				<view class="wrap" v-else-if="a.value.name=='telephone'&& a.value.isSmsVerification" :class="a.value.name === 'telephone'?'wrap more':'wrap'">
					<view class="wrap con">
						<view class="top">
							<!-- <view class="required">
								*
							</view> -->
							<input type="text" class="inputbox telelphone" autocomplete="off" placeholder="请输入手机验证码" v-model="form.mobileCode" id="telephone"
							 maxlength="6" />
							<view class="right telCode Semilight code" @click="getTelephoneCode" v-text="telcode">

							</view>
						</view>
						<view class="text Semilight">请输入手机验证码</view>
					</view>
				</view>
			</view>
			<view class="box" v-if="needPic">
				<view class="wrap">
					<view class="top">
						<!-- <view class="required">
							*
						</view> -->
						<input type="text" class="inputbox pic" autocomplete="off" placeholder="请输入验证码" v-model="form.captcha" id="captcha" />
						<view class="right telCode Semilight code" @click="genCapatch">
							<img :src="captchaSrc" mode="" class="image"></img>
						</view>
					</view>
				</view>
				<view class="text Semilight"></view>
			</view>
			<view class="agreement Regular">
				<i v-if='agreement==1' @click="radioChange" class='pitchOn'></i>
				<i v-else @click="radioChange" class='unselected'></i>
				<text>我已阅读并接受<text style="color: #2676B5;" @click="toAgreement">《JEECMS注册协议》</text></text>
			</view>
			<view @click="handleSubmit()" :style="{ 'background-color':agreement == 1? '#D20505' : '#E6E6E6'}" class="signIn">
				注册
			</view>
		</view>
	</view>
</template>

<script>
	import {desEncrypt} from '../../common/js/util.js'
	export default {
		name: 'register',
		data() {
			return {
				showPassword: true,
				showRePassword: true,
				captchaSrc:'',
				time1: 60,
				timer1: null,
				time2: 60,
				timer2: null,
				codeLoading: false,
				checked: true,
				isDisabled: true,
				list: [],
				// 表单数据
				form: {
					username: '',
					passwords:'',
					password: '',
					repassword: '',
					email: '',
					emailCode: '',
					telephone: '',
					mobileCode: '',
					captcha:'',
					sessionId:''
				},
				needPic: false,
				code: '',
				isRead: false,
				// 表单规则
				rules: {
					// username:[this.$rules.required('请输入用户名')],
					email:[this.$rules.email()],
					telephone:[this.$rules.mobile()],
					// emailCode:[this.$rules.required('请输入邮箱验证码 ')],
					// passwords:[this.$rules.required('请输入密码')],
					repassword:[this.$rules.required('请输入重复密码')],
					// captcha:[this.$rules.required('请输入图形验证码')],
				},
				messages: {

				},
				code: '获取验证码',
				telcode: '获取验证码',
				agreement:0,
				userReg : (rules,value,callback)=>{
				console.log(value);
				let reg = /^[\w\@\-\u4e00-\u9fa5]{6,18}$/
				if (reg.test(value)) {
					callback()
				}else if(!value){
					callback(new Error('此项必填'))
				}else{
					callback(new Error('请输入6-18位字符，可包含数字，字母，汉字和"_","-","@"'))
				}
			}
			};
		},
		mounted() {
			this.getModel()
			this.judgeCapatch()
		},
		computed: {

		},
		methods: {
			// 会员动态字段
			getModel() {
				this.$request.getModel({}).then(res => {
					this.list = res.data.enableJson.formListBase
					this.putRulse()
				})
			},
			// 获取邮箱验证码
			getEmailCode() {
				const reg = new RegExp(/^([a-zA-Z]|[0-9])(\w|\-)+@[a-zA-Z0-9]+\.([a-zA-Z]{2,4})$/)
				if (reg.test(this.form.email)) {
					if (this.time1 == 60) {
						const data = {
							type: 1,
							targetNumber: this.form.email
						}
						this.$request.getEmailCode(data).then(res => {
							if (res.code == 200) {
								this.timer1 = setInterval(() => {
									if (this.time1 > 0) {
										this.time1--;
										this.code = '重新发送(' + this.time1 + ')'
									} else {
										this.time1 = 60
										clearInterval(this.timer1);
										this.timer1 = null;
										this.code = '获取验证码'
									}
								}, 1000);
							} else {
								this.$message(res.message)
							}
						})
					}
				} else {
					this.$message('请输入正确的邮箱')
				}
			},
			// 获取手机验证码
			getTelephoneCode() {
				const reg = new RegExp(
					/^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(16[2|6|7])|(18[0-9])|(17([0-1]|[3]|[5-8]))|(19[1|8|9]))\d{8}$/)
				if (reg.test(this.form.telephone)) {
					if (this.time2 == 60) {
						const data = {
							type: 1,
							targetNumber: this.form.telephone
						}
						this.$request.getTelephoneCode(data).then(res => {
							if (res.code == 200) {
								if (res.data) {
									this.timer2 = setInterval(() => {
										if (this.time2 > 0) {
											this.time2--;
											this.telcode = '重新发送(' + this.time2 + ')'
										} else {
											this.time2 = 60
											clearInterval(this.timer2);
											this.timer2 = null;
											this.telcode = '获取验证码'
										}
									}, 1000);
								}
							} else {
								this.$message(res.message)
							}
						})
					}
				} else {
					this.$message('请输入正确的手机号')
				}
			},
			// 判断是否需要图形验证
			judgeCapatch() {
				this.$request.judgeCapatch({}).then(res => {
					this.needPic = res.data.MemberRegisterCaptcha
					if (this.needPic) {
						this.genCapatch()
					}
				})
			},
			// 获取图形验证码
			genCapatch() {
				this.$request.genCapatch({}).then(res => {
					if (res.code == 200) {
						this.captchaSrc = "data:image/png;base64," + res.data.img
						this.form.sessionId=res.data.sessionId
					} else {
						this.$message(res.message)
					}
				})
			},
			async handleSubmit() {
				if(this.agreement == 1){
					let  passwords = await desEncrypt(this.form.passwords)
					if(this.form.passwords === this.form.repassword){
						this.$rules
							.validator(this.form, this.rules)
							.then(() => {
								this.form.password = passwords
								this.$request.fetchRegister(this.form).then(res => {
									if(res.code == 200){
										this.$message('注册成功')
										uni.navigateTo({
											url: '/pages/login/login'
										})
									}else{
										this.$message(res.message)
									}
								})
							})
							.catch(this.$message);
					}else{
						this.$message('密码和重复密码要相同')
					}
				}else{
					this.$message('请先阅读并同意注册协议')
				}

			},
			radioChange(evt) {
				if(this.agreement == 1){
					this.agreement = 0
				}else{
					this.agreement = 1
				}
			},
			// 添加rules
			putRulse() {
				let array = this.list
				for (var i = 0; i < array.length; i++) {
					var val = array[i].value
					if (val.isRequired) {
						var required = [this.$rules.required('请输入' + val.label)]
						if(val.name == 'password'){
							this.rules.passwords = [this.$rules.required('请重复输入密码')]
						}else if (this.rules[val.name] instanceof Array) {
							this.rules[val.name]=this.rules[val.name].concat(required)
						} else {
							this.rules[val.name] = required
						}
					}
					if (val.isInputLimit&&val.inputLimit) {
						var inputLimit = [this.$rules[val.inputLimit]()]
						if (this.rules[val.name] instanceof Array) {
							this.rules[val.name]=this.rules[val.name].concat(inputLimit)
						} else {
							this.rules[val.name] = inputLimit
						}
					}
					if(val.name == 'telephone'&& val.isSmsVerification){
						this.rules.mobileCode = [this.$rules.required('请输入手机验证码')]
					}
					if(val.name == 'email'&& val.isSmsVerification){
						this.rules.emailCode = [this.$rules.required('请输入邮箱验证码')]
					}
					console.log(val);
					if(val.name == 'username'){
						this.rules.username = [this.$rules.custom(val.tip,this.userReg)]
					}
				}
			},
			handleLogin(){
				uni.navigateTo({
					url: '/pages/login/login'
				})
			},
			// 协议书
			toAgreement(){
				uni.navigateTo({
					url:'/pages/register/agreement'
				})
			}
		}
	};
</script>

<style lang="scss" scoped>
	.content.register {
		.form-wrap {
			padding: 0 115upx;
			.title {
				padding-top: 60upx;
				font-size: 50upx;
				font-family: PingFang SC;
				color: rgba(51, 51, 51, 1);
				padding-bottom: 58upx;
			}
			.wrap {
				position: relative;
				.top {
					border-bottom: 1upx solid rgba(240, 240, 240, 1);
					.code {
						position: absolute;
						right: 0;
						top: 30upx;
						width: 184upx;
						height: 60upx;
						background: rgba(59, 59, 59, 1);
						border-radius: 10upx;
						font-size: 22upx;
						font-family: PingFang SC;
						color: rgba(255, 255, 255, 1);
						text-align: center;
						line-height: 60upx;

						.image {
							position: absolute;
							right: 0;
						}

					}

					.required {
						top: 30upx;
						position: absolute;
						left: 0;
						font-size: 28upx;
						font-family: PingFang SC;
						color: rgba(227, 11, 32, 1);
					}

					.inputbox {
						height: 120upx;
						font-size: 28upx;
						font-family: PingFang SC;
						color: #333;
						border: none;
					}

					.icon {
						position: absolute;
						top: 63upx;
						right: 0;

						.icon-clear {
							width: 32upx;
							height: 32upx;
						}
					}
				}

				.top.red {
					border-bottom: 1upx solid rgba(227, 11, 32, 1);

				}

				.text {
					display: block;
					margin-top: 19upx;
					font-size: 22upx;
					font-family: PingFang SC;
					color: rgba(179, 179, 179, 1);
				}

				.text.red {
					color: rgba(227, 11, 32, 1);
				}
			}
		}
		.agreement{
			font-size: 24upx;
			color: #999999;
			line-height: 140upx;
			.radio{
				position: relative;
				top: -2upx;
			}
			.pitchOn{
				display: inline-block;
				width: 26upx;
				height: 26upx;
				background-image: url('../../static/img/icon/gou.png');
				background-size: 100% 100%;
				background-repeat: no-repeat;
				background-position: center center;
				position: relative;
				top: 4upx;
				margin-right: 10upx;
			}
			.unselected{
				display: inline-block;
				width: 28upx;
				height: 28upx;
				background-image: url('../../static/img/icon/meigou.png');
				background-size: 100% 100%;
				background-repeat: no-repeat;
				background-position: center center;
				position: relative;
				top: 4upx;
				margin-right: 8upx;
			}
		}
		.signIn{
			width: 458upx;
			height: 86upx;
			line-height: 86upx;
			border-radius: 43upx;
			text-align: center;
			color: #fff;
		}

	}
</style>
