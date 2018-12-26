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
	volatile boolean end = false;
	private Socket s;
	//MessageThread msgThread;
	File file = new File("//home//pi//arvis","save_usr_pswd.txt");
	
	public void run(){
		while(!end){
			try {
				s = new Socket(Main.servername, connectPort);
				
				InputStream in = s.getInputStream();
				OutputStream out = s.getOutputStream();
				DataOutputStream dout = new DataOutputStream(out);
				DataInputStream din = new DataInputStream(in); 
				dout.writeUTF(Main.HASH_ID);
				dout.flush();
				System.out.println("hash id sent!");
				try {
		            FileReader reader = new FileReader(file);
		            BufferedReader bufferedReader = new BufferedReader(reader);
		 
		            String line;
		         
		            if((line = bufferedReader.readLine()) != null) {
		            	Main.username = line;
		            	System.out.println("username is: " + line);
		                Main.password = bufferedReader.readLine();
		                NotificationThread.fcm_token= bufferedReader.readLine();
		                SendMail.sendMailTo = bufferedReader.readLine();
		            }else{
		            	System.out.println("no username password is stored");	
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
						Main.notifThread.start();
					}
					while(true){
					int i = in.read();
					if( i == 0) break;
					System.out.println("go ahead and create message thread  int recvd is 0 or not" + i);
					}
					// TODO: if anything other than 0 is recvd then system is under attack
				}else{
					//During Setup-Phase store the username and password
					Main.username = din.readUTF();
					Main.password = din.readUTF();
					//TODO Check whether this is integrated in the server
					NotificationThread.fcm_token = din.readUTF();
					if(NotificationThread.fcm_token != null){
						NotificationThread.readyForNotifs = true;
						Main.notifThread.start();
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
					System.out.println("username recvd: " + Main.username + "      password recvd: " + Main.password);
					System.out.println("FCM tokem : "+NotificationThread.fcm_token);
				}
				
				MessageThread msg = new MessageThread();
				msg.start();
				System.out.println("..................Messsage thread started");
				
				while(!end){
					in.read();
					out.write(3);
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
		try {
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void end(){
		this.end = true;
	}
}
