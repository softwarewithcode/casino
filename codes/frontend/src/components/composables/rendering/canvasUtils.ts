import type { BlackjackHand, BlackjackPlayer, BlackjackTable, Seat } from "@/types/blackjack"
import type { Vector, CasinoFont, Card, SpriteImageMetadata } from "@/types/casino"
import { useCardLocator } from "./cardSpriteUtils"
const infoFont: CasinoFont = {
	color: "blue",
	faceAndSize: "20px Arial"
}
const dealerFont: CasinoFont = {
	color: "E97451",
	faceAndSize: "22px Arial"
}
const reservedSeatFont: CasinoFont = {
	color: "#E97451",
	faceAndSize: "16px Arial"
}

const dealerName = "-"
const INITIAL_DEAL_CARD_DELAY = 1500
let playerBoxHeight
let playerBoxWidth
const cardWidthHeightRatio: number = 0.72
let mainBoxPlayer: BlackjackPlayer
let table: BlackjackTable
const largeBoxIndex = 6
export function useCanvasInitializer(canvas: HTMLCanvasElement) {
	const documentWidth = document.documentElement.clientWidth
	canvas.width = documentWidth > 800 ? 800 : documentWidth
	canvas.height = document.documentElement.clientHeight > 800 ? 800 : document.documentElement.clientHeight
}

export function useActorsPainter(table: BlackjackTable, mainBoxPlayer: BlackjackPlayer, canvas: HTMLCanvasElement) {
	initPainterData(canvas, mainBoxPlayer, table)
	const ctx = canvas.getContext("2d")
	if (!ctx) return
	if (!mainBoxPlayer || Object.keys(mainBoxPlayer).length === 0) return
	paintMainPlayerBox(mainBoxPlayer, canvas, isPlayerInTurn(table, mainBoxPlayer))
	paintPlayersBoxesExcluding(mainBoxPlayer.seatNumber, table, canvas)
	paintDealerBox(canvas)
}
export function useCardsAndHandValuesPainter(table: BlackjackTable, mainBoxPlayer: BlackjackPlayer, canvas: HTMLCanvasElement) {
	initPainterData(canvas, mainBoxPlayer, table)
	const playersWithCards: BlackjackPlayer[] = getPlayersWithCards(table)
	if (playersWithCards.length === 0) return
	paintPlayersCards(playersWithCards, mainBoxPlayer, canvas)
	paintDealerCards(table, canvas)
	paintDealerHandValue(table.dealerHand.values[0], canvas)
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
				//const cardPosition: Vector = calculateCardPositionInPlayerBox(player, mainBoxPlayer, cardIndex, handIndex, canvas)
				paintCard(player.name, card, cardIndex, handIndex, canvas)
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
	paintDealerHandValue(table.dealerHand.values[0], canvas)
	showOneCardFromFirstHandWithDelay(playersWithCards, canvas)
	paintPlayersHandValues(playersWithCards, mainBoxPlayer, canvas)
}

const paintDealerHandValue = (value: number, canvas: HTMLCanvasElement) => {
	paintText("Value:" + value, { x: 2 * playerBoxWidth, y: playerBoxHeight + 20 }, canvas, dealerFont)
}
const getPlayersWithCards = (table: BlackjackTable) => {
	return table.seats.map(seat => seat.player).filter(player => player?.hands && player.hands[0]?.cards.length > 0)
}
const showOneCardFromFirstHandWithDelay = async (players: BlackjackPlayer[], canvas: HTMLCanvasElement) => {
	for (let i = 0; i < players.length; i++) {
		const player = players[i]
		const cardIndex = player.hands[0].cards.findIndex(card => !card.hasOwnProperty("visible"))
		const card = player.hands[0].cards[cardIndex]
		const handIndex = 0
		await paintCardWithDelay(INITIAL_DEAL_CARD_DELAY, player.name, card, cardIndex, handIndex, canvas)
		card.visible = true
	}
}

const paintPlayersHandValues = (playersWithCards: BlackjackPlayer[], mainBoxPlayer: BlackjackPlayer, canvas: HTMLCanvasElement) => {
	playersWithCards.forEach(player => {
		const playerIndex = getPlayerBoxIndexRelativeToMainBoxPlayer(player.name)
		const boxStartingCorner = getPlayerBoxStartingCorner(playerIndex, playerBoxWidth, playerBoxHeight)

		const halfOfPlayerBoxLength = playerIndex === largeBoxIndex ? canvas.width / 2 : playerBoxWidth / 2
		getHands(player).forEach(hand => {
			getHandValues(hand).forEach((value, index) => {
				if (index === 0) {
					paintText("Total:" + value, { x: boxStartingCorner.x + halfOfPlayerBoxLength, y: boxStartingCorner.y + playerBoxHeight - 10 }, canvas, reservedSeatFont)
				}
				if (index === 1) {
					paintText(" / " + value, { x: boxStartingCorner.x + halfOfPlayerBoxLength + 15, y: boxStartingCorner.y + playerBoxHeight - 10 }, canvas, reservedSeatFont)
				}
			})
		})
	})
}

const getHandValues = (hand: BlackjackHand) => {
	return hand ? hand.values : []
}
const getHands = (player: BlackjackPlayer) => {
	return player ? player.hands : []
}

const wait = time => new Promise(r => setTimeout(r, time)) // artificial wait
const initPainterData = (canvas: HTMLCanvasElement, mainPlayer: BlackjackPlayer, tbl: BlackjackTable) => {
	mainBoxPlayer = mainPlayer
	table = tbl
	playerBoxHeight = canvas.height / 4
	playerBoxWidth = canvas.width / 4
}

const paintCardWithDelay = async (delayMillis: number, actorName: string, card: Card, nth: number, handNth: number, canvas: HTMLCanvasElement) => {
	await wait(delayMillis)
	paintCard(actorName, card, nth, handNth, canvas)
}
const cardsSprite: HTMLImageElement = document.getElementById("cardsSprite") as HTMLImageElement

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

	if (actorName === mainBoxPlayer.name) {
		const cardPositionX = playerBoxWidth + nthCard * mainPlayerCardSize.x + 20
		const cardPositionY = 3 * playerBoxHeight + nthHand * mainPlayerCardSize.y
		return { x: cardPositionX, y: cardPositionY }
	}
	const playerIndex = getPlayerBoxIndexRelativeToMainBoxPlayer(actorName)
	let boxStartingCorner = getPlayerBoxStartingCorner(playerIndex, playerBoxWidth, playerBoxHeight)

	let cardPositionX = boxStartingCorner.x + playerCardSize.x * nthCard + 2
	let cardPositionY = boxStartingCorner.y + boxStartingCorner.y / 3 + nthHand * playerCardSize.y
	return { x: cardPositionX, y: cardPositionY }
}

const calculateCardSize = (actorName: string): Vector => {
	if (actorName === dealerName) {
		if (!dealerCardSize) {
			const cardHeight = playerBoxHeight
			const cardWidth = cardHeight * cardWidthHeightRatio
			dealerCardSize = { x: cardWidth, y: cardHeight }
		}
		return dealerCardSize
	}
	const handCount = getPlayerHandCount(actorName)
	if (actorName === mainBoxPlayer.name) {
		if (!mainPlayerCardSize) {
			const mainBoxWidth = playerBoxWidth * 4
			const cardWidth = mainBoxWidth / 10
			const cardHeight = cardWidth / cardWidthHeightRatio / handCount
			mainPlayerCardSize = { x: cardWidth, y: cardHeight }
		}
		return mainPlayerCardSize
	}
	if (!playerCardSize) {
		const cardWidth = playerBoxWidth / 4
		const cardHeight = cardWidth / cardWidthHeightRatio / handCount
		playerCardSize = { x: cardWidth, y: cardHeight }
	}
	return playerCardSize
}

const getPlayerHandCount = (actorName: string) => {
	const playerHandCount = table.seats.find(seat => seat.player?.name === actorName)?.player.hands.length
	if (!playerHandCount) throw new Error("no hand count")
	return playerHandCount
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
			paintPlayerBox(getPlayerBoxStartingCorner(i, playerBoxWidth, playerBoxHeight), seat, canvas, isPlayerInTurn(table, seat.player))
		}
		next = getNextSeatNumber(next, table)
	}
}

const isPlayerInTurn = (table: BlackjackTable, player: BlackjackPlayer): boolean => {
	if (!player) return false
	return player.name === table.playerInTurn?.name
}

const getPlayerBoxIndexRelativeToMainBoxPlayer = (actorName: string) => {
	if (mainBoxPlayer.name === actorName) return largeBoxIndex
	const seat = table.seats.find(seat => seat.player?.name === actorName)
	if (!seat) throw new Error("player not found " + actorName)
	if (mainBoxPlayer.seatNumber > seat.number) {
		return largeBoxIndex - (mainBoxPlayer.seatNumber - seat.number)
	}
	return seat.number - mainBoxPlayer.seatNumber - 1
}

const getPlayerBoxStartingCorner = (index: number, boxWidth: number, boxHeight: number) => {
	let corner: Vector = {} as Vector
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
	//ctx.strokeRect(playerBoxWidth, playerBoxHeight, 2 * playerBoxWidth, 2 * playerBoxHeight)
	paintRectangle({ x: playerBoxWidth, y: playerBoxHeight }, { x: 2 * playerBoxWidth, y: 2 * playerBoxHeight }, canvas, false)
	paintText("Dealer:", { x: playerBoxWidth + 10, y: playerBoxHeight + 20 }, canvas, dealerFont)
}

const paintMainPlayerBox = (player: BlackjackPlayer, canvas: HTMLCanvasElement, isInTurn: boolean) => {
	const ctx = canvas.getContext("2d")
	if (!ctx) return
	paintRectangle({ x: 0, y: canvas.height * 0.75 }, { x: canvas.width, y: canvas.height }, canvas, isInTurn)
	paintText(player.name, { x: 5, y: 3 * playerBoxHeight + 18 }, canvas, reservedSeatFont)
	paintText("$" + player.balance, { x: 5, y: 3 * playerBoxHeight + 35 }, canvas, reservedSeatFont)
}
const paintPlayerBox = (boxStartingCorner: Vector, seat: Seat, canvas: HTMLCanvasElement, isInTurn: boolean) => {
	paintRectangle(boxStartingCorner, { x: playerBoxWidth, y: playerBoxHeight }, canvas, isInTurn)
	if (!seat.player) {
		paintText("Seat " + (seat.number + 1), { x: boxStartingCorner.x + 10, y: boxStartingCorner.y + 50 }, canvas, infoFont)
		return
	}
	paintText(seat.player.name, { x: boxStartingCorner.x + 5, y: boxStartingCorner.y + 18 }, canvas, reservedSeatFont)
	paintText("$" + seat.player.balance, { x: boxStartingCorner.x + 5, y: boxStartingCorner.y + 35 }, canvas, reservedSeatFont)
}

const paintRectangle = (startPosition: Vector, endPosition: Vector, canvas: HTMLCanvasElement, highlight: boolean) => {
	const ctx = canvas.getContext("2d")
	if (!ctx) return
	const originalStyle = ctx.strokeStyle
	const originalWidth = ctx.lineWidth
	if (highlight) {
		ctx.strokeStyle = "green"
		ctx.lineWidth = 10
	}
	ctx.strokeRect(startPosition.x, startPosition.y, endPosition.x, endPosition.y)
	ctx.strokeStyle = originalStyle
	ctx.lineWidth = originalWidth
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
