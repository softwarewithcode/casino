import type { Chip } from "@/types/casino"
import { chip100Image, chip10Image, chip10kImage, chip1Image, chip1kImage, chip5Image, chipAnyImage, chipDealerImage, chipWhiteImage } from "@/types/images"
export const chip10kF = { value: 10000, image: chip10kImage } as Chip
export const chip1kF = { value: 1000, image: chip1kImage } as Chip
export const chip100F = { value: 100, image: chip100Image } as Chip
export const chip10F = { value: 10, image: chip10Image } as Chip
export const chip5F = { value: 5, image: chip5Image } as Chip
export const chip1F = { value: 1, image: chip1Image } as Chip
export const chipAny = { value: 0, image: chipAnyImage } as Chip
export const chipDealer = { value: -1, image: chipDealerImage } as Chip
export const chipWhite = { value: 0, image: chipWhiteImage } as Chip
export const allChips = [chip1F, chip5F, chip10F, chip100F, , chip1kF, chip10kF, chipDealer, chipAny] as Array<Chip>
export const useChipFinder = (chipValue: number) => {
	switch (chipValue) {
		case 1:
			return chip1F
		case 5:
			return chip5F
		case 10:
			return chip10F

		case 100:
			return chip100F

		case 1000:
			return chip1kF

		case 10000:
			return chip10kF
		default:
			throw new Error("no chip " + chipValue)
	}
}
