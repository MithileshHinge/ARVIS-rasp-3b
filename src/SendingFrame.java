import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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
    
    private static String mobUdpIP;
    private static int mobUdpPort;

    public void run() {
    	
		System.out.println("!!!!!!!!!!! LIVEFEED STARTED  !!!!!!!!!!");
        try {
        	socket = new Socket(servername,PORT_LIVEFEED_TCP);
        	udpSocket = new DatagramSocket();
        	out = socket.getOutputStream();
        	
        	DataInputStream din = new DataInputStream(socket.getInputStream());
        	DataOutputStream dout = new DataOutputStream(out);
        	dout.writeUTF(Main.HASH_ID);
        	dout.flush();
        	
        	mobUdpPort = din.readInt();
        	mobUdpIP = din.readUTF();
        	
        	System.out.println("mobIP: " + mobUdpIP);
        	System.out.println("mobPort: " + mobUdpPort);
        	
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }
        while(true) {
        	if (frame == null) continue;
        	long time1 = System.currentTimeMillis();
        	try {
        		out.write(1);
        		out.flush();
        		
	            ByteArrayOutputStream baos = new ByteArrayOutputStream();
	            if (frame == null) continue;
	            ImageIO.write(frame, "jpg", baos);
	            byte[] buf = baos.toByteArray();
	            System.out.println("buff size" + buf.length);
	            
	            InetAddress mobUdpAddress = InetAddress.getByName(mobUdpIP);
	            DatagramPacket imgPacket = new DatagramPacket(buf, buf.length, mobUdpAddress, mobUdpPort);
	            udpSocket.send(imgPacket);
        	} catch (IOException e) {
        		e.printStackTrace();
        		try {
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
        		udpSocket.close();
        		break;
        	}


        	long time2 = System.currentTimeMillis();
        	if (time2 - time1 < 200){
        		try {
					Thread.sleep(200 - (time2 - time1));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        	}
        	//System.out.println("sendingframe time = " + (time2 - time1));
        }
    }
}
