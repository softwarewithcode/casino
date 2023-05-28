export interface Vector {
	x: number
	y: number
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
