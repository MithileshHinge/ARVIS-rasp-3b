import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class SendingFrame extends Thread {
    private static int PORT_LIVEFEED_UDP = 6663;
    private static int PORT_LIVEFEED_TCP = 6666;
    private static DatagramSocket udpSocket;
    private static Socket socket;
    public static BufferedImage frame;
    private static String servername=Main.servername;
    private static OutputStream out;
    
    static int PORT_LISTEN_TCP = 6675;
	static int PORT_LISTEN_UDP =6673;
	public static Socket listenSocket;
    private static TargetDataLine targetDataLine;
    private static AudioFormat format;
    private static DataLine.Info dataLineInfo;
    private static int sampleRate = 16000;
    private static int listenMinBufSize = 4096;
    private static DatagramSocket listenDataSocket;
    public volatile boolean createListenSocketOnce = true;
    private static OutputStream listenOut;
    private static InputStream listenIn;
    private long time1 =  System.currentTimeMillis();
    public volatile boolean listen = false;
    
    public void run() {
    	
		System.out.println("!!!!!!!!!!! LIVEFEED STARTED  !!!!!!!!!!");
        try {
        	socket = new Socket(servername,PORT_LIVEFEED_TCP);
        	
            udpSocket = new DatagramSocket();
            
            listenDataSocket = new DatagramSocket();
            
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }
        
        DatagramPacket dgp;
        byte[] data = new byte[listenMinBufSize];
        
       
        
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
        		targetDataLine.close();
        		listen = false;
        		break;
        	}
        	
        	if (listen){
        		
        		if(createListenSocketOnce){
        			System.out.println(String.format("Listen started"));
        			try {
        				try{
        		            format = new AudioFormat(sampleRate, 16, 1, true, false);
        		            dataLineInfo = new DataLine.Info(TargetDataLine.class, format);
        		            targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
        		            targetDataLine.open(format);
        		            targetDataLine.start();
        		        }catch (LineUnavailableException e){
        		            e.printStackTrace();
        		        }
        				
        				createListenSocketOnce = false;
        				listenSocket = new Socket(servername, PORT_LISTEN_TCP);
        				
        				listenOut = listenSocket.getOutputStream();
                    	listenOut.write(2);
                    	listenOut.flush();
                    	
        				listenIn = listenSocket.getInputStream();
        				int p=listenIn.read();
        				System.out.println("listen in.read successful!!");
        			
        				if(p==1)
        				{
        					p = 0;
        					System.out.println(String.format("listen.................p=1 received"));
        					listenOut.write(3);
        				}
        				else{
        					continue;
        				}
        			} catch (IOException e1) {
        				e1.printStackTrace();
        				return;
        			}
        		}
        		
                try{
                	listenOut.write(1);
                	listenOut.flush();
                	
                    targetDataLine.read(data, 0, data.length);
                    InetAddress serverAddress = InetAddress.getByName(servername);
                    dgp = new DatagramPacket(data,data.length,serverAddress,PORT_LISTEN_UDP);
                    listenDataSocket.send(dgp);
                    System.out.println("..........................................................................................SENDING AUDIO DATA:" + String.valueOf(data) + " time: " + (System.currentTimeMillis() - time1));
                    time1 = System.currentTimeMillis();
                }catch(IOException e){
                	e.printStackTrace();
                	createListenSocketOnce = true;
                    targetDataLine.close();
                    listen = false;
                }
            }
        	
        	long time2 = System.currentTimeMillis();
        	//System.out.println("sendingframe time = " + (time2 - time1));
        }
    }
}
