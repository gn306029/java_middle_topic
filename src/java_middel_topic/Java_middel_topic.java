
package java_middel_topic;

import java.io.*;
import java.net.*;

public class Java_middel_topic {
    
    public static Gossiping gossiping = new Gossiping();
    
    public static void main(String[] args) throws IOException{
        ServerSocket server = new ServerSocket(520);
        while(true){
            Socket incoming = server.accept();
            new BBS_Client(incoming,gossiping).start();
        }
    }
    
}
