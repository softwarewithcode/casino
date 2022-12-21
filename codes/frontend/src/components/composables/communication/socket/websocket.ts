import type { SocketMessageHandler } from "@/types/sockethander"
import { socketHandler } from "./handler";
let websocket: WebSocket
let finalURI: string
let handler: SocketMessageHandler
const base = "ws://localhost:8080/casino/blackjack/"


export async function createSocket(tableId: string) {
  finalURI = base + tableId
  await initSocket()
}
export function send(data: any) {
  websocket.send(JSON.stringify(data))
}

function initSocket() {
  let websocket = new WebSocket(finalURI)
  console.log("websocket called")
  websocket.onopen = (event) => {
    console.log("socket open")
  }
  websocket.onmessage = (event) => {
    let data = JSON.parse(event.data)
    handler.handle(data)
  }
  websocket.onerror = (event) => {
    console.error("socket error" + event)
  }
  websocket.onclose = (event) => {
    console.log("socket closed " + event)
  }
}
