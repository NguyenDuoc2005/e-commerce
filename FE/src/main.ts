import { router } from '@/routes/router'
import { VueQueryPlugin } from '@tanstack/vue-query'
import Antd, { message } from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import { createPinia } from 'pinia'
import { createApp } from 'vue'
import 'vue3-toastify/dist/index.css'
import App from '@/App.vue'
import './index.scss'
import './assets/style/style.css'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
import 'swiper/css'
import 'swiper/css/navigation'
import 'swiper/css/pagination'

;(window as any).global = window

message.config({
  top: '80px',
  duration: 2,
  maxCount: 3,
})

const app = createApp(App)

app.use(router)
app.use(Antd)
app.use(VueQueryPlugin)
app.use(createPinia())

app.mount('#app')
