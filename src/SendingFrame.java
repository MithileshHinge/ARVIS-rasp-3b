import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.imageio.ImageIO;

public class SendingFrame extends Thread {
    private static int PORT_LIVEFEED_UDP = 6663;
    private static int PORT_LIVEFEED_TCP = 6666;
    private static ServerSocket serverSocket;
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
            byte[] buf1 = Main.HASH_ID.getBytes();
            System.out.println("hash id udp packet length is :" + buf1.length);
			DatagramPacket packet = new DatagramPacket(buf1, buf1.length, InetAddress.getByName(servername), PORT_LIVEFEED_UDP);
			for (int i=0; i<10; i++){
				udpSocket.send(packet);
			}
			System.out.println("intial udp packets sent to server");

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
	            if (frame == null) continue;
	            ImageIO.write(frame, "jpg", baos);
	            byte[] buf = baos.toByteArray();
	            System.out.println("buff size" + buf.length);
	            
	            InetAddress serverAddress = InetAddress.getByName(servername);
	            DatagramPacket imgPacket = new DatagramPacket(buf, buf.length, serverAddress, PORT_LIVEFEED_UDP);
	            udpSocket.send(imgPacket);

        	} catch (IOException e) {
        		e.printStackTrace();
        		break;
        	}
        	
        	long time2 = System.currentTimeMillis();
        	if (time2 - time1 < 200){
        		try {
					Thread.sleep(time2 - time1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        	}

        	
        	//System.out.println("sendingframe time = " + (time2 - time1));
        }
    }
}
