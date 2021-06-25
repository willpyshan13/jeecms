import request from '@/api'
import { desEncrypt } from '@/common/js/util'

export default {
	namespaced: true,
	state: {
		user: {}
	},
	mutations: {
	    SET_USER (state, data) {
	      state.user = Object.assign({}, state.user, data)
	    }
	},
	actions: {
	    async fetchLogin ({ state, commit, rootState, dispatch }, data) {
	      data.desStr = await desEncrypt(JSON.stringify({ pStr: data.desStr }))
	      return request.fetchLogin(data).then(res => {
	        if (res.code === 200 && !res.data.nextNeedCaptcha) {
	          commit('SET_USER', res.data)
	          dispatch('fetchSetting')
	        }
	        return res
	      })
	    }
	}
}