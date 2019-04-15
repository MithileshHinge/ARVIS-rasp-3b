import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class SendingVideo extends Thread {
	
	private int port = 6668;
	private SocketChannel listener = null;
	private int beginIndex = Main.outputFilename4android.length();
	
	private ConcurrentHashMap<Integer, String> notifId2filepaths;
	//Socket ssVdo;
	
	public SendingVideo(){
		//this.notifId2filepaths = Main.notifId2filepaths;
		try {
			System.out.println("...in constructor of sending video....");
			InetSocketAddress listenAddr = new InetSocketAddress(port);
			listener = SocketChannel.open();
			/*ssVdo = listener.socket();
			ssVdo.setReuseAddress(true);
			ssVdo.bind(listenAddr);
			*/
			System.out.println("...done with constructor of sending video....");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		
		System.out.println("...in void run of sending video....");
		try {
			listener.connect(new InetSocketAddress(Main.servername, port));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		//SocketChannel sc = listener;
		Socket s = listener.socket();
		try {
			System.out.println("...in while true of sending video....");
			
			InputStream sIn = s.getInputStream();
			DataInputStream dIn = new DataInputStream(sIn);
			OutputStream sOut = s.getOutputStream();
			
			int notifId = dIn.readInt();
			System.out.println("Sending Video value of notifId is " + notifId);
			
			String filepath = Main.notifId2filepaths.get(Integer.valueOf(notifId));
			System.out.println("filepath = " + filepath);
			String filename = filepath.substring(beginIndex);
			DataOutputStream dOut = new DataOutputStream(sOut);
			dOut.writeInt(filename.length());
			dOut.flush();
			sIn.read();
			System.out.println("Sending filename.length = " + filename.length());
			
			sOut.write(filename.getBytes());
			sOut.flush();
			sIn.read();
			System.out.println("Sending filename = " + filename);
			
			if (filepath != null){
				sendVideo(listener, filepath);
				File deleteit = new File (filepath) ;
				System.out.println("delete file " + deleteit);
				deleteit.delete();
			}
			s.close();
			//sc.close();
			listener.close();
			
			System.out.println(".....................Closing sockets");
		} catch (IOException e) {
			e.printStackTrace();
			try {
				s.close();
				//sc.close();
				listener.close();
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}		
		
	}
	
	private void sendVideo(SocketChannel sc, String filepath){
		
		try {
			FileChannel fc = new FileInputStream(filepath).getChannel();
			fc.transferTo(0, fc.size(), sc);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

}
