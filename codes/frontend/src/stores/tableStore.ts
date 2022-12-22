import { onMounted, computed } from "vue";
import { defineStore } from "pinia";
import type { CasinoTable, } from "../types/casino";
import type { BlackjackTable, } from "../types/blackjack";
import { fetchTables } from "../components/composables/communication/http";
import type { TableCard } from "@/types/casino";
export const useTableStore = defineStore("tableStore", {
  state: () => ({
    tables: [] as TableCard[],
    table: {} as BlackjackTable ,//{} as any //{} as CasinoTable,
    //command: {} as string
  }),
  getters: {
    getTables(state) {
      return state.tables;
    },
    getTable(state) {
        return state.table;
      },
  },
  actions: {
    async populateStore() {
      try {
        this.tables = await fetchTables();
      } catch (error) {
        alert(error);
        console.log(error);
      }
    },
  },
});
