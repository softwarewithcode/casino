import { createRouter, createWebHistory } from "vue-router"
import { ViewName } from "@/components/composables/common/Views"
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
			name: ViewName.BLACKJACK_FRONT,
			component: () => import("../views/blackjack/BlackjackFront.vue")
		},
		{
			path: "/blackjack/:tableId",
			name: ViewName.BLACKJACK_TABLE,
			props: true,
			component: () => import("../views/blackjack/BlackjackTableView.vue")
		},
		{
			path: "/texas-holdem/front/",
			name: ViewName.TEXAS_HOLDEM_FRONT,
			props: true,
			component: () => import("../views/texas-holdem/TexasHoldemFront.vue")
		},
		{
			path: "/texas-holdem/:tableId",
			name: ViewName.TEXAS_HOLDEM_TABLE,
			props: true,
			component: () => import("../views/texas-holdem/TexasHoldemTableView.vue")
		},
		{
			path: "/roulette/front/",
			name: ViewName.ROULETTE_FRONT,
			props: true,
			component: () => import("../views/roulette/RouletteFront.vue")
		},
		{
			path: "/roulette/:tableId",
			name: ViewName.ROULETTE_TABLE,
			props: true,
			component: () => import("../views/roulette/RouletteTable.vue")
		}
	]
})

export default router
