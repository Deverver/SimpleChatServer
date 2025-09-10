package org.example;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    private static final int PORT = 6666;
    private static final String HOST = "127.0.0.1";
    // should be an actual path on your machine.
    private static final String clientDir = "ClientFiles";

    public static void main(String[] args) {

        File dir = new File(clientDir);
        if (!dir.exists()) {
            dir.mkdir();
        }


        try (Socket socket = new Socket(HOST, PORT)) {
            InputStream rawIn = socket.getInputStream();
            OutputStream rawOut = socket.getOutputStream();

            BufferedReader clientReader = new BufferedReader(new InputStreamReader(System.in));
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter serverWriter = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Connected to server at: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());

            // After the connection has been established, and I/O has been set up.
            // We can also start receiving and sending messages from the server.

            // Welcome message from server
            String serverMessage = serverReader.readLine(); // Read from Server
            System.out.println("Server: " + serverMessage); // Show to User

            // Server asked for a username
            String username = clientReader.readLine();      // Read User input
            serverWriter.println(username);                 // Send Data to Server

            // Server sends a command list to the client
            serverMessage = serverReader.readLine();        // Read from Server
            System.out.println("Server: " + serverMessage); // Show to User Etc. etc. etc...

            // The While loop is made not only to keep the client running, but also to listen for commands
            while (true) {

                // Starts each loop by prompting the user for input; Sidenote, this is a great indicator for if the loop has restarted
                System.out.print("> ");
                String clientInput = clientReader.readLine();

                // Checks if input is valid, if so, parses the input to uppercase and trims it
                if (clientInput == null) break;
                clientInput = clientInput.toUpperCase().trim();

                ///  This marks from where prompts can be made
                // This project has refreshed a lot of our knowledge lost over the summer.
                // Having user input be separate from the actual command prompts sent to the server is preferable for obvious reasons.

                if ("/EXIT".equals(clientInput)) {
                    serverWriter.println("/EXIT");

                    serverMessage = serverReader.readLine();
                    System.out.println("Server: " + serverMessage);
                }

                if ("/HELP".equals(clientInput)) {
                    serverWriter.println("/HELP");

                    serverMessage = serverReader.readLine();
                    System.out.println("Server: " + serverMessage);
                }

                if ("/SEND".equals(clientInput)) {
                    serverWriter.println("/SEND");

                    serverMessage = serverReader.readLine();
                    System.out.println("Server: " + serverMessage);

                    String userMessage = clientReader.readLine();
                    serverWriter.println(userMessage);

                    // Echo response from Server
                    serverMessage = serverReader.readLine();
                    System.out.println("Server: " + serverMessage);
                }

                if ("/DOWNLOAD".equals(clientInput)) {
                    serverWriter.println("/DOWNLOAD");

                    // Server: Asks for file name
                    serverMessage = serverReader.readLine();
                    System.out.println("Server: " + serverMessage);

                    // Saves file name for later use and sends it to the server.
                    // This operation sends a confirmation response, which we await.
                    String fileName = clientReader.readLine();
                    serverWriter.println(fileName);

                    // Checks response for error cases, the user does not see this
                    serverMessage = serverReader.readLine();
                    if (serverMessage != null && serverMessage.startsWith("ERROR")) {
                        continue;
                    }

                    // User is prompted to accept the download [yes/no]
                    System.out.println("Server: " + serverMessage);
                    String acceptPrompt = clientReader.readLine();
                    serverWriter.println(acceptPrompt);

                    // In case the user wrote "no", or anything else, we handle the response
                    if (!"yes".equalsIgnoreCase(acceptPrompt)) {
                        String cancelled = serverReader.readLine();
                        System.out.println("Server: " + cancelled);
                        continue;
                    }

                    // Our newest send/receive file methods require a Long length parameter, to ensure that -
                    // the send data can mark a defined EOF rather than just the -1 return val
                    String fileLengthResponse = serverReader.readLine();

                    // Ensures that the response is proper
                    // Splits response and parses the value as a Long primitive
                    if (fileLengthResponse == null || !fileLengthResponse.startsWith("LENGTH ")) {
                        System.out.println("Server: unexpected header: " + fileLengthResponse);
                        continue;
                    }

                    long length = Long.parseLong(fileLengthResponse.split(" ")[1]);

                    // Server is waiting for a response before it can start the file transfer
                    serverWriter.println("OK");

                    // Await data from the server
                    receiveFileExactly(fileName, rawIn, length);

                    // To ensure that only the file data is being transferred, we have to wait for a response
                    String fileTransferCompleted = serverReader.readLine();

                    // Show user Server response
                    System.out.println("Server: " + fileTransferCompleted);
                    System.out.println("File [" + fileName + "] has been downloaded");

                } // Download Command

                /*
                // This should not be necessary as the loop starts with a prompt for input >...
                // If any command cases are not met, then send input to the server and await responsea
                serverWriter.println(clientInput);
                String serverResponse = serverReader.readLine();

                // Checks if server has closed the connection, if so, break the loop
                if (serverResponse == null) {
                    System.out.println("Server closed the connection.");
                    break;
                }

                // Finally show server response
                System.out.println("Server: " + serverResponse);
                */

            } // While loop
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    } // MAIN END

    private static void receiveFileExactly(String fileName, InputStream rawIn, long length) throws IOException {
        File file = new File(clientDir, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[8192];
            long received = 0;
            while (received < length) {
                int toRead = (int) Math.min(buffer.length, length - received);
                int read = rawIn.read(buffer, 0, toRead);
                if (read == -1) throw new EOFException("unexpected EOF while receiving file");
                fos.write(buffer, 0, read);
                received += read;
            }
            fos.flush();
        }
    }


} // CLASS END
