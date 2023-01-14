import { useBlackjackStore } from "../../../../../stores/blackjackStore"
import router from "../../../../../router/router"
import { Command } from "@/types/sockethander"
import { useStartCounter } from "../../../timing/clock"
import type { BlackjackPlayer } from "@/types/blackjack"

// @author softwarewithcode from GitHub
const store = useBlackjackStore()

export const BLACKJACK = "BLACKJACK"
export function useBlackjackMessageHandler(data: any) {
	store.$patch({
		command: data.title
	})
	if (store.getPlayer) {
		const isPlayerInStorePlayingInCurrentTable: BlackjackPlayer = data.table.players.find(tablePlayer => tablePlayer.userName === store.getPlayer.userName)
		if (!isPlayerInStorePlayingInCurrentTable) {
			store.logout({})
		}
	}
	switch (data.title) {
		case Command.OPEN_TABLE:
			openTable(data)
			break
		case Command.LOGIN:
			store.$patch({
				player: data.player
			})
			break
		case Command.NO_BETS_NO_DEAL:
			standUp(data)
			break
		case Command.BET_TIME_START:
		case Command.PLAYER_TIME_START:
		case Command.INSURANCE_TIME_START:
		case Command.INITIAL_DEAL_DONE:
			patchStoreAndStartTimer(data)
			break
		case Command.ROUND_COMPLETED:
			finalizeRound(data)
			break
		default:
			patchStore(data)
	}
}

const openTable = async (data: any) => {
	let table = data.table
	router.push({ name: "blackjack", params: { tableId: table.id } })
	patchStoreAndStartTimer(data)
}

const standUp = async (data: any) => {
	let patchObject = { table: data.table, player: data.player, command: data.title, counter: data.table.counterTime }
	store.$patch(patchObject)
}

const patchStore = async (data: any) => {
	let patchObject = { table: data.table, command: data.title, counter: data.table.counterTime }
	const patchPlayer = data.table.players.find(player => player.userName === store.getPlayer?.userName)
	if (store.getPlayer && patchPlayer) {
		patchObject["player"] = patchPlayer
	}
	store.$patch(patchObject)
}
const patchStoreAndStartTimer = async (data: any) => {
	patchStore(data)
	useStartCounter()
}
const finalizeRound = async (data: any) => {
	const counterTime: number = data.table.tableCard.thresholds.phaseDelay
	data.table.counterTime = counterTime / 1000 // millis to seconds
	patchStore(data)
	useStartCounter()
}
