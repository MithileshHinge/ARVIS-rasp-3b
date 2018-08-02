import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Settingsmsd extends Thread {
	static int port_settings= 6676;
	static ServerSocket serverSocket_settings;
	static Socket socket_settings;
	static OutputStream out_settings;
	static InputStream in_settings;
	static int p;
	public Settingsmsd(){
		try {
			serverSocket_settings = new ServerSocket(port_settings);
			serverSocket_settings.setSoTimeout(0);                                             
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(String.format("problem2"));
		}
	}
	@Override
	public void run() {
		while(true){
			try{
			socket_settings =serverSocket_settings.accept();
			System.out.println("######################################.......................mode change requested!!!!!!");
			out_settings = socket_settings.getOutputStream();
			in_settings= socket_settings.getInputStream();
			p= in_settings.read();
			out_settings.write(2);
			out_settings.flush();
			System.out.println("........................still sending p.........................."+ p);
			if(p==1){
				Main.Surv_Mode=false;
				if (Main.writer != null){
					if(Main.writer.isOpen()){
						Main.writer.close();
					}
				}
			}
			if(p==3){
				Main.Surv_Mode=true;
				Main.checkonce=true;
				Main.writer.close();
			}
			socket_settings.close();
			
		}catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(String.format("connection_prob2"));
			e.printStackTrace();
		}
	}
	
}
}
