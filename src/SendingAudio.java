import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ArrayBlockingQueue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;


public class SendingAudio extends Thread{
	
	AudioInputStream audioInputStream;
	static AudioInputStream ais;
	static AudioFormat format;
	
	static int PORT_AUDIO_UDP = 6671;
	static int sampleRate = 44100;                //44100;

	static int PORT_AUDIO_TCP = 6670;
	private static String servername = Main.ipv6 ;
	static Socket socket;
	static OutputStream out;
	static InputStream in;
	static DataLine.Info dataLineInfo;
	static SourceDataLine sourceDataLine;
	static DatagramSocket udpSocket;
	static boolean getout = false;
	static boolean once = true;
	
	private static String mobIP;
    private static int mobPort;
	
	public void run(){
		System.out.println(String.format("Receiving Audio started"));
		try {
			socket = new Socket(servername,PORT_AUDIO_TCP);
			udpSocket = new DatagramSocket();
			udpSocket.setSoTimeout(5000);
			
			out = socket.getOutputStream();
			DataInputStream din = new DataInputStream(socket.getInputStream());
        	DataOutputStream dout = new DataOutputStream(out);
        	
        	dout.writeUTF(Main.HASH_ID);
        	dout.writeInt(udpSocket.getLocalPort());
        	dout.flush();
        	
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		try {
			System.out.println("................next iteration");
	        byte[] receiveData = new byte[4096];   ///1280
	        // ( 1280 for 16 000Hz and 3584 for 44 100Hz (use AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat) to get the correct size)
	
	        format = new AudioFormat(sampleRate, 16, 1, true, false);dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
	        sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
	        sourceDataLine.open(format, 4096 * 100);    // Buffer size of 4096 * 60 bytes = 2048 * 60 samples = approx 3 second  
	        sourceDataLine.start();
	        FloatControl volumeControl = (FloatControl) sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
	        volumeControl.setValue(6f);
	        DatagramPacket receivePacket = new DatagramPacket(receiveData,receiveData.length);
	        
	        // make initial buffer
	        ByteArrayOutputStream initialBuf = new ByteArrayOutputStream();
	        for (int i=0; i<6; i++){
	        	try{
	        		out.write(1);
	        		out.flush();
	        		System.out.println("....receiving packets.....");
	        		udpSocket.receive(receivePacket);
	        		System.out.println("....receiving packets for initial buffer.....");
	        		//if (fin.read(receiveData) == -1) break;
	        		
	        		initialBuf.write(receivePacket.getData());
	        	}catch (IOException e){
	        		e.printStackTrace();
	        		continue;
	        	}
	        }
	        
	        toSpeaker(initialBuf.toByteArray());
	        
	        initialBuf.close();
	        while (true) {
	            System.out.println(String.format("......................................into audio rx while loop"));
	            try{
		        	out.write(1);
		            out.flush();
		            udpSocket.receive(receivePacket);
		            
		            toSpeaker(receivePacket.getData());			            
		            System.out.println(String.format(".....here....................................................."));
	            
	            }catch (SocketTimeoutException s) {
	                System.out.println("Socket timed out!");
	                break;
	            }catch (IOException e){
	            	System.out.println("............Audio sending closed");
	            	break;
	            }
	        }
	        
	        //fin.close();
	       
		} catch (SocketTimeoutException s) {
        	System.out.println(".......Socket timed out!");
        	s.printStackTrace();
     	} catch (IOException e) {
			System.out.println(String.format("connection_prob2"));					
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
		
		once = true;
	 
	    sourceDataLine.drain();
	    sourceDataLine.stop();
        sourceDataLine.close();
	}
	
	public static void toSpeaker(byte soundbytes[]) {
	    try {
	    	System.out.println(String.format("sending to speaker1"));
	        System.out.println("format? :" + sourceDataLine.getFormat());
	        sourceDataLine.write(soundbytes, 0, soundbytes.length);

	    } catch (Exception e) {
	        System.out.println("Not working in speakers...");
	        e.printStackTrace();
	    }
	}

}
