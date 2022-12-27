import type { BlackjackHand, BlackjackPlayer, BlackjackTable, Seat } from "@/types/blackjack"
import type { Vector, CasinoFont, Card } from "@/types/casino"
const availableSeatFont: CasinoFont = {
	color: "#006400",
	faceAndSize: "25px Arial"
}
const reservedSeatFont: CasinoFont = {
	color: "#E97451",
	faceAndSize: "25px Arial"
}
const INITIAL_DEAL_CARD_DELAY = 1500
let playerBoxHeight
let playerBoxWidth
let dealerFirstCardPosition: Vector
let focusPlayerCorner: Vector
let focusPlayerNextCardPosition: Vector
let store

export function useCanvasInitializer(canvas: HTMLCanvasElement) {
	const documentWidth = document.documentElement.clientWidth
	canvas.width = documentWidth > 800 ? 800 : documentWidth
	canvas.height = document.documentElement.clientHeight > 800 ? 800 : document.documentElement.clientHeight
}

export function useActorsPainter(table: BlackjackTable, canvas: HTMLCanvasElement, mainPlayer: BlackjackPlayer) {
	initPainterData(canvas)
	const ctx = canvas.getContext("2d")
	if (!ctx) return
	if (!mainPlayer || Object.keys(mainPlayer).length === 0) return
	paintMainPlayer(mainPlayer, canvas)
	paintPlayersExcludingMainPlayer(mainPlayer.seatNumber, table, canvas)
	paintDealer(canvas)
}
export async function useInitialDealPainter(table: BlackjackTable, focusPlayer: BlackjackPlayer, canvas: HTMLCanvasElement) {
	console.log("useInitialDealPainter")
	initPainterData(canvas)
	const ctx = canvas.getContext("2d")
	if (!ctx) return
	const playersWithCards: BlackjackPlayer[] = table.seats.map((seat) => seat.player).filter((player) => player?.hands.length > 0)
	for (let i = 0; i < playersWithCards.length; i++) {
		const player = playersWithCards[i]
		const card = player.hands[0].cards[0]
		if (focusPlayer.name === player.name) {
			focusPlayerNextCardPosition = { x: focusPlayerCorner.x + canvas.width / 2, y: focusPlayerCorner.y + playerBoxHeight / 2 }
			await paintCardsDelayed(INITIAL_DEAL_CARD_DELAY, focusPlayerNextCardPosition, card, canvas)
		} else {
			await paintCardsDelayed(INITIAL_DEAL_CARD_DELAY, getBoxStartingCorner(player.seatNumber, playerBoxWidth, playerBoxHeight), card, canvas)
		}
	}
	const dealerCard: Card = table.dealerHand.cards[0]
	paintCard(dealerFirstCardPosition, dealerCard, canvas)

	for (let i = 0; i < playersWithCards.length; i++) {
		const player = playersWithCards[i]
		const card = player.hands[0].cards[1]
		if (focusPlayer.name === player.name) {
			focusPlayerNextCardPosition.x = focusPlayerNextCardPosition.x
			focusPlayerNextCardPosition.y = focusPlayerNextCardPosition.y + 30
			await paintCardsDelayed(INITIAL_DEAL_CARD_DELAY, focusPlayerNextCardPosition, card, canvas)
		} else {
			await paintCardsDelayed(INITIAL_DEAL_CARD_DELAY, getBoxStartingCorner(player.seatNumber, playerBoxWidth, playerBoxHeight), card, canvas)
		}
	}
}
const wait = (time) => new Promise((r) => setTimeout(r, time)) // artificial wait
const initPainterData = (canvas: HTMLCanvasElement) => {
	playerBoxHeight = canvas.height / 4
	playerBoxWidth = canvas.width / 4
	dealerFirstCardPosition = {
		x: playerBoxWidth + playerBoxHeight / 3,
		y: playerBoxHeight + playerBoxWidth / 3
	}
	focusPlayerCorner = { x: 0, y: 3 * playerBoxHeight }
}

const paintCardsDelayed = async (delayMillis: number, startingCorner: Vector, card: Card, canvas: HTMLCanvasElement) => {
	await wait(delayMillis)
	paintCard(startingCorner, card, canvas)
}
const paintCard = (startingCorner: Vector, card: Card, canvas: HTMLCanvasElement) => {
	if (!card) {
		console.log("no card ?")
		return
	}
	//paintImage
	paintText(card.rank + " " + card.suit, { x: startingCorner.x, y: startingCorner.y }, canvas, reservedSeatFont)
}

const getNextSeatNumber = (lastDrawnSeat: number, table: BlackjackTable): number => {
	if (lastDrawnSeat === table.seats.length - 1) return 0
	let next = lastDrawnSeat
	next++
	return next
}

const paintPlayersExcludingMainPlayer = (excludedSeat: number, table: BlackjackTable, canvas: HTMLCanvasElement) => {
	const ctx = canvas.getContext("2d")
	if (!ctx) return
	let next = getNextSeatNumber(excludedSeat, table)
	for (let i = 0; i < table.seats.length; i++) {
		const seat: Seat = table.seats.find((seat) => seat.number === next) as Seat
		if (excludedSeat !== seat.number) paintPlayerBox(getBoxStartingCorner(i, playerBoxWidth, playerBoxHeight), seat, canvas)
		next = getNextSeatNumber(next, table)
	}
}

const getBoxStartingCorner = (index: number, boxWidth: number, boxHeight: number) => {
	if (index === 0) {
		return { x: 0, y: boxHeight }
	} else if (index === 1) {
		return { x: 0, y: 0 }
	} else if (index === 2) {
		return { x: boxWidth, y: 0 }
	} else if (index === 3) {
		return { x: 2 * boxWidth, y: 0 }
	} else if (index === 4) {
		return { x: 3 * boxWidth, y: 0 }
	}
	return { x: 3 * boxWidth, y: boxHeight }
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

const paintMainPlayer = (player: BlackjackPlayer, canvas: HTMLCanvasElement) => {
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
