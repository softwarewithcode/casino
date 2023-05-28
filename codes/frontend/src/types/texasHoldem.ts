import type { TableCard } from "./TableCard"
import type { CasinoPlayer, CasinoTable, PlayerStatus, Range } from "./casino"
import type { CardHand, Card } from "./cards"
export interface HoldemTable extends CasinoTable<HoldemPlayer> {
	gamePhase: GamePhase
	activePlayer: HoldemPlayer
	button: Button
	pots: Pot[]
	tableCards?: Card[]
}

export interface HoldemAction {
	type: GameAction
	range: Range
}
export interface ActionWrapper {
	type: GameAction
	player: HoldemPlayer
}

export enum GameAction {
	ALL_IN = "ALL_IN",
	CHECK = "CHECK",
	FOLD = "FOLD",
	BET_RAISE = "BET_RAISE",
	CALL = "CALL"
}

export enum TableAction {
	RELOAD_CHIPS = "RELOAD_CHIPS",
	SIT_OUT_NEXT_HAND = "SIT_OUT_NEXT_HAND",
	CONTINUE_GAME = "CONTINUE_GAME"
}

export interface Button {
	seatNumber: number
}
export enum GamePhase {
	PRE_FLOP = "PRE_FLOP",
	FLOP = "FLOP",
	TURN = "TURN",
	RIVER = "RIVER",
	ROUND_COMPLETED = "ROUND_COMPLETED"
}

export interface HoldemPlayer extends CasinoPlayer {
	chipsOnTable: number
	cards: Card[] | undefined
	hand?: CardHand
	actions: HoldemAction[]
	status: PlayerStatus
	lastAction: GameAction | undefined
}
export interface Pot {
	amountWithTableChips: number
	rake: number
}
