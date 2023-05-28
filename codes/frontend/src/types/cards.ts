import type { Vector } from "@/types/vectors"

export interface CardHand {
	cards: Card[]
}

export interface Card {
	rank: number
	suit: Suit
	visible: boolean
}

export enum Suit {
	CLUB = "CLUB",
	DIAMOND = "DIAMOND",
	HEART = "HEART",
	SPADE = "SPADE"
}

export interface CardImageMetadata {
	position: Vector
	size: Vector
}
