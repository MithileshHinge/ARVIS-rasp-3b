import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;

public class SendingFrame extends Thread {
    private static int PORT_LIVEFEED_TCP = 6666;
    private static DatagramSocket udpSocket;
    private static Socket socket;
    public static BufferedImage frame;
    private static String servername=Main.servername;
    private static OutputStream out;
    private static DataOutputStream dout;


    public void run() {
    	
		System.out.println("!!!!!!!!!!! LIVEFEED STARTED  !!!!!!!!!!");
        try {
        	socket = new Socket(servername,PORT_LIVEFEED_TCP);
            /*udpSocket = new DatagramSocket();
            byte[] buf1 = Main.HASH_ID.getBytes();
            System.out.println("hash id udp packet length is :" + buf1.length);
			DatagramPacket packet = new DatagramPacket(buf1, buf1.length, InetAddress.getByName(servername), PORT_LIVEFEED_UDP);
			for (int i=0; i<10; i++){
				udpSocket.send(packet);
			}
			System.out.println("intial udp packets sent to server");*/

        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }
        while(true) {
        	if (frame == null) continue;
        	long time1 = System.currentTimeMillis();
        	try {
        		out = socket.getOutputStream();
        		dout = new DataOutputStream(out);
	            if (frame == null) continue;
	            //ImageIO.write(frame, "jpg",out);
	            MatOfInt compressParams = new MatOfInt(Imgcodecs.CV_IMWRITE_JPEG_QUALITY, 15);
	           	MatOfByte bufMat = new MatOfByte();
	           	Mat frameInMat = Main.bufferedImageToMat(frame);
	            Imgcodecs.imencode(".jpg", frameInMat, bufMat, compressParams);
	           	byte[] buf = bufMat.toArray();
	            System.out.println("buff size" + buf.length);
	            dout.writeInt(buf.length);
	            dout.flush();
	            dout.write(buf);
	            dout.flush();
	            System.out.println("sending frame to server");

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
