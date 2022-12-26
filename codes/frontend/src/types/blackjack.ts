import type { CasinoTable, CasinoPlayer, CasinoHand } from "./casino";

export interface BlackjackTable extends CasinoTable {
  seats: Seat[];
  title: string;

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
}

export enum Command {
  NEW_PLAYER = "NEW_PLAYER",
  BET_PHASE_STARTS = "BET_PHASE_STARTS",
  INSURANCE_PHASE_STARTS = "INSURANCE_PHASE_STARTS",
  SERVER_WAITS_PLAYER_ACTION = "SERVER_WAITS_PLAYER_ACTION",
  LOGIN = "LOGIN",
  ROUND_COMPLETED = "ROUND_COMPLETED",
  PLAYER_LEFT = "PLAYER_LEFT",
  STATUS_UPDATE = "STATUS_UPDATE",
  OPEN_TABLE = "OPEN_TABLE",
  TIMED_OUT = "TIMED_OUT",
}
