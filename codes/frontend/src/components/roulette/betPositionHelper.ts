import type { RouletteBetPosition } from "@/types/roulette"
import { useVectorXComponentAdder, type DoubleVector, type Vector, useVectorYComponentAdder, useVectorAdder } from "@/types/vectors"
import { isPositionInBetweenVectorPointsInclusive } from "../composables/common/math"
let gridWidth //numbers 1-36 plus last row
let numberSquareWidth
let numberSquareHeight
let numberSquareBetPositionHeight
let numberSquareBetPositionWidth
let smallSquareWidth
let smallSquareHeight
let gapHorizontalFromCanvasLeftToFirstRow

let verticalGapFromCanvasTopToFirstRow
let gapBetweenBetSquares = 7
let verticalGapBetweenBetSquares = 14

let betGridStartPosition = { x: 96, y: 20 } // TODO dynamic
let betGridEndPosition = { x: 730, y: 434 } // TODO dynamic
let sideBetsStartPosition = { x: betGridStartPosition.x, y: 264 }
let sideBetsEndPosition = { x: 730, y: 434 } // TODO dynamic
let redColorBetPositionWidth
let redColorBetPositionHeight
let gapHorizontalFromdatacanvasLeftToFirstRow
let verticalGapFromdatacanvasTopToFirstRow
let betPositions: RouletteBetPosition[]

export const useBetPositionLocator = (canvas: HTMLCanvasElement, betPositions_: RouletteBetPosition[]) => {
	const ctx = canvas.getContext("2d")
	if (ctx === null) throw new Error("no data.canvas ctx")
	gridWidth = canvas.width * 0.837
	gapHorizontalFromdatacanvasLeftToFirstRow = canvas.width * 0.12
	verticalGapFromdatacanvasTopToFirstRow = canvas.height * 0.034

	gapHorizontalFromCanvasLeftToFirstRow = canvas.width * 0.12
	verticalGapFromCanvasTopToFirstRow = canvas.height * 0.034
	numberSquareWidth = gridWidth / 13
	numberSquareHeight = numberSquareWidth * 1.5
	numberSquareBetPositionHeight = 0.9 * numberSquareHeight
	numberSquareBetPositionWidth = 0.9 * numberSquareWidth
	smallSquareWidth = numberSquareBetPositionHeight / 3
	smallSquareHeight = numberSquareBetPositionWidth / 3
	redColorBetPositionWidth = (betGridEndPosition.x - betGridStartPosition.x) / 6
	redColorBetPositionHeight = redColorBetPositionWidth * 0.75
	betPositions = betPositions_
	gapHorizontalFromCanvasLeftToFirstRow
	calculatePositionsFromZeroTo36()
	calculatePositionsFrom37To144()
	calculatePositionsFrom145To147()
	calculatePositions200To208()
}
export const useBetPositionFinder = (betPositions: RouletteBetPosition[], event: MouseEvent): RouletteBetPosition | undefined => {
	const pointerCoords = { x: event.offsetX, y: event.offsetY }
	let square
	if (isPointerOnSideBets(pointerCoords)) {
		if (isPointerOnDozenBets(pointerCoords)) {
			let squares = betPositions.filter(position => position.number >= 200 && position.number <= 203)
			for (let i = 0; i < 3; i++) {
				if (pointerCoords.x >= squares[i].selectSection.startCorner.x) {
					square = squares[i]
				}
			}
			return square
		} else if (isPointerOnLastRowOfSideBets(pointerCoords)) {
			let squares = betPositions.filter(position => position.number >= 203 && position.number <= 208)
			for (let i = 0; i < 6; i++) {
				if (pointerCoords.x >= squares[i].selectSection.startCorner.x) {
					square = squares[i]
				}
			}
			return square
		}
	} else if (isPointerOnMainBets(pointerCoords)) {
		// numbers1-36 and bets in between

		const singleNumberSearchArea = { x: numberSquareBetPositionWidth, y: numberSquareBetPositionHeight }
		const gapBetweenNumberArea = { x: smallSquareWidth, y: smallSquareHeight }

		square = betPositions.find(betSquare => isPositionInBetweenVectorPointsInclusive(pointerCoords, betSquare.selectSection.startCorner, betSquare.selectSection.endCorner))
		return square
	}
}
const isPointerOnDozenBets = (pointerPosition: Vector) => {
	const endY = (sideBetsEndPosition.y - sideBetsStartPosition.y) / 2 + sideBetsStartPosition.y

	return pointerPosition.x >= sideBetsStartPosition.x && pointerPosition.y >= sideBetsStartPosition.y && pointerPosition.x < sideBetsEndPosition.x && pointerPosition.y < endY
}
const isPointerOnLastRowOfSideBets = (pointerPosition: Vector) => {
	return isPointerOnSideBets(pointerPosition) && !isPointerOnDozenBets(pointerPosition)
}
const isPointerOnSideBets = (pointerPosition: Vector) => {
	return pointerPosition.x >= sideBetsStartPosition.x && pointerPosition.y >= sideBetsStartPosition.y && pointerPosition.x < sideBetsEndPosition.x && pointerPosition.y < sideBetsEndPosition.y
}

const isPointerOnMainBets = (pointerPosition: Vector) => {
	const zeroBetPosition = betPositions[0].selectSection

	if (isPositionInBetweenVectorPointsInclusive(pointerPosition, zeroBetPosition.startCorner, zeroBetPosition.endCorner)) {
		return true
	}
	return pointerPosition.x >= sideBetsStartPosition.x && pointerPosition.y < sideBetsStartPosition.y
}
function calculatePositionsFrom37To144() {
	const refNumber = betPositions.find(pos => pos.number === 1) as RouletteBetPosition

	let refPosition = useVectorYComponentAdder(refNumber.selectSection.startCorner, numberSquareHeight)
	let positionNumber = 36
	for (let x = 0; x < 24; x++) {
		if (x % 2 == 0) {
			let positionX = refPosition.x + (x * numberSquareWidth) / 2 + x * gapBetweenBetSquares - smallSquareWidth / 2 - x * 5
			if (x > 3) positionX = positionX - 3
			if (x > 6) positionX = positionX - 6
			for (let y = 0; y < 6; y++) {
				positionNumber++
				const positionY = refPosition.y - (y * numberSquareHeight) / 2 - smallSquareHeight / 2 - (y > 2 ? 9 : 0)
				const pos = betPositions.find(position => position.number === positionNumber) as RouletteBetPosition

				pos.selectSection = {
					startCorner: { x: positionX, y: positionY },
					endCorner: { x: positionX + 15, y: positionY + 15 }
				} as DoubleVector
				pos.highlightSection = pos.selectSection
			}
		} else {
			let positionX = refPosition.x + (x * numberSquareWidth) / 2 + x * gapBetweenBetSquares - smallSquareWidth / 2 - x * 5
			for (let y = 0; y < 3; y++) {
				positionNumber++
				const positionY = refPosition.y - y * numberSquareHeight - smallSquareHeight / 2 - (y > 1 ? 9 : 0)
				const pos = betPositions.find(position => position.number === positionNumber) as RouletteBetPosition

				pos.selectSection = {
					startCorner: { x: positionX, y: positionY },
					endCorner: { x: positionX + 15, y: positionY + 15 }
				} as DoubleVector
				pos.highlightSection = pos.selectSection
			}
		}
	}
}
function calculatePositionsFromZeroTo36() {
	let positionNumber = 0
	for (let x = 0; x < 12; x++) {
		const selectionTopLeftCornerX = x * (numberSquareBetPositionWidth + gapBetweenBetSquares) + gapHorizontalFromdatacanvasLeftToFirstRow
		for (let y = 3; y > 0; y--) {
			positionNumber++
			const selectionTopLeftLeftCornerY = (y - 1) * (numberSquareBetPositionHeight + verticalGapBetweenBetSquares) + verticalGapFromdatacanvasTopToFirstRow
			const selectionTopLeftCorner = { x: selectionTopLeftCornerX, y: selectionTopLeftLeftCornerY } as Vector
			const pos = betPositions.find(position => position.number === positionNumber) as RouletteBetPosition
			pos.selectSection = {
				startCorner: { x: selectionTopLeftCorner.x, y: selectionTopLeftCorner.y },
				endCorner: { x: selectionTopLeftCorner.x + numberSquareBetPositionWidth, y: selectionTopLeftCorner.y + numberSquareBetPositionHeight }
			} as DoubleVector
			pos.highlightSection = pos.selectSection
		}
	}
	const zeroPosition = betPositions.find(position => position.number === 0) as RouletteBetPosition
	zeroPosition.selectSection = {
		startCorner: { x: 30, y: 20 },
		endCorner: { x: 90, y: 260 }
	}
	zeroPosition.highlightSection = zeroPosition.selectSection
}

function calculatePositionsFrom145To147() {
	const position34 = betPositions.find(position => position.number === 34)
	const position145 = betPositions.find(position => position.number === 145) as RouletteBetPosition
	position145.selectSection = {
		startCorner: { x: position34?.selectSection.startCorner.x + numberSquareWidth, y: position34!.selectSection.startCorner.y },
		endCorner: { x: position34?.selectSection.endCorner.x + numberSquareWidth, y: position34!.selectSection.endCorner.y }
	}
	position145.highlightSection = position145.selectSection
	const position146 = betPositions.find(position => position.number === 146) as RouletteBetPosition
	position146.selectSection = {
		startCorner: { x: position145.selectSection.startCorner.x, y: position145.selectSection.startCorner.y - numberSquareHeight },
		endCorner: { x: position145.selectSection.endCorner.x, y: position145.selectSection.endCorner.y - numberSquareHeight }
	}
	position146.highlightSection = position146.selectSection
	const position147 = betPositions.find(position => position.number === 147) as RouletteBetPosition
	position147.selectSection = {
		startCorner: { x: position146.selectSection.startCorner.x, y: position146.selectSection.startCorner.y - numberSquareHeight },
		endCorner: { x: position146.selectSection.endCorner.x, y: position146.selectSection.endCorner.y - numberSquareHeight }
	}
	position147.highlightSection = position147.selectSection
}
const calculatePositions200To208 = () => {
	const positionOne = betPositions.find(pos => pos.number === 1) as RouletteBetPosition
	const position200 = betPositions.find(pos => pos.number === 200) as RouletteBetPosition
	position200.selectSection = {
		startCorner: useVectorYComponentAdder(positionOne.selectSection.startCorner, redColorBetPositionHeight + 5),
		endCorner: useVectorAdder(positionOne.selectSection.startCorner, 2 * redColorBetPositionWidth, 2 * redColorBetPositionHeight)
	} as DoubleVector
	position200.highlightSection = position200.selectSection

	let positionNumber = 200
	for (let i = 0; i < 3; i++) {
		const pos = betPositions.find(position => position.number === positionNumber) as RouletteBetPosition
		pos.selectSection = {
			startCorner: useVectorXComponentAdder(position200.selectSection.startCorner, 2 * i * redColorBetPositionWidth),
			endCorner: useVectorXComponentAdder(position200.selectSection.endCorner, 2 * redColorBetPositionWidth * i)
		} as DoubleVector

		pos.highlightSection = pos.selectSection
		positionNumber++
	}
	positionNumber = 203
	const position203 = betPositions.find(position => position.number === 203) as RouletteBetPosition
	const added = useVectorYComponentAdder(position200.selectSection.startCorner, redColorBetPositionHeight)
	position203.selectSection = {
		startCorner: useVectorYComponentAdder(position200.selectSection.startCorner, redColorBetPositionHeight + 5),
		endCorner: useVectorAdder(position200.selectSection.startCorner, redColorBetPositionWidth, 2 * redColorBetPositionHeight)
	} as DoubleVector
	for (let i = 0; i < 6; i++) {
		const pos = betPositions.find(position => position.number === positionNumber) as RouletteBetPosition
		pos.selectSection = {
			startCorner: useVectorXComponentAdder(position203.selectSection.startCorner, i * redColorBetPositionWidth),
			endCorner: useVectorXComponentAdder(position203.selectSection.endCorner, i * redColorBetPositionWidth)
		} as DoubleVector
		pos.highlightSection = pos.selectSection
		positionNumber++
	}
}
