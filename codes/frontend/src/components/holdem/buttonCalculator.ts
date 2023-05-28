import type { Vector } from "../../types/vectors"
import type { HoldemPlayer, HoldemTable } from "@/types/texasHoldem"

let buttonSize
export const useButtonPositionCalculator = (canvas: HTMLCanvasElement, playerBoxSize: Vector, buttonBoxStartingCorner: Vector, buttonSize: number, mainPlayerHoldsButton: boolean) => {
	buttonSize = buttonSize
	const xStrategy: Function = buttonXPositionStrategy(buttonBoxStartingCorner, playerBoxSize.x)
	const yStrategy: Function = buttonYPositionStrategy(canvas, buttonBoxStartingCorner, mainPlayerHoldsButton)

	return { x: xStrategy(buttonBoxStartingCorner, playerBoxSize.x), y: yStrategy(buttonBoxStartingCorner, playerBoxSize.y) }
}

const buttonXPositionStrategy = (boxStartingCorner: Vector, elementWidth: number) => (boxStartingCorner.x <= elementWidth / 2 ? buttonXPositionStrategyOnLeftSideOfCanvas : buttonXPositionStrategyOnRightSideOfCanvas)

const buttonYPositionStrategy = (canvas: HTMLCanvasElement, boxStartingCorner: Vector, mainPlayerHoldsButton: boolean) => {
	if (mainPlayerHoldsButton) return buttonYPositionStrategyForMainPlayer
	return boxStartingCorner.y <= canvas.height / 2 ? buttonYPositionStrategyOnUpperPartOfCanvas : buttonYPositionStrategyOnLowerPartOfCanvas
}

const buttonXPositionStrategyOnLeftSideOfCanvas = (boxStartingCorner: Vector, elementWidth: number) => boxStartingCorner.x + elementWidth - buttonSize

const buttonXPositionStrategyOnRightSideOfCanvas = (boxStartingCorner: Vector, elementWidth: number) => boxStartingCorner.x

const buttonYPositionStrategyOnUpperPartOfCanvas = (boxStartingCorner: Vector, elementHeight: number): number => boxStartingCorner.y + elementHeight - buttonSize
const buttonYPositionStrategyOnLowerPartOfCanvas = (boxStartingCorner: Vector, elementHeight: number): number => boxStartingCorner.y + buttonSize
const buttonYPositionStrategyForMainPlayer = (boxStartingCorner: Vector, elementHeight: number): number => boxStartingCorner.y - buttonSize

const seatToIndexMapper = (table: HoldemTable, seat: number) => {}
