/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

import core.Server;
import client.Client;
import java.util.Scanner;

/**
 *
 * @author Vassilis
 */
public class ChatServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Server server = null;
        Client client = null;

        Scanner in = new Scanner(System.in);

        System.out.println("Enter 1 for Server or 2 for Client");
        System.out.print(">");

        int funct;
        
        do {
            funct = in.nextInt();
            
            if (funct == 1) {
                server = new Server(9999);
            } else if (funct == 2) {
                client = new Client(9999);
            } else {
                System.out.println("Select one of the above choices");
            }
        } while (funct > 2);
    }

}
