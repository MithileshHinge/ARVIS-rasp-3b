import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class DetectPerson extends Thread {

	private static int port = 6672;
	private static Socket socket;
	public volatile boolean detect = true;
	public ArrayList<Mat> past5frames = new ArrayList<>();
	private long timeLightChange = 0; // to send frames for personDetect every 15 seconds
	
	@Override
	public void run() {
		while(detect){
			if (timeLightChange != 0){
				long t = System.currentTimeMillis();
				if (t-timeLightChange < 15000){
					try {
						Thread.sleep(15000 - (t-timeLightChange));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				past5frames.clear();
				past5frames.addAll(Main.past5frames);
			}
			if (detectPerson(past5frames.get(4)) || detectPerson(past5frames.get(3)) || detectPerson(past5frames.get(2))){
				// people detected, check again after 15 seconds
				timeLightChange = System.currentTimeMillis();
			}else {
				Main.framesRead = 0;
				return;
			}
		}
	}
	
	public boolean detectPerson(Mat frame){
		try {
				socket = new Socket(Main.servername, port);
				OutputStream out = socket.getOutputStream();
				InputStream in = socket.getInputStream();
				DataOutputStream dout = new DataOutputStream(out);
				DataInputStream din = new DataInputStream(in);
				dout.writeUTF(Main.HASH_ID);
				dout.flush();
				in.read();
				dout.writeInt(frame.width());
				dout.writeInt(frame.height());
				int dataSize = frame.width() * frame.height() * frame.channels();
				byte[] data = new byte[dataSize];
				frame.get(0, 0, data);
				
				Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2RGB);
				
				for (int i=0; i<dataSize; i++){
					out.write(data[i]);
				}
	            out.flush();
	            int people = din.readInt();
	            if (people == 0) {
                    return false;
                }
			} catch (IOException e) {
				e.printStackTrace();
			}
		return true;
	}
}
