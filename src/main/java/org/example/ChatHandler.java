package org.example;

import java.io.*;
import java.util.ArrayList;

public class ChatHandler extends Thread {

    public static ArrayList<String> chatHistory = new ArrayList<>();
    public static ArrayList<String> specificChatHistory = new ArrayList<>();

    public static String serverDIR;
    public static String chatDIR = "/ChatFiles";
    public static int latestSent;

    public ChatHandler(String serverDir) {
        super("chatHandler");
        serverDIR = serverDir;
    }

    @Override
    public void run() {
        // Create a history Dir inside the servers files
        String chatHistoryPath = serverDIR + chatDIR;
        File dir = new File(chatHistoryPath);
        if (!dir.exists()) {
            dir.mkdir();
        }

        System.out.println("chatHandler Booting");
        latestSent = chatHistory.size();


        while (true) {

            // Evt. Fremtidig logik (Fx periodisk broadcast)
            // Sleeper thread så den ikke looper
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
            }

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

    public synchronized void broadcastMessage(String message, PrintWriter out) {
        if (chatHistory.isEmpty()) return;
        String chatBroadcast = chatHistory.get(chatHistory.size() - 1);
        out.println(chatBroadcast);

    }

    public synchronized int indexOfChat() {
        return (chatHistory.size() - 1);
    }

    public synchronized String updateChat(int latestRead) {
        return chatHistory.get(latestRead + 1);

    }

    public synchronized boolean chatExists(String chatNameID) throws FileNotFoundException {
        File specificChat = new File(serverDIR + chatDIR + "/" + chatNameID);
        return specificChat.exists() && specificChat.isFile();
    }


    public synchronized String readChatByID(String chatNameID) throws FileNotFoundException {
        String messages;
        if (!chatExists(chatNameID)) {
            return null;
        } else {
            File specificChat = new File(serverDIR + chatDIR + "/" + chatNameID);
            BufferedReader reader = new BufferedReader(new FileReader(specificChat));

            messages = readChatFile(reader);
            return messages;
        }
    }

    // NOT DONE
    public synchronized void writeChatByID(String chatNameID, String message) throws IOException {
        if (!chatExists(chatNameID)) {
            throw new FileNotFoundException();
        } else {
            File specificChat = new File(serverDIR + chatDIR + "/" + chatNameID);
            BufferedWriter writer = new BufferedWriter(new FileWriter(specificChat));

            writer.write(message);



        }
    }

    public synchronized String readChatFile(BufferedReader reader) {
        StringBuilder stringBuilder = new StringBuilder();
        String line = "";

        // A fancy try-with ressources
        try (reader) {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return stringBuilder.toString();
    }




}
