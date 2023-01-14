import { useBlackjackMessageHandler, BLACKJACK } from "./handlers/blackjackMessageHandler"
let websocket: WebSocket
// @author softwarewithcode from GitHub
const base = import.meta.env.VITE_CASINO_WS_ENDPOINT
const openTableJSON = '{ "action": "OPEN_TABLE"}'

export async function useOpenTable(tableType: string, tableId: string) {
	const finalURI = base + `/${tableType}/${tableId}`
	await initSocket(finalURI.toLocaleLowerCase())
}

export function useSend(data: any) {
	websocket.send(JSON.stringify(data))
}

function initSocket(finalURI: string) {
	websocket = new WebSocket(finalURI)
	websocket.onopen = event => {
		useSend(JSON.parse(openTableJSON))
	}
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
			default:
				throw new Error("no handler for data " + JSON.stringify(data))
		}
	}
	websocket.onerror = event => {
		console.error("socket error" + JSON.stringify(event))
		alert("an error occured")
	}
	websocket.onclose = event => {
		console.log("socket closes, bye!")
	}
}
