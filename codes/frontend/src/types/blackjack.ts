import type { CasinoPlayer, CasinoTable, TableGamesPlayer, Seat } from "./casino"
import type { CardHand } from "./cards"
import type { TableCard } from "./TableCard"

export interface BlackjackTable extends CasinoTable<BlackjackPlayer> {
	dealerHand: BlackjackHand
	gamePhase: string
}

export interface BlackjackPlayer extends TableGamesPlayer {
	hands: BlackjackHand[]
	insuranceAmount: number
}

export interface BlackjackHand extends CardHand {
	insured: boolean
	split: boolean
	insuranceBet: number
	blackjack: boolean
	active: boolean
	values: number[]
	bet: number
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
	BET = "BET",
	ROUND_COMPLETED = "ROUND_COMPLETED"
}
