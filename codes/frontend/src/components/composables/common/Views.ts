export enum ViewName {
	BLACKJACK_FRONT = "blackjackFront",
	BLACKJACK_TABLE = "blackjack",
	TEXAS_HOLDEM_FRONT = "texasHoldemFront",
	TEXAS_HOLDEM_TABLE = "texasHoldem",
	ROULETTE_FRONT = "rouletteFront",
	ROULETTE_TABLE = "rouletteTable",
	ERROR = "error"
}

export function useViewIdToViewNameMapper(gameId: number): string {
	switch (gameId) {
		case 0:
			return ViewName.BLACKJACK_FRONT
		case 1:
			return ViewName.TEXAS_HOLDEM_FRONT
		case 2:
			return ViewName.ROULETTE_FRONT
		default:
			return ViewName.ERROR
	}
}
