import { createApp } from "vue"
import { createPinia } from "pinia"

import App from "./App.vue"
import router from "./router/router"

import "./assets/main.css"
import "./assets/cardsFromOpenGameart.png"
const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount("#app")
