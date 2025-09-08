package org.example;

import org.w3c.dom.ls.LSOutput;

import java.io.File;
import java.io.IOException;
import java.net.*;

public class ChatServer {
    private final static int PORT = 6666;

    public static void main(String[] args) {
        System.out.println("[Server Booting]");

        try (ServerSocket serverSocket = new ServerSocket(PORT)
        ) {
            System.out.println("Server listening on port " + PORT);

            // Creates a chatHandler for the server.
            ChatHandler chatHandler = new ChatHandler();
            chatHandler.start();
            System.out.println("[Chat service started]");

            // Makes a File on the Server, ensures that the file does exist.
            File dir = new File("ServerFiles");
            if (!dir.exists()){
                dir.mkdir();
            }


            while (true) {
                // Accept clients connection.
                Socket socket = serverSocket.accept();
                System.out.println("New client connected at: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());

                // Handles the connection and creates threads with access to the chatHandler
                ServerClientHandler serverClientHandler = new ServerClientHandler(socket, chatHandler);
                serverClientHandler.start();
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e1) {
            System.out.println(e1.getMessage());
        }

    }


}