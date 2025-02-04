import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class ClientHandler implements Runnable{
    private static final Set<String> USERNAMES = new HashSet<>();
    private final Socket clientSocket;
    private String username;
    private PrintWriter clientOut;
    private BufferedReader clientIn;

    public ClientHandler(Socket clientSocket){
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
                if(message.equalsIgnoreCase("disconnect")){
                    break;
                }
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
                    Server.broadcastMessage(this, messageToSend);
                }
            }
            System.out.println(username+" has disconnected");
        } catch(IOException ioe){
            System.err.println(username+" has an error");
        } finally {
            Server.getClients().remove(this);
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

    public void sendMessage(String message){
        clientOut.println(message);
    }

    public void sendPrivateMessage(ClientHandler sender, String message, String receiver){
        boolean userFound = USERNAMES.contains(receiver);
        for(ClientHandler c : Server.getClients()){
            if(c.username.equals(receiver)){
                c.sendMessage(message);
            }
        }

        if(!userFound){
            sender.sendMessage("[Server]: User not found");
        }
    }
}