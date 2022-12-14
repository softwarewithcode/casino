import { defineStore } from "pinia"
import type { BlackjackTable, BlackjackPlayer } from "../types/blackjack"
import { useTablesFetch } from "../components/composables/communication/http"
import type { TableCard } from "@/types/casino"

const blackjack = "blackjack"
export const useBlackjackStore = defineStore("blackjackStore", {
	state: () => ({
		tables: [] as TableCard[],
		table: {} as BlackjackTable,
		command: {} as string,
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
				this.tables = await useTablesFetch(blackjack)
			} catch (error) {
				alert(error)
			}
		},
		async login(player: BlackjackPlayer) {
			// To CasinoStore or rename?
			this.player = player
		},
		reduceCounter() {
			let counterCurrent = this.counter
			counterCurrent--
			this.counter = counterCurrent
		}
	}
})
