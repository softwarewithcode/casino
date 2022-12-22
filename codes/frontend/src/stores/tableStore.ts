import { onMounted, computed } from "vue"
import { defineStore } from "pinia"

import { fetchTables } from "../components/composables/communication/http"
import type { TableDescription } from "@/types/casino"
export const tableStore = defineStore("tableStore", {
  state: () => ({
    tables: Array<TableDescription>,
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