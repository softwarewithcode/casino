import { useVectorAdder, useVectorXComponentAdder, useVectorYComponentAdder, type DoubleVector, type Vector, useVectorSubtractor } from "../../types/vectors"
import { RouletteGamePhase, type RoulettePaintData } from "@/types/roulette"
import { rouletteBetsBoard } from "../../types/images"
import { useCanvasClearer, useCirclePainter, useImagePainter, useRectanglePainter, useRectangleTransparentFiller, useTextPainter } from "../composables/rendering/commonPainter"
import { blueFont, blackSmallFont, whiteSmallFont, whiteColorAlpha5, yellowColor, blackSmallBoldFont } from "@/types/fontsAndColors"
import { MATH_PI_X_2, zeroOrValue } from "../composables/common/math"
import { chipAny, chipWhite } from "@/types/chips"
import { useBetPositionLocator } from "./betPositionHelper"

let data: RoulettePaintData
const chipSizeInSelection = { x: 32, y: 32 } as Vector
const chipSizeOnTableNumber = { x: 38, y: 38 } as Vector
const shouldCalculateItemsPositions = () => data.recalculate
export function useRouletteTablePainter(paintData: RoulettePaintData) {
	if (paintData.canvas.getContext("2d") === null) throw new Error("no canvas to paint")
	data = paintData
	if (shouldCalculateItemsPositions()) {
		useBetPositionLocator(paintData.canvas, paintData.betPositions)
	}
	useCanvasClearer(paintData.canvas)
	paintBackgroundImage()
	paintOtherPlayersBets()
	paintHeroData()
	highlightSelections()
	highlightWinningNumber()
	mimicRouletteWheelSpin()
}

const paintHeroData = () => {
	if (!data.hero || !data.hero.chips) return
	paintChipSelection()
	paintSelectedChip()
	paintBalanceAndBets()
	paintAcceptedBets()
	paintInfoText()
	paintPositionsTotalAmounts()
}
const paintPositionsTotalAmounts = () => {
	if (data.hero?.positionsTotalAmounts) {
		for (const [key, value] of Object.entries(data.hero.positionsTotalAmounts)) {
			const betPosition = data.betPositions.find(position => position.number === Number(key))
			const textPosition = useVectorAdder(betPosition!.highlightSection.startCorner, chipSizeOnTableNumber.x / 6, 25)
			useTextPainter(data.canvas, textPosition, value.toString(), blackSmallBoldFont)
		}
	}
}
const paintInfoText = () => {
	if (data.table.gamePhase === RouletteGamePhase.BET) {
		useTextPainter(data.canvas, { x: data.canvas.width / 2, y: data.canvas.height * 0.85 }, "Place bets", blueFont)
	}
}

const mimicRouletteWheelSpin = () => {
	if (data.randomNumber === null) {
		return
	}
	const randomNumberPosition = data.betPositions.find(betPosition => betPosition.number === data.randomNumber)
	if (!randomNumberPosition) return
	const highlightSectionSize = useVectorSubtractor(randomNumberPosition.selectSection.endCorner, randomNumberPosition.selectSection.startCorner)
	useRectanglePainter(data.canvas, randomNumberPosition.selectSection.startCorner, highlightSectionSize, true)
}

const highlightSelections = () => {
	if (data.table.gamePhase !== RouletteGamePhase.BET) return
	if (!data.hero.selectedChip || !data.hero.selectedChip.position) return
	const radiobuttonSelection = data.highlightEffectiveNumbers
	if (radiobuttonSelection) highlightEffectiveNumbers()
	else highlightBetPosition()
}
const paintAcceptedBets = () => {
	if (!data.hero || !data.hero.bets) return
	data.hero.bets.forEach((bet, index) => {
		const positionStartCorner = data.betPositions.find(position => position.number === bet.position)?.selectSection.startCorner as Vector
		let chipStartCorner = useVectorXComponentAdder(positionStartCorner, index * 3)
		if (positionStartCorner.x < chipStartCorner.x + chipSizeOnTableNumber.x) {
			chipStartCorner = positionStartCorner
		}
		useImagePainter(data.canvas, chipWhite.image, chipStartCorner, chipSizeOnTableNumber)
	})
}
const paintBalanceAndBets = () => {
	const startPos = { x: 2, y: data.canvas.height / 2 }
	useTextPainter(data.canvas, startPos, "balance ", whiteSmallFont)
	useTextPainter(data.canvas, useVectorYComponentAdder(startPos, 25), zeroOrValue(data.hero.currentBalance).toString(), whiteSmallFont)
	useTextPainter(data.canvas, useVectorYComponentAdder(startPos, 50), "on bets ", whiteSmallFont)
	useTextPainter(data.canvas, useVectorYComponentAdder(startPos, 75), zeroOrValue(data.hero.totalBet).toString(), whiteSmallFont)
	useTextPainter(data.canvas, useVectorYComponentAdder(startPos, 100), "on table ", whiteSmallFont)
	useTextPainter(data.canvas, useVectorYComponentAdder(startPos, 125), zeroOrValue(data.hero.totalOnTable).toString(), whiteSmallFont)
}
const paintChipSelection = () => {
	if (!data.hero.chips || data.table.gamePhase !== RouletteGamePhase.BET) return
	let index = 0
	data.hero.chips.forEach(chip => {
		const chipPosition = { x: data.canvas.width - 1.5 * chipSizeInSelection.x, y: data.canvas.height * 0.7 - chipSizeInSelection.y * index }
		chip.position = chipPosition
		chip.size = chipSizeInSelection
		useImagePainter(data.canvas, chip.image, chipPosition, chipSizeInSelection)
		useTextPainter(data.canvas, useVectorAdder(chipPosition, 5, 23), chip.value.toString(), blackSmallFont)
		index++
	})
}

const paintOtherPlayersBets = () => {
	const otherPlayers = data.table.seats.filter(seat => !seat.available && seat.player.userName !== data.hero?.userName).map(seat => seat.player)
	otherPlayers.forEach(other => {
		other.bets.forEach(bet => {
			const startCorner = data.betPositions.find(position => position.number === bet.position)?.selectSection.startCorner as Vector
			useImagePainter(data.canvas, chipAny.image, startCorner, useVectorAdder(chipSizeOnTableNumber, -chipSizeOnTableNumber.x / 8, -chipSizeOnTableNumber.y / 8))
		})
	})
}
const paintSelectedChip = () => {
	if (!data.hero.selectedChip || !data.hero.selectedChip.position) return
	useImagePainter(data.canvas, data.hero.selectedChip.image, data.hero.selectedChip.position, chipSizeOnTableNumber)
}

const highlightEffectiveNumbers = () => {
	if (!data.selectedBetPosition) {
		return
	}
	data.selectedBetPosition.tableNumbers.forEach(tableNumber => {
		const fillBetPosition = data.betPositions.find(betPosition => betPosition.number === tableNumber)
		const widthAndHeight = useVectorSubtractor(fillBetPosition!.selectSection.endCorner, fillBetPosition!.selectSection.startCorner)
		useRectangleTransparentFiller(data.canvas, fillBetPosition!.selectSection.startCorner, widthAndHeight, whiteColorAlpha5)
	})
}
const highlightBetPosition = () => {
	if (!data.selectedBetPosition) {
		return
	}
	const highlightSectionSize = useVectorSubtractor(data.selectedBetPosition.highlightSection.endCorner, data.selectedBetPosition.highlightSection.startCorner)
	useRectanglePainter(data.canvas, data.selectedBetPosition.highlightSection.startCorner, highlightSectionSize, true)
}

const highlightWinningNumber = () => {
	const number = data.highlightNumber
	if (number && number >= 0 && number <= 36) {
		const winningNumberBetPosition = data.betPositions.find(betPosition => betPosition.number === data.highlightNumber)
		const numberSquareWidthAndHeight = useVectorSubtractor(winningNumberBetPosition!.selectSection.endCorner, winningNumberBetPosition!.selectSection.startCorner)
		const circleCenterPoint = { x: winningNumberBetPosition!.selectSection.startCorner.x + numberSquareWidthAndHeight.x / 2, y: winningNumberBetPosition!.selectSection.startCorner.y + numberSquareWidthAndHeight.y / 2 }
		useCirclePainter(data.canvas, circleCenterPoint, 33, { x: 0, y: MATH_PI_X_2 }, yellowColor)
	} else if (number === 0) {
		//hard coded circle position for zero
		useCirclePainter(data.canvas, { x: 56, y: 140 }, 37, { x: 0, y: MATH_PI_X_2 }, yellowColor)
	}
}
const paintBackgroundImage = () => {
	useImagePainter(data.canvas, rouletteBetsBoard, { x: 0, y: 0 }, { x: data.canvas.width, y: data.canvas.height * 0.75 })
}
