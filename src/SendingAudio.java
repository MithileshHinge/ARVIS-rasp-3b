import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

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
	
	public void run(){
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
				out = socket.getOutputStream();
				out.write(2);
				out.flush();
				DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
				dout.writeInt(dataSocket.getLocalPort());
				dout.flush();
				in = socket.getInputStream();
				int p=in.read();
				if(p==1)
				{
					p = 0;
					out.write(3);
				}
				else{
					continue;
				}
				
				// UDP hole punching
				byte[] holePunchingBuf = new byte[256];
				DatagramPacket holePunchingPacket = new DatagramPacket(holePunchingBuf, holePunchingBuf.length, InetAddress.getByName(servername), PORT_AUDIO_UDP);
				for (int i=0; i<10; i++){
					dataSocket.send(holePunchingPacket);
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
	            
	            
	           
	            // make initial buffer
	            
	            ByteArrayOutputStream initialBuf = new ByteArrayOutputStream();
	            for (int i=0; i<6; i++){
	            	try{
	            		out.write(1);
	            		out.flush();
	            		//System.out.println("....receiving packets.....");
	            		dataSocket.receive(receivePacket);
	            		//System.out.println("....receiving packets for initial buffer.....");
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
		             try{
	            	out.write(1);
	                out.flush();
	                
	                //if(fin.read(receiveData) == -1) break;
	                
		            dataSocket.receive(receivePacket);
	                
	                //toSpeaker(receiveData);
		            
		            toSpeaker(receivePacket.getData());			            
		            
		            }catch (SocketTimeoutException s) {
		                break;
		            }catch (IOException e){
		            	break;
		            }
	            }
	            
	            //fin.close();
		       
	            }catch (SocketTimeoutException s) {
	                
	             } 	 
			     catch (IOException e) {
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
	    	sourceDataLine.write(soundbytes, 0, soundbytes.length);

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

}
