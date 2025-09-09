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




        }
    }



    public void receiveMessage(String message) throws IOException {
        chatHistory.add(message);
    }

    public String getChatHistory() {
        String historyMessages = "";
        for (String msg : chatHistory) {
            historyMessages += msg + "\n";
        }
        return historyMessages;
    }

    public void broadcastMessage(String message, PrintWriter out){
        String broadcastMsg = "";
        broadcastMsg = chatHistory.getLast();
        out.println(broadcastMsg);

    }
    public int indexOfChat(){
        return chatHistory.lastIndexOf(chatHistory);
    }

    public String updateChat(int latestRead){
        return chatHistory.get(latestRead + 1);

    }




}
