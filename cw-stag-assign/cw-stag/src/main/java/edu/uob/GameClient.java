package edu.uob;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * This is the sample client for you to connect to your game server.
 *
 * <p>Input are taken from stdin and output goes to stdout.
 */
public final class GameClient {

    private static final char END_OF_TRANSMISSION = 4;

    public static void main(String[] args) throws IOException {
        String username = args[0];
        while (!Thread.interrupted()) handleNextCommand(username);
    }

    private static void handleNextCommand(String username) throws IOException {
        System.out.print(username + ":> ");
        BufferedReader commandLine = new BufferedReader(new InputStreamReader(System.in));
        String command = commandLine.readLine();
        try (var socket = new Socket("localhost", 8888);
             var socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             var socketWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
            socketWriter.write(username + ": " + command + "\n");
            socketWriter.flush();
            String incomingMessage = socketReader.readLine();
            if (incomingMessage == null) {
                throw new IOException("Server disconnected (end-of-stream)");
            }
            while (incomingMessage != null && !incomingMessage.contains("" + END_OF_TRANSMISSION + "")) {
                System.out.println(incomingMessage);
                incomingMessage = socketReader.readLine();
            }
        }
    }
}


//public final class GameClient {
//
//    private static final char END_OF_TRANSMISSION = 4;
//
//    public static void main(String[] args) throws IOException {
//        try (BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))){
//            while (true) {
//                // System.out.println("> ");
//                if (args.length < 1) {
//                    // System.out.println("> ");
//                    System.out.println("Usage: java GameClient <player name>");
//                }
//                String fullCommand = consoleReader.readLine();
//
//                if (fullCommand == null) {
//                    System.out.println("Usage: java GameClient <username: command>");
//                    break;
//                }
//                handleNextCommand(fullCommand);
//            }
//        } catch (IOException e) {
//            System.err.println("Error reading input: " + e.getMessage());
//        }
//    }
//    private static void handleNextCommand(String fullCommand) throws IOException {
//        String[] parts = fullCommand.split(":", 2);
//        if (parts.length < 2 || parts[0].trim().isEmpty() || parts[1].trim().isEmpty()){
//            System.out.println("Invalid format. Usage: java GameClient <player name>");
//            return;
//        }
//        //Valid player names can consist of uppercase and lowercase letters, spaces, apostrophes and hyphens.
//        // No other characters are valid and if they occur,
//        // a suitable error message should be returned to the user
//        if (!Pattern.matches("^[a-zA-Z\\s'\\-]+$", parts[0])) {
//            System.out.println("Error: Invalid player name. Valid player names can consist of uppercase and lowercase letters, spaces, apostrophes and hyphens.");
//            return;
//        }
//        // "username: command"
//        String messageToSend = String.format("%s: %s", parts[0], parts[1]);
//        try (var socket = new Socket("localhost", 8888);
//             BufferedReader socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//             BufferedWriter socketWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
//            socketWriter.write(messageToSend + "\n");
//            socketWriter.flush();
//            String incomingMessage = socketReader.readLine();
//            if (incomingMessage == null) {
//                throw new IOException("Server disconnected (end-of-stream)");
//            }
//            while (incomingMessage != null && !incomingMessage.contains("" + END_OF_TRANSMISSION + "")) {
//                System.out.println(incomingMessage);
//                incomingMessage = socketReader.readLine();
//            }
//        } catch (IOException e) {
//            System.err.println("Connection error: " + e.getMessage());
//        }
//    }
//}
//        //Valid player names can consist of uppercase and lowercase letters, spaces, apostrophes and hyphens.
