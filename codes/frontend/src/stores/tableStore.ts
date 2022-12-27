import { onMounted, computed } from "vue"
import { defineStore } from "pinia"
import type { CasinoTable } from "../types/casino"
import type { BlackjackTable, BlackjackPlayer } from "../types/blackjack"
import { fetchTables } from "../components/composables/communication/http"
import type { TableCard } from "@/types/casino"
import { useCounterStart } from "../components/composables/timing/clock"

export const useTableStore = defineStore("tableStore", {
	state: () => ({
		tables: [] as TableCard[],
		table: {} as BlackjackTable, //{} as any //{} as CasinoTable,
		command: {} as string,
		commandPlayerId: {} as string,
		player: {} as BlackjackPlayer,
		counter: {} as number
	}),
	getters: {
		getTables(state) {
			return state.tables
		},
		getTable(state) {
			return state.table
		},
		getCounter(state) {
			return state.counter
		}
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
		async login(player: BlackjackPlayer) {
			this.player = player
			console.log("store Login:" + this.player)
		},
		reduceCounter() {
			let counterCurrent = this.counter
			counterCurrent--
			this.counter = counterCurrent
		},
		showInitialDeal() {
			console.log("STORE INITIAL DEAL")
			// useCounterStart();
		}
	}
})
