import { useBlackjackStore } from "../../../stores/blackjackStore"

let counterId: number
let store
export function useStartCounter() {
	store = useBlackjackStore()
	startCounter()
}

function startCounter() {
	if (counterId) {
		clearInterval(counterId)
	}
	counterId = setInterval(() => {
		store.reduceCounter()
		let left = store.getCounter
		if (left <= 0) {
			clearInterval(counterId)
		}
	}, 1000)
}
