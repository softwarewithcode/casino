import { useBlackjackStore } from "../../../../../stores/blackjackStore"
import router from "../../../../../router/router"
import { ServerCommand } from "@/types/servercommands"
import { useStartCounter, useStopCounter } from "../../../timing/clock"
import { ViewName } from "@/components/composables/common/Views"
import { useTexasHoldemStore } from "@/stores/texasHoldemStore"
import type { CasinoPlayer } from "@/types/casino"
import { useTableViewOpener } from "@/components/composables/common/table"
import { GamePhase, TableAction, type HoldemPlayer, type HoldemTable } from "@/types/texasHoldem"
import { useCasinoStore } from "@/stores/casinoStore"
import { GameAction } from "@/types/texasHoldem"
// @author softwarewithcode from GitHub
const store = useTexasHoldemStore()
const casinoStore = useCasinoStore()
export function useHoldemMessageHandler(data: any) {
	store.$patch({
		command: data.title
	})
	if (store.mainPlayer) {
		const isPlayerInStorePlayingInCurrentTable: CasinoPlayer = data.table.players.find(tablePlayer => tablePlayer.userName === store.mainPlayer?.userName)
		if (!isPlayerInStorePlayingInCurrentTable) store.logout()
	}
	switch (data.title) {
		case ServerCommand.OPEN_TABLE:
			openTable(data)
			break
		case ServerCommand.LOGIN:
			store.$patch({
				mainPlayer: data.player
			})
			break
		case ServerCommand.NO_BETS_NO_DEAL:
			store.clearTableAndPlayers()
			break
		case ServerCommand.ROUND_COMPLETED:
			finalizeRound(data)
			break
		default:
			patchStoresAndStartTimer(data)
	}
}

const openTable = async (data: any) => {
	let table: HoldemTable = data.table
	useTableViewOpener(table, ViewName.TEXAS_HOLDEM_TABLE)
	patchStores(data)
}

const patchStores = async (data: any) => {
	let patchObject = { table: data.table, command: data.title }
	patchMainBoxPlayer(data, patchObject)
	patchLastActor(data, patchObject)
	patchCasinoStore(data.table.counterTime)
	store.$patch(patchObject)
}

const mapPlayedAction = (data: any): GameAction | undefined => {
	if (data.title === GameAction.ALL_IN.toString()) return GameAction.ALL_IN
	if (data.title === GameAction.BET_RAISE.toString()) return GameAction.BET_RAISE
	if (data.title === GameAction.CALL.toString()) return GameAction.CALL
	if (data.title === GameAction.FOLD.toString()) return GameAction.FOLD
	if (data.title === GameAction.CHECK.toString()) return GameAction.CHECK
	return undefined
}

const patchCasinoStore = async (counterValue: number) => {
	let patchObject = { counter: counterValue }
	casinoStore.$patch(patchObject)
}
const patchStoresAndStartTimer = async (data: any) => {
	patchStores(data)
	useStartCounter()
}

const finalizeRound = async (data: any) => {
	useStopCounter()
	patchStores(data)
}

const dataFrameContainsHoleCards = (data: any) => {
	return data.cards
}
function patchMainBoxPlayer(data: any, patchObject: { table: any; command: any }) {
	const mainBoxPlayer = data.table.players.find(player => player.userName === store.mainPlayer?.userName)
	if (store.mainPlayer && mainBoxPlayer) {
		patchObject["mainPlayer"] = mainBoxPlayer
		if (dataFrameContainsHoleCards(data)) {
			patchObject["mainPlayer"]["cards"] = data.cards
		} else if (store.hasMainPlayerHoleCards) {
			patchObject["mainPlayer"]["cards"] = store.mainPlayer.cards
		} else {
			patchObject["mainPlayer"]["cards"] = undefined
		}
	}
}
function patchLastActor(data: any, patchObject: { table: any; command: any }) {
	const actionPlayed = mapPlayedAction(data)
	if (actionPlayed) {
		const actionPlayer: HoldemPlayer = data.table.players.find(player => player.userName === data.player.userName)
		patchObject["lastActor"] = actionPlayer
		patchObject["lastActor"]["lastAction"] = actionPlayed
	}
}
