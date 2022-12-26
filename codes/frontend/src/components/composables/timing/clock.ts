import { useTableStore } from "../../../stores/tableStore";

const store = useTableStore();
let counterId: number;
export function useCounter() {
  startCounter();
}

function startCounter() {
    console.log("startCounter")
  if (counterId) 
    clearInterval(counterId);
  counterId = setInterval(() => {
    store.reduceCounter()
    let left = store.getCounter;
    if (left <= 0) 
        clearInterval(counterId);
  }, 1000);
}
