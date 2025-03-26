package edu.uob;

import com.alexmerz.graphviz.objects.Edge;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

//It is worth noting that every game has a "special" location that is the starting point for each player
// adventure. The start location can be called anything we like,
// however it will always be the first location that appears in the entities file.
public class Location extends GameEntity{
    private String name;
    private String description;
    private HashMap<Integer, String> paths;
    private HashMap<Integer, Artefact> artefacts;
    private HashMap<Integer, Furniture> furniture;
    private HashMap<Integer, Character> characters;

    public Location(String name, String description) {
        super(name, description);
//        this.name = "";
//        this.description = "";
        this.name = name;
        this.description = description;
        this.paths = new HashMap<>();
        //this.paths = getPaths();
        this.artefacts = new HashMap<>();
        this.furniture = new HashMap<>();
        this.characters = new HashMap<>();
    }

    public String getName(){
        return this.name;
    }
    public String getDescription(){
        return this.description;
    }
    public HashMap<Integer, Artefact> getArtefacts(){return this.artefacts;}
    public HashMap<Integer, Character> getCharacters(){return this.characters;}
    public HashMap<Integer, Furniture> getFurniture(){return this.furniture;}


    public void addArtefact(Artefact artefact){
        int maxIndex = 0;
        for(Map.Entry<Integer,Artefact> entry : this.getArtefacts().entrySet()){
            if(entry.getKey() > maxIndex){
                maxIndex = entry.getKey();
            }
        }
        this.artefacts.put(maxIndex+1,artefact);
    }
    public void removeArtefact(String artefactName){
        Iterator<Map.Entry<Integer, Artefact>> iterator = this.getArtefacts().entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<Integer, Artefact> entry = iterator.next();
            if(Objects.equals(entry.getValue().getName(), artefactName)){
                iterator.remove();
            }
        }
    }
    public void addFurniture(Furniture newFurniture){
        int maxIndex = 0;
        for(Map.Entry<Integer,Furniture> entry : this.furniture.entrySet()){
            if(entry.getKey() > maxIndex){
                maxIndex = entry.getKey();
            }
        }
        this.furniture.put(maxIndex+1,newFurniture);
    }
    public void addCharacter(Character character){
        int maxIndex = 0;
        for(Map.Entry<Integer,Character> entry : this.characters.entrySet()){
            if(entry.getKey() > maxIndex){
                maxIndex = entry.getKey();
            }
        }
        this.characters.put(maxIndex+1,character);
    }
    public void addPath(String toName){
        int maxIndex = 0;
        for(Map.Entry<Integer,String> entry : this.getPaths().entrySet()){
            if(entry.getKey() > maxIndex){
                maxIndex = entry.getKey();
            }
        }
        int index = maxIndex+1;
        this.paths.put(index,toName);
    }
    public HashMap<Integer, String> getPaths(){return this.paths;}

    public String listAllArtefacts(){
        StringBuilder names = new StringBuilder();
//for(int i = 0; i<this.artefacts.size()-1; i++){
        for(Map.Entry<Integer,Artefact> entry : this.getArtefacts().entrySet()){
            names.append(entry.getValue().getName()).append(", ");
        }
        //names.deleteCharAt(names.length()-1);
        return names.toString();
    }

    public String listAllFurniture(){
        StringBuilder names = new StringBuilder();
//        for(int i = 0; i<this.furniture.size()-1; i++){
//            String name = this.furniture.get(i).getName();
//            names.append(name).append(", ");
//        }
        for(Map.Entry<Integer,Furniture> entry : this.getFurniture().entrySet()){
            names.append(entry.getValue().getName()).append(", ");
        }
        //names.deleteCharAt(names.length()-1);
        return names.toString();
    }
}
