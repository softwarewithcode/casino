import type { BlackjackHand, BlackjackPlayer, BlackjackTable, Seat } from "@/types/blackjack"
import type { Vector, CasinoFont, Card } from "@/types/casino"
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
let largeBoxPlayerCorner: Vector

let store

export function useCanvasInitializer(canvas: HTMLCanvasElement) {
	const documentWidth = document.documentElement.clientWidth
	canvas.width = documentWidth > 800 ? 800 : documentWidth
	canvas.height = document.documentElement.clientHeight > 800 ? 800 : document.documentElement.clientHeight
}

export function useActorsPainter(table: BlackjackTable, largeBoxPlayer: BlackjackPlayer, canvas: HTMLCanvasElement) {
	console.log("useActorsPainter")
	initPainterData(canvas)
	const ctx = canvas.getContext("2d")
	if (!ctx) return
	if (!largeBoxPlayer || Object.keys(largeBoxPlayer).length === 0) return
	paintlargeBoxPlayer(largeBoxPlayer, canvas)
	paintPlayersExcludinglargeBoxPlayer(largeBoxPlayer.seatNumber, table, canvas)
	paintDealer(canvas)
}
export function useCardsAndHandValuesPainter(table: BlackjackTable, largeBoxPlayer: BlackjackPlayer, canvas: HTMLCanvasElement) {
	console.log("useCardsAndHandValuesPainter")
	initPainterData(canvas)
	const playersWithCards: BlackjackPlayer[] = table.seats.map(seat => seat.player).filter(player => player?.hands.length > 0)
	if (playersWithCards.length === 0) return
	playersWithCards.forEach(player => {
		console.log("useCardsAndHandValuesPainter 3")
		player.hands.forEach((hand, handIndex) => {
			console.log("useCardsAndHandValuesPainter 2")
			hand.cards.forEach((card, cardIndex) => {
				console.log("useCardsAndHandValuesPainter 2")
				const cardCoordinates: Vector = calculateCardCoordinates(player, largeBoxPlayer, cardIndex, handIndex)
				console.log("useCardsAndHandValuesPainter 1:" + handIndex + " cardIndex:" + cardIndex)
				paintCard(cardCoordinates, card, canvas)
			})
		})
	})
	const dealerCard: Card = table.dealerHand.cards.forEach((dealerCard, index) => {
		paintCard(dealerFirstCardPosition + index * 25, dealerCard, canvas)
	})
	paintHandValues(playersWithCards, largeBoxPlayer, canvas)
}
export async function useInitialDealPainter(table: BlackjackTable, largeBoxPlayer: BlackjackPlayer, canvas: HTMLCanvasElement) {
	console.log("useInitialDealPainter")
	const ctx = canvas.getContext("2d")
	if (!ctx) return
	initPainterData(canvas)
	const playersWithCards: BlackjackPlayer[] = table.seats.map(seat => seat.player).filter(player => player?.hands.length > 0)
	showOneCardFromFirstHandWithDelay(playersWithCards, largeBoxPlayer)
	const dealerCard: Card = table.dealerHand.cards[0]
	console.log("dealerCard:" + dealerCard)
	await paintCardWithDelay(INITIAL_DEAL_CARD_DELAY, dealerFirstCardPosition, dealerCard, canvas)
	showOneCardFromFirstHandWithDelay(playersWithCards, largeBoxPlayer)
	paintHandValues(playersWithCards, largeBoxPlayer, canvas)
}
const showOneCardFromFirstHandWithDelay = async (players: BlackjackPlayer[], largeBoxPlayer: BlackjackPlayer) => {
	for (let i = 0; i < players.length; i++) {
		const player = players[i]
		console.log("hands0 delay:" + JSON.stringify(player.hands[0]))
		const cardIndex = player.hands[0].cards.findIndex(card => !card.hasOwnProperty("visible"))
		const card = player.hands[0].cards[cardIndex]
		const cardCoordinates = calculateCardCoordinates(player, largeBoxPlayer, cardIndex, 0)
		console.log("playerCard:" + card)
		await paintCardWithDelay(INITIAL_DEAL_CARD_DELAY, cardCoordinates, card, canvas)
		card.visible = true
	}
}
const calculateCardCoordinates = (player: BlackjackPlayer, largeBoxPlayer: BlackjackPlayer, cardIndex: number, handIndex: number): Vector => {
	const playerIndex = getPlayerIndexRelativeToLargeBoxPlayer(largeBoxPlayer, player)
	let boxStartingCorner = getBoxStartingCorner(playerIndex, playerBoxWidth, playerBoxHeight)
	const cardXCoordinateInPlayerBox = playerIndex === largeBoxIndex ? canvas.width / 2 : boxStartingCorner.x + playerBoxWidth / 2
	const cardYCoordinateInPlayerBox = boxStartingCorner.y + playerBoxHeight / 2 + cardIndex * 25
	return { x: cardXCoordinateInPlayerBox, y: cardYCoordinateInPlayerBox }
}

const paintHandValues = (playersWithCards: BlackjackPlayer[], largeBoxPlayer: BlackjackPlayer, canvas: HTMLCanvasElement) => {
	playersWithCards.forEach(player => {
		const playerIndex = getPlayerIndexRelativeToLargeBoxPlayer(largeBoxPlayer, player)
		let boxStartingCorner = getBoxStartingCorner(playerIndex, playerBoxWidth, playerBoxHeight)
		const halfOfPlayerBoxLength = playerIndex === largeBoxIndex ? canvas.width / 2 : playerBoxWidth / 2
		getHands(player).forEach(hand => {
			getHandValues(hand).forEach((value, index) => {
				if (index === 0) {
					paintText(value, { x: boxStartingCorner.x + halfOfPlayerBoxLength, y: boxStartingCorner.y + playerBoxHeight - 20 }, canvas, reservedSeatFont)
				}
				if (index === 1) {
					paintText(" / " + value, { x: boxStartingCorner.x + halfOfPlayerBoxLength + 30, y: boxStartingCorner.y + playerBoxHeight - 20 }, canvas, reservedSeatFont)
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
		x: playerBoxWidth + playerBoxHeight / 3,
		y: playerBoxHeight + playerBoxWidth / 3
	}
	largeBoxPlayerCorner = { x: 0, y: 3 * playerBoxHeight }
}

const paintCardWithDelay = async (delayMillis: number, startingCorner: Vector, card: Card, canvas: HTMLCanvasElement) => {
	paintCard(startingCorner, card, canvas)
	await wait(delayMillis)
}
const paintCard = (startingCorner: Vector, card: Card, canvas: HTMLCanvasElement) => {
	if (!card) {
		console.log("no card ?")
		return
	}
	console.log("painted card:" + JSON.stringify(card))

	//paintImage
	paintText(card.rank + " " + card.suit, { x: startingCorner.x, y: startingCorner.y }, canvas, reservedSeatFont)
}

const getNextSeatNumber = (featuredSeatNumber: number, table: BlackjackTable): number => {
	if (featuredSeatNumber === table.seats.length - 1) return 0
	let next = featuredSeatNumber
	next++
	return next
}

const paintPlayersExcludinglargeBoxPlayer = (excludedSeatNumber: number, table: BlackjackTable, canvas: HTMLCanvasElement) => {
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
const getPlayerIndexRelativeToLargeBoxPlayer = (largeBoxPlayer: BlackjackPlayer, iterationPlayer: BlackjackPlayer) => {
	if (largeBoxPlayer.name === iterationPlayer.name) return largeBoxIndex
	if (largeBoxPlayer.seatNumber > iterationPlayer.seatNumber) {
		return largeBoxIndex - (largeBoxPlayer.seatNumber - iterationPlayer.seatNumber)
	}
	return iterationPlayer.seatNumber - largeBoxPlayer.seatNumber - 1
}

const getBoxStartingCorner = (index: number, boxWidth: number, boxHeight: number) => {
	let corner: Vector
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

const paintDealer = (canvas: HTMLCanvasElement) => {
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

const paintlargeBoxPlayer = (player: BlackjackPlayer, canvas: HTMLCanvasElement) => {
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
