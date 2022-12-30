export interface CasinoTable {
	tableCard: TableCard
}
export interface CasinoPlayer {
	name: string
	id: string
	balance: number
	totalBet: number
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
}

export interface Thresholds {
	minimumBet: number
	maximumBet: number
	maxPlayers: number
	seatCount: number
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

export interface SpriteImageMetadata {
	position: Vector
	size: Vector
}
