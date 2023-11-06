import type { TableCard } from "./TableCard"
import type { Vector } from "./vectors"

export interface GameType {
	type: string
	index: number
}

export interface CasinoPlayer {
	userName: string
	id: string
	currentBalance: number
	initialBalance: number
	seatNumber: number
	status: PlayerStatus
}

export interface TableGamesPlayer extends CasinoPlayer {
	totalBet: number
	totalOnTable: number
	lastBet: number
	actions: string[]
}
export interface Range {
	min: number
	max: number
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
export enum TableType {
	MULTIPLAYER = "MULTIPLAYER",
	SINGLEPLAYER = "SINGLEPLAYER"
}

export interface ChipStack {
	chips: Map<Chip, number>
}

export interface Chip {
	value: number
	image: HTMLImageElement
	position?: Vector
	size?: Vector
}

export enum Games {
	BLACKJACK = "blackjack",
	TEXAS_HOLDEM = "texas-holdem",
	ROULETTE = "roulette"
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
