import { defineStore } from "pinia"
import type { BlackjackTable, BlackjackPlayer } from "../types/blackjack"
import { useTablesFetch } from "../components/composables/communication/http"
import { Games } from "@/types/casino"
import type { TableCard } from "@/types/TableCard"

export const useBlackjackStore = defineStore("blackjackStore", {
	state: () => ({
		tables: [] as TableCard[],
		table: {} as BlackjackTable,
		command: {} as string,
		player: {} as BlackjackPlayer
	}),
	getters: {
		getTables(state) {
			return state.tables
		},
		getTable(state) {
			return state.table
		},
		getPlayer(state) {
			return state.player
		}
	},
	actions: {
		async populateStore() {
			try {
				let tables = await useTablesFetch(Games.BLACKJACK)
				this.tables = tables.sort((a, b) => a.thresholds.minimumBet - b.thresholds.minimumBet)
			} catch (error) {
				alert(error)
			}
		},
		logout(empty) {
			this.player = empty
		}
	}
})
