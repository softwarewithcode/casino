import type { CasinoTable, CasinoPlayer, CasinoHand, Card } from "./casino"

export interface BlackjackTable extends CasinoTable {
	seats: Seat[]
	title: string
	dealerHand: BlackjackHand
	gamePhase: string
	playerInTurn: BlackjackPlayer
}
export interface BlackjackPlayer extends CasinoPlayer {
	hands: BlackjackHand[]
	seatNumber: number
	insuranceAmount: number
}
export interface Seat {
	number: number
	player: BlackjackPlayer
	available: boolean
}
export interface BlackjackHand extends CasinoHand {
	insured: boolean
	split: boolean
	insuranceBet: number
	blackjack: boolean
}

export enum PlayerAction {
	TAKE = "TAKE",
	DOUBLE_DOWN = "DOUBLE_DOWN",
	STAND = "STAND",
	SPLIT = "SPLIT",
	INSURE = "INSURE",
	BET = "BET"
}

export enum GamePhase {
	INSURE = "INSURE",
	BET = "BET"
}
