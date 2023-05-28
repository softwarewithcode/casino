import { useCasinoStore } from "../../../stores/casinoStore"

let counterId: number = -1
let store
export function useStartCounter() {
	store = useCasinoStore()
	startCounter()
}
export function useCounterRunningChecker() {
	return counterId !== -1
}

export function useStopCounter() {
	if (counterId) {
		clearInterval(counterId)
		counterId = -1
	}
}
function startCounter() {
	useStopCounter()
	counterId = setInterval(() => {
		store.reduceCounter()
		let left = store.getCounter
		if (left <= 0) {
			useStopCounter()
		}
	}, 1000)
}
