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
            "/SEND",
            "/DOWNLOAD"
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
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // We have to separate our OutputStream and our PrintWriter
                // Since raw data "bytes" cannot be sent via the Writer
                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(output), true)
        ) {
            writer.println("Welcome to the chat service!");
            writer.println(chatHandler.getChatHistory());

            int latestRead = chatHandler.indexOfChat();

            writer.println("Please enter username: ");
            clientName = input.readLine();

            writer.println("User [" + clientName + "] has connected to the server");
            writer.println("You can now send Commands");

            for (String command : commandsList) {
                writer.println("Command: " + command);
            }

            String rawClientInput;
            while ((rawClientInput = input.readLine()) != null) {
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
                                writer.println(commandsList.toString());
                            } catch (Exception ignore) {
                            }
                            break;

                        case ("/SEND"):
                            try {
                                writer.println("Ready to receive message");
                                String clientMessage = input.readLine();
                                chatHandler.receiveMessage(clientName + ": " + clientMessage + "\n" + "send at: " + LocalDateTime.now());
                                writer.println(chatHandler.updateChat(latestRead));
                                latestRead = chatHandler.indexOfChat();

                            } catch (IOException ignore) {
                            }
                            break;
                        case ("/DOWNLOAD"):
                            try {
                                writer.println("Enter the name of the file you wish to download: ");
                                String clientMessage = input.readLine();

                                System.out.println("Client [" + clientName + "] has requested file: " + clientMessage);
                                sendFile(clientMessage, output);

                            } catch (IOException ignore) {
                            }
                            break;
                        default:
                            writer.println("Error: " + rawClientInput + " is not a valid command");

                    }
                } else {
                    writer.println("Error: " + rawClientInput + " is not a valid command");
                }
            }

        } catch (IOException ex) {
            System.out.println("Error connection has been closed: " + ex.getMessage());
        } finally {
            try {
                socket.close();
                System.out.println("Error: socket closed");
            } catch (IOException ignore) {
            }
        }

    }

    private void sendFile(String fileName, OutputStream out) throws IOException {
        File file = new File("ServerFiles/" + fileName);

        if (file.exists()) {
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = bis.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
                System.out.println("File [" + fileName + "] has been sent to the client");
            }

        } else {
            PrintWriter writer = new PrintWriter(out, true);
            writer.println("Error: File not found [" + fileName + "] does not exist");
            System.out.println("Error: File not found [" + fileName + "] does not exist");
        }
    }
}
