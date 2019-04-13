import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ConnectThread extends Thread{
	
	private static final int connectPort = 6660;
	private String localIP;
	volatile boolean end = false;
	private Socket s;
	//MessageThread msgThread;
	File file = new File(Main.ROOT_DIR , "save_usr_pswd.txt");
	MessageThread msg;
	
	public void run(){
		while(!end){
			try {
				s = new Socket(Main.servername, connectPort);
				localIP = s.getLocalAddress().toString();
				System.out.println(s.getLocalAddress().getHostName());
				InputStream in = s.getInputStream();
				OutputStream out = s.getOutputStream();
				DataOutputStream dout = new DataOutputStream(out);
				DataInputStream din = new DataInputStream(in); 
				
				dout.writeUTF(Main.HASH_ID);
				dout.flush();
				in.read();
				try {
		            FileReader reader = new FileReader(file);
		            BufferedReader bufferedReader = new BufferedReader(reader);
		 
		            String line;
		         
		            if((line = bufferedReader.readLine()) != null) {
		            	Main.username = line;
		            	Main.password = bufferedReader.readLine();
		                NotificationThread.fcm_token= bufferedReader.readLine();
		                SendMail.sendMailTo = bufferedReader.readLine();
		            }
		            reader.close();
		 
		        } catch (IOException e) {
		            e.printStackTrace();
		        }					
				if (Main.username != null && Main.password != null){
					dout.writeUTF(Main.username);
					dout.writeUTF(Main.password);
					dout.flush();
					if(NotificationThread.fcm_token != null){
						NotificationThread.readyForNotifs = true;
					}
					int i;
					while(true){
					i = in.read();
					if( i == 0 | i == 3) break;
		
					}
					if(i == 3)
						continue;
					// TODO: if anything other than 0 is recvd then system is under attack
				}else{
					//During Setup-Phase store the username and password
					Main.username = din.readUTF();
					Main.password = din.readUTF();
					//TODO Check whether this is integrated in the server
					NotificationThread.fcm_token = din.readUTF();
					if(NotificationThread.fcm_token != null){
						NotificationThread.readyForNotifs = true;
					}
					SendMail.sendMailTo = din.readUTF();
					
					try {
						FileWriter fileWriter = new FileWriter(file);
						fileWriter.write(Main.username);
						fileWriter.write("\r\n");            // write new line
						fileWriter.write(Main.password);
						fileWriter.write("\r\n");            // write new line
						fileWriter.write(NotificationThread.fcm_token);
						fileWriter.write("\r\n");
						fileWriter.write(SendMail.sendMailTo);
						fileWriter.flush();
						fileWriter.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					}
				
				dout.writeUTF(localIP);
				dout.flush();
				msg = new MessageThread();
				msg.start();
				s.setSoTimeout(12000);
				
				while(!end){
					out.write(3);
					out.flush();
					try{
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			} catch (IOException e) {
				//e.printStackTrace();
				msg.end();
				try {
					s.close();
				} catch (IOException e1) {
					//e1.printStackTrace();
					}
			} 
		}
		msg.end();
		try {
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*public void end(){
		this.end = true;
	}*/
}
