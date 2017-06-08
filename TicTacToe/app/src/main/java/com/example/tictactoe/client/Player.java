package com.example.tictactoe.client;

public class Player {
    private String login;
    private String password;
    private int wins;
    private int draws;
    private int losses;

    Player(String login, String password, int wins, int draws, int losses) {
        this.login = login;
        this.password = password;
        this.wins = wins;
        this.draws = draws;
        this.losses = losses;
    }

    public String getLogin() {
        return login;
    }

    public int getWins() {
        return wins;
    }

    public int getDraws() {
        return draws;
    }

    public int getLosses() {
        return losses;
    }

    public void incrementWins() {
        wins++;
    }

    public void incrementDraws() {
        draws++;
    }

    public void incrementLosses() {
        losses++;
    }
}
