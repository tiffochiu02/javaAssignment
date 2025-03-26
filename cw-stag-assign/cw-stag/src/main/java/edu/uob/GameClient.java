package edu.uob;

import java.io.*;
import java.lang.Character;
import java.net.Socket;
import java.net.Socket;
import java.util.regex.Pattern;

/**
* This is the sample client for you to connect to your game server.
*
* <p>Input are taken from stdin and output goes to stdout.
*/
public final class GameClient {

    private static final char END_OF_TRANSMISSION = 4;


    public static void main(String[] args) throws IOException {
        try (BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))){
            while (true) {
                // System.out.println("> ");
                if (args.length < 1) {
                    // System.out.println("> ");
                    System.out.println("Usage: java GameClient <player name>");
                }
                String fullCommand = consoleReader.readLine();

                if (fullCommand == null) {
                    System.out.println("Usage: java GameClient <username: command>");
                    break;
                }
                handleNextCommand(fullCommand);
            }
        } catch (IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
        }
    }
    private static void handleNextCommand(String fullCommand) throws IOException {
        String[] parts = fullCommand.split(":", 2);
        if (parts.length < 2 || parts[0].trim().isEmpty() || parts[1].trim().isEmpty()){
            System.out.println("Invalid format. Usage: java GameClient <player name>");
            return;
        }
        //Valid player names can consist of uppercase and lowercase letters, spaces, apostrophes and hyphens.
        // No other characters are valid and if they occur,
        // a suitable error message should be returned to the user
        if (!Pattern.matches("^[a-zA-Z\\s'\\-]+$", parts[0])) {
            System.out.println("Error: Invalid player name. Valid player names can consist of uppercase and lowercase letters, spaces, apostrophes and hyphens.");
            return;
        }
        // "username: command"
        String messageToSend = String.format("%s: %s", parts[0], parts[1]);
        try (var socket = new Socket("localhost", 8888);
             BufferedReader socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter socketWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
            socketWriter.write(messageToSend + "\n");
            socketWriter.flush();
            String incomingMessage = socketReader.readLine();
            if (incomingMessage == null) {
                throw new IOException("Server disconnected (end-of-stream)");
            }
            while (incomingMessage != null && !incomingMessage.contains("" + END_OF_TRANSMISSION + "")) {
                System.out.println(incomingMessage);
                incomingMessage = socketReader.readLine();
            }
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }
}
        //Valid player names can consist of uppercase and lowercase letters, spaces, apostrophes and hyphens.


//public final class GameClient {
//    private static final char END_OF_TRANSMISSION = 4;
//    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z '-]+$");
//
//    public static void main(String[] args) {
//        System.out.println("Welcome to the game!");
//        System.out.println("Enter commands in format: username: command");
//        System.out.println("Example: simon: look around");
//
//        try (BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))) {
//            while (true) {
//                System.out.print("> ");
//                String fullCommand = consoleReader.readLine();
//
//                if (fullCommand == null || fullCommand.equalsIgnoreCase("quit")) {
//                    System.out.println("Goodbye!");
//                    break;
//                }
//
//                // Validate command format
//                String[] parts = fullCommand.split(":", 2);
//                if (parts.length < 2 || parts[0].trim().isEmpty() || parts[1].trim().isEmpty()) {
//                    System.out.println("Invalid format. Use: username: command");
//                    continue;
//                }
//
//                // Validate username format
//                if (!USERNAME_PATTERN.matcher(parts[0].trim()).matches()) {
//                    System.out.println("Invalid username. Only letters, spaces, hyphens and apostrophes allowed.");
//                    continue;
//                }
//
//                // Create new connection for each command
//                try (Socket socket = new Socket("localhost", 8888);
//                     PrintWriter socketWriter = new PrintWriter(socket.getOutputStream(), true);
//                     BufferedReader socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
//
//                    // Send command
//                    socketWriter.println(fullCommand);
//
//                    // Read response
//                    String response;
//                    while ((response = socketReader.readLine()) != null) {
//                        if (response.indexOf(END_OF_TRANSMISSION) >= 0) {
//                            break; // End of transmission
//                        }
//                        System.out.println(response);
//                    }
//                } catch (IOException e) {
//                    System.err.println("Connection error: " + e.getMessage());
//                }
//            }
//        } catch (IOException e) {
//            System.err.println("Error reading input: " + e.getMessage());
//        }
//    }
//}
