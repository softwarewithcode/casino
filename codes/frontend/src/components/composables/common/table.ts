import type { CasinoPlayer, CasinoTable, PlayerStatus } from "@/types/casino"
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
export const useTableJoiner = () => useSocketSend({ action: "JOIN" })
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

export const useHeroFinder = (table: CasinoTable<CasinoPlayer>, playerFromStore: CasinoPlayer): CasinoPlayer | undefined => {
	return playerFromStore?.userName ? playerFromStore : table.seats.find(seat => seat?.player?.userName)?.player
}

export const isPlayerInTable = (table: CasinoTable<CasinoPlayer>, hero: CasinoPlayer): boolean => {
	if (!hero) return false
	const playerInTable =
		table.seats
			.filter(seat => seat.player != null)
			.map(seat => seat.player)
			.filter(player => player.userName === hero.userName).length > 0
	return playerInTable
}

export const hasAvailableSeat = (table: CasinoTable<CasinoPlayer>) => {
	return table.seats.some(seat => seat.available)
}

export const useJoinChecker = (table: CasinoTable<CasinoPlayer>, hero: CasinoPlayer) => {
	return !isPlayerInTable(table, hero) && hasAvailableSeat(table) && hasSufficentFunds(table, hero)
}
export const hasSufficentFunds = (table: CasinoTable<CasinoPlayer>, hero: CasinoPlayer) => {
	return hero.initialBalance >= table.tableCard.gameData.minBuyIn
}
export const searchPlayerFromTable = (table: CasinoTable<CasinoPlayer>, hero: CasinoPlayer): CasinoPlayer => {
	return table.seats
		.filter(seat => seat.player != null)
		.map(seat => seat.player)
		.filter(player => player.userName === hero?.userName)[0]
}
export const useDescendingSeatsSorter = <T>(table: CasinoTable<T>) => table.seats.filter(seat => seat).sort((a, b) => a.number - b.number)

export const useSeatChecker = (player: CasinoPlayer) => player?.seatNumber >= 0

export const useGameCanvasFinder = (): HTMLCanvasElement => document.getElementById("canvas") as HTMLCanvasElement
