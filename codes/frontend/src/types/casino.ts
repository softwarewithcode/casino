export interface CasinoTable {
  description: TableDescription;
}
export interface CasinoPlayer {
  name: string;
  id: string;
  balance: number;
  totalBet: number;
}
export interface CasinoHand {
  cards: Card[];
  values: [];
  bet: number;
}

export interface TableDescription {
  thresholds: Thresholds;
  availablePositions: Array<Number;
  totalPositions: number;
  name: string;
  id: string;
  language: string;
  game: string;
}

export interface Thresholds {
  minimumBet: number;
  maximumBet: number;
}

export interface Card {
  rank: number;
  suit: SUIT;
}

export enum SUIT {
  CLUB,
  DIAMOND,
  HEART,
  SPADE,
}
