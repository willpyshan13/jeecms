<template>
	<div class="my-loading"><i class="my-loading-i" v-if="isload"></i><span>{{load}}</span></div>
</template>

<script>
	export default {
		name: 'myloading',
		data() {
			return {}
		},
		props: {
			load: {
				type: String,
				required: true,
				default: '加载更多'
			},
			isload: {
				type: Boolean,
				default: false
			}
		},
		created() {
			var that = this
			document.body.onscroll = function() {
				//变量scrollTop是滚动条滚动时，距离顶部的距离
				var scrollTop =
					document.documentElement.scrollTop || document.body.scrollTop; //变量windowHeight是可视区的高度
				var windowHeight =
					document.documentElement.clientHeight || document.body.clientHeight; //变量scrollHeight是滚动条的总高度
				var scrollHeight =
					document.documentElement.scrollHeight || document.body.scrollHeight; //滚动条到底部的条件

				
			};
		},
		methods: {
			getData() {
				//此次用来加载数据（对应加载不同page下数据）
				this.page = this.page + 1;
				/*发送请求*/
			},
			initScroll() {
				this.scroll = new BScroll(this.$refs.bscroll, {
					click: true,
					scrollbar: true,
					//上拉
					pullUpLoad: {
						threshold: 50
					}
				});
				//上拉
				this.scroll.on('pullingUp', () => {
					this.getData();
				})
			},
		}
	}
</script>

<style >
	.my-loading {
		padding: 50upx 0;
		text-align: center;
		font-size: 24upx;
		color: #999999;
	}
	
	.my-loading .my-loading-i {
		background-image: url('../../static/img/icon/jiazai.png');
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
</style>
