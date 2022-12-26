let counterId: number;
let time: number;

export function useCounter(seconds: number) {
  time = seconds;
  startCounter();
}

function startCounter() {
  if (counterId) 
    clearInterval(counterId);
  counterId = setInterval(() => {
    time--;
    if (time <= 0) 
        clearInterval(counterId);
  }, 1000);
}
