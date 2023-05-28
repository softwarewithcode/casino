import type { Vector } from "../../../types/vectors"
import type { Card } from "@/types/cards"
import type { CasinoColor, CasinoFont } from "@/types/fontsAndColors"
import { cardBackSideImage, cardsSprite } from "@/types/images"
import { useCardLocator } from "./cardLocator"
export const useDefaultCardWidthRatio: number = 0.72

export const useCanvasClearer = (canvas: HTMLCanvasElement): HTMLCanvasElement => {
	const ctx = canvas.getContext("2d")
	ctx?.clearRect(0, 0, canvas.width, canvas.height)
	return canvas
}

export function useCanvasInitializer(canvas: HTMLCanvasElement) {
	const documentWidth = document.documentElement.clientWidth
	canvas.width = documentWidth > 800 ? 800 : documentWidth
	canvas.height = document.documentElement.clientHeight > 800 ? 800 : document.documentElement.clientHeight
}

export const useRectanglePainter = (canvas: HTMLCanvasElement, startPosition: Vector, widthAndHeight: Vector, highlight: boolean, color?: CasinoColor) => {
	const ctx = canvas.getContext("2d")
	if (!ctx) return
	const originalStyle = ctx.strokeStyle
	const originalWidth = ctx.lineWidth
	const originalAlpha = ctx.globalAlpha
	const originalFill = ctx.fillStyle
	if (highlight) {
		ctx.strokeStyle = "green"
		ctx.lineWidth = 10
	}
	if (color) {
		ctx.globalAlpha = color.alpha
		ctx.fillStyle = color.color
		ctx.fillRect(startPosition.x, startPosition.y, widthAndHeight.x, widthAndHeight.y)
	}
	ctx.strokeRect(startPosition.x, startPosition.y, widthAndHeight.x, widthAndHeight.y)
	ctx.strokeStyle = originalStyle
	ctx.lineWidth = originalWidth
	ctx.globalAlpha = originalAlpha
	ctx.fillStyle = originalFill
}

export const useTextPainter = (canvas: HTMLCanvasElement, startPosition: Vector, text: string, font: CasinoFont) => {
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

export const useCardBackSidePainter = (canvas: HTMLCanvasElement, startPosition: Vector, size: Vector) => {
	useImagePainter(canvas, cardBackSideImage, startPosition, size)
}

export const useImagePainter = (canvas: HTMLCanvasElement, element: HTMLImageElement, startPosition: Vector, size: Vector) => {
	const ctx = canvas.getContext("2d")
	if (!ctx) return
	if (!size || !size.x) {
		ctx.drawImage(element, startPosition.x, startPosition.y, (canvas.width / 10) * useDefaultCardWidthRatio, canvas.height / 10)
	} else {
		ctx.drawImage(element, startPosition.x, startPosition.y, size.x, size.y)
	}
}

export const useImagePainter2 = (canvas: HTMLCanvasElement, image: HTMLImageElement, clipStartPositionOnImage: Vector, clipEndPositionOnImage: Vector, clippedImageStartPositionOnCanvas: Vector, clippedImageSizeOnCanvas: Vector) => {
	const ctx = canvas.getContext("2d")
	if (!ctx) return

	ctx.drawImage(
		image,
		clipStartPositionOnImage.x,
		clipStartPositionOnImage.y,
		clipEndPositionOnImage.x,
		clipEndPositionOnImage.y,
		clippedImageStartPositionOnCanvas.x,
		clippedImageStartPositionOnCanvas.y,
		clippedImageSizeOnCanvas.x,
		clippedImageSizeOnCanvas.y
	)
}

export const useCardPainter = (canvas: HTMLCanvasElement, card: Card, startPosition: Vector, size: Vector) => {
	const ctx = canvas.getContext("2d")
	if (!card || !ctx) {
		return
	}
	const cardData = useCardLocator(card)
	if (!size) {
		ctx.drawImage(cardsSprite, cardData.position.x, cardData.position.y, cardData.size.x, cardData.size.y, startPosition.x, startPosition.y, (canvas.width / 10) * useDefaultCardWidthRatio, canvas.height / 10)
	} else {
		ctx.drawImage(cardsSprite, cardData.position.x, cardData.position.y, cardData.size.x, cardData.size.y, startPosition.x, startPosition.y, size.x, size.y)
	}
}
export const useWait = time => new Promise(r => setTimeout(r, time)) // artificial wait

export const usePlayerBoxStartingCornerCalculator = (index: number, boxWidth: number, boxHeight: number, mainBoxIndex: number): Vector => {
	let corner: Vector = {} as Vector

	if (index === mainBoxIndex) {
		corner = { x: 0, y: boxHeight * 3 }
		return corner
	} //is not dynamic based on seatCount. Will not work if more than 6 seats.. TODO
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
	} else if (index === mainBoxIndex) {
		corner = { x: 0, y: boxHeight * 3 }
	}
	return corner
}
