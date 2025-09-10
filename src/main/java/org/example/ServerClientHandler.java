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

                BufferedReader reader = new BufferedReader(new InputStreamReader(rawInput));
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(rawOutput), true)


        ) {
            int latestRead = chatHandler.indexOfChat();

            // After sending an inout request to the client, we wait for the response.
            writer.println("Welcome to the chat service! Please enter your username");
            clientName = reader.readLine();

            System.out.println("User [" + clientName + "] has connected to the server");

            // Sending commandsList to Client; Now utilizing String.join, instead of doing manual appends
            writer.println("You can now send Commands! Commands Available: " + String.join(", ", commandsList));

            /*
            Has to be a command to see chat history
            writer.println(chatHandler.getChatHistory());
            */

            // Ensures that the client reader is not null and is trimmed
            String rawClientInput;
            while ((rawClientInput = reader.readLine()) != null) {
                String clientInput = rawClientInput.trim();

                // Makes sure that the reader is not empty and less than 255 characters
                // Now features more correct if-statement in loop
                if (clientInput.isEmpty() || clientInput.length() >= 255) {
                    writer.println("Error: " + rawClientInput + " is not a valid command");
                    continue;
                }

                // Parses the client input uppercase to ensure uniform command prompts
                switch (clientInput.toUpperCase()) {

                    case ("/EXIT"): {
                        try { socket.close();
                        } catch (IOException ignore) {}
                        return;
                    } // Exit End

                    case ("/HELP"): {
                        writer.println("Available Commands: " + String.join(", ", commandsList));
                        break;
                    } // Help End

                    case ("/SEND"): {
                        writer.println("Ready to receive message");
                        String userChatMessage = "";
                        userChatMessage = reader.readLine();


                        if (userChatMessage == null) break;

                        chatHandler.receiveMessage(clientName + ": " + userChatMessage + " sent at: " + LocalDateTime.now());

                        String updated = chatHandler.updateChat(latestRead);
                        writer.println(updated);
                        latestRead++;
                        System.out.println(latestRead);
                        break;
                    } // Send End

                    case ("/DOWNLOAD"): {
                        // Asks for file name and awaits response
                        writer.println("Enter the name of the file you wish to download: ");
                        String fileName = reader.readLine();

                        // Checks if fileName is valid
                        if (fileName == null) break;

                        // Creates a file object from path
                        File file = new File(serverDir + "/" + fileName);

                        // Checks if file exists and actually is a file
                        if (!file.exists() || !file.isFile()) {
                            writer.println("ERROR File not found: " + fileName);
                            break;
                        }

                        // Asks for confirmation, if file does exist
                        writer.println("Download Is Ready do you want to download? [yes/no]");
                        String accept = reader.readLine();

                        // Handles responses from the client
                        if (!"yes".equalsIgnoreCase(accept)) {
                            writer.println("Download Cancelled");
                            break;
                        }

                        // Read file-length and sends it to the client
                        long length = file.length();
                        writer.println("LENGTH " + length);

                        // Client handles Errors in case Length is not a long primitive, Server awaits a confirmation reply
                        String ok = reader.readLine();
                        if (!"OK".equalsIgnoreCase(ok)) {
                            writer.println("Download Cancelled");
                            break;
                        }

                        // Starts the new File Transfer methods with the precise length as a parameter
                        sendFileExactly(file, rawOutput, length);

                        // Client awaits confirmation of file transfer to ensure that no other data is being sent in the meantime.
                        writer.println("DONE");
                        System.out.println("File [" + fileName + "] has been sent");
                        break;

                    } // Download End

                    default:
                        writer.println("Error: " + rawClientInput + " is not a valid command");
                }
            }

        } catch (IOException ex) {
            System.out.println("Error connection has been closed: " + ex.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ignore) {
            }
            System.out.println("socket closed: " + getName());
        }
    } // RUN END


    private void sendFileExactly(File file, OutputStream out, long length) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            long sent = 0;
            while (sent < length) {
                int toRead = (int) Math.min(buffer.length, length - sent);
                int read = fis.read(buffer, 0, toRead);
                if (read == -1) throw new EOFException("unexpected EOF while reading server file");
                out.write(buffer, 0, read);
                sent += read;
            }
            out.flush();
        }
    }


} // CLASS END