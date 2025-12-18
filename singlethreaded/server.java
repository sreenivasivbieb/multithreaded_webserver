import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
public class server{
     
     

     
     public void run(){
        int clientServerTimeout = 10000;
        int port=8888;
        try(ServerSocket server=new ServerSocket(port);){

        server.setSoTimeout(clientServerTimeout);
        while(true){
        try {
            System.out.println("Server is connected to :"+port);

            Socket acceptedConnection=server.accept();
            System.out.println("Server is connected to this address"+acceptedConnection.getRemoteSocketAddress());

            PrintWriter toClient =new PrintWriter(acceptedConnection.getOutputStream());
            BufferedReader fromClient= new BufferedReader(new InputStreamReader(acceptedConnection.getInputStream()));
            toClient.println("Hello from the Server");
            System.out.println(fromClient.readLine());
            toClient.flush();

            
            

            

            // server loop / handling goes here
            
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        }
        }catch( IOException ex){
            System.out.println("Server couldn't start");
            ex.printStackTrace();
        }

     }


    public static void main (String args[]){
            System.out.println("Server is accepting connections");
            server socket= new server();
            socket.run();

           

    }
}