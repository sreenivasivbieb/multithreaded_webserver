import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

public class client {

    public static void main(String[] args) {

        int port = 8888;
        try {
            InetAddress address = InetAddress.getByName("localhost");
            Socket client = new Socket(address, port);

            BufferedReader fromserver = new BufferedReader(new InputStreamReader(client.getInputStream()));
            System.out.println(fromserver.readLine());

            client.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}