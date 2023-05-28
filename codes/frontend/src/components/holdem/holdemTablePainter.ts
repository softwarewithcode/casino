import { PlayerStatus, type ChipStack, type Seat } from "@/types/casino"
import { useVectorAdder, useVectorXComponentAdder, useVectorYComponentAdder, type Vector } from "../../types/vectors"

import { blackFont, blueFont, grayColorAlpha, grayColorAlpha6, orangeLargeFont } from "../../types/fontsAndColors"

import { chipDealer, chipAnyImage } from "../../types/images"
import { useCardBackSidePainter, useCardPainter, useImagePainter, useImagePainter2, usePlayerBoxStartingCornerCalculator, useRectanglePainter, useTextPainter, useWait } from "../composables/rendering/commonPainter"
import { useActivePlayerChecker, useNextSeatNumberCalculator, usePlayerAllowedStatusesChecker, usePlayerEqualsChecker, useSeatToIndexMapper } from "../composables/common/table"
import { type HoldemTable, type HoldemPlayer, GamePhase, type HoldemAction, TableAction, type GameAction } from "@/types/texasHoldem"
import { bgImage } from "../../types/images"
import { useChipsTransformer } from "./chipsCalculator"
let playerBoxHeight
let playerBoxWidth
let mainPlayerBoxWidth
let mainPlayerBoxHeight
let mainBoxPlayerStartingCorner: Vector
let mainBoxPlayer: HoldemPlayer
let table: HoldemTable
let mainPlayerBoxIndex: number
let chipSize: Vector
let buttonSize: number
let cardSize: Vector
let hiddenCardSize: Vector
const potText = "Pot:"
let canvasMiddlePoint: Vector
let potChipsStartPoint: Vector
let potChipsEndPoint: Vector
let dealerButtonSize: Vector

export function useHoldemTablePainter(table: HoldemTable, mainBoxPlayer: HoldemPlayer, canvas: HTMLCanvasElement) {
	initPainterData(canvas, mainBoxPlayer, table)
	const ctx = canvas.getContext("2d")
	if (!ctx) return
	paintBackgroundImage(canvas)
	if (!mainBoxPlayer || !mainBoxPlayer.userName) return
	paintMainPlayerBox(mainBoxPlayer, canvas, useActivePlayerChecker(table, mainBoxPlayer))
	paintPlayersBoxesExcluding(canvas, mainBoxPlayer.seatNumber, table)
	paintPots(canvas, table)
	paintTableCards(canvas, table)
}
const initPainterData = (canvas: HTMLCanvasElement, mainPlayer: HoldemPlayer, tbl: HoldemTable) => {
	mainBoxPlayer = mainPlayer
	table = tbl
	playerBoxHeight = canvas.height / 4
	playerBoxWidth = canvas.width / 4
	mainPlayerBoxHeight = canvas.width / 4
	mainPlayerBoxWidth = canvas.width
	mainBoxPlayerStartingCorner = { x: 0, y: canvas.height * 0.75 }
	chipSize = { x: canvas.width / 25, y: canvas.height / 25 }
	buttonSize = canvas.width / 20
	mainPlayerBoxIndex = tbl.seats.length - 1
	canvasMiddlePoint = { x: canvas.width / 2, y: canvas.height / 2 }
	cardSize = { x: 75, y: 75 }
	potChipsStartPoint = { x: playerBoxWidth * 1.25, y: 1.5 * playerBoxHeight }
	potChipsEndPoint = useVectorXComponentAdder(potChipsStartPoint, playerBoxWidth)
	hiddenCardSize = { x: cardSize.x / 2, y: cardSize.y / 2 }
	const buttonWidthAndHeight = canvas.width / 25
	dealerButtonSize = { x: buttonWidthAndHeight, y: buttonWidthAndHeight }
}

function paintDealerButton(canvas: HTMLCanvasElement, position: Vector) {
	useImagePainter2(canvas, chipDealer, { x: 0, y: 0 }, { x: chipDealer.width, y: chipDealer.height }, position, dealerButtonSize)
}

const paintMainPlayerBox = (mainPlayer: HoldemPlayer, canvas: HTMLCanvasElement, isInTurn: boolean) => {
	const ctx = canvas.getContext("2d")
	if (!ctx) return
	const statusColor = usePlayerAllowedStatusesChecker(mainPlayer, PlayerStatus.ACTIVE) ? undefined : grayColorAlpha
	useRectanglePainter(canvas, { x: 0, y: canvas.height * 0.75 }, { x: canvas.width, y: canvas.height }, isInTurn, statusColor)
	useTextPainter(canvas, { x: 5, y: 3 * playerBoxHeight + 18 }, mainPlayer.userName, blueFont)
	useTextPainter(canvas, { x: 5, y: 3 * playerBoxHeight + 35 }, "total " + mainPlayer.currentBalance, blackFont)
	const boxStartingCorner: Vector = { x: 0, y: canvas.height * 0.75 }
	const firstCardPosition = { x: canvas.width / 3, y: boxStartingCorner.y + 10 }

	if (mainPlayer.cards && mainPlayer.cards.length >= 2) {
		useCardPainter(canvas, mainPlayer.cards[0], firstCardPosition, cardSize)
		useCardPainter(canvas, mainPlayer.cards[1], useVectorXComponentAdder(firstCardPosition, 95), cardSize)
	} else if (mainPlayer.cards && mainPlayer.cards.length === 0) {
		console.log("mainPlayerCards:" + mainPlayer.cards.length)
		useCardBackSidePainter(canvas, firstCardPosition, hiddenCardSize)
		useCardBackSidePainter(canvas, useVectorXComponentAdder(firstCardPosition, 50), hiddenCardSize)
	}

	if (mainPlayer.chipsOnTable > 0) {
		const chipStack = useChipsTransformer(mainPlayer.chipsOnTable)
		const chipsStartingCorner = useVectorAdder(mainBoxPlayerStartingCorner, mainPlayerBoxWidth / 3, -chipSize.y - 10)
		const chipsEndingCorner = useVectorXComponentAdder(chipsStartingCorner, 250)
		useTextPainter(canvas, useVectorYComponentAdder(chipsStartingCorner, -10), mainPlayer.chipsOnTable.toFixed(2), blackFont)
		paintChips(canvas, chipsStartingCorner, chipsEndingCorner, chipStack)
	}
	if (table.button?.seatNumber === mainPlayer.seatNumber) {
		const dealerButtonPosition: Vector = useVectorAdder(mainBoxPlayerStartingCorner, mainPlayerBoxWidth * 0.75, -25)
		paintDealerButton(canvas, dealerButtonPosition)
	}
	if (mainPlayer.lastAction) {
		const startPosition = mainBoxPlayerStartingCorner //useVectorXComponentAdder(mainBoxPlayerStartingCorner, mainPlayerBoxWidth / 3)
		const widthAndHeight = { x: mainPlayerBoxWidth, y: mainPlayerBoxHeight }
		const textStartPosition = useVectorAdder(startPosition, mainPlayerBoxWidth / 3, mainPlayerBoxHeight / 2)
		paintPlayedAction(canvas, startPosition, widthAndHeight, textStartPosition, mainPlayer)
	}
}

const paintPlayedAction = (canvas: HTMLCanvasElement, highLightBoxStartPosition: Vector, boxWidthAndHeight: Vector, textStartPosition: Vector, player: HoldemPlayer) => {
	if (player.lastAction) {
		useRectanglePainter(canvas, highLightBoxStartPosition, boxWidthAndHeight, false, grayColorAlpha6)
		useTextPainter(canvas, textStartPosition, player.lastAction, orangeLargeFont)
	}
}

const paintPlayerBox = (canvas: HTMLCanvasElement, boxStartingCorner: Vector, seat: Seat<HoldemPlayer>, isInTurn: boolean) => {
	useRectanglePainter(canvas, boxStartingCorner, { x: playerBoxWidth, y: playerBoxHeight }, isInTurn)
	if (!seat.player) {
		useTextPainter(canvas, { x: boxStartingCorner.x + 10, y: boxStartingCorner.y + 50 }, "Seat " + seat.number, blueFont)
		return
	}
	if (!usePlayerAllowedStatusesChecker(seat.player, PlayerStatus.ACTIVE)) {
		//SIT_OUT style
		useRectanglePainter(canvas, boxStartingCorner, { x: playerBoxWidth, y: playerBoxHeight }, isInTurn, grayColorAlpha)
	}
	useTextPainter(canvas, { x: boxStartingCorner.x + 5, y: boxStartingCorner.y + 18 }, seat.player.userName, blueFont)
	useTextPainter(canvas, { x: boxStartingCorner.x + 5, y: boxStartingCorner.y + 35 }, "$" + seat.player.currentBalance, blueFont)
	const firstCardPosition = useVectorAdder(boxStartingCorner, 50, 50)
	if (seat.player.cards && seat.player.cards.length >= 2) {
		useCardPainter(canvas, seat.player.cards[0], firstCardPosition, cardSize)
		useCardPainter(canvas, seat.player.cards[1], useVectorXComponentAdder(firstCardPosition, cardSize.x), cardSize)
	} else if (seat.player.cards && seat.player.cards.length === 0) {
		useCardBackSidePainter(canvas, firstCardPosition, hiddenCardSize)
		useCardBackSidePainter(canvas, useVectorXComponentAdder(firstCardPosition, 50), hiddenCardSize)
	}

	useTextPainter(canvas, { x: boxStartingCorner.x + 5, y: boxStartingCorner.y + 35 }, "$" + seat.player.currentBalance, blueFont)
	if (seat.player.chipsOnTable > 0) {
		//CHIPS
		const chipStack: ChipStack = useChipsTransformer(seat.player.chipsOnTable)
		const chipsAreaStart = useVectorYComponentAdder(boxStartingCorner, playerBoxHeight - chipSize.y)
		useTextPainter(canvas, useVectorYComponentAdder(chipsAreaStart, -10), seat.player.chipsOnTable.toFixed(2), blackFont)
		const chipsAreaEnd = useVectorAdder(boxStartingCorner, playerBoxWidth, playerBoxHeight - 20)
		paintChips(canvas, chipsAreaStart, chipsAreaEnd, chipStack)
	}
	if (table.button?.seatNumber === seat.number) {
		//DEALER_BUTTON
		const dealerButtonPosition: Vector = useVectorAdder(boxStartingCorner, playerBoxWidth - dealerButtonSize.x, 0)
		paintDealerButton(canvas, dealerButtonPosition)
	}

	if (seat.player.lastAction) {
		//PlayedAction visualizer
		const startPosition = boxStartingCorner
		const widthAndHeight = { x: playerBoxWidth, y: playerBoxHeight }
		const textStartPosition = useVectorAdder(startPosition, 20, playerBoxHeight / 2)
		paintPlayedAction(canvas, startPosition, widthAndHeight, textStartPosition, seat.player)
	}
}

const paintChips = (canvas: HTMLCanvasElement, startingPoint: Vector, endPoint: Vector, chipStack: ChipStack) => {
	let paintedChipsCount = 0
	for (let [chip, chipCount] of chipStack.chips.entries()) {
		if (chipCount < 1) continue
		for (let j = 0; j < chipCount; j++) {
			const chipStartingPoint = useVectorXComponentAdder(startingPoint, chipSize.x * paintedChipsCount)
			if (chipStartingPoint.x > useVectorXComponentAdder(endPoint, -chipSize.x).x) {
				useImagePainter2(canvas, chipAnyImage, { x: 0, y: 0 }, { x: chip.image.width, y: chip.image.height }, useVectorXComponentAdder(endPoint, -chipSize.x), chipSize)
			} else {
				useImagePainter2(canvas, chip.image, { x: 0, y: 0 }, { x: chip.image.width, y: chip.image.height }, chipStartingPoint, chipSize)
			}
			paintedChipsCount++
		}
	}
}

const paintPlayersBoxesExcluding = (canvas: HTMLCanvasElement, excludedSeatNumber: number, table: HoldemTable) => {
	const ctx = canvas.getContext("2d")
	if (!ctx) return
	let next = useNextSeatNumberCalculator(excludedSeatNumber, table)
	for (let i = 0; i < table.seats.length; i++) {
		const seat = table.seats.find(seat => seat.number === next) as Seat<HoldemPlayer>
		if (excludedSeatNumber !== seat.number) {
			const startingCorner = usePlayerBoxStartingCornerCalculator(i, playerBoxWidth, playerBoxHeight, table.seats.length - 1)
			paintPlayerBox(canvas, startingCorner, seat, useActivePlayerChecker(table, seat.player))
		}
		next = useNextSeatNumberCalculator(next, table)
	}
}
function paintBackgroundImage(canvas: HTMLCanvasElement) {
	useImagePainter(canvas, bgImage, { x: 0, y: 0 }, { x: canvas.width, y: canvas.height })
}
function paintPots(canvas: HTMLCanvasElement, table: HoldemTable) {
	if (!table.pots) return

	for (const pot of table.pots) {
		const text = potText + pot.amountWithTableChips.toFixed(2)
		if (table.gamePhase !== GamePhase.PRE_FLOP) {
			const chipStack = useChipsTransformer(pot.amountWithTableChips)
			paintChips(canvas, potChipsStartPoint, potChipsEndPoint, chipStack)
		}
		useTextPainter(canvas, useVectorYComponentAdder(potChipsStartPoint, -20), text, blueFont)
	}
}
function paintTableCards(canvas: HTMLCanvasElement, table: HoldemTable) {
	if (!table.tableCards || table.tableCards.length === 0) return
	const tableCardsStart: Vector = { x: playerBoxWidth * 1.15, y: canvas.height / 2 } //1.15 is just a number to position tableCards in the middle area
	for (let i = 0; i < table.tableCards.length; i++) {
		useCardPainter(canvas, table.tableCards[i], useVectorXComponentAdder(tableCardsStart, i * cardSize.x), cardSize)
	}
}
