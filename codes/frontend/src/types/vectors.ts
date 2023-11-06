export interface Vector {
	x: number
	y: number
}
export interface DoubleVector {
	startCorner: Vector
	endCorner: Vector
}

export interface KeyValue {
	[key: number]: number
}
//Negative numbers allowed in all adders
export const useVectorYComponentAdder = (vector: Vector, addAmount: number): Vector => {
	const totalY = vector.y + addAmount
	return { x: vector.x, y: totalY }
}
export const useVectorXComponentAdder = (vector: Vector, addAmount: number): Vector => {
	const totalX = vector.x + addAmount
	return { x: totalX, y: vector.y }
}

export const useVectorAdder = (vector: Vector, addX: number, addY: number): Vector => {
	const totalX = vector.x + addX
	const totalY = vector.y + addY
	return { x: totalX, y: totalY }
}

export const useVectorSubtractor = (from: Vector, operand: Vector) => {
	const totalX = from.x - operand.x
	const totalY = from.y - operand.y
	return { x: totalX, y: totalY }
}
