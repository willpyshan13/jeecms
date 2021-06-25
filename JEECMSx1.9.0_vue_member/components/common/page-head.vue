<template name="page-head">
	<view class="uni-page-head">
		<view>
			<uni-icons class="iconfont iconfanhui" @click='back()'></uni-icons>
		</view>
		<view v-if="pageTitle" :style="{'padding-left':search?'65rpx':''}" class="pageTitle">
			<text class="Medium" :class="classTitle">{{pageTitle}}</text>
		</view>
		<view class="uni-page-head-title" :class="fontWeight" ><image v-if="search" class="search" @click="gosearch" src="../../static/img/icon/sousuo-hei.png" mode=""></image><text @click="click">{{title}}</text></view>
	</view>
</template>
<script>
	export default {
		name: "page-head",
		props: {
			title: {//右侧按钮
				type: String,
				default: ""
			},
			url:{
				type: String,
				default: ""
			},
			search: {//是否显示搜索按钮
				type: Boolean,
				default: false
			},
			pageTitle: {//中间标题
				type: String,
				default: ""
			},
			fontWeight:{
				type: String,
				default: "Medium"
			},
			classTitle:{
				type: String,
				default: ""
			},
			
		},
		data () {
			return {
				hasBackPage: false
			}
		},
		methods:{
			click(){
				this.$emit('click')
			},
			back(){
				if(this.url){
					uni.navigateTo({
						url:this.url
					})
				}else{
					window.history.go(-1)
				}
			},
			gosearch(){
				this.$emit('gosearch')
			}
		}
	}
</script>
<style lang="scss" scoped>
	.uni-page-head-title{
		display: flex;
		justify-content: center;
		align-items: center;
	}
	.search{
		width: 30rpx;
		height: 30rpx;
		flex: 1;
		// box-sizing: border-box;
		// margin-right: 35rpx;
		margin-right: 35rpx;
	}
	.title{
		font-size: 32rpx;
		font-family: PingFang;
		font-weight: 500 !important;
		// color: #333333;
		line-height: 40rpx;
	}
</style>