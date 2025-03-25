package edu.uob;

import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.objects.Edge;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Node;
//import org.junit.Test;

import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class EntityFileReader {
    private HashMap<Integer,Location> locations;
    public EntityFileReader() {
        locations = new HashMap<>();
    }

    public HashMap<Integer,Location> getLocations() {
        return this.locations;
    }
    public void EntityHandling(File entitiesFile) {
        try{
            //File entitiesFile = Paths.get("config" + File.separator + "basic-entities.dot").toAbsolutePath().toFile();
            Parser parser = new Parser();
            //FileReader reader = new FileReader("config" + File.separator + "basic-entities.dot");
            FileReader reader = new FileReader(entitiesFile);
            parser.parse(reader);
            Graph wholeDocument = parser.getGraphs().get(0);
            LinkedList<Graph> sections = new LinkedList<>();
            sections.add(wholeDocument);

            Graph allLocations = sections.get(0).getSubgraphs().get(0);
            int numOfLocation = allLocations.getSubgraphs().size();
            System.out.println("num of locations = " + numOfLocation);
            for(int i=0;i<numOfLocation;i++){
                setLocation(allLocations,i);
            }
            Graph allPaths = sections.get(0).getSubgraphs().get(1);
            setPaths(allPaths);

            for (Map.Entry<Integer, Location> entry : locations.entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue().getName());
                int sizeOfPath = entry.getValue().getPaths().size();
                for(int j=0;j<sizeOfPath;j++){
                    System.out.println(entry.getValue().getPaths().toString());
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void setPaths(Graph allPaths){
        int numOfPaths = allPaths.getEdges().size();
        System.out.println("num of paths = " + numOfPaths);

        for(Map.Entry<Integer,Location> entry : locations.entrySet()){
            for(int i=0;i<numOfPaths;i++){
                Edge path = allPaths.getEdges().get(i);
                Node fromLocation = path.getSource().getNode();
                Node toLocation = path.getTarget().getNode();
                String from = fromLocation.getId().getId();
                String to = toLocation.getId().getId();
                if(entry.getValue().getName().equals(from)){
                    entry.getValue().addPath(i,to);
                }
            }
        }
    }

    public void setLocation(Graph allLocation,int locationIndex) throws FileNotFoundException, IOException {
        Graph location = allLocation.getSubgraphs().get(locationIndex);
        Node locationDetails = location.getNodes(false).get(0);
        String locationName = locationDetails.getId().getId();
        System.out.println("locationName = " + locationName);
        String locationDescriptionName = locationDetails.getAttributes().get("description");
        System.out.println("the location description is: " + locationDescriptionName);
        this.locations.put(locationIndex, new Location(locationName, locationDescriptionName));

        setEntities(location, locationIndex);
    }

    public void setEntities(Graph location, int locationIndex) throws FileNotFoundException, IOException{
        int numOfNodes = location.getSubgraphs().size();
        if (numOfNodes < 1) {
            return;
        }
        for (int i = 0; i < numOfNodes; i++) {
            Graph entityDetails = location.getSubgraphs().get(i);
            String entityName = entityDetails.getId().getId();
            System.out.println("entityName = " + entityName);

            if (entityName.equals("artefacts")) {
                setArtefact(location, i, locationIndex);
            } else if (entityName.equals("furniture")) {
                setFurniture(location, i, locationIndex);
            } else if (entityName.equals("characters")) {
                System.out.println("entered hereeeee! detected characters");
                setCharacter(location, i, locationIndex);
            }
        }
    }

    public void setArtefact(Graph location,int entityIndex, int locationIndex) throws FileNotFoundException, IOException{
         Graph Artefact = location.getSubgraphs().get(entityIndex);
         int numOfArtefact = location.getSubgraphs().get(entityIndex).getNodes(true).size();
         for (int i = 0; i < numOfArtefact; i++) {
             Node ArtefactDetails = Artefact.getNodes(false).get(i);
             String ArtefactName = ArtefactDetails.getId().getId();
             System.out.println("ArtefactName = " + ArtefactName);
             String ArtefactDescriptionName = ArtefactDetails.getAttributes().get("description");
             System.out.println("ArtefactDescriptionName = " + ArtefactDescriptionName);
             Artefact artefact = new Artefact(ArtefactName, ArtefactDescriptionName);
             this.locations.get(locationIndex).addArtefact(artefact);
         }
    }

    public void setFurniture(Graph location,int entityIndex, int locationIndex) throws FileNotFoundException, IOException{
        Graph Furniture = location.getSubgraphs().get(entityIndex);

        int numOfFurniture = location.getSubgraphs().get(entityIndex).getNodes(true).size();
        for (int i = 0; i < numOfFurniture; i++) {
            Node furnitureDetails = Furniture.getNodes(false).get(i);
            String FurnitureName = furnitureDetails.getId().getId();
            System.out.println("FurnitureName = " + FurnitureName);
            String FurnitureDescriptionName = furnitureDetails.getAttributes().get("description");
            System.out.println("FurnitureDescriptionName = " + FurnitureDescriptionName);
            Furniture furniture = new Furniture(FurnitureName, FurnitureDescriptionName);
            this.locations.get(locationIndex).addFurniture(furniture);
        }
    }

    public void setCharacter(Graph location,int entityIndex, int locationIndex) throws FileNotFoundException, IOException {
        Graph Character = location.getSubgraphs().get(entityIndex);

        int numOfCharacter = location.getSubgraphs().get(entityIndex).getNodes(true).size();
//        if(numOfCharacter == 0){
//            throw new FileNotFoundException();
//        }
        System.out.println("numOfCharacter = " + numOfCharacter);
        for (int i = 0; i < numOfCharacter; i++) {
            Node characterDetails = Character.getNodes(false).get(i);
            String characterName = characterDetails.getId().getId();
            System.out.println("characterName = " + characterName);
            String characterDescriptionName = characterDetails.getAttributes().get("description");
            System.out.println("characterDescriptionName = " + characterDescriptionName);
            Character character = new Character(characterName, characterDescriptionName);
            this.locations.get(locationIndex).addCharacter(character);
        }
    }






}
