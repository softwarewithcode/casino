import { defineStore } from "pinia"
import { TableAction, GameAction, type HoldemPlayer, type HoldemTable } from "@/types/texasHoldem"
import { useTablesFetch } from "../components/composables/communication/http"
import { Games } from "@/types/casino"
import { type Card } from "@/types/cards"
import type { TableCard } from "@/types/TableCard"
import { usePlayerEqualsChecker } from "@/components/composables/common/table"
import { useStopCounter } from "@/components/composables/timing/clock"

export const useTexasHoldemStore = defineStore("texasHoldemStore", {
	state: () => ({
		tables: [] as TableCard[],
		table: {} as HoldemTable,
		command: {} as string,
		mainPlayer: {} as HoldemPlayer,
		lastActor: {} as HoldemPlayer,
		holeCards: [] as Card[]
	}),
	getters: {
		tableCards(state) {
			return state.tables
		},
		callAmount(state): number | undefined {
			return state.mainPlayer.actions?.find(action => action.type === GameAction.CALL)!.range.max
		},
		hasMainPlayerHoleCards(state) {
			return state.mainPlayer.cards && state.mainPlayer.cards.length >= 2
		}
	},
	actions: {
		async populateStore() {
			try {
				let tables = await useTablesFetch(Games.TEXAS_HOLDEM)
				this.tables = tables
			} catch (error) {
				alert(error)
			}
		},
		resetLastActionFromPlayer(userName: string) {
			if (this.mainPlayer && this.mainPlayer.userName === userName) {
				this.mainPlayer.lastAction = undefined
				return
			}
			let player = this.table.seats.find(seat => seat.player && seat.player.userName === userName)?.player
			if (player) {
				//	debugger
				player.lastAction = undefined
			}
		},
		clearTableAndPlayers() {
			useStopCounter()
			this.table.seats
				.filter(seat => seat.player)
				.map(seat => seat.player)
				.forEach(player => {
					player.actions = []
					player.lastAction = undefined
					player.cards = undefined
					player.chipsOnTable = 0
				})
			this.table.pots = []
			this.mainPlayer.actions = []
			this.mainPlayer.cards = undefined
			this.mainPlayer.chipsOnTable = 0
		},

		logout() {
			this.mainPlayer = {} as HoldemPlayer
		}
	}
})
