import { useBlackjackStore } from "../../../../../stores/blackjackStore"
import router from "../../../../../router/router"
import { Command } from "@/types/sockethander"
import { useStartCounter } from "../../../timing/clock"
import type { BlackjackPlayer, BlackjackTable } from "@/types/blackjack"
import type { CasinoPlayer } from "@/types/casino"
import { useTableViewOpener } from "@/components/composables/common/table"
import { ViewName } from "@/components/composables/common/Views"
import { useCasinoStore } from "@/stores/casinoStore"

// @author softwarewithcode from GitHub
const store = useBlackjackStore()
const casinoStore = useCasinoStore()

export function useBlackjackMessageHandler(data: any) {
	store.$patch({
		command: data.title
	})
	if (store.getPlayer) {
		const isPlayerInStorePlayingInCurrentTable: CasinoPlayer = data.table.players.find(tablePlayer => tablePlayer.userName === store.getPlayer.userName)
		if (!isPlayerInStorePlayingInCurrentTable) store.logout({})
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
			patchStores(data)
	}
}

const openTable = async (data: any) => {
	let table: BlackjackTable = data.table
	useTableViewOpener(table, ViewName.BLACKJACK_TABLE)
	patchStoreAndStartTimer(data)
}

const standUp = async (data: any) => {
	let patchObject = { table: data.table, player: data.player, command: data.title }
	store.$patch(patchObject)
}

const patchStores = async (data: any) => {
	let patchObject = { table: data.table, command: data.title }
	const patchPlayer = data.table.players.find(player => player.userName === store.getPlayer?.userName)
	if (store.getPlayer && patchPlayer) {
		patchObject["player"] = patchPlayer
	}
	patchCasinoStore(data.table.counterTime)
	store.$patch(patchObject)
}

const patchCasinoStore = async (counterValue: number) => {
	let patchObject = { counter: counterValue }
	casinoStore.$patch(patchObject)
}

const patchStoreAndStartTimer = async (data: any) => {
	patchStores(data)
	useStartCounter()
}
const finalizeRound = async (data: any) => {
	const counterTime: number = data.table.tableCard.gameData.roundDelay
	data.table.counterTime = counterTime / 1000 // millis to seconds
	patchStores(data)
	useStartCounter()
}
