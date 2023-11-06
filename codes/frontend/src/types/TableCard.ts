import type { TableType, Range } from "./casino"

export interface TableCard {
	availablePositions: Array<Number>
	name?: string
	id: string
	language: string
	game: string
	type: TableType
	thresholds: Thresholds
	gameData: GameData
}
export interface Thresholds {
	maxPlayers: number
	seatCount: number
}
export interface GameData {
	// has props which are not used in all tables
	minPlayers: string
	maxPlayers: string
	type: string
	betType: string
	minBuyIn: number
	maxBuyIn: number
	smallBlind: number
	bigBlind: number
	rakeCap: number
	rakePercent: number
	minBet: number
	maxBet: number
	roundDelay: number
	playerTime: number
	tableNumbers: Range
}
