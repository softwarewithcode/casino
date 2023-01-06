import { onMounted, computed } from "vue"
import { defineStore } from "pinia"
import type { CasinoTable } from "../types/casino"
import type { BlackjackTable, BlackjackPlayer } from "../types/blackjack"
import { fetchTables } from "../components/composables/communication/http"
import type { TableCard } from "@/types/casino"
import { useStartCounter } from "../components/composables/timing/clock"

const blackjackGameType = "blackjack"
export const useBlackjackStore = defineStore("blackjackStore", {
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
		},
		getPlayer(state) {
			return state.player
		}
	},
	actions: {
		async populateStore() {
			try {
				this.tables = await fetchTables(blackjackGameType)
			} catch (error) {
				alert(error)
			}
		},
		async login(player: BlackjackPlayer) {
			this.player = player
		},
		reduceCounter() {
			let counterCurrent = this.counter
			counterCurrent--
			this.counter = counterCurrent
		}
	}
})
