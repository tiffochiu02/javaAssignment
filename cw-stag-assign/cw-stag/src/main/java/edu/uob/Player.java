package edu.uob;

import java.awt.geom.Area;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Player {
    private String name;

    private HashMap<Integer,Artefact> inventory;
    private Location currentLocation;
    private GameAction currentAction;

    public Player(String name, GameEngine engine) {

        this.name = name;

        this.inventory = new HashMap<>();
        this.currentLocation = getCurrentLocation();
        //this.currentLocation = engine.getLocations().get(0);
        this.currentAction = getCurrentAction();
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public HashMap<Integer,Artefact> getInventory() {
        return inventory;
    }

    public void setCurrentLocation(Location newLocation){
        this.currentLocation = newLocation;
    }
    public Location getCurrentLocation() {
        return this.currentLocation;
    }
    public String getArtefactName(int artefactId){
        return this.inventory.get(artefactId).getName();
    }
    public String listArtefacts(){
        StringBuilder allArtefacts = new StringBuilder();
        for(int i = 0; i<this.inventory.size(); i++){
            allArtefacts.append(this.inventory.get(i).getName()).append(", ");
        }
        return allArtefacts.toString();
    }
    public void retrieveArtefact(Artefact artefact){
        int maxIndex = 0;
        for(Map.Entry<Integer, Artefact> entry : this.inventory.entrySet()){
            if(entry.getKey()>maxIndex){
                maxIndex = entry.getKey();
            }
        }
        int newIndex = maxIndex++;
        this.inventory.put(newIndex,artefact);
    }
    public void removeArtefact(Artefact artefact){
        for(Map.Entry<Integer, Artefact> entry : this.inventory.entrySet()){
            if(entry.getValue().equals(artefact)){
                this.inventory.remove(entry.getKey());
            }
        }
    }
    public void setCurrentAction(GameAction action){
        this.currentAction = action;
    }
    public GameAction getCurrentAction(){
        return this.currentAction;
    }
}
