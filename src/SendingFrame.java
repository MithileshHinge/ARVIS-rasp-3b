import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;

import javax.imageio.ImageIO;

public class SendingFrame extends Thread {
    private static int PORT_LIVEFEED_UDP = 6663;
    private static int PORT_LIVEFEED_TCP = 6666;
    //private static ServerSocket serverSocket;
    private static DatagramSocket udpSocket;
    private static Socket socket;
    public static BufferedImage frame;
    private static String servername=Main.servername;
    private static OutputStream out;

    public void run() {
    	
		System.out.println("!!!!!!!!!!! LIVEFEED STARTED  !!!!!!!!!!");
        try {
        	socket = new Socket(servername,PORT_LIVEFEED_TCP);
            udpSocket = new DatagramSocket();
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }
        while(true) {
        	if (frame == null) continue;
        	long time1 = System.currentTimeMillis();
        	try {
        		out = socket.getOutputStream();
        		out.write(1);
        		out.flush();
        	
	            ByteArrayOutputStream baos = new ByteArrayOutputStream();
	            ImageIO.write(frame, "jpg", baos);
	            byte[] buf = baos.toByteArray();
	            
	            InetAddress serverAddress = InetAddress.getByName(servername);
	            DatagramPacket imgPacket = new DatagramPacket(buf, buf.length, serverAddress, PORT_LIVEFEED_UDP);
	            udpSocket.send(imgPacket);

        	} catch (IOException e) {
        		e.printStackTrace();
        		break;
        	}


        	long time2 = System.currentTimeMillis();
        	//System.out.println("sendingframe time = " + (time2 - time1));
        }
    }
}
