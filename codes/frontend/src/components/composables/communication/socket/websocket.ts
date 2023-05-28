import { BLACKJACK, TEXAS_HOLDEM } from "@/types/games"
import { useBlackjackMessageHandler } from "./handlers/blackjackMessageHandler"
import { useHoldemMessageHandler } from "./handlers/holdemMessagHandler"
let websocket: WebSocket
// @author softwarewithcode from GitHub
const base = import.meta.env.VITE_CASINO_WS_ENDPOINT
const openTableJSON = '{ "action": "OPEN_TABLE"}'

export async function useOpenTable(tableType: string, tableId: string) {
	const finalURI = base + `/${tableType}/${tableId}`
	await initSocket(finalURI.toLocaleLowerCase())
}

export const useSocketSend = (data: any) => websocket.send(JSON.stringify(data))
export const useSocketClose = () => websocket?.close()

function initSocket(finalURI: string) {
	websocket = new WebSocket(finalURI)
	websocket.onopen = event => useSocketSend(JSON.parse(openTableJSON))
	websocket.onmessage = event => {
		let data = JSON.parse(event.data)
		if (!data.table) {
			alert("error")
			return
		}
		switch (data.table.tableCard.game) {
			case BLACKJACK:
				useBlackjackMessageHandler(data)
				break
			case TEXAS_HOLDEM:
				useHoldemMessageHandler(data)
				break
			default:
				throw new Error("no handler for data " + JSON.stringify(data))
		}
	}
	websocket.onerror = event => {
		console.error("socket error" + JSON.stringify(event))
		showNotification()
	}
	websocket.onclose = event => {
		console.log("socket closes, bye!")
		showNotification()
	}

	const showNotification = () => {
		console.log("closed")
	}
}
