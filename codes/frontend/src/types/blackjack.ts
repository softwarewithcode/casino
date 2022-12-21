import type { CasinoTable, CasinoPlayer, CasinoHand } from "./casino";

export interface BlackjackTable extends CasinoTable {
  players: BlackjackPlayer[];
}
export interface BlackjackPlayer extends CasinoPlayer {
  hands: BlackjackHand[];
}

export interface BlackjackHand extends CasinoHand {
  insured: boolean;
}
