package edu.uob;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public final class GameServer {

    private static final char END_OF_TRANSMISSION = 4;
    //private static HashMap<Integer,Player> players;
    private static ActionsFileReader newActionHandler;
    private static EntityFileReader newEntitiesHandler;
    private static HashMap<Integer, Location> newLocations;
    private static LinkedList<GameAction> newGameActions;
    private GameEngine engine;


    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        File entitiesFile = Paths.get("config" + File.separator + "basic-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "basic-actions.xml").toAbsolutePath().toFile();
        GameServer server = new GameServer(entitiesFile, actionsFile);
        server.blockingListenOn(8888);
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * Instanciates a new server instance, specifying a game with some configuration files
    *
    * @param entitiesFile The game configuration file containing all game entities to use in your game
    * @param actionsFile The game configuration file containing all game actions to use in your game
    */
    public GameServer(File entitiesFile, File actionsFile) {
        // TODO implement your server logic here
        //GameEngine engine = new GameEngine();
        this.newActionHandler = new ActionsFileReader();
        this.newEntitiesHandler = new EntityFileReader();
        this.newActionHandler.ActionsHandling(actionsFile);
        this.newEntitiesHandler.EntityHandling(entitiesFile);
        this.newLocations = this.newEntitiesHandler.getLocations();
        this.newGameActions = this.newActionHandler.getActions();
        this.engine = new GameEngine();
        engine.setGameActions(this.newGameActions);
        engine.setLocations(this.newLocations);
        //this.players = new HashMap<>();

    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * This method handles all incoming game commands and carries out the corresponding actions.</p>
    *
    * @param command The incoming command to be processed
    */
    public String handleCommand(String command) {
        // TODO implement your server logic here
        //GameEngine engine = new GameEngine();
        String playerName = "";
        String query = "";
        for(int i = 0; i < command.length(); i++) {
            if(command.charAt(i) == ':'){
                playerName = command.substring(0,i);
                query = command.substring(i+1);
            }
        }
        if(playerName.isEmpty()){
            return "No player found.";
        }
        if(query.isEmpty()){
            return "No action found.";
        }
        System.out.println("Player name = " + playerName);
        setCurrentPlayer(playerName);
//        this.engine.setGameActions(this.newGameActions);
//        this.engine.setLocations(this.newLocations);

        LinkedList<String> parsedQuery = Tokenizer.setup(query);
        GameCommandsHandling parser = new GameCommandsHandling();
        return parser.allCommandsHandling(parsedQuery,this.engine);
    }
    public boolean checkUniquePlayers(String newPlayerName) {
        System.out.println("engine.getPlayers()" + this.engine.getPlayers().size());
        for(Map.Entry<Integer, Player> player: this.engine.getPlayers().entrySet()) {
            System.out.println("newPlayerName" + newPlayerName + "playerName: "+ player.getValue().getName());
            if(newPlayerName.equalsIgnoreCase(player.getValue().getName())) {
                return false; //the player already exists
            }
        }
        return true;
    }

    public void setCurrentPlayer(String name) {
        if(name == null || name.isEmpty() || this.engine == null) {
            return;
        }
        if(!checkUniquePlayers(name)){
           for(Map.Entry<Integer, Player> player: this.engine.getPlayers().entrySet()) {
               if(name.equalsIgnoreCase(player.getValue().getName())) {
                   System.out.println("Player already exists. Not unique player");
                   this.engine.setCurrentPlayer(player.getValue());
                   return;
               }
           }
        }
        System.out.println("Unique player");
        Player currentPlayer = new Player(name,this.engine);
        currentPlayer.setCurrentLocation(this.engine.getLocations().get(0));
        this.engine.addPlayer(currentPlayer);
        this.engine.setCurrentPlayer(currentPlayer);
    }


    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * Starts a *blocking* socket server listening for new connections.
    *
    * @param portNumber The port to listen on.
    * @throws IOException If any IO related operation fails.
    */
    public void blockingListenOn(int portNumber) throws IOException {
        try (ServerSocket s = new ServerSocket(portNumber)) {
            System.out.println("Server listening on port " + portNumber);
            while (!Thread.interrupted()) {
                try {
                    blockingHandleConnection(s);
                } catch (IOException e) {
                    System.out.println("Connection closed");
                }
            }
        }
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * Handles an incoming connection from the socket server.
    *
    * @param serverSocket The client socket to read/write from.
    * @throws IOException If any IO related operation fails.
    */
    private void blockingHandleConnection(ServerSocket serverSocket) throws IOException {
        try (Socket s = serverSocket.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {
            System.out.println("Connection established");
            String incomingCommand = reader.readLine();
            if(incomingCommand != null) {
                System.out.println("Received message from " + incomingCommand);
                String result = handleCommand(incomingCommand);
                writer.write(result);
                writer.write("\n" + END_OF_TRANSMISSION + "\n");
                writer.flush();
            }
        }
    }
}
