import Vue from 'vue'
import Vuex from 'vuex'
import newsColum from './modules/app.js'
Vue.use(Vuex)

export default new Vuex.Store({
	modules:{
         newsColum
    }
})
