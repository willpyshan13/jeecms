<template>
	<view class="content personal">
		<page-head pageTitle="个人中心" @click="handleReg"></page-head>
		<view class="personal-top">
			<view @click="gotoDetails()" class="top-left"  v-if="!admin&&userImg" :style="'background-image: url('+userImg+');'">
			</view>
			<view @click="gotoDetails()" class="top-left" v-else style="background-image: url('static/img/icon/yidenglu.png')">
			</view>
			<view class="top-right">
				<text @click="gotoDetails()" class="name Medium" v-text="dataField.username"></text>
				<text @click="gotoDetails()" class="signature" v-if="dataField.sign" v-text="dataField.sign">这个人太懒了啥也没有！</text>
				<text @click="gotoDetails()" class="signature" v-else >这个人太懒了啥也没有！</text>
				<view class="nature">
					<img v-if='levelIcon' :src="levelIcon" alt="">
					<view class="nature-text">
						<text class="signature">积分：</text>
						<text class="signature-num" v-text="dataField.integral">2000</text>
					</view>
				</view>
			</view>
		</view>
		<!-- 分隔 -->
		<view class="divide"></view>
		<view class="personal-list">
			
			<view class="personal-several" @click="toCollections">
				<view class="several ">
					<view class="several-left">
						<image src="../../../static/img/geren-icon01@2x.png"></image>
						<text class="Medium headline">我的收藏</text>
					</view>
					<view class="several-right">
						<uni-icons class="iconfont iconwodeyemianjiantou"></uni-icons>
					</view>
				</view>
			</view>
			<view class="personal-several" @click="toInteraction">
				<view class="several ">
					<view class="several-left">
						<image src="../../../static/img/geren-icon02@2x.png"></image>
						<text class="Medium headline">我的互动</text>
					</view>
					<view class="several-right">
						<uni-icons class="iconfont iconwodeyemianjiantou"></uni-icons>
					</view>
				</view>
			</view>
			<view class="personal-several" @click="toLike">
				<view class="several ">
					<view class="several-left">
						<image src="../../../static/img/geren-icon03@2x.png"></image>
						<text class="Medium headline">我的点赞</text>
					</view>
					<view class="several-right">
						<uni-icons class="iconfont iconwodeyemianjiantou"></uni-icons>
					</view>
				</view>
			</view>
			<view class="personal-several" @click="toContribution">
				<view class="several ">
					<view class="several-left">
						<image src="../../../static/img/geren-icon04@2x.png"></image>
						<text class="Medium headline">我的投稿</text>
					</view>
					<view class="several-right">
						<uni-icons class="iconfont iconwodeyemianjiantou"></uni-icons>
					</view>
				</view>
			</view>
			
			<view @click="gotoEmail()" class="personal-several">
				<view class="several ">
					<view class="several-left">
						<image src="../../../static/img/geren-icon05@2x.png"></image>
						<text class="Medium headline">邮箱</text>
					</view>
					<view class="several-right">
						<uni-icons class="iconfont iconwodeyemianjiantou"></uni-icons>
					</view>
				</view>
			</view>
			<view @click="changePassword()" class="personal-several">
				<view class="several ">
					<view class="several-left">
						<image src="../../../static/img/geren-icon06@2x.png"></image>
						<text class="Medium headline">修改密码</text>
					</view>
					<view class="several-right">
						<uni-icons class="iconfont iconwodeyemianjiantou"></uni-icons>
					</view>
				</view>
			</view>
			<view @click="gotoDetails()" class="personal-several">
				<view class="several ">
					<view class="several-left">
						<image src="../../../static/img/geren-icon07@2x.png"></image>
						<text class="Medium headline">个人信息</text>
					</view>
					<view class="several-right">
						<uni-icons class="iconfont iconwodeyemianjiantou"></uni-icons>
					</view>
				</view>
			</view>
			
		</view>
		<view class="divide"></view>
		<view class="dropUp" @click="dropOut()">退出账户</view>
		<view class="Return home" >
			<view class="returnHome" @click="toHome">				
					<img src="../../../static/img/icon/shouye-b.png" alt="">
			</view>
		</view>
		<neilmodal :show="showModal" :showContent="true" @close="modelClose" :title="title" @cancel="modelClose" @confirm="confirmdel"></neilmodal>
	</view>

</template>

<script>
	import neilmodal from '../../../components/common/neil-modal.vue';
	export default {
		name: "personal",
		components: {
			neilmodal
		},
		data() {
			return {
				dataField:{},
				levelIcon:'',
				showModal:false,
				title:'确定退出JEECMS新闻网？',
				admin:true,
				userImg:'',
			}
		},
		mounted() {
			this.fetchMemberinfo()
		},
		activated(){
			this.fetchMemberinfo()
　　	 },
		methods:{
			fetchMemberinfo(){
				this.$request.fetchMemberinfo().then(res => {
					if(res.code == 200){
						this.admin=res.data.admin
						this.dataField = res.data.dataField
						this.levelIcon = res.data.levelIcon
						this.userImg = res.data.dataField.userImg
					}
				})
			},
			
			
			gotoDetails(){
				uni.navigateTo({
					url:'/pages/personal/user/details'
				})
			},
			gotoEmail(){
				uni.navigateTo({
					url:'/pages/personal/email/index',
				})
			},
			// 跳到我的收藏
			toCollections(){
				uni.navigateTo({
					url:'/pages/personal/collections/collections',
				})
			},
			// 跳到我的点赞
			toLike(){
				uni.navigateTo({
					url:'/pages/personal/like/like',
				})
			},
			// 跳到我的投稿
			toContribution(){
				uni.navigateTo({
					url:'/pages/personal/contribution/contribution',
				})
			},
			// 跳到我的互动
			toInteraction(){
				uni.navigateTo({
					url:'/pages/personal/interaction/interaction',
				})
			},
			// 跳到修改密码页面
			changePassword(){
				uni.navigateTo({
					url:'/pages/personal/password/index',
				})
			},
			// 退出
			dropOut(){
				this.showModal = true
			},
			// 关闭遮罩
			modelClose() {
				this.showModal = false
			},
			// 确定退出
			confirmdel() {
				this.$request.fetchLogout().then(res => {
					if(res.code == 200){
						localStorage.setItem('JEECMS-Auth-Token', '')
						window.location.href = this.$baseUrl
						// uni.navigateTo({
						// 	url:'/pages/index/index'
						// })
					}
				})
			},
			// 回到首页
			toHome(){
				window.location.href = this.$baseUrl
			}
		},
		onReachBottom(){
			
		}
	}
</script>

<style lang="scss" scoped>
	.Return>.returnHome{
		width:90upx;
		height:90upx;
		display: flex;
		align-items: center;
		justify-content: center;
		background:rgba(255,255,255,1);
		box-shadow:0px 2upx 13upx 0px rgba(18,39,104,0.1);
		border-radius:50%;
	}
	
	.Return>.returnHome {
		display: flex;
		align-items: center;
		justify-content: center;
		margin-bottom: 230upx;
	}
	
	.Return>.returnHome img{
		width: 32upx;
		height: 36upx;
	}
	.Return{
		position: fixed;
		right:40upx;
		bottom: 10%;
		z-index: 1000;
	}
	.content.personal {
		.uni-page-head{
			background-color: #FFFFFF;
		}
		background-color: #F5F5FA;
		.personal-top{
			background-color: #FFFFFF;
			padding: 50upx 40upx;
			display: flex;
			.top-left{
				width: 150upx;
				height: 150upx;
				overflow: hidden;
				border-radius: 50%;
				background-size:cover ;
				background-position: center center;
				background-repeat: no-repeat;
				margin-right: 21upx;
			}
			.top-right{
				padding-top: 19upx;
				.name{
					font-size: 32upx;
					color: #333333;
					display: block;
					margin-bottom: 19upx;
				}
				.signature{
					font-size: 22upx;
					color: #999999;
					display: block;
					margin-bottom: 19upx;
				}
				.nature{
					display: flex;
					img{
						height: 40upx;
						margin-right: 27upx;
					}
					.nature-text{
						display: flex;
						line-height: 40upx;
						
						text{
							font-size: 24upx;
						}
						.signature{
							color: #666666;
						}
						.signature-num{
							color: #333333;
						}
					}
					
				}
			}
		}
		.divide{
			width: 100%;
			height: 20upx;
			background-color: #F5F5FA;
		}
		.personal-list{
			background-color: #FFFFFF;
			.personal-several{
				padding: 37upx 42upx 0 38upx;
				.several{
					font-size: 28upx;
					padding-bottom: 37upx;
					border-bottom: 1upx solid #F0F0F0;
					display: flex;
					justify-content: space-between;
					line-height: 28upx;
					.several-left{
						display: flex;
						.headline{
							color: #333;
						}
						image{
							width: 28upx;
							height: 28upx;
							margin-right: 20upx;
						}
						text{
							font-size: 28upx;
						}
					}
				}
			}
			.personal-several:last-child .several{
				border-bottom: none;
			}
		}
		.dropUp{
			width: 100%;
			height: 100upx;
			line-height: 100upx;
			color: #E30B20;
			font-size: 30upx;
			background-color: #FFFFFF;
			text-align: center;
		}
	}
</style>
