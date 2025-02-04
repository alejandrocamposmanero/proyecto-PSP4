import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    private final static List<ClientHandler> CLIENTS = new ArrayList<>();

    public static void main(String[] args) {
        try(ServerSocket serverSocket = new ServerSocket(22025)){
            System.out.println("Server started");
            while(true){
                try {
                    Socket client = serverSocket.accept();

                    /*
                    String message = clientIn.readLine();
                    System.out.println("@c1: "+message);
                    clientOut.println(message.toUpperCase());

                    Code to prove that the client and the server communicate between each other
                    */

                    ClientHandler clientSocket = new ClientHandler(client);
                    CLIENTS.add(clientSocket);
                    new Thread(clientSocket).start();
                } catch(IOException ioex) {
                    System.out.println("Error with the client");
                }
            }
        } catch(IOException ioe){
            System.out.println("Error with the server");
        }
    }

    public static void broadcastMessage(ClientHandler sender, String message){
        CLIENTS.forEach((c) -> {
            if(c != sender){
                c.sendMessage(message);
            }
        });
    }

    public static List<ClientHandler> getClients(){
        return CLIENTS;
    }
}
