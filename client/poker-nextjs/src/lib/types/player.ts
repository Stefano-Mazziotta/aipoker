// Player DTOs matching Java backend
export interface PlayerDTO {
  playerId: string;
  playerName: string;
  chips: number;
}

export interface PlayerStateDTO {
  id: string;
  name: string;
  chips: number;
  bet: number;
  isActive: boolean;
  folded: boolean;
  isDealer?: boolean;
  isSmallBlind?: boolean;
  isBigBlind?: boolean;
  cards?: string[];
}

export interface PlayerInfo {
  id: string;
  name: string;
  chips: number;
}
