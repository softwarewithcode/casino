export interface CasinoTable {
	tableCard: TableCard
}
export interface GameType {
	type: string
	index: number
}
export interface CasinoPlayer {
	name: string
	id: string
	balance: number
	totalBet: number
	lastBet: number
	actions: string[]
}

export interface CasinoHand {
	values: number[]
	cards: Card[]
	bet: number
}

export interface TableCard {
	thresholds: Thresholds
	availablePositions: Array<Number>
	name: string
	id: string
	language: string
	game: string
	type: TableType
}

export interface Thresholds {
	minimumBet: number
	maximumBet: number
	maxPlayers: number
	seatCount: number
	phaseDelay: number
}

export interface Card {
	rank: number
	suit: Suit
	visible: boolean
}

export interface Vector {
	x: number
	y: number
}
export interface CasinoFont {
	faceAndSize: string
	color: string
}

export enum Suit {
	CLUB = "CLUB",
	DIAMOND = "DIAMOND",
	HEART = "HEART",
	SPADE = "SPADE"
}

export enum TableType {
	MULTIPLAYER = "MULTIPLAYER",
	SINGLE_PLAYER = "SINGLE_PLAYER"
}

export interface CardImageMetadata {
	position: Vector
	size: Vector
}
