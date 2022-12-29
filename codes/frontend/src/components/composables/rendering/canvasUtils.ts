import type { BlackjackHand, BlackjackPlayer, BlackjackTable, Seat } from "@/types/blackjack"
import type { Vector, CasinoFont, Card, ImageProps } from "@/types/casino"
const availableSeatFont: CasinoFont = {
	color: "#006400",
	faceAndSize: "25px Arial"
}
const reservedSeatFont: CasinoFont = {
	color: "#E97451",
	faceAndSize: "16px Arial"
}
const INITIAL_DEAL_CARD_DELAY = 1500
let playerBoxHeight
let playerBoxWidth
let dealerFirstCardPosition: Vector
let mainBoxPlayerCorner: Vector

let store

export function useCanvasInitializer(canvas: HTMLCanvasElement) {
	const documentWidth = document.documentElement.clientWidth
	canvas.width = documentWidth > 800 ? 800 : documentWidth
	canvas.height = document.documentElement.clientHeight > 800 ? 800 : document.documentElement.clientHeight
}

export function useActorsPainter(table: BlackjackTable, mainBoxPlayer: BlackjackPlayer, canvas: HTMLCanvasElement) {
	initPainterData(canvas)
	const ctx = canvas.getContext("2d")
	if (!ctx) return
	if (!mainBoxPlayer || Object.keys(mainBoxPlayer).length === 0) return
	paintMainPlayerBox(mainBoxPlayer, canvas)
	paintPlayersBoxesExcluding(mainBoxPlayer.seatNumber, table, canvas)
	paintDealerBox(canvas)
}
export function useCardsAndHandValuesPainter(table: BlackjackTable, mainBoxPlayer: BlackjackPlayer, canvas: HTMLCanvasElement) {
	initPainterData(canvas)
	const playersWithCards: BlackjackPlayer[] = getPlayersWithCards(table)
	if (playersWithCards.length === 0) return
	playersWithCards.forEach(player => {
		player.hands.forEach((hand, handIndex) => {
			hand.cards.forEach((card, cardIndex) => {
				const cardCoordinates: Vector = calculateCardCoordinates(player, mainBoxPlayer, cardIndex, handIndex)
				paintCard(cardCoordinates, card, canvas)
			})
		})
	})
	const dealerCard: Card = table.dealerHand.cards.forEach((dealerCard, index) => {
		let cardPosition = dealerFirstCardPosition
		cardPosition.y = +cardPosition.y + index * 25
		paintCard(cardPosition, dealerCard, canvas)
	})
	paintDealerHandValue(table.dealerHand.values[0])
	paintPlayersHandValues(playersWithCards, mainBoxPlayer, canvas)
}
export async function useInitialDealPainter(table: BlackjackTable, mainBoxPlayer: BlackjackPlayer, canvas: HTMLCanvasElement) {
	const ctx = canvas.getContext("2d")
	if (!ctx) return
	initPainterData(canvas)
	const playersWithCards: BlackjackPlayer[] = getPlayersWithCards(table)
	showOneCardFromFirstHandWithDelay(playersWithCards, mainBoxPlayer)
	const dealerCard: Card = table.dealerHand.cards[0]
	await paintCardWithDelay(INITIAL_DEAL_CARD_DELAY, dealerFirstCardPosition, dealerCard, canvas)
	paintDealerHandValue(table.dealerHand.values[0])
	showOneCardFromFirstHandWithDelay(playersWithCards, mainBoxPlayer)
	paintPlayersHandValues(playersWithCards, mainBoxPlayer, canvas)
}

const paintDealerHandValue = (value: number) => {
	paintText("Total:" + value, { x: dealerFirstCardPosition.x, y: dealerFirstCardPosition.y + 20 }, canvas, reservedSeatFont)
}
const getPlayersWithCards = (table: BlackjackTable) => {
	return table.seats.map(seat => seat.player).filter(player => player?.hands && player.hands[0]?.cards.length > 0)
}
const showOneCardFromFirstHandWithDelay = async (players: BlackjackPlayer[], mainBoxPlayer: BlackjackPlayer) => {
	for (let i = 0; i < players.length; i++) {
		const player = players[i]

		const cardIndex = player.hands[0].cards.findIndex(card => !card.hasOwnProperty("visible"))
		const card = player.hands[0].cards[cardIndex]
		const cardCoordinates = calculateCardCoordinates(player, mainBoxPlayer, cardIndex, 0)

		await paintCardWithDelay(INITIAL_DEAL_CARD_DELAY, cardCoordinates, card, canvas)
		card.visible = true
	}
}
const calculateCardCoordinates = (player: BlackjackPlayer, mainBoxPlayer: BlackjackPlayer, cardIndex: number, handIndex: number): Vector => {
	const playerIndex = getPlayerIndexRelativeToMainBoxPlayer(mainBoxPlayer, player)
	let boxStartingCorner = getBoxStartingCorner(playerIndex, playerBoxWidth, playerBoxHeight)
	const cardXCoordinateInPlayerBox = playerIndex === largeBoxIndex ? canvas.width / 2 : boxStartingCorner.x + playerBoxWidth / 2
	const cardYCoordinateInPlayerBox = boxStartingCorner.y + playerBoxHeight / 2 + cardIndex * 25
	return { x: cardXCoordinateInPlayerBox, y: cardYCoordinateInPlayerBox }
}

const paintPlayersHandValues = (playersWithCards: BlackjackPlayer[], mainBoxPlayer: BlackjackPlayer, canvas: HTMLCanvasElement) => {
	playersWithCards.forEach(player => {
		const playerIndex = getPlayerIndexRelativeToMainBoxPlayer(mainBoxPlayer, player)
		const boxStartingCorner = getBoxStartingCorner(playerIndex, playerBoxWidth, playerBoxHeight)
		const halfOfPlayerBoxLength = playerIndex === largeBoxIndex ? canvas.width / 2 : playerBoxWidth / 2
		getHands(player).forEach(hand => {
			getHandValues(hand).forEach((value, index) => {
				if (index === 0) {
					paintText("Total:" + value, { x: boxStartingCorner.x + halfOfPlayerBoxLength, y: boxStartingCorner.y + playerBoxHeight - 10 }, canvas, reservedSeatFont)
				}
				if (index === 1) {
					paintText(" / " + value, { x: boxStartingCorner.x + halfOfPlayerBoxLength + 5, y: boxStartingCorner.y + playerBoxHeight - 10 }, canvas, reservedSeatFont)
				}
			})
		})
	})
}

const getHandValues = (hand: Hand) => {
	return hand ? hand.values : []
}
const getHands = (player: BlackjackPlayer) => {
	return player ? player.hands : []
}

const wait = time => new Promise(r => setTimeout(r, time)) // artificial wait
const initPainterData = (canvas: HTMLCanvasElement) => {
	playerBoxHeight = canvas.height / 4
	playerBoxWidth = canvas.width / 4
	dealerFirstCardPosition = {
		x: playerBoxWidth + playerBoxWidth / 3,
		y: playerBoxHeight + playerBoxHeight / 3
	}
	mainBoxPlayerCorner = { x: 0, y: 3 * playerBoxHeight }
}

const paintCardWithDelay = async (delayMillis: number, startingCorner: Vector, card: Card, canvas: HTMLCanvasElement) => {
	paintCard(startingCorner, card, canvas)
	await wait(delayMillis)
}
const cardsImage: HtmlImageElement = document.getElementById("cardsImage")
const paintCard = (startingCorner: Vector, card: Card, canvas: HTMLCanvasElement) => {
	if (!card) {
		console.log("no card")
		return
	}
	const ctx = canvas.getContext("2d")
	if (!ctx) return
	const coords = findImageCoordinates(startingCorner, card)
	ctx.drawImage(cardsImage, coords.clipFromX, coords.clipFromY, coords.sourceWidth, coords.sourceHeight, coords.destinationWidth, coords.destinationHeight, coords.toX, coords.toY)
	//paintText(card.rank + " " + card.suit, { x: startingCorner.x, y: startingCorner.y }, canvas, reservedSeatFont)
}

const cardsWidth = 100
const cardsHeight = 100
const findImageCoordinates = (startingCorner: Vector, card: Card): ImageProps => {
	const fromX = card.rank * 100
	const fromy = card.rank * 100
	return {
		clipFromX: 0,
		clipFromY: 0,
		sourceWidth: suitToNumber(card.suit) * cardsWidth,
		sourceHeight: card.rank * cardsHeight,
		destinationWidth: startingCorner.x,
		destinationHeight: startingCorner.y,
		toX: 100,
		toY: 100
	}
}
const suitToNumber = (suit: Suit) => {
	return 2
	// (suit === Suit.CLUB) return 3
	//if (suit === Suit.HEART) return 1
	//if (suit === Suit.SPADE) return 4
	//if (suit === Suit.DIAMOND) return 2
}
const getNextSeatNumber = (featuredSeatNumber: number, table: BlackjackTable): number => {
	if (featuredSeatNumber === table.seats.length - 1) return 0
	let next = featuredSeatNumber
	next++
	return next
}

const paintPlayersBoxesExcluding = (excludedSeatNumber: number, table: BlackjackTable, canvas: HTMLCanvasElement) => {
	const ctx = canvas.getContext("2d")
	if (!ctx) return
	let next = getNextSeatNumber(excludedSeatNumber, table)
	for (let i = 0; i < table.seats.length; i++) {
		const seat: Seat = table.seats.find(seat => seat.number === next) as Seat
		if (excludedSeatNumber !== seat.number) {
			paintPlayerBox(getBoxStartingCorner(i, playerBoxWidth, playerBoxHeight), seat, canvas)
		}
		next = getNextSeatNumber(next, table)
	}
}
const largeBoxIndex = 6
const getPlayerIndexRelativeToMainBoxPlayer = (mainBoxPlayer: BlackjackPlayer, iterationPlayer: BlackjackPlayer) => {
	if (mainBoxPlayer.name === iterationPlayer.name) return largeBoxIndex
	if (mainBoxPlayer.seatNumber > iterationPlayer.seatNumber) {
		return largeBoxIndex - (mainBoxPlayer.seatNumber - iterationPlayer.seatNumber)
	}
	return iterationPlayer.seatNumber - mainBoxPlayer.seatNumber - 1
}

const getBoxStartingCorner = (index: number, boxWidth: number, boxHeight: number) => {
	let corner: VectorpaintDealerBox
	if (index === 0) {
		corner = { x: 0, y: boxHeight }
	} else if (index === 1) {
		corner = { x: 0, y: 0 }
	} else if (index === 2) {
		corner = { x: boxWidth, y: 0 }
	} else if (index === 3) {
		corner = { x: 2 * boxWidth, y: 0 }
	} else if (index === 4) {
		corner = { x: 3 * boxWidth, y: 0 }
	} else if (index === 5) {
		corner = { x: 3 * boxWidth, y: boxHeight }
	} else if (index === largeBoxIndex) {
		corner = { x: 0, y: boxHeight * 3 }
	}
	return corner
}

const paintDealerBox = (canvas: HTMLCanvasElement) => {
	const ctx = canvas.getContext("2d")
	if (!ctx) return
	ctx.strokeRect(playerBoxWidth, playerBoxHeight, 2 * playerBoxWidth, 2 * playerBoxHeight)
	paintText("Dealer:", { x: playerBoxWidth + 10, y: playerBoxHeight + 20 }, canvas, reservedSeatFont)
}

const paintPlayerBox = (boxStartingCorner: Vector, seat: Seat, canvas: HTMLCanvasElement) => {
	const ctx = canvas.getContext("2d")
	if (!ctx) return
	ctx.strokeRect(boxStartingCorner.x, boxStartingCorner.y, playerBoxWidth, playerBoxHeight)
	paintText(seat.player?.name + " " + seat.number, { x: boxStartingCorner.x + 10, y: boxStartingCorner.y + 20 }, canvas, reservedSeatFont)
	paintText("Balance " + seat.player?.balance, { x: boxStartingCorner.x + 10, y: boxStartingCorner.y + 50 }, canvas, reservedSeatFont)
}

const paintMainPlayerBox = (player: BlackjackPlayer, canvas: HTMLCanvasElement) => {
	const ctx = canvas.getContext("2d")
	if (!ctx) return
	ctx.strokeRect(0, canvas.height * 0.75, canvas.width, canvas.height)
	paintText(player.name + " " + player.seatNumber, { x: 10, y: canvas.height - 60 }, canvas, reservedSeatFont)
	paintText("Balance " + player.balance, { x: 10, y: canvas.height - 30 }, canvas, reservedSeatFont)
}

const paintText = (text: string, startPosition: Vector, canvas: HTMLCanvasElement, font: CasinoFont) => {
	const ctx = canvas.getContext("2d")
	if (!ctx || !text) return
	const originalFont = ctx.font
	const originalColor = ctx.fillStyle
	ctx.font = font.faceAndSize
	ctx.fillStyle = font.color
	ctx.fillText(text, startPosition.x, startPosition.y)
	ctx.font = originalFont
	ctx.fillStyle = originalColor
}
