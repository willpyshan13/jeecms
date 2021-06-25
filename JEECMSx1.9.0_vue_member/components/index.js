import Vue from 'vue'

let contexts = require.context('./common', true, /\.vue$/)
contexts.keys().forEach(component => {
  let componentEntity = contexts(component).default
  if (componentEntity.name) {  
	Vue.component(componentEntity.name, componentEntity)
  }
})
