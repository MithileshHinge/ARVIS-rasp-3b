import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
	static DatagramSocket dataSocket;
	static boolean getout = false;
	static boolean once = true;
	public volatile boolean receivedAudioPacket = false;
	
	public void run(){
		System.out.println(String.format("Receiving Audio started"));
		try {
			socket = new Socket(servername,PORT_AUDIO_TCP);
			//dataSocket = new DatagramSocket(PORT_AUDIO_UDP);
			dataSocket = new DatagramSocket();
			dataSocket.setSoTimeout(5000);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		while (true) {
			try {
				System.out.println("................next iteration");
				
				//System.out.println(String.format("....................................................connection sapadla"));
				out = socket.getOutputStream();
				out.write(2);
				out.flush();
				/*DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
				dout.writeInt(dataSocket.getLocalPort());
				dout.flush();*/
				System.out.println("!!!!!!!!!!!!!!!!!!! Local Port  " + dataSocket.getLocalPort());
				in = socket.getInputStream();
				int p=in.read();
				System.out.println("sendingaudio in.read successful!!");
			
				if(p==1)
				{
					p = 0;
					System.out.println(String.format(".................p=1 received"));
					out.write(3);
				}
				else{
					continue;
				}
				
				// UDP hole punching
				/*
				*/
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						try{
							byte[] holePunchingBuf = Main.HASH_ID.getBytes();
							DatagramPacket holePunchingPacket = new DatagramPacket(holePunchingBuf, holePunchingBuf.length, InetAddress.getByName(Main.servername), PORT_AUDIO_UDP);
							while(!receivedAudioPacket){
								System.out.println("UDP Hole Punching sending Audio...");
								dataSocket.send(holePunchingPacket);
								}
						}catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
						}
					}).start();
				
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
	            		dataSocket.receive(receivePacket);
	            		receivedAudioPacket = true;
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
	                
		            dataSocket.receive(receivePacket);
		            receivedAudioPacket = true;
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
		            	receivedAudioPacket = false;
		            	break;
		            }
	            }
	            
	            //fin.close();
		       
	            }catch (SocketTimeoutException s) {
	                System.out.println(".......Socket timed out!");
	                
	             } 	 
			     catch (IOException e) {
					System.out.println(String.format("connection_prob2"));					
					e.printStackTrace();
					break;
			     } catch (LineUnavailableException e) {
					e.printStackTrace();
				}
			
				once = true;
			 
			    sourceDataLine.drain();
			    sourceDataLine.stop();
	            sourceDataLine.close();
	    }
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
