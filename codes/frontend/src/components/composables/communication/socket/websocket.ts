import { useTableDataHandler } from "./handler"
let websocket: WebSocket

const base = import.meta.env.VITE_BLACKJACK_WS_ENDPOINT
const openTableJSON = '{ "action": "OPEN_TABLE"}'

export async function useOpenTable(tableId: string) {
	const finalURI = base + `/${tableId}`
	await initSocket(finalURI)
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
		useTableDataHandler(data)
	}
	websocket.onerror = event => {
		console.error("socket error" + event)
	}
	websocket.onclose = event => {}
}
