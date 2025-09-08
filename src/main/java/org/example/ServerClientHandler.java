package org.example;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.List;


public class ServerClientHandler extends Thread {
    private final Socket socket;
    private final ChatHandler chatHandler;
    private final List<String> commandsList = List.of(
            "/EXIT",
            "/SEND"
    );

    private String clientName;

    ServerClientHandler(Socket socket, ChatHandler chatHandler) {
        super("Client-" + socket.getRemoteSocketAddress());
        this.socket = socket;
        this.chatHandler = chatHandler;
    }


    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)
        ) {
            out.println("Welcome to the chat service!");
            out.println(chatHandler.getChatHistory());
            int latestRead = chatHandler.indexOfChat();
            out.println("Please enter username: ");
            clientName = in.readLine();

            out.println("User [" + clientName + "] has connected to the server");
            out.println("You can now send Commands");
            for (String command : commandsList) {
                out.println("Command: "+ command);
            }

                String rawClientInput;
                while ((rawClientInput = in.readLine()) != null) {
                    String clientInput = rawClientInput.toUpperCase().trim();

                    if (clientInput.length() > 0 && clientInput.length() < 255) {

                        switch (clientInput) {
                            case ("/EXIT"):
                                try {
                                    System.out.println("Hello");
                                    socket.close();
                                } catch (IOException ignore) {
                                }
                                break;

                            case ("/HELP"):
                                try {
                                    out.println(commandsList.toString());
                                } catch (Exception ignore) {
                                }
                                break;

                            case ("/SEND"):
                                try {
                                    out.println("Ready to receive message");
                                    String clientMessage = in.readLine();
                                    chatHandler.receiveMessage(clientName + ": " + clientMessage + "\n" + "send at: " + LocalDateTime.now());
                                    out.println(chatHandler.updateChat(latestRead));
                                    latestRead = chatHandler.indexOfChat();

                                } catch (IOException ignore) {
                                }
                                break;
                            default:
                                out.println("Error: " + rawClientInput + " is not a valid command");

                        }
                    } else {
                        out.println("Error: " + rawClientInput + " is not a valid command");
                    }
                }

        } catch (IOException ex) {
            System.out.println("Error connection has been closed: " + ex.getMessage());
        } finally {
            try {
                socket.close();
                System.out.println("Error: socket closed");
            } catch (IOException ignore) {}
        }

    }

    private void sendMessage(String message, ChatHandler chatHandler) {
    }
}
