import { Suit, type Card, type CardImageMetadata } from "@/types/cards"
import type { Vector } from "../../../types/vectors"
const cardWidthInSprite = 76
const cardHeightInSprite = 107.2
const gapHorizontalInSprite = 21
const gapVerticalInSprite = 34.6
const originalCardWidthAndHeight: Vector = { x: cardWidthInSprite, y: cardHeightInSprite }

export function useCardLocator(card: Card): CardImageMetadata {
	const positionInArray: Vector = { x: getColumnNumber(card), y: getRowNumber(card) }
	const x = positionInArray.x * cardWidthInSprite + gapHorizontalInSprite * positionInArray.x
	const y = positionInArray.y * cardHeightInSprite + gapVerticalInSprite * positionInArray.y + 0.7
	const topLeftCornerOfCard: Vector = { x: x, y: y }
	return { position: topLeftCornerOfCard, size: originalCardWidthAndHeight }
}
const getColumnNumber = (card: Card) => {
	if (card.rank < 8) {
		if (card.suit === Suit.HEART) return 0
		if (card.suit === Suit.DIAMOND) return 1
		if (card.suit === Suit.CLUB) return 2
		if (card.suit === Suit.SPADE) return 3
	}
	if (card.suit === Suit.HEART) return 4
	if (card.suit === Suit.DIAMOND) return 5
	if (card.suit === Suit.CLUB) return 6
	return 7
}

const getRowNumber = (card: Card) => {
	if (card.rank === 1 || card.rank === 8) {
		return 0
	}
	if (card.rank === 2 || card.rank === 9) {
		return 1
	}
	if (card.rank === 3 || card.rank === 10) {
		return 2
	}
	if (card.rank === 4 || card.rank === 11) {
		return 3
	}
	if (card.rank === 5 || card.rank === 12) {
		return 4
	}
	if (card.rank === 6 || card.rank === 13) {
		return 5
	}
	return 6
}
