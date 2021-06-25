import config from '../../config.js'

export default {
	data () {
		return {
			$baseUrl: config.baseUrl
		}
	},
	methods: {
		$loading (title) {
			uni.showLoading({
				title
			})
		},
		$message (title, icon = 'none') {
			uni.showToast({
				title,
				icon
			})
		},
		$nav (url) {
			if (url) {
				uni.navigateTo({
					url
				})
			}
		},
		$back (delta = 1) {
			uni.navigateBack({
				delta
			})
		}
	}
}
