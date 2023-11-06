import { defineStore } from "pinia"

import { useTablesFetch } from "../components/composables/communication/http"
import { Games } from "@/types/casino"
import type { TableCard } from "@/types/TableCard"
import { type RoulettePlayer, type RouletteTable, type RouletteBetPosition, BetName, type RoundResult } from "@/types/roulette"

export const useRouletteStore = defineStore("rouletteStore", {
	state: () => ({
		tables: [] as TableCard[],
		table: {} as RouletteTable,
		betPositions: [] as RouletteBetPosition[], // tableBetPositions belongs directly to RouletteTable, but is required to serialize and transfer only once
		redNumbers: [] as number[],
		blackNumbers: [] as number[],
		serverCommand: {} as String,
		mainCharacter: {} as RoulettePlayer
	}),
	getters: {
		getTableCards(state) {
			return state.tables
		},
		getTable(state) {
			return state.table
		},
		getMainCharacter(state) {
			return state.mainCharacter
		},
		getTableBetPositions(state) {
			return state.betPositions
		},
		getBlackNumbers: state => state.blackNumbers,
		getRedNumbers: state => state.redNumbers,
		getReversedResultBoard: state => state.table.wheel.resultBoard.slice().reverse()
	},
	actions: {
		async populateStore() {
			try {
				let tables = await useTablesFetch(Games.ROULETTE)
				this.tables = tables.sort((a, b) => a.thresholds.minimumBet - b.thresholds.minimumBet)
			} catch (error) {
				alert(error)
			}
		},
		logout(empty) {
			this.mainCharacter = empty
		}
	}
})
