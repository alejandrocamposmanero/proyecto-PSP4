import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    private final static List<ClientHandler> CLIENTS = new ArrayList<>();
    private final static Set<String> USERNAMES = new HashSet<>();

    public static void main(String[] args) {
        try(ServerSocket serverSocket = new ServerSocket(22025)){
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

    private static void broadcast(ClientHandler sender, String message){
        CLIENTS.forEach((c) -> {
            if(c != sender){
                c.sendMessage(message);
            }
        });
    }

    private static class ClientHandler implements Runnable{
        private final Socket clientSocket;
        private String username;
        private PrintWriter clientOut;
        private BufferedReader clientIn;

        private ClientHandler(Socket clientSocket){
            this.clientSocket = clientSocket;
            try {
                clientOut = new PrintWriter(this.clientSocket.getOutputStream(), true);
                clientIn = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch(IOException ioe) {
                System.out.println("Error intializing client");
            }

        }

        @Override
        public void run() {
            try{
                // Asking the user for their username
                do {
                    clientOut.println("[Server] Write your username: ");
                    this.username = "@" + clientIn.readLine();
                } while(!USERNAMES.add(username));
                clientOut.println("You've logged correctly");
                System.out.println(username+" logged correctly");

                // Loop for receiving and sending the messages to the other users
                String message;
                while((message = clientIn.readLine()) != null) {
                    boolean privateMessage = message.startsWith("@");
                    if(privateMessage){
                        String receiver = message.substring(0, message.indexOf(" "));
                        String finalMessage = message.substring(message.indexOf(" ")+1);
                        System.out.println(username+" to "+receiver+": "+finalMessage);
                        String messageToSend = username+" to you: "+finalMessage;
                        sendPrivateMessage(this, messageToSend, receiver);
                    } else {
                        String messageToSend = username+": "+message;
                        System.out.println(messageToSend);
                        broadcast(this, messageToSend);
                    }
                }
                System.out.println(username+" has disconnected");
            } catch(IOException ioe){
                System.err.println(username+" has an error");
            } finally {
                CLIENTS.remove(this);
                USERNAMES.remove(username);
                try {
                    clientIn.close();
                    clientOut.close();
                    clientSocket.close();
                } catch(IOException ioe){
                    System.err.println("Error closing resources from "+username);
                }
            }
        }

        private void sendMessage(String message){
            clientOut.println(message);
        }

        private void sendPrivateMessage(ClientHandler sender, String message, String receiver){
            boolean userFound = USERNAMES.contains(receiver);
            for(ClientHandler c : CLIENTS){
                if(c.username.equals(receiver)){
                    c.sendMessage(message);
                }
            }

            if(!userFound){
                sender.sendMessage("[Server]: User not found");
            }
        }
    }
}
