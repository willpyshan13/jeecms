<template>
	<view class="content tougao">
		<page-head pageTitle="我的投稿"></page-head>
		<view class="channel" v-if="begin||list1.length||list2.length">
			<view :class="current == 0 ? 'ul-con' : 'current ul-con'" >
				<view class="li-item" @click="tabChange(index)" v-for="(a,index) in navList" :key="index">
					<view :class="index==current?'mark-red Medium a-item':'Regular a-item'" v-text="a">
					</view>
				</view>
			</view>
		</view>
		<view class="content-con">
			<view :class="current==index1?'con-item show':'con-item'" v-for="(a,index1) in navList" :key="index1">
				<view v-if="list.length">
					<view v-for="(item,index2 ) in list" :key="index2">
						<view class="item" >
							<view class="item-con">
								<!-- 视频 -->
								<view :class="index1==0?'already item-top':'item-top'" v-if="item.modelId=='963'">
									<view class="important-top-video">
										<view class="video-play">
											 <img class="big-img" v-if="item.imageJson.shoujidatu&&item.imageJson.shoujidatu.url" :src="item.imageJson.shoujidatu.url" @click="navigate(item.url)">
											 <view v-else class="no-video" @click="navigate(item.url)"></view>
											<img class="play-icon" src="../../../static/img/icon/bofan-b.png">
											<view class="play-time p-item">
												<img src="../../../static/img/icon/liualnliang-s-bai.png" alt="" class="view" v-if="current==0">
												<view class="Regular span-item left" v-text="item.views" v-if="current==0"></view>
												<view class="you">
													<img src="../../../static/img/icon/xiaobofang.png" class="bofang">
													<view class="Regular span-item right" v-text="item.videoJson.videoSrc.resourceDate&&item.videoJson.videoSrc.resourceDate!=''?item.videoJson.videoSrc.resourceDate:'00:00'"></view>
												</view>
												
											</view>
										</view>
									</view>
									<view class="important-top-no">
										<view class="Medium h4-item" @click="navigate(item.url)" v-text="item.title.slice(0,22)"></view>
										<view class="Regular p-item">
											<view class="span-item Semilight" v-text="item.channelName"></view>
											<view class="span-item" v-text="item.createTime"></view>
										</view>
									</view>
								</view>
								<!-- 图片 -->
								<view :class="index1==0?'already item-top':'item-top'" v-else-if="item.modelId=='913'">
									<view class="top" v-if="item.imageJson.shoujidatu&&item.imageJson.shoujidatu.url">
										<img :src="item.imageJson.shoujidatu.url" @click="navigate(item.url)">
										<view class="num Semilight span-item" v-if="item.imageJson.photos.length" v-text="item.imageJson.photos.length+'图'"></view>
									</view>
									<view class="Medium h4-item" @click="navigate(item.url)"v-text="item.title.slice(0,22)"></view>
									<view class="from">
										<view class="Semilight span-item" v-text="item.channelName"></view>
										<view class="Semilight span-item" v-text="item.createTime"></view>
									</view>
								</view>
								<!-- 新闻 -->
								<view :class="index1==0?'already item-top':'item-top'" v-else-if="item.modelId=='912'">
									<view class="important-top-yes">
										<view class="important-top-left">
											<view class="Medium h4-item" @click="navigate(item.url)" v-text="item.title.slice(0,22)"></view>
											<view class="Regular p-item">
												<view class="Regular span-item" v-text="item.channelName"></view>
												<view class="span-item" v-text="item.createTime"></view>
											</view>
										</view>
										<view class="important-top-right" v-if="item.imageJson.shoujiliebiaotu&&item.imageJson.shoujiliebiaotu.url">
											<img :src="item.imageJson.shoujiliebiaotu.url" @click="navigate(item.url)">
										</view>
									</view>
								</view>
								
								<view :class="index1==0?'already item-top':'item-top'" v-else>
									<view class="important-top-no">
										<view class="Medium h4-item" @click="navigate(item.url)" v-text="item.title.slice(0,22)"></view>
										<view class="Regular p-item">
											<view class="Regular span-item" v-text="item.channelName"></view>
											<view class="span-item" v-text="item.createTime"></view>
										</view>
									</view>
								</view>

								<view class="important-bottom" v-if="current==0">
									
									<view class="div-item" @click="navigate(item.url)">
										<img src="../../../static/img/icon/pinglun-s.png">
										<view class="Light span-item" v-text="item.comments"></view>
									</view>
									<view class="div-item" @click="navigate(item.url)">
										<img src="../../../static/img/icon/dianzan-s.png">
										<view class="Light span-item" v-text="item.ups"></view>
									</view>
									</div>
								</view>
							</view>
							<view class="grey-blank">

							</view>

						</view>
						
						
						

					</view>
					<myloading :load="current==0?load1:load2" :isload="current==0?isload1:isload2"></myloading>

				</view>
				<myloading load="加载中" :isload="true" v-if="begin&&!list.length"></myloading>
				<view class="blank-page" v-if="!begin&&!list.length">
					<img src="../../../static/img/personal/wutougao.png" alt="">
					<view class="p-item Semilight">
				                    你还没有投稿哦
				    </view>
				</view>
			</view>
		</view>
	</view>

</template>

<script>
	//上拉加载插件
	import myloading from '../../../components/common/loading.vue';
	export default {
		name: 'tougao',
		components: {
			myloading
		},
		data() {
			return {
				url: '',
				list1: [],
				list2: [],
				current: 0,
				data1: {
					status: 2,
					page: 1,
					size: 10
				},
				data2: {
					status: 1,
					page: 1,
					size: 10
				},
				navList: ['已发布', '待审核'],
				load1: '加载更多',
				isload1: false,
				uploading1: true,
				load2: '加载更多',
				isload2: false,
				uploading2: true,
				begin:true
			}
		},
		created() {
			this.getList1(this.data1)
			this.getList2(this.data2)
		},
		mounted() {
			console.log(this.list1)
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
		computed: {
			list() {
				if (this.current == 0) {
					return this.list1
				} else {
					return this.list2
				}
			},
			
		},
		onReachBottom(){
			var that = this;
			if (this.current == 0) {
				if (this.uploading1) {
					this.uploading1 = false
					if (this.data1.size==this.list1.length) {
						this.load1 = '加载中'
						this.isload1 = true
						this.data1.size += 10
						this.getContentPage()				
					} else {
						this.load1 = '没有更多了'
						this.isload1 = false
					}
				}
			} else {
				if (this.uploading2) {
					this.uploading2 = false
					if (this.data2.size==this.list2.length) {
						this.load2 = '加载中'
						this.isload2 = true
						this.data2.size += 10
						this.getContentPage()
					} else {
						this.load2 = '没有更多了'
						this.isload2 = false
					}
				}
			}
		},
		methods: {
			// 已发布
			getList1(data1) {
				this.$request.getTougao(data1).then(res => {
					if (res.code == 200) {
						this.list1 = res.data.content
						if (this.list1.length < 10) {
							this.load1 = '没有更多了'
							this.isload1 = false
						}
						this.begin=false
					}
				})
			},
			// 待审核
			getList2(data2) {
				this.$request.getTougao(data2).then(res => {
					if (res.code == 200) {
						this.list2 = res.data.content
						if (this.list2.length < 10) {
							this.load2 = '没有更多了'
							this.isload2 = false
						}
					}
				})
			},
			// 切换导航
			tabChange(index) {
				this.current = index
			},
			// 跳转页面
			navigate(urll) {
				window.location.href = urll
				// uni.navigateTo({
				// 	url: '/pages/index/index?urll=' + urll
				// })
			},
			getContentPage() {
				if (this.current == 0) {
					this.$request.getTougao(this.data1).then(res => {
						if (res.code == 200) {
							if (this.data1.size>this.list1.length) {
								this.load1 = '没有更多了'
								this.isload1 = false
							} else {
								this.load1 = '加载更多'
								this.isload1 = true
							}
							this.list1 = res.data.content
							this.uploading1 = true
						}
					})
				} else {
					this.$request.getTougao(this.data2).then(res => {
						if (res.code == 200) {
							this.last2 = res.data.last
							if (this.data2.size>this.list2.length) {
								this.load2 = '没有更多了'
								this.isload2 = false
							} else {
								this.load2 = '加载更多'
								this.isload2 = true
							}
							this.list2 = res.data.content
							this.uploading2 = true
						}

					})
				}

			}


		}
	};
</script>

<style lang="scss" scoped>
	.blank-page{
		padding-left: 76upx;
		padding-top: 143upx;
		img{
			width: 610upx;
			height: 402upx;
			margin-bottom: 43upx;
		}
		.p-item{
			font-size:30upx;
			font-family:PingFang SC;
			color:rgba(153,153,153,1);
			line-height:48upx;
			text-align: center;
		}
	}
	.my-loading {
		padding: 50upx 0;
		text-align: center;
		font-size: 24upx;
		color: #999999;
	}

	.my-loading .my-loading-i {
		background-image: url('../../../static/img/icon/jiazai.png');
		width: 24upx;
		height: 24upx;
		background-size: 100% 100%;
		background-repeat: no-repeat;
		background-position: center center;
		display: inline-block;
		margin-right: 10upx;
		position: relative;
		top: 3upx;
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
		padding: 40upx;
	}

	.item-top.already {
		padding-bottom: 30upx;
		border-bottom: 1upx solid rgba(240, 240, 240, 1);
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
		font-size: 26upx;
		margin-right: 50upx;
	}

	.important-top-no .p-item .span-item:last-child {
		color: #999999;
		font-size: 26upx;
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
