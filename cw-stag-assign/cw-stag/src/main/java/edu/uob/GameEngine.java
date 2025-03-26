package edu.uob;

import java.security.KeyStore;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class GameEngine {
    private HashMap<Integer,Player> players;
//    private ActionsFileReader newActions;
//    private EntityFileReader newEntities;
    private Player currentPlayer;
    private HashMap<Integer, Location> locations;
    private LinkedList<GameAction> gameActions;

    public GameEngine() {
        this.players = new HashMap<>();
//        this.newActions = new ActionsFileReader();
//        this.newEntities = new EntityFileReader();
        //this.currentPlayer = getCurrentPlayer();
        this.locations = new HashMap<>();
        this.gameActions = new LinkedList<>();
    }

//    public ActionsFileReader getActions() {
//        return this.newActions;
//    }
//    public void setActions(ActionsFileReader newActions) {
//        this.newActions = newActions;
//    }
//    public EntityFileReader getEntities() {
//        return this.newEntities;
//    }

    public Player getCurrentPlayer() {
        return this.currentPlayer;
    }
    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }
    public HashMap<Integer, Location> getLocations() {
        return this.locations;
    }
    public void setLocations(HashMap<Integer, Location> newLocations) {
        this.locations = newLocations;
    }
    public LinkedList<GameAction> getGameActions() {
        return this.gameActions;
    }
    public void setGameActions(LinkedList<GameAction> newGameActions) {
        this.gameActions = newGameActions;
    }
    public HashMap<Integer, Player> getPlayers() {
        return this.players;
    }
    public void setPlayers(HashMap<Integer, Player> newPlayers) {
        this.players = newPlayers;
    }
    public void addPlayer(Player player) {
        int maxIndex = 0;
       for(Map.Entry<Integer,Player> entry : this.getPlayers().entrySet()) {
           if(entry.getKey() > maxIndex) {
               maxIndex = entry.getKey();
           }
       }
       this.getPlayers().put(maxIndex + 1,player);
    }

}
