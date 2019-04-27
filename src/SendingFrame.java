import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
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
	private static OutputStream out;
	private static String servername=Main.ipv6;

	private static String mobUdpIP;
	private static int mobUdpPort;

	static int PORT_LISTEN_TCP = 6675;
	static int PORT_LISTEN_UDP =6673;
	public static Socket listenSocket;
	private static TargetDataLine targetDataLine;
	private static AudioFormat format;
	private static DataLine.Info dataLineInfo;
	private static int sampleRate = 8000;
	private static int listenMinBufSize = 8184;
	private static DatagramSocket listenDataSocket;
	public volatile boolean createListenSocketOnce = true;
	private static OutputStream listenOut;
	private static InputStream listenIn;
	private long time1 =  System.currentTimeMillis();
	public volatile boolean listen = false;
	private static byte[] data;
	private static DatagramPacket dgp;
	private static int mobUdpPortlisten ;
	private static String mobUdpIplisten ;
	
	public void run() {

		System.out.println("!!!!!!!!!!! LIVEFEED STARTED  !!!!!!!!!!");
		try {
			socket = new Socket(servername,PORT_LIVEFEED_TCP);
			udpSocket = new DatagramSocket();
			listenDataSocket = new DatagramSocket();

			out = socket.getOutputStream();
			DataInputStream din = new DataInputStream(socket.getInputStream());
			DataOutputStream dout = new DataOutputStream(out);
			dout.writeUTF(Main.HASH_ID);
			dout.flush();

			mobUdpPort = din.readInt();
			mobUdpIP = din.readUTF();

			System.out.println("mobIP: " + mobUdpIP);
			System.out.println("mobPort: " + mobUdpPort);
			
			data = new byte[listenMinBufSize];

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
						listenIn = listenSocket.getInputStream();
						DataOutputStream listendout = new DataOutputStream(listenOut);
						DataInputStream listendin = new DataInputStream(listenIn);
						
						listendout.writeUTF(Main.HASH_ID);
						listendout.flush();
						
						mobUdpPortlisten = listendin.readInt();
						mobUdpIplisten = listendin.readUTF();
						
						
					} catch (IOException e1) {
						e1.printStackTrace();
						return;
					}
				}

				try{
					listenOut.write(1);
					listenOut.flush();

					targetDataLine.read(data, 0, data.length);
					InetAddress mobIpAddressListen = InetAddress.getByName(mobUdpIplisten);
					dgp = new DatagramPacket(data,data.length,mobIpAddressListen,mobUdpPortlisten);
					listenDataSocket.send(dgp);
					System.out.println("..........................................................................................SENDING AUDIO DATA:" + String.valueOf(data) + " time: " + (System.currentTimeMillis() - time1));
				}catch(IOException e){
					e.printStackTrace();
					createListenSocketOnce = true;
					targetDataLine.close();
					listen = false;
				}
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
