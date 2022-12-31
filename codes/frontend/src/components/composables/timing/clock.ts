import { useTableStore } from "../../../stores/tableStore"

let counterId: number
let store
export function useStartCounter() {
	store = useTableStore()
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
