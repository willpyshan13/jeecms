import Vue from 'vue'
import App from './App'
import config from './config.js'
import store from './store'
import globalMixin from '@/common/js/global'
import rules from '@/common/js/rules'
import request from '@/api'
import '@/components/index'

Vue.config.productionTip = false

Vue.prototype.$store = store
Vue.prototype.$rules = rules
Vue.prototype.$baseUrl = config.baseUrl
Vue.prototype.$tsetUrl = config.tsetUrl
Vue.prototype.$request = request

App.mpType = 'app'

Vue.mixin(globalMixin)

const app = new Vue({
	store,
	...App
})
app.$mount()
