import { useVectorXComponentAdder, type Vector } from "@/types/vectors"
export const MATH_PI_X_2 = 2 * Math.PI
//negative numbers!=
export const isBiggerOrEqualValue = (actualValue: number, referenceValue: number) => {
	verifyValues(actualValue, referenceValue)
	return actualValue >= referenceValue
}
//negative numbers!=
export const isSmallerOrEqualValue = (actualValue: number, referenceValue: number) => {
	verifyValues(actualValue, referenceValue)
	return actualValue <= referenceValue
}
const verifyValues = (actualValue: number, referenceValue: number) => {
	if (actualValue < 0 || referenceValue < 0) throw new Error("value is missing=" + actualValue + " : " + referenceValue)
}

export const isPositionInBetweenVectorPointsInclusive = (position: Vector, areaStartPosition: Vector, areaEndPosition: Vector) => {
	try {
		if (!isBiggerOrEqualValue(position.x, areaStartPosition.x)) return false
		if (!isSmallerOrEqualValue(position.x, areaEndPosition.x)) return false
		if (!isBiggerOrEqualValue(position.y, areaStartPosition.y)) return false
		if (!isSmallerOrEqualValue(position.y, areaEndPosition.y)) return false
	} catch (e) {
		console.error("error " + e)
		return false
	}
	return true
}
export const zeroOrValue = (value: number) => {
	if (!value) return 0
	return value
}
