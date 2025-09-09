package org.example;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    private static final int PORT = 6666;
    private static final String HOST = "127.0.0.1";

    // should be an actual path on your machine.
    private static final String clientDir = "ClientFiles";
    public static boolean fileReceived = false;

    public static void main(String[] args) {

        File dir = new File(clientDir);
        if (!dir.exists()) {
            dir.mkdir();
        }


        try (Socket socket = new Socket(HOST, PORT)) {
            BufferedReader clientReader = new BufferedReader(new InputStreamReader(System.in));
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter serverWriter = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Connected to server at: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());

            while (true) {

                // Outer I/O LOOP
                String fileName = "";
                String serverMessage = serverReader.readLine();
                System.out.println("Server: " + serverMessage);

                String clientMessage = clientReader.readLine();
                clientMessage.toUpperCase().trim();

                if (clientMessage.equals("/DOWNLOAD")) {
                    while (fileReceived != true){
                        // Inner I/O LOOP
                        serverWriter.println(clientMessage);
                        serverMessage = serverReader.readLine();

                        // Answer from Server
                        System.out.println("Server: " + serverMessage);


                        // FILE NAME
                        fileName = clientReader.readLine();
                        serverWriter.println(fileName);
                        serverMessage = serverReader.readLine();

                        if (serverMessage.contains("Download Is Ready")){
                            System.out.println("Server: " + serverMessage);
                            String acceptPrompt = clientReader.readLine();
                            serverWriter.println(acceptPrompt);

                            downloadFile(fileName, socket.getInputStream());
                            System.out.println("File [" + fileName + "] has been downloaded");
                            fileReceived = true;
                        }

                        serverWriter.println(clientMessage);
                    }
                    fileReceived = false;
                }



                // Finally sends the message to the server
                serverWriter.println(clientMessage);

            }


        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

// this works
    private static void downloadFile(String fileName, InputStream input) throws IOException {
        DataInputStream dis = new DataInputStream(input);

        File file = new File(clientDir + "/" + fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            dis.transferTo(fos);
        }

        System.out.println("File [" + fileName + "] has been downloaded");
    }

    /* //This shi also dont work
    private static void downloadFile(String fileName, InputStream input) throws IOException {
        File file = new File(clientDir + "/" + fileName);
        try (BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file))) {
            int bytesRead;
            byte[] buffer = new byte[1024];
            while ((bytesRead = input.read(buffer)) != -1) {

                output.write(buffer, 0, bytesRead);

            }

            output.flush();

            System.out.println("File [" + fileName + "] has been downloaded");

        }
    }
    */
}
