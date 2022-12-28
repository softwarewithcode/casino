import type { CasinoTable, CasinoPlayer, CasinoHand, Card } from "./casino"

export interface BlackjackTable extends CasinoTable {
	seats: Seat[];
	title: string;
	dealerHand: BlackjackHand;
	gamePhase: string;
}
export interface BlackjackPlayer extends CasinoPlayer {
	hands: BlackjackHand[];
	seatNumber: number;
}
export interface Seat {
	number: number;
	player: BlackjackPlayer;
	available: boolean;
}
export interface BlackjackHand extends CasinoHand {
	insured: boolean;
    values:[] number;
    doubled:boolean;
    split:boolean;
    bet:number;
    insuranceBet:number;
    active:boolean;
    blackjack:boolean;
}