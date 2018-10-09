import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ConnectThread extends Thread{
	
	private static final int connectPort = 6660;
	
	private Socket s;
	
	public void run(){
		while(true){
			try {
				s = new Socket(Main.servername, connectPort);
				
				InputStream in = s.getInputStream();
				OutputStream out = s.getOutputStream();
				DataOutputStream dout = new DataOutputStream(out);
				dout.writeUTF(Main.HASH_ID);
				dout.flush();
				
				if (Main.username != null && Main.password != null){
					dout.writeUTF(Main.username);
					dout.writeUTF(Main.password);
					dout.flush();
				}
				
				MessageThread msg = new MessageThread();
				msg.start();
				
				while(true){
					in.read();
					out.write(1);
					out.flush();
				}

			} catch (IOException e) {
				e.printStackTrace();
				
				try {
					s.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
}
