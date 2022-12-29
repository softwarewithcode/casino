import { useTableStore } from "../../../stores/tableStore"

let counterId: number
let store
export function useStartCounter() {
	store = useTableStore()
	startCounter()
}

function startCounter() {
	console.log("startCounter")
	if (counterId) {
		clearInterval(counterId)
	}
	counterId = setInterval(() => {
		console.log("storeLeft:")
		store.reduceCounter()
		let left = store.getCounter
		if (left <= 0) {
			clearInterval(counterId)
		}
	}, 1000)
}
