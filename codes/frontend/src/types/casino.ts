import type { TableCard } from "./TableCard"
import type { BlackjackPlayer } from "./blackjack"
import type { HoldemPlayer } from "./texasHoldem"

export interface GameType {
	type: string
	index: number
}

export interface CasinoPlayer {
	userName: string
	id: string
	currentBalance: number
	seatNumber: number
	status: PlayerStatus
}

export interface CasinoTable<T> {
	title: string
	tableCard: TableCard
	activePlayer: T
	seats: Seat<T>[]
	id: string
}

export interface Seat<T> {
	number: number
	available: boolean
	player: T
}

export interface ChipStack {
	chips: Map<Chip, number>
}

export interface Chip {
	value: number
	image: HTMLImageElement
}

export enum Games {
	BLACKJACK = "blackjack",
	TEXAS_HOLDEM = "texas-holdem"
}

export enum PlayerStatus {
	SIT_OUT = "SIT_OUT",
	ACTIVE = "ACTIVE",
	NEW = "NEW"
}
export interface Range {
	min: number
	max: number
}
export const rangeInputUpdate = "rangeInputUpdate"
