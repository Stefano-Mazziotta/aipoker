package com.poker.game.application.dto;

import java.util.List;

/**
 * Request DTO for starting a game.
 */
public class StartGameRequest {
    private String lobbyId;
    private List<String> playerIds;
    private int smallBlind;
    private int bigBlind;

    public StartGameRequest() {}

    public StartGameRequest(String lobbyId, List<String> playerIds, int smallBlind, int bigBlind) {
        this.lobbyId = lobbyId;
        this.playerIds = playerIds;
        this.smallBlind = smallBlind;
        this.bigBlind = bigBlind;
    }

    public String getLobbyId() {
        return lobbyId;
    }

    public void setLobbyId(String lobbyId) {
        this.lobbyId = lobbyId;
    }

    public List<String> getPlayerIds() {
        return playerIds;
    }

    public void setPlayerIds(List<String> playerIds) {
        this.playerIds = playerIds;
    }

    public int getSmallBlind() {
        return smallBlind;
    }

    public void setSmallBlind(int smallBlind) {
        this.smallBlind = smallBlind;
    }

    public int getBigBlind() {
        return bigBlind;
    }

    public void setBigBlind(int bigBlind) {
        this.bigBlind = bigBlind;
    }
}
