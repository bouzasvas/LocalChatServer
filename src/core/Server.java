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
            System.out.println("User with IP: " + client.getInetAddress() + " is connected");
            ClientThread c = new ClientThread(userID++, client); //add userID
            clients.add(c);

            Thread clThread = new Thread(c);
            clThread.start();
        }
    }

    public void sendToClient(int clientID, String msg) {
        for (ClientThread client : clients) {
            if (clientID == client.getClientID()) {
                client.write(msg);
            }
        }
    }

    public void sendToAll(int clientID, String msg) {
        for (ClientThread client : clients) {
            if (client.clientID != clientID) {
                client.write(msg);
            }
        }
    }

//    public void sendListOfClients() {
//        for (ClientThread client : clients) {
//            client.sendClients(availableClients());
//        }
//    }
    public String[] availableClients() {
        String[] avClients = new String[clients.size()];
        for (int client = 0; client < avClients.length; client++) {
            avClients[client] = String.valueOf(clients.get(client).getClientID());
        }
        return avClients;
    }

    public class ClientThread implements Runnable {

        private int clientID;
        private int destinationID;

        String[] onClients;

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
            boolean broadcast = (destinationID == -1);
            String message = null;
            while (true) {
                try {
                    message = (String) in.readObject();
                } catch (IOException | ClassNotFoundException ex) {
                    System.out.println("CLient with ID: " + clientID + " disconnected");
                    closeConnection();
                    clients.remove(this);
                    break;
                }
                if (broadcast) {
                    sendToAll(this.clientID, message);
                } else {
                    sendToClient(destinationID, message);
                }
            }
        }

        public void write(String msg) {
            try {
                out.writeObject(msg);
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public int getClientID() {
            return clientID;
        }

        public void onlineClients() {
            int ref;
            do {
                onClients = availableClients();
                try {
                    out.writeObject(onClients);
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
                ref = selectDestination();
            } while (ref == 0);
        }

        public int selectDestination() {
            int destIndex = -10;
            String destText = null;
            try {
                destText = (String) in.readObject();
            } catch (IOException ex) {
                System.out.println("CLient with ID: " + clientID + " disconnected");
                closeConnection();
                clients.remove(this);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
            if ((destText.equals("def")) && (destText!=null)) {
                    destinationID = -1;
                    return destinationID;
                } else {
                    destIndex = Integer.valueOf(destText);
                }

                if ((destIndex != 0) && (destIndex != -10)) {
                    this.destinationID = Integer.valueOf(onClients[destIndex - 1]);
                }
            return destIndex;
        }

        public void closeConnection() {
            try {
                in.close();
                out.close();
                client.close();
            } catch (IOException closeEx) {
                closeEx.printStackTrace();
            }
        }

        @Override
        public void run() {
            onlineClients();
            receiveMessage();
        }

    }
}
