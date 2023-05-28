export interface CasinoFont {
	faceAndSize: string
	color: string
}
export interface CasinoColor {
	alpha: number
	color: string
}
export const blueFont: CasinoFont = {
	color: "blue",
	faceAndSize: "20px Arial"
}
export const blackFont: CasinoFont = {
	color: "E97451",
	faceAndSize: "22px Arial"
}
export const orangeFont: CasinoFont = {
	color: "#E97451",
	faceAndSize: "20px Arial"
}

export const orangeLargeFont: CasinoFont = {
	color: "#E97451",
	faceAndSize: "30px Arial"
}
export const smallBoxFont: CasinoFont = {
	color: "#E97451",
	faceAndSize: "15px Arial"
}

export const grayColorAlpha: CasinoColor = {
	alpha: 0.2,
	color: "bfb7b5"
}

export const grayColorAlpha6: CasinoColor = {
	alpha: 0.6,
	color: "bfb7b5"
}
