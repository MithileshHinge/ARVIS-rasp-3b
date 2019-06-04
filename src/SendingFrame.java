import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;

public class SendingFrame extends Thread {
    private static int PORT_LIVEFEED_TCP = 6666;
    private static Socket socket;
    public static BufferedImage frame;
    private static String servername=Main.servername;
    private static OutputStream out;
    private static DataOutputStream dout;
    
    public void run() {
    	
		System.out.println("!!!!!!!!!!! LIVEFEED STARTED  !!!!!!!!!!");
        try {
        	socket = new Socket(servername,PORT_LIVEFEED_TCP);
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
	           	
	            /*dout.writeInt(buf.length);
	            dout.flush();*/
	            /*// Create buffer of length
	            ByteBuffer lengthByteBuf = ByteBuffer.allocate(String.valueOf(buf.length).length());
	            lengthByteBuf.putInt(buf.length);
	            byte[] lengthBuf = lengthByteBuf.array();
	            System.out.println("buff " + buf + " buff size" + buf.length + " lengthBuff " + lengthBuf + " lengthBuff size" + lengthBuf.length);
	            
	            // Creating final buffer
	            byte[] finalBuf = new byte[lengthBuf.length + buf.length + 1];
	            System.arraycopy(lengthBuf, 0, finalBuf, 0, lengthBuf.length);
	            System.arraycopy(buf, 0, finalBuf, lengthBuf.length+1, buf.length);
	            //finalBuf[lengthBuf.length] = (byte)'_';
	            System.out.println("finalbuff " + finalBuf + " final buff size" + finalBuf.length);
	            System.out.println("buf.toString() length = " + buf.toString().length() + " new String(buf) length = " + new String(buf).length());
	            */
	           	String bufIntoString = new String(buf,StandardCharsets.ISO_8859_1);
	            dout.writeUTF(bufIntoString);
	            dout.flush();
	            System.out.println("SF : Frame sent ");
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
        try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
