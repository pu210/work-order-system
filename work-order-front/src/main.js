import { createApp } from 'vue'
import App from './App.vue'
import {createPinia} from 'pinia'
import router from './router/router.js';
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate';

const pinia = createPinia()
        .use(piniaPluginPersistedstate);


createApp(App).use(router).use(pinia).mount('#app')
