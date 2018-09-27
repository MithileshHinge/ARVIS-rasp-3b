import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;

import javax.imageio.ImageIO;

public class SendingFrame extends Thread {
    private static int udpPort = 6663;
    private static int port = 6666;
    private static ServerSocket serverSocket;
    private static DatagramSocket udpSocket;
    private static Socket socket;
    public static BufferedImage frame;
    private static String servername=Main.servername;
    private static OutputStream out;

    public void run() {
		
        try {
        	socket = new Socket(servername,port);
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
	            DatagramPacket imgPacket = new DatagramPacket(buf, buf.length, serverAddress, udpPort);
	            udpSocket.send(imgPacket);

        	} catch (IOException e) {
        		e.printStackTrace();
        		break;
        	}


        	long time2 = System.currentTimeMillis();
        	System.out.println("sendingframe time = " + (time2 - time1));
        }
    }
}
