import { BLACKJACK, ROULETTE, TEXAS_HOLDEM } from "@/types/games"
import { useBlackjackMessageHandler } from "./handlers/blackjackMessageHandler"
import { useHoldemMessageHandler } from "./handlers/holdemMessageHandler"
import { useRouletteMessageHandler } from "./handlers/rouletteMessageHandler"
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

		//TODO common login and openTable handler
		switch (data.game) {
			case BLACKJACK:
				useBlackjackMessageHandler(data)
				break
			case TEXAS_HOLDEM:
				useHoldemMessageHandler(data)
				break
			case ROULETTE:
				useRouletteMessageHandler(data)
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
		showNotification()
	}

	const showNotification = () => {}
}
