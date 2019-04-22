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
	private static String servername = Main.servername ;
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
        	int serverUDPport = din.readInt();
        	byte[] serverBuf = new byte[2];
        	DatagramPacket serverPacket = new DatagramPacket(serverBuf, serverBuf.length, InetAddress.getByName(servername), serverUDPport);
        	for (int i=0; i<10; i++){
        		udpSocket.send(serverPacket);
        	}
        	
        	dout.writeUTF(socket.getLocalAddress().getHostAddress());
        	dout.writeInt(udpSocket.getLocalPort());
        	dout.flush();
        	
        	mobIP = din.readUTF();
        	mobPort = din.readInt();
        	
        	System.out.println("mobIP: " + mobIP);
        	System.out.println("mobPort: " + mobPort);
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		try {
			System.out.println("................next iteration");
			
			out.write(2);
			out.flush();
			in = socket.getInputStream();
			
			// UDP hole punching to mobile
			System.out.println("UDP Hole Punching...");
			byte[] holePunchingBuf = new byte[2];
			DatagramPacket holePunchingPacket = new DatagramPacket(holePunchingBuf, holePunchingBuf.length, InetAddress.getByName(mobIP), mobPort);
			for (int i=0; i<10; i++){
				udpSocket.send(holePunchingPacket);
			}
			
	        byte[] receiveData = new byte[4096];   ///1280
	        // ( 1280 for 16 000Hz and 3584 for 44 100Hz (use AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat) to get the correct size)
	
	        format = new AudioFormat(sampleRate, 16, 1, true, false);dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
	        sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
	        sourceDataLine.open(format, 4096 * 100);    // Buffer size of 4096 * 60 bytes = 2048 * 60 samples = approx 3 second  
	        sourceDataLine.start();
	        FloatControl volumeControl = (FloatControl) sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
	        volumeControl.setValue(6f);
	        //System.out.println(String.format("here"));
	        DatagramPacket receivePacket = new DatagramPacket(receiveData,receiveData.length);
	        
	        
	        //ByteArrayInputStream baiss = new ByteArrayInputStream(receivePacket.getData());
	        
	        //final ArrayBlockingQueue<byte[]> audioQueue = new ArrayBlockingQueue<>(200);
	        
	       /* new Thread(new Runnable(){
	        	public void run(){
	        		while (true){
	        		if (once){
	        			if (audioQueue.size() > 80){
	            			try {
	            				once = false;
	            				System.out.println("Playing audio packet.....");
								toSpeaker(audioQueue.take());
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
	        			}
	        		}else {
	        			try {
	        				System.out.println("Playing audio packet.....");
							toSpeaker(audioQueue.take());
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
	        		}
	        	}
	        	}
	        }).start();*/
	        
	        
	        //FileInputStream fin = new FileInputStream("/home/odroid/Desktop/recording.raw");
	        
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
		            
		            //if(fin.read(receiveData) == -1) break;
		            
		            udpSocket.receive(receivePacket);
		            
		            /*try {
						//audioQueue.put(receivePacket.getData());
		            	audioQueue.put(receiveData);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}*/
		            
		            //toSpeaker(receiveData);
		            
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
