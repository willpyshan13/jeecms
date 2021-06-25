<template>
	<view class="content user-details">
		<page-head pageTitle="个人信息" :title='operateText' @click="operate()" fontWeight='Regular'></page-head>
		<view class="header">
			<view @click="showShadow()" class="header-img" v-if="!admin&&userImg" :style="'background-image: url('+userImg+');'">
				<view v-if="!admin" class="header-camera" style="background-image: url('static/img/icon/xiangji.png')"></view>
			</view>
			<view @click="showShadow()" class="header-img" v-else style="background-image: url('static/img/icon/yidenglu.png')">
				<view v-if="!admin" class="header-camera" style="background-image: url('static/img/icon/xiangji.png')"></view>
			</view>
		</view>
		<view class="divide"></view>
		<uni-popup ref="popup" class="popup-box" type="bottom">
			<!-- <view class="Kerley"></view> -->
			<view @click="take()" class=" header-none header-phone">拍照
				<view class="header-mask">拍照</view>
			</view>
			<!-- <view class="Kerley"></view> -->
			<view @click="photo()" class="header-none header-phone">我的相册<view class="header-mask">我的相册</view></view>
			<view @click="popupNone()" class="header-none ">取消<view class="header-mask" style="font-size:28upx;line-height:72upx">取消</view></view>
		</uni-popup>
		<view class="details-from">
			<view class="details-list">
				<text class="details-label">真实姓名</text>
				<input type="text" :class="isOperate?'darkColor':'simpleColor'" :disabled="!isOperate" :maxlength="realnameMax"
				 v-model="detailsData.realname" :placeholder="realnamePla" />
			</view>
			<view class="details-list" style="display: none;">
				<text class="details-label">性别</text>
				<label class="gender-radio">
					<radio-group class="gender-radio-group" @change="radioChange">
						
						<label class="gender-radio-group-label">
							<view v-if="genderIcon!=1" class="unselected"></view>
							<view v-if="genderIcon==1&&!isOperate" class="nan-jinyong"></view>
							<view v-if="genderIcon==1&&isOperate" class="nan"></view>
							<radio class="choice" style="opacity: 0;" :disabled="!isOperate" value="1" :checked="1 == detailsData.isGender" /><text
							 :class="isOperate?'darkColor':'simpleColor'">男</text>
						</label>
						
						<label class="gender-radio-group-label" style="margin-left: 80upx;">
							<view v-if="genderIcon!=2" class="unselected"></view>
							<view v-if="genderIcon==2&&!isOperate" class="nv-jinyong"></view>
							<view v-if="genderIcon==2&&isOperate" class="nv"></view>
							<radio class="choice" style="opacity: 0;" :disabled="!isOperate" value="2" :checked="2 == detailsData.isGender" /><text
							 :class="isOperate?'darkColor':'simpleColor'">女</text>
						</label>
					</radio-group>
				</label>
			</view>
			<view class="details-list">
				<text class="details-label">手机号</text>
				<input type="text" :class="isOperate?'darkColor':'simpleColor'" :disabled="!isOperate" :maxlength="telephoneMax"
				 v-model="detailsData.telephone" :placeholder="telephonePla" />
			</view>
			<view class="details-list" style="display: none;">
				<text class="details-label">个性签名</text>
				<input type="text" :class="isOperate?'darkColor':'simpleColor'" :disabled="!isOperate" :maxlength="signMax" v-model="detailsData.sign"
				 :placeholder="signPla" />
			</view>
		</view>
	</view>
</template>

<script>
	import uniPopup from "@/components/uni-popup/uni-popup.vue"
	export default {
		name: 'userDetails',
		components: {
			uniPopup
		},
		data() {
			return {
				operateText: '编辑',
				levelIcon: '',
				isOperate: false,
				Memberinfo: {},
				detailsData: {
					headerImg: {
						url: ''
					},
					realname: '',
					isGender: 1,
					sign: '',
					telephone: ''
				},
				realnamePla: '',
				telephonePla: '',
				signPla: '',
				rules: {
					telephone: [this.$rules.required('请输入手机号'), this.$rules.mobile()],
					realname: [this.$rules.required('请输入真实姓名')]
				},
				signMax: null,
				realnameMax: null,
				telephoneMax: null,
				genderIcon:1,
				admin:false,
				userImg:'',
				resourceId:''
			}
		},
		mounted() {
			this.fetchMemberinfo()
		},
		methods: {
			fetchMemberinfo() {
				this.$request.fetchMemberinfo().then(res => {
					if (res.code == 200) {
						this.detailsData = res.data.dataField
						if (this.detailsData.gender) {
							this.detailsData.isGender = this.detailsData.gender.value
							this.genderIcon = this.detailsData.isGender
						}
						this.userImg = res.data.dataField.userImg
						this.admin=res.data.admin
						this.levelIcon = res.data.levelIcon
						this.Memberinfo = res.data
						this.putPlaceholder()
					}
				})
			},
			popupNone(){
				this.$refs.popup.close()
			},
			operate() {
				if (this.isOperate) {
					this.finish()
				} else {
					this.operateText = '完成'
					this.isOperate = true
				}
			},
			finish() {
				if(this.resourceId){
					this.fetchMemberinfoCustom(this.resourceId)
				}
				this.$rules
					.validator(this.detailsData, this.rules)
					.then(() => {
						this.$request.fetchMemberinfoPut({
							'telephone': this.detailsData.telephone,
							'realname': this.detailsData.realname,
							'json': {
								sign: this.detailsData.sign,
								gender: {
									value: Number(this.detailsData.isGender)
								}
							}
						}).then(res => {
							if (res.code == 200) {
								this.$message('修改个人信息成功')
								this.operateText = '编辑'
								this.isOperate = false
							} else {
								this.$message(res.message)
							}
						})
						
					})
					.catch((e) => {
						this.$message(e)
					});
			},
			showShadow() {
				console.log(this.admin)
				console.log(this.operateText)
				if(this.operateText==='完成'&&this.admin==false){
					this.$refs.popup.open()
				}
			},
			take() {
				let _this = this
				console.log(_this.$baseUrl);
				uni.chooseImage({
					count: 1, //默认9
					sizeType: ['original', 'compressed'], //可以指定是原图还是压缩图，默认二者都有
					sourceType: ['camera'], //album 从相册选图，camera 使用相机，默认二者都有
					success: function(chooseImageRes) {
						let tempFilePaths = chooseImageRes.tempFilePaths
						uni.uploadFile({
							url: _this.$baseUrl+'/member/upload/o_upload', //仅为示例，非真实的接口地址
							filePath: tempFilePaths[0],
							name: 'uploadFile',
							formData: {
								addToRes: true,
							},
							header:{
								'JEECMS-Auth-Token': localStorage.getItem('JEECMS-Auth-Token'),
								'Redirect-Header': false,
							},
							success: (uploadFileRes) => {
								const data = JSON.parse(uploadFileRes.data)
								if (data.code == 200) {
									_this.detailsData.headerImg = {}
									_this.detailsData.headerImg.url = ''
									_this.detailsData.headerImg.url = data.data.fileUrl
									if(!data.data.fileUrl.includes("http")){
										_this.userImg  =  _this.$baseUrl+data.data.fileUrl
									}
									_this.$message('上传头像成功')
									_this.$refs.popup.close()
									_this.resourceId = data.data.resourceId
									// _this.fetchMemberinfoCustom(data.data.resourceId)
								}else{
									_this.$message(data.message)
								}
							},
							fail:(uploadFileRes)=>{
								const data = JSON.parse(uploadFileRes.data)
								_this.$message(data.message)
							},
							complete: (uploadFileRes)=> {
								console.log(uploadFileRes);
							}
						});

					}
				});
			},
			photo() {
				let _this = this
				uni.chooseImage({
					count: 1, //默认9
					sizeType: ['original', 'compressed'], //可以指定是原图还是压缩图，默认二者都有
					sourceType: ['album'], //album 从相册选图，camera 使用相机，默认二者都有
					success: function(chooseImageRes) {
						const tempFilePaths = chooseImageRes.tempFilePaths
						uni.uploadFile({
							url: _this.$baseUrl+'/member/upload/o_upload', //仅为示例，非真实的接口地址
							filePath: tempFilePaths[0],
							name: 'uploadFile',
							formData: {
								addToRes: true,
							},
							header:{
								'JEECMS-Auth-Token': localStorage.getItem('JEECMS-Auth-Token'),
								'Redirect-Header': false,
							},
							success: (uploadFileRes) => {
								console.log(uploadFileRes);
								const data = JSON.parse(uploadFileRes.data)
								if (data.code == 200) {
									_this.detailsData.headerImg = {}
									_this.detailsData.headerImg.url = ''
									_this.detailsData.headerImg.url = data.data.fileUrl
									if(!data.data.fileUrl.includes("http")){
										_this.userImg  =  _this.$baseUrl+data.data.fileUrl
									}
									_this.$message('上传头像成功')
									_this.resourceId = data.data.resourceId
									_this.$refs.popup.close()
									// _this.fetchMemberinfoCustom(data.data.resourceId)
								}else{
									_this.$message(data.message)
								}
							},
							fail:(uploadFileRes)=>{
								const data = JSON.parse(uploadFileRes.data)
								_this.$message(data.message)
							},
							complete: (uploadFileRes)=> {
								console.log(uploadFileRes);
							}
						});

					}
				});
			},
			fetchMemberinfoCustom(id) {
				id += ''
				this.$request.fetchMemberinfoCustom({
					'pic':id
				}).then(res => {
					if (res.code == 200) {
						this.fetchMemberinfo()
						this.$refs.popup.close()
					} else {
						this.$message(res.message)
					}
				})
			},
			//添加placeholder
			putPlaceholder() {
				let array = this.Memberinfo.renderingField.formListBase
				for (var i = 0; i < array.length; i++) {
					if (array[i].value.name == 'realname') {
						this.realnamePla = array[i].value.tip
						this.realnameMax = array[i].value.max
					} else if (array[i].value.name == 'telephone') {
						this.telephonePla = array[i].value.tip
						this.telephoneMax = array[i].value.max
					} else if (array[i].value.name == 'sign') {
						this.signPla = array[i].value.tip
						this.signMax = array[i].value.max
					}
				}
			},
			radioChange(evt) {
				if (this.isOperate) {
					this.detailsData.isGender = evt.target.value
					this.genderIcon = this.detailsData.isGender
					console.log(this.genderIcon)
				} else {
					return
				}

			}
		}
	}
</script>

<style lang="scss" >
	/deep/ .uni-sample-toast{
		z-index: 1200;
	}
	.content.user-details {
		width: 100%;
		.uni-page-head{
			.uni-page-head-title{
				font-weight: normal !important;
			}
		}
		.header {
			width: 100%;
			padding: 60upx 0 60upx;

			.header-img {
				width: 150upx;
				height: 150upx;
				margin: 0 auto;
				border-radius: 50%;
				background-size: cover;
				background-position: center center;
				background-repeat: no-repeat;
				position: relative;

				.header-camera {
					position: absolute;
					bottom: 0;
					right: 0;
					background-color: #fff;
					background-position: center center;
					background-repeat: no-repeat;
					background-size: 60%;
					width: 50upx;
					height: 50upx;
					border-radius: 50%;
				}
			}
		}

		.divide {
			width: 100%;
			height: 20upx;
			background-color: #F5F5FA;
		}

		.popup-box {
			z-index: 998;
			// background: rgba(0, 0, 0, .4);
			/deep/ .uni-popup__wrapper-box {
				border-radius: 0 !important;
				padding: 0  !important;
				background-color: transparent !important;
			}
			.header-title {
				width: 400upx;
				height: 100upx;
				line-height: 100upx;
				color: #333333;
				font-size: 32upx;
				text-align: center;
			}
			.header-none{
				font-size: 28upx;
				color: #333;
				margin: 20upx 0;
				text-align: center;
				width: 100%;
				background-color: #FFFFFF;
				padding: 23upx 0;
				position: relative;
			}
			.header-mask{
				position: absolute;
				top: 0;
				left: 50%;
				width: 0;
				height: 100%;
				transition: .8s;
				background-color: #e6e6e6;
				color: transparent;
				font-size: 32upx;
				line-height: 108upx;
			}
			.header-mask:nth-child(3){
				font-size: 28upx;
				line-height: 72upx;
			}
			.header-none:hover{
				.header-mask{
					width: 100%;
					left: 0;
					color: #333;
				}
			}
			.header-phone{
				font-size: 32upx;
				font-weight: 500;
				padding: 39upx 0;
				margin: 0;
			}
			.header-phone:nth-child(1){
				border-bottom: 1upx solid #f0f0f0;
			} 
			.Kerley {
				width: 320upx;
				margin: 0 auto;
				height: 2upx;
				background-color: #fafafa;
			}

			.header-alter {
				width: 400upx;
				height: 80upx;
				line-height: 80upx;
				color: #333333;
				font-size: 28upx;
				text-align: center;

			}

			.header-alter:hover {
				background-color: #E6E6E6;
				color: #E30B20;
			}
		}

		.details-from {
			padding: 0 40upx;

			.details-list {
				height: 100upx;
				line-height: 100upx;
				display: flex;
				width: 100%;
				border-bottom: 1upx solid #F0F0F0;

				.details-label {
					width: 168upx;
					color: #999999;
					font-size: 26upx;
				}

				input {
					width: 500upx;
					height: 100upx;
					line-height: 100upx;
				}

				.gender-radio {
					width: 276upx;

					.gender-radio-group {
						width: 100%;
						display: flex;
					}

					.gender-radio-group-label {
						height: 100upx;
						line-height: 100upx;
						display: flex;
						.nan {
							background-image: url('../../../static/img/icon/nan.png');
							width: 28upx;
							height: 28upx;
							border-radius: 50%;
							background-position: center center;
							background-size: 100% 100%;
							background-repeat: no-repeat;
							margin-top: 36upx;
						}

						.nan-jinyong {
							background-image: url('../../../static/img/icon/nan-jinyong.png');
							width: 28upx;
							height: 28upx;
							border-radius: 50%;
							background-position: center center;
							background-size: 100% 100%;
							background-repeat: no-repeat;
							margin-top: 36upx;
						}

						.nv {
							background-image: url('../../../static/img/icon/nv.png');
							width: 28upx;
							height: 28upx;
							border-radius: 50%;
							background-position: center center;
							background-size: 100% 100%;
							background-repeat: no-repeat;
							margin-top: 36upx;
						}
						.nv-jinyong {
							background-image: url('../../../static/img/icon/nv-jinyong.png');
							width: 28upx;
							height: 28upx;
							border-radius: 50%;
							background-position: center center;
							background-size: 100% 100%;
							background-repeat: no-repeat;
							margin-top: 36upx;
						}
						.choice {
							margin-left: -38upx;
						}
						.unselected{
							width: 28upx;
							height: 28upx;
							background-color: #F0F0F0;
							margin-top: 36upx;
							border-radius: 50%;
						}
					}
				}
			}

			.simpleColor {
				color: #666 !important;
				input{
					color: #666 !important;
				}
				.uni-input-input{
					color: #666 !important;
				}
				
			}

			.darkColor {
				color: #333 !important;
				input{
					color: #333 !important;
				}
				.uni-input-input{
					color: #333 !important;
				}
			}
		}
	}
</style>
