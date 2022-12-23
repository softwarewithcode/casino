import { createRouter, createWebHistory } from "vue-router";
import { fetchTables } from "../components/composables/communication/http";
import type { TableCard } from "@/types/casino"
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      name: "casino",
      component: () => import("../views/Casino.vue"),
    },
    {
        path: "/blackjack/:tableId",
        name: "blackjack",
        props: true,
        component: () => import("../views/BlackjackTableView.vue"),
      },
  ],
});

export default router;
