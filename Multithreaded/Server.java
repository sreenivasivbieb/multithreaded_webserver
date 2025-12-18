import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;






public class Server{
    public Consumer<Socket> getConsumer(){

          return (clientSocket)->{
            try{
            PrintWriter printer= new PrintWriter(clientSocket.getOutputStream());
            BufferedReader reader= new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            printer.println();
            printer.close();
            reader.close();
                

            }catch(IOException ex){
                ex.printStackTrace();
            }
          
    };
    }

    
    public void main(String[] arg){
         int port=8888;
         Server socket=new Server();

         try {
            ServerSocket server= new ServerSocket(port);
            server.setSoTimeout(100000);
            while (true) { 
                Socket address=server.accept();
                Thread thread= new Thread(()->socket.getConsumer().accept(address));
                thread.start();

            }
            


         } catch (Exception e) {
            e.printStackTrace();
         }


    }
}