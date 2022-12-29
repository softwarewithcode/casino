import { useTableStore } from "../../../../stores/tableStore"
import { useRouter, useRoute } from "vue-router"
import router from "../../../../router/router"
import { Command } from "@/types/sockethander"
import { useStartCounter } from "../../timing/clock"
const store = useTableStore()
//const router = useRouter()

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
			patchTableAndCommandAndStartTimer(data)
			break
		case Command.INSURANCE_PHASE_STARTS:
			console.log("insurance phaseTimer")
			patchTableAndCommandAndStartTimer(data)
			break
		default:
			patchTableAndCommand(data)
	}
}

const handleTableOpen = async (data: any) => {
	let table = data.table
	router.push({ name: "blackjack", params: { tableId: table.id } })
	store.$patch({
		table: table
	})
}

const patchTableAndCommand = async (data: any) => {
	store.$patch({
		table: data.table,
		command: data.title
	})
}
const patchTableAndCommandAndStartTimer = async (data: any) => {
	await store.$patch({
		table: data.table,
		command: data.title,
		counter: data.table.counterTime
	})
	useStartCounter()
}
