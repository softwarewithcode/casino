import { useBlackjackDataHandler } from "./handler"
let websocket: WebSocket

const base = import.meta.env.VITE_CASINO_WS_ENDPOINT
const openTableJSON = '{ "action": "OPEN_TABLE"}'

export async function useOpenTable(tableType: string, tableId: string) {
	const finalURI = base + `/${tableType}/${tableId}`
	await initSocket(finalURI.toLocaleLowerCase())
}

export function useSend(data: any) {
	websocket.send(JSON.stringify(data))
}

async function initSocket(finalURI: string) {
	websocket = new WebSocket(finalURI)
	websocket.onopen = event => {
		useSend(JSON.parse(openTableJSON))
	}
	websocket.onmessage = event => {
		let data = JSON.parse(event.data)
		switch (data.table.tableCard.game) {
			case "BLACKJACK":
				useBlackjackDataHandler(data)
				break
			default:
				throw new Error("no handler for data " + JSON.stringify(data))
		}
	}
	websocket.onerror = event => {
		console.error("socket error" + event)
	}
	websocket.onclose = event => {}
}
