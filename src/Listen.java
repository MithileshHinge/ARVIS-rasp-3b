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

	static int listen_handshake = 6675;
	static int listenport =6673;
	static ServerSocket serverSocket;
	static Socket socket;
	static OutputStream out;
	static InputStream in;
	static DatagramSocket udpSocket;

	static int sampleRate = 44100; 
	static AudioFormat format;
	static DataLine.Info dataLineInfo;
	static TargetDataLine targetDataLine;

	public static boolean listen = true;
	///////////////////////////// SYSTEM TO ANDROID VOICE COMMUNICATION
	public void run(){
		System.out.println(String.format("Listen thread started"));
		try {
			socket = new Socket(Main.servername,listen_handshake);
			udpSocket = new DatagramSocket();

			format = new AudioFormat(sampleRate, 16, 1, true, false);
			dataLineInfo = new DataLine.Info(TargetDataLine.class, format);
			targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
			targetDataLine.open(format);
			targetDataLine.start();


			byte[] holePunchingBuf = Main.HASH_ID.getBytes();
			DatagramPacket holePunchingPacket = new DatagramPacket(holePunchingBuf, holePunchingBuf.length, InetAddress.getByName(Main.servername), listenport);
			for(int i=0; i<10;i++){
				System.out.println("UDP Hole Punching listen...");
				udpSocket.send(holePunchingPacket);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

			while(true){
				try{			
					out = socket.getOutputStream();
					out.write(1);
					out.flush();

					byte[] data = new byte[4096];
					//InetAddress destination = ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress();
					InetAddress destination = InetAddress.getByName(Main.servername);

					targetDataLine.read(data, 0, data.length);
					DatagramPacket dgp = new DatagramPacket(data,data.length,destination,listenport);
					udpSocket.send(dgp);
					/*while(listen){
					try{
						//if(out!= null) out.close();	
						out = socket.getOutputStream();
						out.write(3);
						out.flush();

						System.out.println("..........................................................................................SENDING AUDIO DATA:" + data);
					}catch(IOException e){
						e.printStackTrace();
						targetDataLine.close();
						System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!      IO Exception  BREAK ..........");
						//if(socket != null) socket.close();
						break;
					}		
				}*/
				}catch(IOException e){
					e.printStackTrace();
					targetDataLine.close();
					break;
				}

			}

		}

	}