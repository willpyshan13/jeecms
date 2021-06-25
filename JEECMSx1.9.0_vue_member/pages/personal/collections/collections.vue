<template>
	<view class="content shoucang">
		<page-head pageTitle="我的收藏" :title="!list.length?'':checkdo?'取消':'编辑'" @click="edit"></page-head>
		<view v-if="list.length">
			<view v-for="(item,index2 ) in list" :key="index2">
				<view :class="item.ischecked?'ischecked item':checkdo?'checkdo item':'item'">
					<view :class="item.ischecked?'ischecked round':checkdo?'round checkdo':'round'" @click="gouxuan(item.id)"></view>
					<view :class="item.ischecked?'ischecked item-con':checkdo?'checkdo item-con':'item-con'">
						<!-- 视频 -->
						<view class="item-top already" v-if="item.contentMobileVo.modelId=='963'">
							<view class="important-top-video">
								<view class="video-play">
									<img class="big-img" v-if="item.contentMobileVo.imageJson.shoujidatu&&item.contentMobileVo.imageJson.shoujidatu.url" :src="item.contentMobileVo.imageJson.shoujidatu.url" @click="navigate(item.contentMobileVo.url)">
									<view v-else class="no-video" @click="navigate(item.contentMobileVo.url)"></view>
									<img class="play-icon" src="../../../static/img/icon/bofan-b.png">
									<view class="play-time p-item">
										<img src="../../../static/img/icon/liualnliang-s-bai.png" alt="" class="view">
										<view class="Regular span-item left" v-text="item.contentMobileVo.views"></view>
										<view class="you">
											<img src="../../../static/img/icon/xiaobofang.png" class="bofang">
											<view class="Regular span-item right" v-text="item.contentMobileVo.videoJson.videoSrc.resourceDate&&item.contentMobileVo.videoJson.videoSrc.resourceDate!=''?item.contentMobileVo.videoJson.videoSrc.resourceDate:'00:00'"></view>
										</view>
										
									</view>
								</view>
							</view>
							<view class="important-top-no">
								<view class="Medium h4-item" @click="navigate(item.contentMobileVo.url)" v-text="item.title.slice(0,22)"></view>
								<view class="Regular p-item">
									<view class="span-item Semilight" v-text="item.contentMobileVo.channelName"></view>
									<view class="span-item" v-text="item.collectionTime"></view>
								</view>
							</view>
						</view>
						
						<!-- 图片 -->
						<view class="item-top already" v-else-if="item.contentMobileVo.modelId=='913'">
							<view class="top" v-if="item.contentMobileVo.imageJson.shoujidatu&&item.contentMobileVo.imageJson.shoujidatu.url">
								<img :src="item.contentMobileVo.imageJson.shoujidatu.url" @click="navigate(item.contentMobileVo.url)">
								<view class="num Semilight span-item" v-if="item.contentMobileVo.imageJson.photos.length" v-text="item.contentMobileVo.imageJson.photos.length+'图'"></view>
							</view>
							<view class="Medium h4-item" @click="navigate(item.contentMobileVo.url)" v-text="item.title.slice(0,22)"></view>
							<view class="from">
								<view class="Semilight span-item" v-text="item.contentMobileVo.channelName"></view>
								<view class="Semilight span-item" v-text="item.collectionTime"></view>
							</view>
						</view>
						
						
						<!-- 多图 -->
						<!-- <view class="item-top already" v-else-if="item.contentMobileVo.imageJson&&item.contentMobileVo.imageJson.photos&&item.contentMobileVo.imageJson.photos.length">
							<view class="top">
								<img :src="item.contentMobileVo.imageJson.photos[0].resourcesSpaceData.url&&item.contentMobileVo.imageJson.photos[0].resourcesSpaceData.url!=''?_data.$baseUrl+item.contentMobileVo.imageJson.photos[0].resourcesSpaceData.url:'../../../static/img/index/tu-s-01.png'"
								 @click="navigate(item.contentMobileVo.url)">
								<view class="num Semilight span-item" v-text="item.contentMobileVo.imageJson.photos.length+'图'"></view>

							</view>
							<view class="Medium h4-item" @click="navigate(item.contentMobileVo.url)" v-text="item.title.slice(0,22)"></view>
							<view class="from">
								<view class="Semilight span-item" v-text="item.contentMobileVo.channelName"></view>
								<view class="Semilight span-item" v-text="item.collectionTime"></view>
							</view>
						</view> -->
						
						<!-- 新闻 -->
						<view class="item-top already" v-else-if="item.contentMobileVo.modelId=='912'">
							<view class="important-top-yes">
								<view class="important-top-left">
									<view class="Medium h4-item" @click="navigate(item.contentMobileVo.url)" v-text="item.title.slice(0,22)"></view>
									<view class="Regular p-item">
										<view class="Regular span-item" v-text="item.contentMobileVo.channelName"></view>
										<view class="span-item" v-text="item.collectionTime"></view>
									</view>
								</view>
								<view class="important-top-right" v-if="item.contentMobileVo.imageJson.shoujiliebiaotu&&item.contentMobileVo.imageJson.shoujiliebiaotu.url">
									<img :src="item.contentMobileVo.imageJson.shoujiliebiaotu.url" @click="navigate(item.contentMobileVo.url)">
								</view>
							</view>
						</view>
						
						<!-- 单图 -->
						<!-- <view class="item-top already" v-else-if="item.contentMobileVo.imageJson&&item.contentMobileVo.imageJson.resource&&item.contentMobileVo.imageJson.resource.url!=''">
							<view class="important-top-yes">
								<view class="important-top-left">
									<view class="Medium h4-item" @click="navigate(item.contentMobileVo.url)" v-text="item.title.slice(0,22)"></view>
									<view class="Regular p-item">
										<view class="Regular span-item" v-text="item.contentMobileVo.channelName"></view>
										<view class="span-item" v-text="item.collectionTime"></view>
									</view>
								</view>
								<view class="important-top-right">
									<img :src="item.contentMobileVo.imageJson.resource.url&&item.contentMobileVo.imageJson.resource.url!=''?_data.$baseUrl+item.contentMobileVo.imageJson.resource.url:'../../../static/img/index/tu-s-01.png'"
									 @click="navigate(item.contentMobileVo.url)">
								</view>
							</view>
						</view> -->

						<view class="item-top already" v-else>
							<view class="important-top-no">
								<view class="Medium h4-item" @click="navigate(item.contentMobileVo.url)" v-text="item.title.slice(0,22)"></view>
								<view class="Regular p-item">
									<view class="Regular span-item" v-text="item.contentMobileVo.channelName"></view>
									<view class="span-item" v-text="item.collectionTime"></view>
								</view>
							</view>
						</view>


					</view>


				</view>
			</view>
			<myloading :load="load" :isload="isload"></myloading>

		</view>
		<myloading load="加载中" :isload="true" v-if="begin&&!list.length"></myloading>
		<view class="blank-page" v-if="!begin&&!list.length">
			<img src="../../../static/img/personal/wushoucang.png" alt="">
			<view class="p-item Semilight">
                            哎呀，暂无收藏
                        </view>
			<view class="p-item Semilight">你这家伙太懒了吧</view>
		</view>
		<view class="bottom" v-if="checkdo">
			<view class="del-al Light" @click="deleteAll">一键清空</view>
			<view :class="ischeckedNum?'del Light red':'del Light'" @click="delitem" :disabled="!ischeckedNum">删除</view>
		</view>
		<neilmodal :show="showModal" :showContent="true" @close="modelClose" :title="title" @cancel="modelClose" @confirm="confirmdel"></neilmodal>
	</view>
</template>

<script>
	//上拉加载组件
	import myloading from '../../../components/common/loading.vue';
	import neilmodal from '../../../components/common/neil-modal.vue';
	export default {
		name: 'tougao',
		components: {
			myloading,
			neilmodal
		},
		data() {
			return {
				url: '',
				list: [],
				data: {
					page: 1,
					size: 10
				},
				navList: ['已发布', '待审核'],
				load: '加载更多',
				isload: false,
				last: false,
				uploading: true,
				checkdo: false,
				showModal: false,
				title: '',
				type: 1,
				begin:true
			}
		},
		created() {
			this.getList()
		},
		filters: {
			ellipsis(value) {
				if (!value) return ''
				if (value.length > 22) {
					return value.slice(0, 22) + '...'
				}
				return value
			}
		},
		onReachBottom() {
			var that = this;
			if (this.uploading) {
				this.uploading = false
				if (!this.last) {
					this.isload = true
					this.load = '加载中'
					this.data.page += 1
					this.getContentPage()
				} else {
					this.isload = false
					this.load = '没有更多了'
				}
			}

		},
		computed: {
			ischeckedNum() {
				if (this.list.find(item => item.ischecked)) {
					return true
				} else {
					return false
				}
			},
			ischeckedIds() {
				return this.list.filter(item => item.ischecked).map(item => {
					return item.id
				})
			}
		},
		methods: {
			// 收藏内容
			getList() {
				this.$request.getCollections(this.data).then(res => {
					if (res.code == 200) {
						this.list = res.data.content.map(item => {
							return {
								...item,
								ischecked: false
							}
						})
						if (this.list.length < 10) {
							this.load = '没有更多了'
							this.isload = false
							this.last = true
						}
						
							this.begin=false
						
					}
				})
			},

			// 跳转页面
			navigate(urll) {
				window.location.href = urll
				// uni.navigateTo({
				// 	url: '/pages/index/index?urll=' + urll
				// })
			},
			// 编辑取消编辑操作
			edit() {
				this.checkdo = !this.checkdo
				this.list = this.list.map(item => {
						return {
							...item,
							ischecked: false
						}
					})
				
			},
			// 勾选取消勾选操作
			gouxuan(id) {
				this.list.find(item => item.id == id).ischecked = !this.list.find(item => item.id == id).ischecked
			},
			// 删除操作
			delitem() {
				if (this.list.filter(item => item.ischecked).length >= 1) {
					this.title = '确定删除这' + this.list.filter(item => item.ischecked).length + '条收藏？'
					this.showModal = true
					this.type = 1
				}
			},
			// 一键清空操作
			deleteAll() {
				this.title = '确定要清空吗？清空后将永久无法找回，请谨慎操作！'
				this.showModal = true
				this.type = 2
			},
			// 关闭遮罩
			modelClose() {
				this.showModal = false
			},
			// 确定删除
			confirmdel() {
				if (this.type == 1) {
					if (this.list.filter(item => item.ischecked).length == 1) {
						this.$request.cancelCollection({
							id: this.list.find(item => item.ischecked).id
						}).then(res => {
							if (res.code == 200) {
								this.getList()
								this.checkdo = false
							}
						})
					} else {
						this.$request.cancelCollections({
							ids: this.ischeckedIds
						}).then(res => {
							if (res.code == 200) {
								this.getList()
								this.checkdo = false
							}
						})
					}
				} else {
					this.$request.deleteAllCollections().then(res => {
						if (res.code == 200) {
							this.getList()
							this.checkdo = false
						}
					})
				}

			},
			getContentPage() {
				this.$request.getCollections(this.data).then(res => {
					if (res.code == 200) {
						this.uploading = true
						this.list = this.list.concat(res.data.content.map(item => {
							return {
								...item,
								ischecked: false
							}
						}))
						this.last = res.data.last
						if (!this.last) {
							this.isload = true
							this.load = '加载更多'
						} else {
							this.isload = false
							this.load = '没有更多了'
						}
					}
					
				})
			}
		}
	};
</script>

<style lang="scss" scoped>
	.blank-page{
		padding: 0 70upx;
		img{
			width: 610upx;
			height: 410upx;
			margin-top: 147upx;
			margin-bottom: 37upx;
		}
		.p-item{
			font-size:30upx;
			font-family:PingFang SC;
			color:rgba(153,153,153,1);
			line-height:48upx;
			text-align: center;
		}
	}
	.bottom {
		position: fixed;
		left: 0;
		bottom: 0;
		border-top: 1upx solid rgba(245, 245, 245, 1);
		background: #FFFFFF;
		width: 100%;
		height: 86upx;
		display: flex;
		align-items: center;
		

		.del-al,
		.del {
			margin: 23upx 0;
			width: 49.5%;
			line-height:40upx;
			font-size: 28upx;
			font-family: PingFang SC;
			color: rgba(51, 51, 51, 1);
			background: #FFFFFF;
			text-align: center;
			line-height: 40upx;
			height: 40upx;
			border: none ;
		}

		.del {
			color: rgba(227, 11, 32, 1);
			opacity: 0.6;
			border-left: 1upx solid rgba(245,245,245,1);
		}

		.del.red {
			opacity: 1;
		}
	}

	

	.channel {
		height: 80upx;
		width: 100%;
	}

	.channel .ul-con {
		display: flex;
		padding: 0 40upx 0 40upx;
	}

	.channel .li-item {
		width: 31%;
		display: flex;
		flex-direction: row;
		justify-content: center;
	}

	.channel .li-item:first-child {
		justify-content: flex-start;
	}


	.channel .li-item .a-item {
		display: block;
		font-size: 26upx;
		font-family: PingFang SC;
		padding-top: 24upx;
		padding-bottom: 24upx;
		color: rgba(102, 102, 102, 1);
	}

	.channel .li-item .a-item.mark-red {
		border-bottom: 4upx solid rgba(227, 11, 32, 1);
	}

	.channel {
		border-bottom: 1upx solid rgba(245, 245, 245, 1);
	}

	.channel .ul-con {
		height: 80upx;
	}

	.channel .li-item .a-item {
		font-size: 28upx;
		padding-top: 28upx;
		padding-bottom: 26upx;
		line-height: 1;
		color: rgba(51, 51, 51, 1);
	}

	.channel .li-item .mark-red {
		font-size: 32upx;
		padding-top: 24upx;
		margin-bottom: 1upx;
		padding-bottom: 20upx;
	}

	.con-item {
		display: none;
	}

	.con-item.show {
		display: block;
	}

	.grey-blank {
		width: 750upx;
		height: 20upx;
		background: rgba(245, 245, 250, 1);
	}

	.item-con {
		width: 670upx;
		border-bottom: 1upx solid rgba(240, 240, 240, 1);
		padding: 40upx 0;

	}

	.item {
		display: flex;
		padding: 0 40upx;
		align-items: center;

		.round {
			width: 40upx;
			height: 40upx;
			flex-shrink: 1;
			box-sizing: border-box;
			background-size: 100% 100%;
			background-position: center center;
			margin-right: 40upx;
			display: none;

		}

		.round.checkdo {
			display: block;
			background-image: url('../../../static/img/icon/weixuan.png');
		}

		.round.ischecked {
			display: block;
			background-image: url('../../../static/img/icon/yixuan.png');
		}
	}

	.item.checkdo,.item.ischecked {
		padding-right: 0;
		padding-right: -40upx;
		width: 830upx;
	}

	.item-con.checkdo ,.item-con.ischecked{
		margin-right: -40upx;
		flex-grow: 1;
		flex-shrink: 1;
		width: 670upx;
	}

	.important-top-video {
		margin-bottom: 22upx;
	}

	.important-top-video .video-play {
		position: relative;
	}

	.important-top-video .video-play .big-img {
		width: 670upx;
		height: 380upx;
		border-radius: 10upx;
	}
	.important-top-video .video-play .no-video {
		width: 670upx;
		height: 380upx;
		background-image: url('../../../static/img/index/noImg.png');
		background-position: center center;
		background-repeat: no-repeat;
		background-size: 150upx 150upx;
		border-radius: 10upx;
	}
	.important-top-video .video-play .play-icon {
		position: absolute;
		left: 50%;
		top: 50%;
		width: 100upx;
		height: 100upx;
		margin-left: -50upx;
		margin-top: -50upx;
	}

	.important-top-video .video-play .play-time {
		position: absolute;
		width: 100%;
		bottom: 4upx;
		height: 80upx;
		line-height: 80upx;
		border-bottom-left-radius: 10upx;
		border-bottom-right-radius: 10upx;
		background: linear-gradient(0deg, rgba(0, 0, 0, 0.6) 0%, rgba(0, 0, 0, 0) 100%);
		opacity: .85;
		.you{
			display: flex;
			position: absolute;
			right: 19upx;
			bottom: 20upx;
			align-items: center;
			flex-direction: row;
		}
	}
	
	.important-top-video .video-play .play-time img.view {
		width: 25upx;
		height: 20upx;
		position: absolute;
		bottom: 20upx;
		left: 20upx;
	}
	
	.important-top-video .video-play .play-time img.bofang {
		width: 14upx;
		height: 18upx;
		margin-right: 13upx;
	}
	
	.important-top-video .video-play .play-time .span-item.right {
		font-size: 22upx;
		color: #FFFFFF;
	}
	.important-top-video .video-play .play-time .span-item.left {
		font-size: 22upx;
		color: #FFFFFF;
		position: absolute;
		left: 55upx;
		bottom: 20upx;
	}

	.important-top-video video {
		display: none;
		width: 100%;
	}

	.important-top-no .h4-item {
		font-size: 32upx;
		font-family: PingFang SC;
		color: rgba(51, 51, 51, 1);
		line-height: 48upx;
	}

	.important-top-no .p-item {
		display: flex;
		margin-top: 16upx;
	}

	.important-top-no .p-item .span-item:first-child {
		color: #333333;
		font-size: 20upx;
		margin-right: 50upx;
	}

	.important-top-no .p-item .span-item:last-child {
		color: #999999;
		font-size: 20upx;
	}

	.important-bottom {
		display: flex;
		justify-content: space-between;
		margin-top: 30upx;
	}

	.important-bottom .div-item {
		display: flex;
		color: #666666;
		font-size: 28upx;
		line-height: 28upx;
		align-items: flex-end;
	}

	.important-bottom .div-item img {
		margin-right: 14upx;
		width: 30upx;
		height: 28upx;
	}

	.important-bottom .div-item .span-item {
		color: #666666;
		font-size: 28upx;
	}

	.important-bottom .div-item:nth-child(2) .span-item {
		font-size: 26upx;
	}

	.important-top-yes {
		display: flex;
		justify-content: space-between;
	}

	.important-top-yes .important-top-left {
		width: auto;
		padding-top: 15upx;
		margin-right: 15upx;
	}

	.important-top-yes .important-top-right {
		width: auto;
	}

	.important-top-yes .important-top-left .h4-item {
		color: #333333;
		font-size: 32upx;
		line-height: 50upx;
	}

	.important-top-yes .important-top-left .p-item {
		display: flex;
		margin-top: 20upx;
	}

	.important-top-yes .important-top-left .p-item .span-item:first-child {
		color: #333333;
		font-size: 26upx;
		margin-right: 45upx;
	}

	.important-top-yes .important-top-left .p-item .span-item:last-child {
		color: #999999;
		font-size: 26upx;
	}

	.important-top-yes .important-top-right img {
		width: 218upx;
		height: 160upx;
		border-radius: 10upx;
	}

	.item-top .top {
		position: relative;
		width: 670upx;
		height: 380upx;
		margin-bottom: 22upx;
	}

	.item-top .top img {
		position: absolute;
		top: 0;
		left: 0;
		width: 670upx;
		height: 380upx;
		border-radius: 10upx;
	}

	.item-top .top .num {
		position: absolute;
		right: 20upx;
		bottom: 20upx;
		padding: 9upx 20upx;
		background: rgba(0, 0, 0, 1);
		opacity: 0.7;
		border-radius: 20upx;
		font-size: 22upx;
		font-family: PingFang SC;
		color: rgba(255, 255, 255, 1);
	}

	.item-top .h4-item {
		font-size: 32upx;
		font-family: PingFang SC;
		color: rgba(51, 51, 51, 1);
		margin-bottom: 14upx;
		line-height: 48upx;
	}

	.item-top .from {
		display: flex;
		align-items: center;
	}

	.item-top .from .span-item:first-child {
		font-size: 26upx;
		font-family: PingFang SC;
		color: rgba(51, 51, 51, 1);
		margin-right: 48upx;
	}

	.item-top .from .span-item:last-child {
		font-size: 26upx;
		font-family: PingFang SC;
		color: rgba(153, 153, 153, 1);
	}
</style>
