import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class Listen extends Thread {
	static int PORT_LISTEN_TCP = 6675;
	static int PORT_LISTEN_UDP =6673;
	static Socket socket;
	static OutputStream out;
	static DatagramSocket dataSocket;
	private static String servername = Main.servername;
	
	static int sampleRate = 16000; 
	static AudioFormat format;
	static DataLine.Info dataLineInfo;
	static TargetDataLine targetDataLine;
	
	
	public void run(){
		System.out.println(String.format("Listen thread started"));
		try {
			socket = new Socket(servername, PORT_LISTEN_TCP);
			dataSocket = new DatagramSocket();
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		
		while(true){
			try{
				System.out.println("..............LISTEN STARTED..............");
				out = socket.getOutputStream();
				out.write(1);
				out.flush();
				
				format = new AudioFormat(sampleRate, 16, 1, true, false);
				dataLineInfo = new DataLine.Info(TargetDataLine.class, format);
				targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
				targetDataLine.open(format);
				targetDataLine.start();
				
				int numBytesRead;
				byte[] data = new byte[40960];
				DatagramPacket dgp;
				InetAddress destination = ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress();
				numBytesRead =  targetDataLine.read(data, 0, data.length);
				//System.out.println("n: " + numBytesRead);
				dgp = new DatagramPacket(data,data.length,destination,PORT_LISTEN_UDP);
				//System.out.println(data);
				dataSocket.send(dgp);
				System.out.println(".........LISTEN THREAD DATA SEND.............");
				targetDataLine.close();

			}catch(IOException e){
				e.printStackTrace();
				targetDataLine.close();
				break;
				
			} catch (LineUnavailableException e) {
				e.printStackTrace();
				targetDataLine.close();
			}
			
		}
		
	}
	
}