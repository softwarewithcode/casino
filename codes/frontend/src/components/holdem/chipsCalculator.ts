import type { Chip, ChipStack } from "@/types/casino"
import { chip100F, chip10F, chip10kF, chip1F, chip1kF, chip5F } from "../../types/chips"

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
