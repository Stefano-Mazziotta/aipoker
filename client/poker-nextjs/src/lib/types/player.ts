// Player DTOs matching Java backend
export interface PlayerDTO {
  id: string;
  name: string;
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