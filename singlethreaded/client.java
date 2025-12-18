import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class client{
     
    public void run()throws IOException,UnknownHostException{
        int port = 8888;
        InetAddress address=InetAddress.getByName("localhost");
        Socket socket=new Socket(address,port);
        
        PrintWriter toserver=new PrintWriter(socket.getOutputStream());
        BufferedReader fromserver= new BufferedReader(new InputStreamReader(socket.getInputStream()));
        toserver.println("Hello from client");
        toserver.flush();
        System.out.println(fromserver.readLine());

         

    }

    public static void main(String args[]){
       System.out.println("Starting the client");
       client socket= new client();
       try {
         socket.run();
       } catch (Exception ex) {
               ex.printStackTrace();
               
       }
    }
}