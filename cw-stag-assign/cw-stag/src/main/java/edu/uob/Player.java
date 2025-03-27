package edu.uob;

import java.awt.geom.Area;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class Player {
    private String name;

    private HashMap<Integer,Artefact> inventory;
    private Location currentLocation;
    private GameAction currentAction;
    private int healthLevels;

    public Player(String name, GameEngine engine) {
        this.name = name;
        this.inventory = new HashMap<>();
        this.currentLocation = engine.getLocations().get(0);
        this.currentAction = getCurrentAction();
        this.healthLevels = 3;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public HashMap<Integer,Artefact> getInventory() {
        return this.inventory;
    }

    public void setCurrentLocation(Location newLocation){
        this.currentLocation = newLocation;
    }
    public Location getCurrentLocation() {
        return this.currentLocation;
    }
    public String getArtefactName(int artefactId){
        return this.getInventory().get(artefactId).getName();
    }
    public String listArtefacts(){
        StringBuilder allArtefacts = new StringBuilder();
        for(Map.Entry<Integer,Artefact> entry : this.getInventory().entrySet()){
            allArtefacts.append(entry.getValue().getName()).append("\n");
        }
        return allArtefacts.toString();
    }
    public void retrieveArtefact(Artefact artefact){
        int maxIndex = 0;
        for(Map.Entry<Integer, Artefact> entry : this.getInventory().entrySet()){
            if(entry.getKey() > maxIndex){
                maxIndex = entry.getKey();
            }
        }
        int newIndex = maxIndex+1;
        this.getInventory().put(newIndex,artefact);
    }
    public void removeArtefact(String artefactName){
        Iterator<Map.Entry <Integer, Artefact>> iterator = this.getInventory().entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry <Integer, Artefact> entry = iterator.next();
            if(entry.getValue().getName().equals(artefactName)){
                iterator.remove();
            }
        }
    }
    public void setCurrentAction(GameAction action){
        this.currentAction = action;
    }
    public GameAction getCurrentAction(){
        return this.currentAction;
    }
    public int getHealthLevels(){
        return this.healthLevels;
    }
    public void resetHealthLevels(){
        this.healthLevels = 3;
        getInventory().entrySet().removeAll(this.getInventory().entrySet());

    }
    public void addHealth(){
        if(this.getHealthLevels() < 3){
            this.healthLevels += 1;
        }
    }
    public void loseHealth(){
        this.healthLevels -= 1;
    }

}
