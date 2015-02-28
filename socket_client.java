//java socket client example
import java.io.*;
import java.net.*;
 
public class socket_client
{
    public static void main(String[] args) throws IOException
    {
        Socket s = new Socket();
    String host = "www.google.com";
    PrintWriter s_out = null;
    BufferedReader s_in = null;
         
        try
        {
        s.connect(new InetSocketAddress(host , 80));
        System.out.println("Connected");
             
        //writer for socket
            s_out = new PrintWriter( s.getOutputStream(), true);
            //reader for socket
            s_in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        }
         
        //Host not found
        catch (UnknownHostException e)
        {
            System.err.println("Don't know about host : " + host);
            System.exit(1);
        }
         
        //Send message to server
    String message = "GET / HTTP/1.0\r\n\r\n";
    s_out.println( message );
             
    System.out.println("Message send");
         
    //Get response from server
    String response;
    while ((response = s_in.readLine()) != null)
    {
        System.out.println( response );
    }
    //close the i/o streams
    s_out.close();
    s_in.close();
             
    //close the socket
    s.close();
    }
}