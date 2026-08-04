import { createApp } from 'vue'
import App from './App.vue'
import {createPinia} from 'pinia'
import router from './router/router.js';
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate';
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
import 'bootstrap-icons/font/bootstrap-icons.css'

const pinia = createPinia()
        .use(piniaPluginPersistedstate);


createApp(App).use(router).use(pinia).mount('#app')
