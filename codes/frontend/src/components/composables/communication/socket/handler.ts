import { useTableStore } from "../../../../stores/tableStore"
import { useRouter, useRoute } from "vue-router"
import router from "../../../../router/router"
import { Command } from "@/types/sockethander"
import { useCounterStart } from "../../timing/clock"
const store = useTableStore()
//const router = useRouter()

export function handle(data: any) {
	console.log("incoming data from socket" + data)
	store.$patch({
		command: data.title
	})
	switch (data.title) {
		case Command.LOGIN:
			console.log("login")
			store.$patch({
				player: data.player
			})
			break
		case Command.OPEN_TABLE:
			handleTableOpen(data)
			break
		case Command.NEW_PLAYER:
			patchTableAndCommand(data)
			break
		case Command.PLAYER_LEFT: //Server keeps the player in list until round is finished
			patchTableAndCommand(data)
			break
		case Command.BET_PHASE_STARTS:
			startBetPhase(data)
			break
		case Command.INITIAL_DEAL_DONE:
			showInitialDeal(data)
			break
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
	console.log("updateTable:")
	store.$patch({
		table: data.table,
		command: data.title
	})
}
const startBetPhase = async (data: any) => {
	console.log("startBetPhase:" + JSON.stringify(data))
	await store.$patch({
		command: data.title,
		counter: data.table.counterTime
	})
	useCounterStart()
}

const showInitialDeal = (data: any) => {
	store.$patch({
		table: data.table,
		command: data.title,
		counter: data.table.counterTime
	})
	store.showInitialDeal() // reactivity !
	// useCounterStart();
}
