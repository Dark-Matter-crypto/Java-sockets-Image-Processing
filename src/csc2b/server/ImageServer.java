package csc2b.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ImageServer {
    private static ServerSocket ss;
    private static int port;
    private static boolean serverRunning = false;

    //Constructor
    public ImageServer(int port){
        this.port = port;

        System.out.println("Binding to port: " + port);
        try {
            ss = new ServerSocket(port);
            serverRunning = true;
            System.out.println("Waiting for connections. . .\n");
            runServer();
        }
        catch (IOException e) {
            System.err.println("Failed to bind to port: " + port);
        }
    }

    //Method to start running the server
    public static void runServer(){

        while(serverRunning){
            try {
                Socket clientConnection = ss.accept();  //Accepts a client connection
                ImageServerHandler serverHandler = new ImageServerHandler(clientConnection);
                Thread thread = new Thread(serverHandler);
                thread.start();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ImageServer serverConnection = new ImageServer(7455);
        serverConnection.runServer();
    }
}
