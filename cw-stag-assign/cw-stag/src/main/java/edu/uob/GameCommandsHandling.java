package edu.uob;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class GameCommandsHandling {


    public static Location getCurrentLocation(GameEngine gameEngine) {
        return gameEngine.getCurrentPlayer().getCurrentLocation();
    }

    public static String allCommandsHandling(LinkedList<String> tokens, GameEngine gameEngine) {
        if(tokens.isEmpty()) {return "No actions found.";}
        if(builtInCommandHandling(tokens,gameEngine) != null){
            return builtInCommandHandling(tokens,gameEngine);
        } else if(customCommandHandling(tokens,gameEngine) != null){
            return customCommandHandling(tokens,gameEngine);
        } else {
            return "No valid action detected.";
        }
    }

    public static String builtInCommandHandling(LinkedList<String> tokens, GameEngine gameEngine) {
        System.out.println("Entered builtInCommandHandling!!!!");
        for (String token : tokens) {
            if(token.equalsIgnoreCase("inventory") || token.equalsIgnoreCase("inv")) {
                return gameEngine.getCurrentPlayer().listArtefacts();
            }
            else if(token.equalsIgnoreCase("get")) {
                return getArtefact(tokens,gameEngine);
            }
            else if(token.equalsIgnoreCase("drop")) {
                return dropArtefact(tokens,gameEngine);
            }
            else if(token.equalsIgnoreCase("goto")) {
                return gotoLocation(tokens,gameEngine);
            }
            else if(token.equalsIgnoreCase("look")) {
                return lookLocation(tokens,gameEngine);
            }
        }
        return null;
    }

    public static String getArtefact(LinkedList<String> tokens,GameEngine gameEngine) {
        for (String token : tokens) {
            for(int i = 0; i < getCurrentLocation(gameEngine).getArtefacts().size(); i++) {
                String artefactName = getCurrentLocation(gameEngine).getArtefacts().get(i).getName();
                if(token.equalsIgnoreCase(artefactName)) {
                    Artefact retrievedArtefact = getCurrentLocation(gameEngine).getArtefacts().get(i);
                    gameEngine.getCurrentPlayer().retrieveArtefact(retrievedArtefact);
                    getCurrentLocation(gameEngine).getArtefacts().remove(i);
                    StringBuilder sMessage = new StringBuilder();
                    return sMessage.append("Added Artefact: ").append(token).append(" to the inventory.").toString();
                }
            }
        }
        StringBuilder fMessage = new StringBuilder();
        return fMessage.append("No artefact ").append(" found.").toString();
    }
    public static String dropArtefact(LinkedList<String> tokens, GameEngine gameEngine) {
        for (String token : tokens) {
            for(int i = 0; i < gameEngine.getPlayers().get(0).getInventory().size(); i++) {
                String artefactName = gameEngine.getPlayers().get(0).getInventory().get(i).getName();
                if(token.equalsIgnoreCase(artefactName)){
                    Artefact droppedArtefact = gameEngine.getPlayers().get(0).getInventory().get(i);
                    gameEngine.getCurrentPlayer().removeArtefact(droppedArtefact);
                    getCurrentLocation(gameEngine).getArtefacts().put(getCurrentLocation(gameEngine).getArtefacts().size(), droppedArtefact);
                    StringBuilder sMessage = new StringBuilder();
                    return sMessage.append("Dropped Artefact: ").append(token).append(" from the inventory.").toString();
                }
            }
        }
        StringBuilder fMessage = new StringBuilder();
        return fMessage.append("No artefact ").append(" found.").toString();
    }

    public static String gotoLocation(LinkedList<String> tokens, GameEngine gameEngine) {
        String newLocationName = "";
        for (String token : tokens) {
           int numOfPaths = getCurrentLocation(gameEngine).getPaths().size();
           for(int i = 0; i < numOfPaths; i++) {
               if(token.equalsIgnoreCase(getCurrentLocation(gameEngine).getPaths().get(i))) {
                   newLocationName = getCurrentLocation(gameEngine).getPaths().get(i);
               }
           }
        }
        for(int i = 0; i < gameEngine.getLocations().size(); i++) {
            if(newLocationName.equalsIgnoreCase(gameEngine.getLocations().get(i).getName())) {
                gameEngine.getCurrentPlayer().setCurrentLocation(gameEngine.getLocations().get(i));
                StringBuilder sMessage = new StringBuilder();
                return sMessage.append("Arrived at location: ").append(newLocationName).append(".").toString();
            }
        }
        StringBuilder fMessage = new StringBuilder();
        return fMessage.append("No location ").append(" found.").toString();
    }

    //look prints details of the current location (including all entities present) and lists the paths to other locations
    public static String lookLocation(LinkedList<String> tokens, GameEngine gameEngine) {
        String currentLocationName = getCurrentLocation(gameEngine).getName();
        String currentLocationDescription = getCurrentLocation(gameEngine).getDescription();
        String allArtefacts = getCurrentLocation(gameEngine).listAllArtefacts();
        String allFurniture = getCurrentLocation(gameEngine).listAllFurniture();
        String allPaths = getCurrentLocation(gameEngine).getPaths().toString();
        StringBuilder otherPresentPlayers = new StringBuilder();
        for(Map.Entry<Integer,Player> entry: gameEngine.getPlayers().entrySet()){
            if(entry.getValue().getCurrentLocation().getName().equalsIgnoreCase(currentLocationName)){
                otherPresentPlayers.append(entry.getValue().getName()).append(", ");
            }
        }
        //delete the final comma
        otherPresentPlayers.deleteCharAt(otherPresentPlayers.length()-2);
        StringBuilder sb = new StringBuilder();

        sb.append("You are at ").append(currentLocationName).append(": ").append(currentLocationDescription).append("\n");
        sb.append("Artefacts: ").append(allArtefacts).append("\n");
        sb.append("Furniture: ").append(allFurniture).append("\n");
        sb.append("Paths: ").append(allPaths).append("\n");
        sb.append("Other players in your location: ").append(otherPresentPlayers).append("\n");
        return sb.toString();
    }

    public static String customCommandHandling(LinkedList<String> tokens, GameEngine gameEngine) {
        setCurrentAction(tokens,gameEngine);
        GameAction currentAction = gameEngine.getCurrentPlayer().getCurrentAction();
        if(currentAction == null){
            return "No action detected.";
        }
        if(consumedEntity(gameEngine)){
            return currentAction.getNarration();
        }
        return null;
    }

    public static boolean consumedEntity(GameEngine gameEngine) {
        GameAction currentAction = gameEngine.getCurrentPlayer().getCurrentAction();
        Location storeroom = new Location("","");
        for(Integer key: gameEngine.getLocations().keySet()){
            storeroom = gameEngine.getLocations().get(key);
        }
        for(Integer key: currentAction.getConsumed().keySet()) {
            String consumed = currentAction.getConsumed().get(key);
            Artefact consumedInvArtefact = consumeInventory(consumed,gameEngine);
            Artefact consumedArtefact = consumeArtefact(consumed,gameEngine);
            Furniture consumedFurniture = consumeFurniture(consumed,gameEngine);
            Character consumedCharacter = consumeCharacter(consumed,gameEngine);

            if(consumedArtefact != null) {
                storeroom.addArtefact(consumedArtefact);
                return true;
            } else if(consumedFurniture != null) {
                storeroom.addFurniture(consumedFurniture);
                return true;
            } else if(consumedCharacter != null) {
                storeroom.addCharacter(consumedCharacter);
                return true;
            } else if(consumedInvArtefact != null) {
                storeroom.addArtefact(consumedInvArtefact);
                return true;
            }
        }
        return false;
    }

    public static Artefact consumeInventory(String consumed, GameEngine gameEngine) {
        for(Integer subKey: gameEngine.getCurrentPlayer().getInventory().keySet()){
            String assetName = gameEngine.getCurrentPlayer().getInventory().get(subKey).getName();
            String assetDescription = gameEngine.getCurrentPlayer().getInventory().get(subKey).getDescription();
            if(consumed.equalsIgnoreCase(assetName)){
                gameEngine.getCurrentPlayer().getInventory().remove(subKey);
                return new Artefact(assetName, assetDescription);
            }
        }
        return null;
    }
    public static Artefact consumeArtefact(String consumed, GameEngine gameEngine) {
        for(Integer subKey: getCurrentLocation(gameEngine).getArtefacts().keySet()){
            String artefactName = getCurrentLocation(gameEngine).getArtefacts().get(subKey).getName();
            String artefactDescription = getCurrentLocation(gameEngine).getArtefacts().get(subKey).getDescription();
            if(consumed.equalsIgnoreCase(artefactName)){
                getCurrentLocation(gameEngine).getArtefacts().remove(subKey);
                return new Artefact(artefactName, artefactDescription);
            }
        }
        return null;
    }
    public static Furniture consumeFurniture(String consumed, GameEngine gameEngine) {
        for(Integer subKey: getCurrentLocation(gameEngine).getFurniture().keySet()){
            String furnitureName = getCurrentLocation(gameEngine).getFurniture().get(subKey).getName();
            String furnitureDescription = getCurrentLocation(gameEngine).getFurniture().get(subKey).getDescription();
            if(consumed.equalsIgnoreCase(furnitureName)){
                getCurrentLocation(gameEngine).getFurniture().remove(subKey);
                return new Furniture(furnitureName, furnitureDescription);
            }
        }
        return null;
    }
    public static Character consumeCharacter(String consumed, GameEngine gameEngine) {
        for(Integer subKey: getCurrentLocation(gameEngine).getCharacters().keySet()){
            String characterName = getCurrentLocation(gameEngine).getCharacters().get(subKey).getName();
            String characterDescription = getCurrentLocation(gameEngine).getCharacters().get(subKey).getDescription();
            if(consumed.equalsIgnoreCase(characterName)){
                getCurrentLocation(gameEngine).getCharacters().remove(subKey);
                return new Character(characterName, characterDescription);
            }
        }
        return null;
    }

    //Note that the action is only valid if ALL subject entities (as specified in the actions file)
    // are available to the player. If a valid action is found,
    // your server must undertake the relevant additions/removals (production/consumption).
    public static void setCurrentAction(LinkedList<String> tokens, GameEngine gameEngine) {
        boolean triggerMatched = false;
        boolean allSubjectMatched = false;
        GameAction currentAction = new GameAction();
        for(int i = 0; i < gameEngine.getGameActions().size(); i++) {
            currentAction = gameEngine.getGameActions().get(i);
            for(int j = 0; j < currentAction.getTriggers().size(); j++){
                for (String token : tokens) {
                    if(token.equalsIgnoreCase(currentAction.getTriggers().get(j))) {
                       triggerMatched = true;
                    }
                }
            }
        }
        allSubjectMatched = checkAllSubjectMatched(tokens,currentAction,gameEngine);
        if(triggerMatched && allSubjectMatched) {
           gameEngine.getCurrentPlayer().setCurrentAction(currentAction);
        }
    }

    //check if location entities match with the subjects of this current action
    //All subject entities have to be either in the inventory or in the current location
    public static boolean checkAllSubjectMatched(LinkedList<String> tokens, GameAction currentDetectedAction, GameEngine gameEngine) {
        boolean allMatched = false;
        for(int m = 0; m < currentDetectedAction.getSubjects().size(); m++) {
            String subjectName = currentDetectedAction.getSubjects().get(m);
            boolean subjectMatched = false;
            for(int k = 0; k < getCurrentLocation(gameEngine).getArtefacts().size(); k++) {
                String artefactName = getCurrentLocation(gameEngine).getArtefacts().get(k).getName();
                if(subjectName.equalsIgnoreCase(artefactName)) {
                    subjectMatched = true;
                }
            }
            for(int k = 0; k < getCurrentLocation(gameEngine).getFurniture().size(); k++) {
                String furnitureName = getCurrentLocation(gameEngine).getFurniture().get(k).getName();
                if(subjectName.equalsIgnoreCase(furnitureName)) {
                    subjectMatched = true;
                }
            }
            for(int k = 0; k < gameEngine.getCurrentPlayer().getInventory().size(); k++) {
                String assetName = gameEngine.getCurrentPlayer().getInventory().get(k).getName();
                if(subjectName.equalsIgnoreCase(assetName)) {
                    subjectMatched = true;
                }
            }
            for(int k = 0; k < getCurrentLocation(gameEngine).getCharacters().size(); k++) {
                String characterName = getCurrentLocation(gameEngine).getCharacters().get(k).getName();
                if(subjectName.equalsIgnoreCase(characterName)) {
                    subjectMatched = true;
                }
            }if(!subjectMatched){return false; }
            allMatched = true;
        }
        return allMatched;
    }
}


