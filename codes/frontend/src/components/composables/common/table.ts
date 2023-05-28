import type { CasinoPlayer, CasinoTable, PlayerStatus } from "@/types/casino"
import type { Vector } from "../../../types/vectors"
import { useSocketSend } from "../communication/socket/websocket"
import router from "@/router/router"
import { ViewName } from "../common/Views"

export const useNextSeatNumberCalculator = (featuredSeatNumber: number, table: CasinoTable<CasinoPlayer>): number => {
	if (featuredSeatNumber === table.seats.length - 1) return 0
	let next = featuredSeatNumber
	next++
	return next++
}

export const useActivePlayerChecker = (table: CasinoTable<CasinoPlayer>, player: CasinoPlayer): boolean => usePlayerEqualsChecker(player, table.activePlayer)
export const usePlayerAllowedStatusesChecker = (player: CasinoPlayer, allowedStatus: PlayerStatus[] | PlayerStatus): boolean => {
	if (!player) return false
	if (allowedStatus && allowedStatus.length > 0) {
		return allowedStatus.includes(player.status)
	}
	return allowedStatus === player.status
}
export const usePlayerEqualsChecker = (player: CasinoPlayer, player2: CasinoPlayer) => {
	if (!player || !player2) return false
	return player.userName === player2.userName
}
export const useSeatTaker = (seat: number) => useSocketSend({ action: "JOIN", seat: seat.toString() })

export const useTableViewOpener = async <T>(table: CasinoTable<T>, tableName: ViewName) => await router.push({ name: tableName, params: { tableId: table.id } })

export const useSeatToIndexMapper = <T extends CasinoPlayer>(table: CasinoTable<T>, seatNumber: number, mainBoxPlayer: CasinoPlayer) => {
	if (mainBoxPlayer.seatNumber === seatNumber) return table.seats.length - 1
	const seat = table.seats.find(seat => seat.number === seatNumber)
	if (!seat) throw new Error("seatNotFound " + seat)
	if (mainBoxPlayer.seatNumber > seat.number) {
		return table.seats.length - 1 - (mainBoxPlayer.seatNumber - seat.number)
	}
	return seat.number - mainBoxPlayer.seatNumber - 1
}

export const useMainBoxPlayerFinder = (table: CasinoTable<CasinoPlayer>, playerFromStore: CasinoPlayer): CasinoPlayer | undefined => {
	return playerFromStore?.userName ? playerFromStore : table.seats.find(seat => seat?.player?.userName)?.player
}

export const useDescendingSeatsSorter = <T>(table: CasinoTable<T>) => table.seats.filter(seat => seat).sort((a, b) => a.number - b.number)

export const useSeatChecker = (player: CasinoPlayer) => player?.seatNumber >= 0

export const useGameCanvasFinder = (): HTMLCanvasElement => document.getElementById("canvas") as HTMLCanvasElement
