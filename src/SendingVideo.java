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
	private FileInputStream fileInputStream;
	
	public SendingVideo(ConcurrentHashMap<Integer, String> notifId2filepaths){
		this.notifId2filepaths = notifId2filepaths;
		/*try {
			InetSocketAddress listenAddr = new InetSocketAddress(port);
			listener = SocketChannel.open();
			Socket ssVdo = listener.socket();
			ssVdo.setReuseAddress(true);
			ssVdo.bind(listenAddr);
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}
	
	@Override
	public void run() {
		while(true){
			try {
				/*listener.connect(new InetSocketAddress(Main.servername, port));
				SocketChannel sc = listener;
				Socket s = sc.socket();*/
				Socket s = new Socket(Main.servername,port);
				SocketChannel sc = s.getChannel();
				InputStream sIn = s.getInputStream();
				DataInputStream dIn = new DataInputStream(sIn);
				OutputStream sOut = s.getOutputStream();
				int notifId = dIn.readInt();
				System.out.println("Sending Video value of notifId is " + notifId);
				String filepath = notifId2filepaths.get(Integer.valueOf(notifId));
				String filename = filepath.substring(beginIndex);
				DataOutputStream dOut = new DataOutputStream(sOut);
				dOut.writeInt(filename.length());
				dOut.flush();
				sIn.read();
				sOut.write(filename.getBytes());
				sOut.flush();
				sIn.read();
				if (filepath != null){
					sendVideo(sc, filepath);
				}
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	private void sendVideo(SocketChannel sc, String filepath){
		
		try {
			fileInputStream = new FileInputStream(filepath);
			FileChannel fc = fileInputStream.getChannel();
			fc.transferTo(0, fc.size(), sc);
			fc.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
}
