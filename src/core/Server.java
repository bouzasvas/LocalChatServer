/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vassilis
 */
public class Server {

    static int port;
    ServerSocket server = null;
    Socket client = null;

    static int userID = 1000; //TO-DO only 2-way communication
    List<ClientThread> clients = null;

    public Server(int port) {
        this.port = port;
        initServer();
    }

    private void initServer() {
        clients = new ArrayList<ClientThread>();
        try {
            server = new ServerSocket(port);
        } catch (IOException initEx) {
            System.err.println("Could not init server...");
        }
        while (true) {
            try {
                client = server.accept();
            } catch (IOException ex) {
                System.err.println("Client connection failed...");
            }
            System.out.println("User with IP: "+client.getInetAddress()+" is connecting...");
            ClientThread c = new ClientThread(userID++, client); //add userID
            clients.add(c);

            Thread clThread = new Thread(c);
            clThread.start();
        }
    }

    public void sendToAll(int clientID, String msg) {
        for (ClientThread client : clients) {
            if (client.clientID != clientID) {
                client.write(msg);
            }
        }
    }

    public class ClientThread implements Runnable {

        private int clientID;

        private Socket client = null;

        private ObjectInputStream in = null;
        private ObjectOutputStream out = null;

        public ClientThread(int userID, Socket client) {
            this.clientID = userID;
            this.client = client;

            try {
                this.out = new ObjectOutputStream(client.getOutputStream());
                this.in = new ObjectInputStream(client.getInputStream());
            } catch (IOException streamsEx) {
                System.err.println("Could not init streams :(");
            }

        }

        public void receiveMessage() {
            String message = null;
            while (true) {
                try {
                    message = (String) in.readObject();
                } catch (IOException | ClassNotFoundException ex) {
                    System.out.println("CLient with ID: "+clientID+" disconnected");
                    clients.remove(this);
                    break;
                }
                sendToAll(this.clientID, message);
            }
        }

        public void write(String msg) {
            try {
                out.writeObject(msg);
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void run() {
            receiveMessage();
        }

    }
}
