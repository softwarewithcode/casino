import type { BlackjackPlayer, BlackjackTable } from "@/types/blackjack"
import type { CasinoPlayer, CasinoTable, Seat } from "@/types/casino"
import type { Vector } from "../../types/vectors"
import type { Card } from "@/types/cards"
import { blackFont, type CasinoFont, blueFont, orangeFont } from "../../types/fontsAndColors"
import { useCardLocator } from "../composables/rendering/cardLocator"
import { cardsSprite } from "../../types/images"
import { useDefaultCardWidthRatio, usePlayerBoxStartingCornerCalculator, useRectanglePainter, useTextPainter, useWait } from "../composables/rendering/commonPainter"
import { useActivePlayerChecker, useNextSeatNumberCalculator } from "../composables/common/table"
const dealerName = "-"
const INITIAL_DEAL_CARD_DELAY = 1500
let playerBoxHeight
let playerBoxWidth

let mainBoxPlayer: BlackjackPlayer
let table: BlackjackTable
const largeBoxIndex = 6

export function useActorsPainter(table: BlackjackTable, mainBoxPlayer: BlackjackPlayer, canvas: HTMLCanvasElement) {
	initPainterData(canvas, mainBoxPlayer, table)
	const ctx = canvas.getContext("2d")
	if (!ctx) return
	if (!mainBoxPlayer || Object.keys(mainBoxPlayer).length === 0) return
	paintMainPlayerBox(mainBoxPlayer, canvas, useActivePlayerChecker(table, mainBoxPlayer))
	paintPlayersBoxesExcluding(mainBoxPlayer.seatNumber, table, canvas)
	paintDealerBox(table, canvas)
}
export function useCardsAndHandValuesPainter(table: BlackjackTable, mainBoxPlayer: BlackjackPlayer, canvas: HTMLCanvasElement) {
	initPainterData(canvas, mainBoxPlayer, table)
	const playersWithCards: BlackjackPlayer[] = getPlayersWithCards(table)
	if (playersWithCards.length === 0) return
	paintPlayersCards(playersWithCards, mainBoxPlayer, canvas)
	paintDealerCards(table, canvas)
	paintPlayersHandValues(playersWithCards, mainBoxPlayer, canvas)
}
const paintDealerCards = (table: BlackjackTable, canvas: HTMLCanvasElement) => {
	table.dealerHand.cards.forEach((dealerCard, cardIndex) => {
		paintCard(dealerName, dealerCard, cardIndex, 0, canvas)
	})
}

const paintPlayersCards = (playersWithCards: BlackjackPlayer[], mainBoxPlayer: BlackjackPlayer, canvas: HTMLCanvasElement) => {
	playersWithCards.forEach(player => {
		player.hands.forEach((hand, handIndex) => {
			hand.cards.forEach((card, cardIndex) => {
				paintCard(player.userName, card, cardIndex, handIndex, canvas)
			})
		})
	})
}
export async function useInitialDealPainter(table: BlackjackTable, mainBoxPlayer: BlackjackPlayer, canvas: HTMLCanvasElement) {
	const ctx = canvas.getContext("2d")
	if (!ctx) return
	initPainterData(canvas, mainBoxPlayer, table)
	const playersWithCards: BlackjackPlayer[] = getPlayersWithCards(table)
	showOneCardFromFirstHandWithDelay(playersWithCards, canvas)
	const dealerCard: Card = table.dealerHand.cards[0]
	await paintCardWithDelay(INITIAL_DEAL_CARD_DELAY, dealerName, dealerCard, 0, 0, canvas)
	showOneCardFromFirstHandWithDelay(playersWithCards, canvas)
	paintPlayersHandValues(playersWithCards, mainBoxPlayer, canvas)
}

const getPlayersWithCards = (table: CasinoTable<BlackjackPlayer>) => {
	return table.seats.map(seat => seat.player).filter(player => player?.hands && player.hands[0]?.cards.length > 0)
}
const showOneCardFromFirstHandWithDelay = async (players: BlackjackPlayer[], canvas: HTMLCanvasElement) => {
	for (let i = 0; i < players.length; i++) {
		const player = players[i]
		const cardIndex = player.hands[0].cards.findIndex(card => !card.hasOwnProperty("visible"))
		const card = player.hands[0].cards[cardIndex]
		const handIndex = 0
		await paintCardWithDelay(INITIAL_DEAL_CARD_DELAY, player.userName, card, cardIndex, handIndex, canvas)
		card.visible = true
	}
}

const paintPlayersHandValues = (playersWithCards: BlackjackPlayer[], mainBoxPlayer: BlackjackPlayer, canvas: HTMLCanvasElement) => {
	for (const player of playersWithCards) {
		const playerIndex = getPlayerBoxIndexRelativeToMainBoxPlayer(player.userName)
		const boxStartingCorner = usePlayerBoxStartingCornerCalculator(playerIndex, playerBoxWidth, playerBoxHeight, largeBoxIndex)
		calculateCardSize(player.userName)
		const cardHeight = player.userName === mainBoxPlayer.userName ? mainPlayerCardSize.y : playerCardSize.y
		getHands(player).forEach((hand, handIndex) => {
			const verticalPositionAsideOfCard = boxStartingCorner.y + cardHeight / 2 + cardHeight * handIndex
			if (hand.values.length === 1) {
				useTextPainter(canvas, { x: boxStartingCorner.x + playerBoxWidth - 50, y: verticalPositionAsideOfCard }, hand.values[0].toString(), blueFont)
			} else {
				useTextPainter(canvas, { x: boxStartingCorner.x + playerBoxWidth - 50, y: verticalPositionAsideOfCard }, hand.values[0].toString() + "/" + hand.values[1].toString(), blueFont)
			}
		})
	}
}

const getHands = (player: BlackjackPlayer) => {
	return player ? player.hands : []
}

const initPainterData = (canvas: HTMLCanvasElement, mainPlayer: BlackjackPlayer, tbl: BlackjackTable) => {
	mainBoxPlayer = mainPlayer
	table = tbl
	playerBoxHeight = canvas.height / 4
	playerBoxWidth = canvas.width / 4
}

const paintCardWithDelay = async (delayMillis: number, actorName: string, card: Card, nth: number, handNth: number, canvas: HTMLCanvasElement) => {
	await useWait(delayMillis)
	paintCard(actorName, card, nth, handNth, canvas)
}

const paintCard = (actorName: string, card: Card, nth: number, handNth: number, canvas: HTMLCanvasElement) => {
	const ctx = canvas.getContext("2d")
	if (!card || !ctx) {
		console.error("cannot draw")
		return
	}
	const cardData = useCardLocator(card)
	const cardSize = calculateCardSize(actorName)
	const cardPosition = calculateCardPosition(actorName, nth, handNth)
	ctx.drawImage(cardsSprite, cardData.position.x, cardData.position.y, cardData.size.x, cardData.size.y, cardPosition.x, cardPosition.y, cardSize.x, cardSize.y)
}
let dealerCardSize: Vector
let playerCardSize: Vector
let mainPlayerCardSize: Vector

const calculateCardPosition = (actorName: string, nthCard: number, nthHand: number): Vector => {
	if (actorName === dealerName) {
		const cardPositionX = playerBoxWidth + nthCard * dealerCardSize.x + 20
		const cardPositionY = playerBoxHeight + playerBoxHeight
		return { x: cardPositionX, y: cardPositionY }
	}

	if (actorName === mainBoxPlayer.userName) {
		const cardPositionX = playerBoxWidth + nthCard * mainPlayerCardSize.x + 20
		const cardPositionY = 3 * playerBoxHeight + nthHand * mainPlayerCardSize.y
		return { x: cardPositionX, y: cardPositionY }
	}
	const playerIndex = getPlayerBoxIndexRelativeToMainBoxPlayer(actorName)
	let boxStartingCorner = usePlayerBoxStartingCornerCalculator(playerIndex, playerBoxWidth, playerBoxHeight, largeBoxIndex)

	let cardPositionX = boxStartingCorner.x + playerCardSize.x * nthCard + 2
	let cardPositionY = boxStartingCorner.y + boxStartingCorner.y / 3 + nthHand * playerCardSize.y
	return { x: cardPositionX, y: cardPositionY }
}

const calculateCardSize = (actorName: string): Vector => {
	if (actorName === dealerName) {
		if (!dealerCardSize) {
			const cardHeight = playerBoxHeight * 0.65
			const cardWidth = cardHeight * useDefaultCardWidthRatio
			dealerCardSize = { x: cardWidth, y: cardHeight }
		}
		return dealerCardSize
	}
	const handCount = getPlayerHandCount(actorName)
	if (actorName === mainBoxPlayer.userName) {
		if (!mainPlayerCardSize) {
			//const mainBoxWidth = playerBoxWidth * 4
			const cardHeight = handCount > 0 ? playerBoxHeight / 2 : playerBoxHeight
			const cardWidth = cardHeight * useDefaultCardWidthRatio
			mainPlayerCardSize = { x: cardWidth, y: cardHeight }
		}
		return mainPlayerCardSize
	}
	if (!playerCardSize) {
		const cardHeight = handCount > 0 ? playerBoxHeight / 2.5 : playerBoxHeight / 1.5
		const cardWidth = cardHeight * useDefaultCardWidthRatio
		playerCardSize = { x: cardWidth, y: cardHeight }
	}
	return playerCardSize
}

const getPlayerHandCount = (actorName: string) => {
	const playerHandCount = table.seats.find(seat => seat.player?.userName === actorName)?.player.hands.length
	if (!playerHandCount) throw new Error("no hand count")
	return playerHandCount
}

const paintPlayersBoxesExcluding = (excludedSeatNumber: number, table: BlackjackTable, canvas: HTMLCanvasElement) => {
	const ctx = canvas.getContext("2d")
	if (!ctx) return
	let next = useNextSeatNumberCalculator(excludedSeatNumber, table)
	for (let i = 0; i < table.seats.length; i++) {
		const seat = table.seats.find(seat => seat.number === next) as Seat<BlackjackPlayer>
		if (excludedSeatNumber !== seat.number) {
			paintPlayerBox(usePlayerBoxStartingCornerCalculator(i, playerBoxWidth, playerBoxHeight, largeBoxIndex), seat, canvas, useActivePlayerChecker(table, seat.player))
		}
		next = useNextSeatNumberCalculator(next, table)
	}
}

const getPlayerBoxIndexRelativeToMainBoxPlayer = (actorName: string) => {
	if (mainBoxPlayer.userName === actorName) return largeBoxIndex
	const seat = table.seats.find(seat => seat.player?.userName === actorName)
	if (!seat) throw new Error("player not found " + actorName)
	if (mainBoxPlayer.seatNumber > seat.number) {
		return largeBoxIndex - (mainBoxPlayer.seatNumber - seat.number)
	}
	return seat.number - mainBoxPlayer.seatNumber - 1
}

const paintDealerBox = (table: BlackjackTable, canvas: HTMLCanvasElement) => {
	const ctx = canvas.getContext("2d")
	if (!ctx) return
	useRectanglePainter(canvas, { x: playerBoxWidth, y: playerBoxHeight }, { x: 2 * playerBoxWidth, y: 2 * playerBoxHeight }, false)
	const dealerText = "Dealer" + (table.dealerHand.values[0] > 0 ? " has " + table.dealerHand.values[0].toString() : "")
	useTextPainter(canvas, { x: playerBoxWidth + 10, y: playerBoxHeight + 20 }, dealerText, blackFont)
}

const paintMainPlayerBox = (player: BlackjackPlayer, canvas: HTMLCanvasElement, isInTurn: boolean) => {
	const ctx = canvas.getContext("2d")
	if (!ctx) return
	useRectanglePainter(canvas, { x: 0, y: canvas.height * 0.75 }, { x: canvas.width, y: canvas.height }, isInTurn)
	useTextPainter(canvas, { x: 5, y: 3 * playerBoxHeight + 18 }, player.userName, blueFont)
	useTextPainter(canvas, { x: 5, y: 3 * playerBoxHeight + 35 }, "total " + player.currentBalance, blackFont)
	const totalBet = player.totalBet != null ? player.totalBet : 0
	useTextPainter(canvas, { x: 5, y: 3 * playerBoxHeight + 55 }, "bet " + totalBet, blueFont)
}

const paintPlayerBox = (boxStartingCorner: Vector, seat: Seat<BlackjackPlayer>, canvas: HTMLCanvasElement, isInTurn: boolean) => {
	useRectanglePainter(canvas, boxStartingCorner, { x: playerBoxWidth, y: playerBoxHeight }, isInTurn)
	if (!seat.player) {
		useTextPainter(canvas, { x: boxStartingCorner.x + 10, y: boxStartingCorner.y + 50 }, "Seat " + seat.number, blueFont)
		return
	}
	useTextPainter(canvas, { x: boxStartingCorner.x + 5, y: boxStartingCorner.y + 18 }, seat.player.userName, blueFont)
	useTextPainter(canvas, { x: boxStartingCorner.x + 5, y: boxStartingCorner.y + 35 }, "$" + seat.player.currentBalance, blueFont)
}
