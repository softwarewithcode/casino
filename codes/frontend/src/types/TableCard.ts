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
	minimumBet: number
	maximumBet: number
	roundDelay: number
	playerTime: number
}

export enum TableType {
	MULTIPLAYER = "MULTIPLAYER",
	SINGLE_PLAYER = "SINGLE_PLAYER"
}
