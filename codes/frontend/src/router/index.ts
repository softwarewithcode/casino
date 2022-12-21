import { createRouter, createWebHistory } from "vue-router";
import { fetchTables } from "../components/composables/communication/http";
import type { TableDescription } from "@/types/casino"
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      name: "casino",
      component: () => import("../views/Casino.vue"),
    },
    {
        path: "/:id",
        name: "blackjack",
        props: true,
        component: () => import("../views/BlackjackTable.vue"),
      },
  ],
});

export default router;
