import { onMounted, computed } from "vue"
import { defineStore } from "pinia"

import { fetchTables } from "../components/composables/communication/http"
import type { TableCard } from "@/types/casino"
export const tableStore = defineStore("tableStore", {
  state: () => ({
    tables: Array<TableCard>,
  }),
  getters: {
    getTables(state) {
      return state.tables
    },
  },
  actions: {
    async populateStore() {
      try {
        this.tables = await fetchTables()
      } catch (error) {
        alert(error)
        console.log(error)
      }
    },
  },
  
})
