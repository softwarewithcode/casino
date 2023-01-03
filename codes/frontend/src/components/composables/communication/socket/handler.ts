import { useTableStore } from "../../../../stores/tableStore"
import { useRouter, useRoute } from "vue-router"
import router from "../../../../router/router"
import { Command } from "@/types/sockethander"
import { useStartCounter } from "../../timing/clock"
const store = useTableStore()

export function useTableDataHandler(data: any) {
	store.$patch({
		command: data.title
	})
	switch (data.title) {
		case Command.LOGIN:
			store.$patch({
				player: data.player
			})
			break
		case Command.OPEN_TABLE:
			handleTableOpen(data)
			break
		case Command.BET_PHASE_STARTS:
		case Command.WAITING_PLAYER_ACTION:
		case Command.INSURANCE_PHASE_STARTS:
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

const handleTableOpen = async (data: any) => {
	let table = data.table
	router.push({ name: "blackjack", params: { tableId: table.id } })
	store.$patch({
		table: table
	})
}

const patchStore = async (data: any) => {
	let patchObject = { table: data.table, command: data.title, counter: data.table.counterTime }
	const patchPlayer = data.table.players.find(player => player.name === store.getPlayer?.name)
	if (patchPlayer) {
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
