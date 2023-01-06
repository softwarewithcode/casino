import { createRouter, createWebHistory } from "vue-router"
import { fetchTables } from "../components/composables/communication/http"
import type { TableCard } from "@/types/casino"
const router = createRouter({
	history: createWebHistory(import.meta.env.BASE_URL),
	routes: [
		{
			path: "/",
			name: "casino",
			component: () => import("../views/Casino.vue")
		},
		{
			path: "/blackjack/front/",
			name: "blackjackFront",
			component: () => import("../views/blackjack/BlackjackFront.vue")
		},
		{
			path: "/blackjack/:tableId",
			name: "blackjack",
			props: true,
			component: () => import("../views/blackjack/BlackjackTableView.vue")
		}
	]
})

export default router
