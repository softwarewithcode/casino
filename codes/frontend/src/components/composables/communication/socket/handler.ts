import { useTableStore } from "../../../../stores/tableStore"
import { useRouter, useRoute } from "vue-router"
import router from "../../../../router/router"
import { Command } from "@/types/sockethander"
import { useStartCounter } from "../../timing/clock"
const store = useTableStore()

export function handle(data: any) {
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
			patchStoreAndStartTimer(data)
			break
		case Command.INSURANCE_PHASE_STARTS:
			patchStoreAndStartTimer(data)
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
