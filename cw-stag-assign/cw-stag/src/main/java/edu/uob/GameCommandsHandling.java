package edu.uob;

import java.util.*;

public class GameCommandsHandling {


    public static Location getCurrentLocation(GameEngine gameEngine) {
        return gameEngine.getCurrentPlayer().getCurrentLocation();
    }

    public static String allCommandsHandling(LinkedList<String> tokens, GameEngine gameEngine) {
        if(!onlyOneTrigger(tokens, gameEngine)) {return "Invalid actions number.";}
        String builtInResult = builtInCommandHandling(tokens,gameEngine);
        if(builtInResult != null){
            return builtInResult;
        } else {
            String customResult = customCommandHandling(tokens,gameEngine);
            if(customResult != null) {
                return customResult;
            }
        }
        return "No valid action detected.";
    }

    public static boolean onlyOneTrigger(LinkedList<String> tokens, GameEngine gameEngine) {
        Set<String> cmds = new HashSet<String>();
        cmds.add("inventory");
        cmds.add("inv");
        cmds.add("get");
        cmds.add("drop");
        cmds.add("goto");
        cmds.add("look");
        for(int i = 0; i < gameEngine.getGameActions().size(); i++) {
            GameAction currentAction = gameEngine.getGameActions().get(i);
            cmds.addAll(currentAction.getTriggers().values());
        }
        int cmdCounts = 0;
        for (String token: tokens) {
            if (token == null) {continue; }
            if(cmds.contains(token.toLowerCase())) {
                cmdCounts++;
            }
        }
        return cmdCounts == 1;
    }

    public static String builtInCommandHandling(LinkedList<String> tokens, GameEngine gameEngine) {
        System.out.println("current player name is: " + gameEngine.getCurrentPlayer().getName());
        System.out.println("current location is: " + gameEngine.getCurrentPlayer().getCurrentLocation().getName());

        Set<String> basicCommands = new HashSet<String>();
        basicCommands.add("inventory");
        basicCommands.add("inv");
        basicCommands.add("get");
        basicCommands.add("drop");
        basicCommands.add("goto");
        basicCommands.add("look");

        String command = null;
        Iterator<String> iterator = tokens.iterator();
        while(iterator.hasNext()) {
            String token = iterator.next();
            if (token == null) {continue; }
            if(basicCommands.contains(token.toLowerCase())) {
                command = token.toLowerCase();
                iterator.remove();
                break;
            }
        }
        if(command == null) {
            return null;
        }

        switch(command) {
            case "inventory":
                return gameEngine.getCurrentPlayer().listArtefacts();
            case "inv":
                return gameEngine.getCurrentPlayer().listArtefacts();
            case "get":
                return getArtefact(tokens, gameEngine);
            case "drop":
                return dropArtefact(tokens, gameEngine);
            case "goto":
                return gotoLocation(tokens, gameEngine);
            case "look":
                return lookLocation(tokens, gameEngine);
            default:
                return "Unknown command";
        }

    }

    //    public static String getArtefact(LinkedList<String> tokens,GameEngine gameEngine) {
//        System.out.println("Entered getArtefact!!!!");
//        Location currentLocation = getCurrentLocation(gameEngine);
//        Player currentPlayer = gameEngine.getCurrentPlayer();
//        boolean found = false;
//        String artefactName = "";
//        for (String token : tokens) {
//            System.out.println("Entered for loop of tokens");
//            boolean matchedArtefactFound = false;
//            Iterator<Map.Entry<Integer, Artefact>> iterator = currentLocation.getArtefacts().entrySet().iterator();
//            while (iterator.hasNext()) {
//                Map.Entry<Integer, Artefact> entry = iterator.next();
//                if (token.equalsIgnoreCase(entry.getValue().getName())) {
//                    System.out.println("Matched Artefact found!!!");
//                    artefactName = entry.getValue().getName();
//                    Artefact newArtefact = entry.getValue();
//                    currentPlayer.retrieveArtefact(newArtefact);
//                    iterator.remove();
//                    matchedArtefactFound = true;
//                    break;
//                }
//            }if(matchedArtefactFound){
//                found = true;
//                break;
//            }
//        }
//        if(!found) {
//            return "Artefact not found";
//        }
//
//        StringBuilder sMessage = new StringBuilder();
//        return sMessage.append("Added Artefact: ").append(artefactName).append(" to the inventory.").toString();
//    }
    public static String getArtefact(LinkedList<String> tokens, GameEngine gameEngine) {
        Location currentLocation = getCurrentLocation(gameEngine);
        Player currentPlayer = gameEngine.getCurrentPlayer();
        boolean found = false;
        String artefactName = "";

        for (String token : tokens) {
            System.out.println("Processing token: '" + token + "'");
            boolean matchedArtefactFound = false;
            Iterator<Map.Entry<Integer, Artefact>> iterator = currentLocation.getArtefacts().entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, Artefact> entry = iterator.next();
                System.out.println("Comparing token: '" + token + "' with artefact name: '"
                        + entry.getValue().getName() + "'");
                if (token.equalsIgnoreCase(entry.getValue().getName())) {
                    System.out.println("Matched Artefact found!!!");
                    artefactName = entry.getValue().getName();
                    currentPlayer.retrieveArtefact(entry.getValue());
                    iterator.remove();
                    matchedArtefactFound = true;
                    break;
                }
            }
            System.out.println("Token: '" + token + "' matched? " + matchedArtefactFound);
            if (matchedArtefactFound) {
                found = true;
                break;
            }
        }

        System.out.println("Final flag - found: " + found + ", artefactName: '" + artefactName + "'");
        if (!found) {
            return "Artefact not found";
        }

        return "Added Artefact: " + artefactName + " to the inventory.";
    }

    public static String dropArtefact(LinkedList<String> tokens, GameEngine gameEngine) {
        String artefactName = "";
        boolean artefactFound = false;
        for (String token : tokens) {
//            for(int i = 0; i < gameEngine.getCurrentPlayer().getInventory().size(); i++) {
//                String artefactName = gameEngine.getCurrentPlayer().getInventory().get(i).getName();
            for(Map.Entry<Integer,Artefact> entry: gameEngine.getCurrentPlayer().getInventory().entrySet()){
                artefactName = entry.getValue().getName();
                if(token.equalsIgnoreCase(artefactName)) {
                    Artefact droppedArtefact = entry.getValue();
                    //gameEngine.getCurrentPlayer().removeArtefact(droppedArtefact);
                    getCurrentLocation(gameEngine).addArtefact(droppedArtefact);
                    artefactFound = true;
                    break;
                }
            }

        }
        gameEngine.getCurrentPlayer().removeArtefact(artefactName);
        if(!artefactFound) {
            return "No artefact found.";
        }
        StringBuilder sMessage = new StringBuilder();
        return sMessage.append("Dropped Artefact: ").append(artefactName).append(" from the inventory.").toString();
    }

    public static String gotoLocation(LinkedList<String> tokens, GameEngine gameEngine) {
        String newLocationName = "";
        for (String token : tokens) {
            for(Map.Entry<Integer,String> path: getCurrentLocation(gameEngine).getPaths().entrySet()){
                if(token.equalsIgnoreCase(path.getValue())) {
                    newLocationName = path.getValue();
                }
            }
        }
        for(Map.Entry<Integer,Location> entry: gameEngine.getLocations().entrySet()){
            if(newLocationName.equalsIgnoreCase(entry.getValue().getName())) {
                gameEngine.getCurrentPlayer().setCurrentLocation(entry.getValue());
                StringBuilder sMessage = new StringBuilder();
                return sMessage.append("Arrived at location: ").append(newLocationName).append(".").toString();
            }
        }
        StringBuilder fMessage = new StringBuilder();
        return fMessage.append("No location ").append(newLocationName).append(" found.").toString();
    }

    //look prints details of the current location (including all entities present) and lists the paths to other locations
    public static String lookLocation(LinkedList<String> tokens, GameEngine gameEngine) {
        String currentLocationName = getCurrentLocation(gameEngine).getName();
        String currentLocationDescription = getCurrentLocation(gameEngine).getDescription();
        String allArtefacts = getCurrentLocation(gameEngine).listAllArtefactsDescription();
        String allFurniture = getCurrentLocation(gameEngine).listAllFurnitureDescription();
        String allCharacters = getCurrentLocation(gameEngine).listAllCharactersDescription();
        String allPaths = getCurrentLocation(gameEngine).listAllPaths();
        StringBuilder otherPresentPlayers = new StringBuilder();
        for(Map.Entry<Integer,Player> entry: gameEngine.getPlayers().entrySet()){
            if(entry.getValue().getName().equalsIgnoreCase(gameEngine.getCurrentPlayer().getName())){
                continue;
            }
            if(entry.getValue().getCurrentLocation().getName().equalsIgnoreCase(currentLocationName)){
                otherPresentPlayers.append(entry.getValue().getName()).append(", ");
            }
        }
        String otherPlayers = otherPresentPlayers.toString();
        if(otherPlayers.contains(",")){
            otherPlayers = otherPlayers.substring(0, otherPlayers.lastIndexOf(","));
        }

        StringBuilder sb = new StringBuilder();

        sb.append("You are at ").append(": ").append(currentLocationDescription).append("\n");
        sb.append("You can see: ").append(allArtefacts).append(allFurniture);
        if(!allCharacters.isEmpty()) {
            sb.append(allCharacters).append(".\n");
        }
        sb.append("There is a path to ").append(allPaths).append(".\n");

        if(!otherPlayers.isEmpty()) {
            sb.append("Other players in your location: ").append(otherPlayers).append(".\n");
        }
        return sb.toString();
    }

    public static String customCommandHandling(LinkedList<String> tokens, GameEngine gameEngine) {
        setCurrentAction(tokens,gameEngine);
        Player currentPlayer = gameEngine.getCurrentPlayer();
        GameAction currentAction = currentPlayer.getCurrentAction();
        if(currentAction == null){
            return "No action detected.";
        }
        boolean subjectTriggered = false;
        for(String token : tokens) {
            for(Map.Entry<Integer, String> entry: currentAction.getSubjects().entrySet()){
                if(token.equalsIgnoreCase(entry.getValue())) {
                    subjectTriggered = true;
                    break;
                }
            }
        }

        if(subjectTriggered && consumedEntity(gameEngine)){
            if(currentPlayer.getHealthLevels() == 0){
                playerReset(gameEngine, currentPlayer);
                return "Oh no! You died and lost everything!";
            }
            return currentAction.getNarration();
        }
        return null;
    }

    public static void playerReset(GameEngine gameEngine, Player currentPlayer) {
        currentPlayer.setCurrentLocation(gameEngine.getLocations().get(0));
        currentPlayer.resetHealthLevels();
    }

    public static void produceEntity(GameAction currentAction, GameEngine gameEngine, Location storeroom) {
        for(Integer key: currentAction.getProduced().keySet()) {
            String produced = currentAction.getProduced().get(key);
            for(Map.Entry<Integer,Location> producedLocation: gameEngine.getLocations().entrySet()){
                if(producedLocation.getValue().getName().equalsIgnoreCase(produced)) {
                    for(Map.Entry<Integer,Location> producedLocation2: gameEngine.getLocations().entrySet()) {
                        System.out.println(producedLocation2.getValue().getPaths());
                    }
                    System.out.println("----");
                    getCurrentLocation(gameEngine).addPath(produced);
                    for(Map.Entry<Integer,Location> producedLocation2: gameEngine.getLocations().entrySet()) {
                        System.out.println(producedLocation2.getValue().getPaths());
                    }
                    break;
                }
            }

            Iterator<Map.Entry<Integer,Artefact>> iterator = storeroom.getArtefacts().entrySet().iterator();
            while(iterator.hasNext()){
                Map.Entry<Integer,Artefact> entry = iterator.next();
                Artefact artefact = entry.getValue();
                if(produced.equalsIgnoreCase(artefact.getName())) {
                    iterator.remove();
                    if(produced.equalsIgnoreCase("health")){
                        gameEngine.getCurrentPlayer().addHealth();
                        break;
                    }
                    getCurrentLocation(gameEngine).addArtefact(artefact);
                    break;
                }
            }

            Iterator<Map.Entry<Integer, Furniture>> furnitureIterator = storeroom.getFurniture().entrySet().iterator();
            while (furnitureIterator.hasNext()) {
                Map.Entry<Integer, Furniture> entry = furnitureIterator.next();
                Furniture furniture = entry.getValue();
                if (produced.equalsIgnoreCase(furniture.getName())) {
                    furnitureIterator.remove();
                    getCurrentLocation(gameEngine).addFurniture(furniture);
                    break;
                }
            }

            Iterator<Map.Entry<Integer, Character>> characterIterator = storeroom.getCharacters().entrySet().iterator();
            while (characterIterator.hasNext()) {
                Map.Entry<Integer, Character> entry = characterIterator.next();
                Character character = entry.getValue();
                if (produced.equalsIgnoreCase(character.getName())) {
                    characterIterator.remove();
                    getCurrentLocation(gameEngine).addCharacter(character);
                    break;
                }
            }

//            for(Map.Entry<Integer,Artefact> artefact: storeroom.getArtefacts().entrySet()){
//                if(artefact.getValue().getName().equalsIgnoreCase(produced)) {
//                    System.out.println("Entered produce entity - getArtefact: " + artefact.getValue().getName());
//                    getCurrentLocation(gameEngine).addArtefact(artefact.getValue());
//                    break;
//                }
//            }
//            for(Map.Entry<Integer,Furniture> furniture: storeroom.getFurniture().entrySet()){
//                if(furniture.getValue().getName().equalsIgnoreCase(produced)) {
//                    getCurrentLocation(gameEngine).addFurniture(furniture.getValue());
//                    break;
//                }
//            }
//            for(Map.Entry<Integer,Character> character: storeroom.getCharacters().entrySet()){
//                if(character.getValue().getName().equalsIgnoreCase(produced)) {
//                    getCurrentLocation(gameEngine).addCharacter(character.getValue());
//                    break;
//                }
//            }
        }
    }

    public static boolean consumedEntity(GameEngine gameEngine) {
        GameAction currentAction = gameEngine.getCurrentPlayer().getCurrentAction();
        Location storeroom = new Location("","");
        for(Map.Entry<Integer,Location> entry: gameEngine.getLocations().entrySet()){
            if(entry.getValue().getName().equalsIgnoreCase("storeroom")) {
                storeroom = entry.getValue();
            }
        }
        produceEntity(currentAction, gameEngine, storeroom);
        for(Integer key: currentAction.getConsumed().keySet()) {
            String consumed = currentAction.getConsumed().get(key);
            Artefact consumedInvArtefact = consumeInventory(consumed,gameEngine);
            Artefact consumedArtefact = consumeArtefact(consumed,gameEngine);
            Furniture consumedFurniture = consumeFurniture(consumed,gameEngine);
            Character consumedCharacter = consumeCharacter(consumed,gameEngine);

            if(consumed.equalsIgnoreCase("health")) {
                gameEngine.getCurrentPlayer().loseHealth();
                return true;
            }
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
                        //if a valid trigger phrase is found, then break
                        triggerMatched = true;
                        break;
                    }
                }
                if (triggerMatched) {
                    break;
                }
            }
            if (triggerMatched) {
                break;
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
            for(Map.Entry<Integer,Artefact> entry: getCurrentLocation(gameEngine).getArtefacts().entrySet()) {
                String artefactName = entry.getValue().getName();
                if(subjectName.equalsIgnoreCase(artefactName)) {
                    subjectMatched = true;
                    break;
                }
            }
            for(Map.Entry<Integer,Character> entry: getCurrentLocation(gameEngine).getCharacters().entrySet()) {
                String characterName = entry.getValue().getName();
                if(subjectName.equalsIgnoreCase(characterName)) {
                    subjectMatched = true;
                    break;
                }
            }
            for(Map.Entry<Integer,Furniture> entry: getCurrentLocation(gameEngine).getFurniture().entrySet()) {
                String furnitureName = entry.getValue().getName();
                if(subjectName.equalsIgnoreCase(furnitureName)) {
                    subjectMatched = true;
                    break;
                }
            }
            for(Map.Entry<Integer,Artefact> entry: gameEngine.getCurrentPlayer().getInventory().entrySet()) {
                String artefactName = entry.getValue().getName();
                if(subjectName.equalsIgnoreCase(artefactName)) {
                    subjectMatched = true;
                    break;
                }
            }
            if(!subjectMatched){return false; }
            allMatched = true;
        }
        return allMatched;
    }
}


