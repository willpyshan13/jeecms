<template>
	<view class="content like">
		<page-head pageTitle="我的点赞"></page-head>
		<view v-if="list.length">
			<view v-for="(item,index2 ) in list" :key="index2">
				<view class="item">
					<!-- 视频 -->
					<view class="item-con" :style="item.type==2?'padding-top:40upx':''">
						<!-- 上部 -->
						<view class="parentComment" v-if="item.type==1&&item.commentType==1">
							<view class="head">
								<image :src="item.headImage==''&&item.username==''?'../../../static/img/icon/weidenglu.png':item.headImage==''?'../../../static/img/icon/yidenglu.png':_data.$baseUrl+item.headImage" mode="" class="headImage"></image>
								<view class="article">
									<view class="name Medium" v-text="item.username?item.username:'匿名用户'"></view>
									<view class="time Light" v-text="item.time"></view>
								</view>
							</view>
							<view class="p-item Light" v-text="item.comment"></view>
						</view>

						<view class="parentComment" v-if="item.type==1&&item.commentType==2">
							<view class="head">
								<image :src="item.headImage==''&&item.username==''?'../../../static/img/icon/weidenglu.png':item.headImage==''?'../../../static/img/icon/yidenglu.png':_data.$baseUrl+item.headImage" mode="" class="headImage"></image>
								<view class="article">
									<view class="name Medium" v-text="item.username?item.username:'匿名用户'"></view>
									<view class="time Light" v-text="item.time"></view>
								</view>
							</view>
							<view class="p-item Light" v-text="'回复 @'+item.replyUsername+': '+item.comment" v-if="item.replyUsername&&item.replyUsername!=''"></view>
							<view class="p-item Light" v-text="'回复 @匿名用户: '+item.comment" v-else></view>
						</view>
						<!-- 视频 -->
						<view :class="item.type==1?'item-top already':'item-top'" v-if="item.mobileContent.modelId=='963'">
							<view class="important-top-video">
								<view class="video-play">
									<!-- <img class="big-img" :src="item.mobileContent.imageJson.resource.url&&item.mobileContent.imageJson.resource.url!==''?_data.$baseUrl+item.mobileContent.imageJson.resource.url:'../../../static/img/index/tu-b-02.png'"
									 @click="navigate(item.url)"> -->
									 <img class="big-img" v-if="item.mobileContent.imageJson.shoujidatu&&item.mobileContent.imageJson.shoujidatu.url" :src="item.mobileContent.imageJson.shoujidatu.url" @click="navigate(item.url)">
									 <view v-else class="no-video" @click="navigate(item.url)"></view>
									<img class="play-icon" src="../../../static/img/icon/bofan-b.png">
									<view class="play-time p-item">
										
										<img src="../../../static/img/icon/liualnliang-s-bai.png" alt="" class="view">
										<view class="Regular span-item left" v-text="item.mobileContent.views"></view>
										<view class="you">
											<img src="../../../static/img/icon/xiaobofang.png" class="bofang">
											<view class="Regular span-item right" v-text="item.mobileContent.videoJson.videoSrc.resourceDate&&item.mobileContent.videoJson.videoSrc.resourceDate!=''?item.mobileContent.videoJson.videoSrc.resourceDate:'00:00'"></view>
										</view>

									</view>
								</view>
							</view>
							<view class="important-top-no">
								<view class="Medium h4-item" @click="navigate(item.mobileContent.url)" v-text="item.mobileContent.title.slice(0,22)"></view>
								<view class="Regular p-item">
									<view class="span-item Semilight" v-text="item.mobileContent.channelName"></view>
									<view class="span-item" v-text="item.mobileContent.publishTime"></view>
								</view>
							</view>
						</view>
						<!-- 图片 -->
						<view :class="item.type==1?'item-top already photos':'item-top photos'" v-else-if="item.mobileContent.modelId=='913'">
							<view class="top" v-if="item.mobileContent.imageJson.shoujidatu&&item.mobileContent.imageJson.shoujidatu.url">
								<img :src="item.mobileContent.imageJson.shoujidatu.url" @click="navigate(item.mobileContent.url)">
								<view class="num Semilight span-item" v-if="item.mobileContent.imageJson.photos.length" v-text="item.mobileContent.imageJson.photos.length+'图'"></view>
							</view>
							<view class="Medium h4-item" @click="navigate(item.mobileContent.url)" v-text="item.mobileContent.title.slice(0,22)"></view>
							<view class="from">
								<view class="Semilight span-item" v-text="item.mobileContent.channelName"></view>
								<view class="Semilight span-item" v-text="item.mobileContent.publishTime"></view>
							</view>
						</view>
						<!-- 新闻 -->
						<view :class="item.type==1?'item-top already':'item-top'" v-else-if="item.mobileContent.modelId=='912'">
							<view class="important-top-yes">
								<view class="important-top-left">
									<view class="Medium h4-item" @click="navigate(item.mobileContent.url)" v-text="item.mobileContent.title.slice(0,22)"></view>
									<view class="Regular p-item">
										<view class="Regular span-item" v-text="item.mobileContent.channelName"></view>
										<view class="span-item" v-text="item.mobileContent.publishTime"></view>
									</view>
								</view>
								<view class="important-top-right" v-if="item.mobileContent.imageJson.shoujiliebiaotu&&item.mobileContent.imageJson.shoujiliebiaotu.url">
									<img :src="item.mobileContent.imageJson.shoujiliebiaotu.url" @click="navigate(item.mobileContent.url)">
								</view>
							</view>
						</view>

						<view :class="item.type==1?'item-top already':'item-top'" v-else>
							<view class="important-top-no">
								<view class="Medium h4-item" @click="navigate(item.mobileContent.url)" v-text="item.mobileContent.title.slice(0,22)"></view>
								<view class="Regular p-item">
									<view class="Regular span-item" v-text="item.mobileContent.channelName"></view>
									<view class="span-item" v-text="item.mobileContent.publishTime"></view>
								</view>
							</view>
						</view>
						<!-- 底部 -->
						<view class="important-bottom">
							<view class="div-item" @click="navigateCommentList(item.mobileContent.id)" v-if="item.type==1">
								<view class="image"></view>
								<view class="Light span-item" v-text="item.type==2&&item.mobileContent.comments>0?item.mobileContent.comments:item.type==1&&item.commentSum>0?item.commentSum:'评论'"></view>
							</view>
							<view class="div-item" @click="navigate(item.mobileContent.url)" v-else>
								<view class="image"></view>
								<view class="Light span-item" v-text="item.type==2&&item.mobileContent.comments>0?item.mobileContent.comments:item.type==1&&item.commentSum>0?item.commentSum:'评论'"></view>
							</view>
							<view class="div-item">
								<view :class="item.like?'image like':'image'" @click="cancelLike({id:item.mobileContent.id,type:item.type,commentId:item.commentId})"></view>

								<view :class="item.like?'Light span-item red':'Light span-item'" v-text="item.type==2&&item.mobileContent.ups>0?item.mobileContent.ups:item.type==1&&item.ups>0?item.ups:'点赞'"></view>
							</view>
							</div>
						</view>


					</view>

					<view class="grey-blank">

					</view>
				</view>
			</view>
			<myloading :load="load" :isload="isload"></myloading>

		</view>
		<myloading load="加载中" :isload="true" v-if="begin&&!list.length"></myloading>
		<view class="blank-page" v-if="!begin&&!list.length">
			<img src="../../../static/img/personal/wudianzan.png" alt="">
			<view class="p-item Semilight">
				你还没有任何点赞哦~
			</view>
		</view>
	</view>


</template>

<script>
	//上拉加载组件
	import myloading from '../../../components/common/loading.vue';
	export default {
		name: 'tougao',
		components: {
			myloading
		},
		data() {
			return {
				url: '',
				list: [],
				data: {
					page: 1,
					size: 10
				},
				load: '加载更多',
				isload: false,
				last: false,
				uploading: true,
				begin: true
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


		},
		methods: {
			// 点赞内容
			getList() {
				this.$request.getMyLikes(this.data).then(res => {
					if (res.code == 200) {
						this.list = res.data.content.map(item => {
							return {
								...item,
								like: true
							}
						})
						if (this.list.length < 10) {
							this.load = '没有更多了'
							this.isload = false
							this.last = true
						}
						this.begin = false
						
					}
				})
			},
			// 取消点赞
			cancelLike(obj) {
				if (obj.type == 1) {
					if (this.list.find(item => item.commentId == obj.commentId).like) {
						this.$request.cancelLike({
							id: obj.commentId,
							type: 1
						}).then(res => {
							if (res.code == 200) {
								this.list = this.list.filter(item => item.commentId != obj.commentId)
							}

						})
					} else {

					}
				} else {
					if (this.list.find(item => item.mobileContent.id == obj.id).like) {
						this.$request.cancelLike({
							id: obj.id,
							type: 2
						}).then(res => {
							this.list = this.list.filter(item => item.mobileContent.id != obj.id)

						})
					}
				}

			},

			// 跳转页面
			navigate(urll) {
				window.location.href = urll
				// uni.navigateTo({
				// 	url: '/pages/index/index?urll=' + urll
				// })
			},
			// 跳转到评论列表页面
			navigateCommentList(contentId){
				window.location.href = this.$baseUrl+'/content-commentList.htm?contentId=' + contentId
				// uni.navigateTo({
				// 	url: '/pages/index/index?contentId=' + contentId
				// })
			},

			getContentPage() {
				this.$request.getCollections(this.data).then(res => {
					if (res.code == 200) {
						this.uploading = true
						this.list = this.list.concat(res.data.content.map(item => {
							return {
								...item,
								like: true
							}
						}))
						this.last = res.data.last
						if (!this.last) {
							this.isload = true
							that.load = '加载更多'
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
	.blank-page {
		padding: 0 70upx;

		img {
			width: 610upx;
			height: 410upx;
			margin-top: 147upx;
			margin-bottom: 36upx;
		}

		.p-item {
			font-size: 30upx;
			font-family: PingFang SC;
			color: rgba(153, 153, 153, 1);
			line-height: 48upx;
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
		padding-top: 30upx;
	}

	.parentComment {
		padding-bottom: 20upx;

		.head {
			display: flex;
			align-items: center;
			margin-bottom: 20upx;

			.headImage {
				width: 80upx;
				height: 80upx;
				border-radius: 50%;
				margin-right: 30upx;
				
			}
			

			.article {
				.name {
					font-size: 30upx;
					font-family: PingFang SC;
					color: rgba(51, 51, 51, 1);
					margin-bottom: 20upx;
				}

				.time {
					font-size: 22upx;
					font-family: PingFang SC;
					color: rgba(153, 153, 153, 1);
				}
			}
		}

		.p-item {
			font-size: 30upx;
			font-family: PingFang SC;
			color: rgba(51, 51, 51, 1);
			line-height: 50upx;
		}
	}

	.item-top {
		padding-bottom: 30upx;
		border-bottom: 1upx solid rgba(240, 240, 240, 1);
	}

	.item-top.photos,
	.item-top.already.photos {
		border: none;
		padding-bottom: 0;
	}

	.item-top.already {
		padding: 30upx 0;
		border-top: 1upx solid rgba(240, 240, 240, 1);
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

	.important-bottom .div-item img,
	.important-bottom .div-item .image {
		margin-right: 14upx;
		width: 30upx;
		height: 28upx;
	}

	.important-bottom .div-item .image {
		background-image: url('../../../static/img/icon/dianzan-l.png');
		background-position: center center;
		background-size: 100% 100%;
		background-repeat: no-repeat;
	}

	.important-bottom .div-item .image.like {
		background-image: url('../../../static/img/icon/dianzan-l-h.png');
	}

	.important-bottom .div-item:first-child .image {
		background-image: url('../../../static/img/icon/pinglun-i.png');
	}

	.important-bottom .div-item .span-item {
		color: #666666;
		font-size: 28upx;
	}

	.important-bottom .div-item:nth-child(2) .span-item {
		font-size: 26upx;
	}

	.important-bottom .div-item:nth-child(2) .span-item.red {
		color: rgba(227, 11, 32, 1);
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
