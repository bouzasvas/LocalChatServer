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
    }

    public void newMessage() {
        System.out.print("Your message:");
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
            e.printStackTrace(System.err);
        }
    }

    public void commProc() {
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
