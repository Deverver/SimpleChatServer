package org.example;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.List;


public class ServerClientHandler extends Thread {
    private final Socket socket;
    private final ChatHandler chatHandler;
    private final String serverDir;
    private final List<String> commandsList = List.of(
            "/EXIT",
            "/SEND",
            "/DOWNLOAD"
    );

    private String clientName;

    ServerClientHandler(Socket socket, ChatHandler chatHandler, String serverDir) {
        super("Client-" + socket.getRemoteSocketAddress());
        this.socket = socket;
        this.chatHandler = chatHandler;
        this.serverDir = serverDir;
    }


    @Override
    public void run() {
        try (
                // Raw I/O Data
                InputStream rawInput = socket.getInputStream();
                OutputStream rawOutput = socket.getOutputStream();

                //
                BufferedReader reader = new BufferedReader(new InputStreamReader(rawInput));
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(rawOutput), true)


        ) {
            int latestRead = chatHandler.indexOfChat();


            // Defines ServerMessage to Client
            String toClient;
            toClient = ("Welcome to the chat service! Please enter your username");

            // After sending an inout request to the client, we wait for the response.
            writer.println(toClient);
            clientName = reader.readLine();

            System.out.println("User [" + clientName + "] has connected to the server");

            // Sending commandsList to Client
            // Now utilising String.join, instead of doing manual appends
            toClient = "You can now send Commands! Commands Available: " + String.join(", ", commandsList);
            writer.println(toClient);

            /*
            Has to be a command to see chat history
            writer.println(chatHandler.getChatHistory());
            */

            // Ensures that the client reader is not null and is trimmed
            String rawClientInput;
            while ((rawClientInput = reader.readLine()) != null) {
                String clientInput = rawClientInput.trim();

                // Makes sure that the reader is not empty and less than 255 characters
                // Now features the shorter more correct if-statement in loop
                if (clientInput.isEmpty() || clientInput.length() >= 255) {
                    writer.println("Error: " + rawClientInput + " is not a valid command");
                    continue;
                }

                // Parses the client input uppercase to ensure uniform command prompts
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
                            toClient = "Ready to receive message";
                            writer.println(toClient);
                            String clientMessage = reader.readLine();

                            chatHandler.receiveMessage(clientName + ": " + clientMessage + " send at: " + LocalDateTime.now());


                            writer.println(chatHandler.updateChat(latestRead));
                            latestRead = chatHandler.indexOfChat();

                        } catch (IOException ignore) {
                        }
                        break;
                    case ("/DOWNLOAD"):
                        System.out.println("you got here");
                        try {
                            // Asks for file name
                            toClient = "Enter the name of the file you wish to download: ";
                            writer.println(toClient);

                            // Gets user input
                            String clientMessage = reader.readLine();

                            // Sets fileName to equal clientMessage
                            String fileName = clientMessage;

                            File file = new File(serverDir + "/" + fileName);

                            if (file.exists()) {
                                System.out.println("Client [" + clientName + "] has requested file: " + fileName);

                                writer.println("Download Is Ready" + " do you want to download?" + "[yes/no]");
                                clientMessage = reader.readLine();

                                if (clientMessage.equals("yes")) {
                                    sendFile(file, rawOutput);
                                    System.out.println("File [" + fileName + "] has been sent");
                                    writer.println("File [" + fileName + "] has been sent");
                                } else if (clientMessage.equals("no")) {
                                    writer.println("Download Cancelled");
                                }

                            } else {
                                writer.println("Error: File not found [" + fileName + "] does not exist");
                                System.out.println("Error: File not found [" + fileName + "] does not exist");
                            }

                        } catch (IOException ignore) {
                        }
                        break;
                    default:
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

    /**
     * For some god-forsaken reason using the standard BufferedOutputStream with byte[], -
     * - does not read the actual content of the files we are trying to download
     * <p>
     * However newer versions of Java allows DataOutputStreams to "transferTo()" which does exactly what we need,
     * We can simply pass data from the read stream to the output stream with no data loss.
     **/

    private void sendFile(File file, OutputStream out) throws IOException {
        if (file.exists()) {
            DataOutputStream dos = new DataOutputStream(out);
            try (FileInputStream fis = new FileInputStream(file)) {
                dos.writeLong(file.length());
                fis.transferTo(dos);
                dos.flush();
            }
        }
    }


}
