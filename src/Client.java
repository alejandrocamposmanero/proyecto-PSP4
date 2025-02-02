import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try(Socket socket = new Socket("localhost", 22025)) {
            PrintWriter bufferOut = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader bufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner sc = new Scanner(System.in);

            /*
            while(true){
                bufferOut.println(sc.nextLine());
                System.out.println("Response: "+ bufferIn.readLine());
            }

            Code to prove that the client and the server can communicate between each other
             */

            // Reading thread
            new Thread(() -> {
                try {
                    String message;
                    while((message = bufferIn.readLine()) != null) {
                        System.out.println(message);
                    }
                } catch(IOException ioe) {
                    System.out.println("Error reading messages");
                }
            }).start();

            // Writing messages
            String message;
            while((message = sc.nextLine()) != null) {
                bufferOut.println(message);
            }

            sc.close();
            bufferOut.close();
            bufferIn.close();
        }catch(IOException ioe){
            System.out.println("Client has an error");
        }
    }
}
