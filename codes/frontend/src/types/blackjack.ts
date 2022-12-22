import type { CasinoTable, CasinoPlayer, CasinoHand } from "./casino";

export interface BlackjackTable extends CasinoTable {
  players: BlackjackPlayer[];
  title: string;
}
export interface BlackjackPlayer extends CasinoPlayer {
  hands: BlackjackHand[];
  seatNumber: number;
}

export interface BlackjackHand extends CasinoHand {
  insured: boolean;
}
