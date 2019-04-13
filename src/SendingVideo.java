import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
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
			InetSocketAddress listenAddr = new InetSocketAddress(port);
			listener = SocketChannel.open();
			/*ssVdo = listener.socket();
			ssVdo.setReuseAddress(true);
			ssVdo.bind(listenAddr);
			*/
			} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		
		try {
			listener.connect(new InetSocketAddress(Main.servername, port));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		//SocketChannel sc = listener;
		Socket s = listener.socket();
		try {
			InputStream sIn = s.getInputStream();
			DataInputStream dIn = new DataInputStream(sIn);
			OutputStream sOut = s.getOutputStream();
			
			int notifId = dIn.readInt();
			String filepath = Main.notifId2filepaths.get(Integer.valueOf(notifId));
			System.out.println("filepath = " + filepath);
			String filename = filepath.substring(beginIndex);
			DataOutputStream dOut = new DataOutputStream(sOut);
			dOut.writeInt(filename.length());
			dOut.flush();
			sIn.read();
			sOut.write(filename.getBytes());
			sOut.flush();
			sIn.read();
			if (filepath != null){
				sendVideo(listener, filepath);
				File deleteit = new File (filepath) ;
				deleteit.delete();
			}
			s.close();
			//sc.close();
			listener.close();
			
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
