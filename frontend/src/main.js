import { createApp } from 'vue'
import { createPinia } from 'pinia'
// Element Plus 组件由 unplugin-vue-components 按需引入；
// 这里只补三块：v-loading 指令注册 + 程序化调用的 Message/MessageBox 样式
import { ElLoading } from 'element-plus'
import 'element-plus/es/components/loading/style/css'
import 'element-plus/es/components/message/style/css'
import 'element-plus/es/components/message-box/style/css'
import './style.css'
import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.directive('loading', ElLoading.directive)

app.directive('img-fade', {
  mounted(el) {
    el.classList.add('img-fade')
    const onLoad = () => el.classList.add('is-loaded')
    if (el.complete) onLoad()
    else el.addEventListener('load', onLoad)
  }
})

app.mount('#app')
