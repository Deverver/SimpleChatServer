package org.example;

import org.w3c.dom.ls.LSOutput;

import java.io.IOException;
import java.net.*;

public class ChatServer {
    private final static int PORT = 6666;

    public static void main(String[] args) {

        System.out.println("[Server Booting]");
        try(ServerSocket serverSocket = new ServerSocket(PORT);

        ){

            ChatHandler chatHandler = new ChatHandler();
            try {
                chatHandler.start();
            } catch (Exception e){
                e.printStackTrace();
            }


            while(true){
                Socket socket = serverSocket.accept();
                System.out.println("New client connected at: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
                ServerClientHandler serverClientHandler = new ServerClientHandler(socket, chatHandler);
                serverClientHandler.start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e1){
            System.out.println(e1.getMessage());
        }

    }




}