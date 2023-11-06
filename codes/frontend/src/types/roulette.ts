import type { CasinoTable, Chip, TableGamesPlayer, TableType } from "./casino"
import type { DoubleVector, KeyValue } from "./vectors"

export interface RouletteTable extends CasinoTable<RoulettePlayer> {
	wheel: RouletteWheel
	gamePhase: RouletteGamePhase
	type: TableType
}
export interface RouletteWheel {
	winningNumber: number
	resultBoard: SpinResult[]
	spinId: string
}
export interface RoulettePlayer extends TableGamesPlayer {
	bets: RouletteBet[]
	chips: Chip[]
	selectedChip?: Chip
	roundResults: RoundResult[]
	positionsTotalAmounts: KeyValue[]
}

export interface SpinResult {
	id: string
	winningNumber: number
	roundNumber: number
}
export interface RoulettePaintData {
	canvas: HTMLCanvasElement
	table: RouletteTable
	hero: RoulettePlayer
	recalculate: boolean
	betPositions: RouletteBetPosition[]
	selectedBetPosition: RouletteBetPosition | undefined
	highlightEffectiveNumbers: boolean
	highlightNumber: number | null
	randomNumber: number | null
}
export interface RoundResult {
	spinResult: SpinResult
	playerResult: PlayerResult
}
export interface SpinResult {
	spinId: string
	winningNumber: number
	roundNumber: number
}
export interface PlayerResult {
	totalBets: number
	totalWinnings: number
	totalRemainingBets: number
	winningNumber: number
	losingBets: RouletteBet[]
	winningBets: RouletteBet[]
	remainingBets: RouletteBet[]
}
export interface RouletteBet {
	amount: number
	position: number
}
export interface RouletteBetPosition {
	selected: boolean
	selectSection: DoubleVector
	highlightSection: DoubleVector
	number: number
	tableNumbers: number[]
	type: BetType
}
export interface BetType {
	paysOut: number
	name: BetName
}
export enum BetName {
	SINGLE_NUMBER = "SINGLE_NUMBER",
	DOUBLE_NUMBER = "DOUBLE_NUMBER",
	TRIPLE_NUMBER = "TRIPLE_NUMBER",
	QUADRUPLE_NUMBER = "QUADRUPLE_NUMBER",
	SIX_NUMBER = "SIX_NUMBER",
	RED = "RED",
	BLACK = "BLACK",
	FIRST_DOZEN = "FIRST_DOZEN",
	SECOND_DOZEN = "SECOND_DOZEN",
	THIRD_DOZEN = "THIRD_DOZEN",
	LOW_NUMBERS = "LOW_NUMBERS",
	HIGH_NUMBERS = "HIGH_NUMBERS",
	FIRST_COLUMN = "FIRST_COLUMN",
	SECOND_COLUMN = "SECOND_COLUMN",
	THIRD_COLUMN = "THIRD_COLUMN",
	EVEN = "EVEN",
	ODD = "ODD",
	FIRST_HALF = "FIRST_HALF",
	SECOND_HALF = "SECOND_HALF"
}
export enum PlayerAction {
	REMOVE_BET_FROM_POSITION = "REMOVE_BET_FROM_POSITION",
	REMOVE_LAST_OR_ALL = "REMOVE_LAST_OR_ALL_BETS",
	BET = "BET",
	PLAY = "PLAY",
	REPEAT_LAST = "REPEAT_LAST",
	FETCH_BET_POSITIONS = "FETCH_BET_POSITIONS"
}
export enum RouletteGamePhase {
	BET = "BET",
	ROUND_COMPLETED = "ROUND_COMPLETED",
	SPINNING = "SPINNING"
}
