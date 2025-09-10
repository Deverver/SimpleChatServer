package org.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class ChatHandler extends Thread {

    public static ArrayList<String> chatHistory = new ArrayList<>();
    public static int latestSent;

    public ChatHandler(){
        super("chatHandler");
    }

    @Override
    public void run(){
        System.out.println("chatHandler Booting");
        latestSent = chatHistory.size();


        while (true){

            // evt. fremtidig logik (fx periodisk broadcast)
            // Sleeper thread så den ikke looper
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {}

        }
    }



    public synchronized void receiveMessage(String message) throws IOException {
        chatHistory.add(message);
    }

    public String getChatHistory() {
        StringBuilder stringBuilder = new StringBuilder();

        for (String chatMessage : chatHistory) {
            stringBuilder.append(chatMessage).append("\n");
        }
        return stringBuilder.toString();
    }

    public synchronized void broadcastMessage(String message, PrintWriter out){
        if (chatHistory.isEmpty()) return;
        String chatBroadcast = chatHistory.get(chatHistory.size() - 1);
        out.println(chatBroadcast);

    }

    public synchronized int indexOfChat(){
        return (chatHistory.size() - 1);
    }

    public synchronized String updateChat(int latestRead){
        return chatHistory.get(latestRead + 1);

    }




}
