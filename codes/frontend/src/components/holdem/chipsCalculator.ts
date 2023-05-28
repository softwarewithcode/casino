import type { Chip, ChipStack } from "@/types/casino"
import { chip100Image, chip10Image, chip10kImage, chip1Image, chip1kImage, chip5Image } from "@/types/images"

const chip10kF = { value: 10000, image: chip10kImage }
const chip1kF = { value: 1000, image: chip1kImage }
const chip100F = { value: 100, image: chip100Image }
const chip10F = { value: 10, image: chip10Image }
const chip5F = { value: 5, image: chip5Image }
const chip1F = { value: 1, image: chip1Image }
export const useChipsTransformer = (amount: number): ChipStack => {
	if (amount === 0) return {} as ChipStack
	//debugger
	let temp = amount
	const chips10k = Math.floor(temp / 10000)
	if (chips10k >= 1) temp -= chips10k * 10000
	const chips1k = Math.floor(temp / 1000)
	if (chips1k >= 1) temp -= chips1k * 1000
	const chips100 = Math.floor(temp / 100)
	if (chips100 >= 1) temp -= chips100 * 100
	const chips10 = Math.floor(temp / 10)
	if (chips10 >= 1) temp -= chips10 * 10
	const chips5 = Math.floor(temp / 5)
	if (chips5 >= 1) temp -= chips5 * 5
	const chips1 = Math.floor(temp / 1)
	if (chips1 >= 1) temp -= chips1
	let chipsAnyValue = 0

	const chipStack = new Map<Chip, number>()
	chipStack.set(chip10kF, chips10k)
	chipStack.set(chip1kF, chips1k)
	chipStack.set(chip100F, chips100)
	chipStack.set(chip10F, chips10)
	chipStack.set(chip5F, chips5)
	chipStack.set(chip1F, chips1)
	return { chips: chipStack }
}
