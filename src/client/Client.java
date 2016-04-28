/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Vassilis
 */
package client;

import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {

    int port;
    int dest = 0;

    Scanner input = new Scanner(System.in);

    Socket connection = null;
    ObjectInputStream in = null;
    ObjectOutputStream out = null;

    public Client(int port) {
        this.port = port;
        initConnection();
        commProc();
    }

    private void initConnection() {
        try {
            connection = new Socket(InetAddress.getByName("localhost"), port);
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
        } catch (IOException initClientEx) {
            initClientEx.printStackTrace(System.err);
        }
        System.out.println("Successfully connected to: " + connection.getInetAddress());
    }

    public void newMessage() {
        System.out.println("Your message:");
        System.out.print(">");
        String msg = input.nextLine();
        sendMessage(msg);
    }

    public void getMessage() {
        String msg = null;

        try {
            msg = (String) in.readObject();
            System.out.println("Message from other client: " + msg);
        } catch (IOException | ClassNotFoundException ex) {
            System.out.println("Server disconnected");
            System.exit(1);
        }
    }

    public void sendMessage(String msg) {
        try {
            out.writeObject(msg);
        } catch (IOException e) {
            System.out.println("Server disconnected");
            System.exit(1);
        }
    }

    public void chooseClient() {
        String CL = null;
        do {
            String[] availableClients = null;
            try {
                availableClients = (String[]) in.readObject();
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("\nThe online clients right now are: ");
            for (int cl = 0; cl < availableClients.length; cl++) {
                System.out.println((cl + 1) + ". " + availableClients[cl]);
            }
            System.out.println("Type the number you wish to talk to or type def for broadcast messages"); 
            System.out.println("You can also type 0 to refresh the list! ");
            CL = input.nextLine();
            try {
                out.writeObject(CL);
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (CL.equals("0"));
    }

    public void commProc() {
        if (dest == 0) {
            System.out.println("You should choose a client to talk to:");
            chooseClient();
        }

        Runnable newM = new Runnable() {

            @Override
            public void run() {
                while (true) {
                    newMessage();
                }
            }
        };

        Runnable getM = new Runnable() {

            @Override
            public void run() {
                while (true) {
                    getMessage();
                }
            }
        };

        Thread Nmes = new Thread(newM);
        Nmes.start();

        Thread gMes = new Thread(getM);
        gMes.start();
    }
}
