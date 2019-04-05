
/*import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class NotificationThread extends Thread {
	
	static int port_note = 6667;
	static ServerSocket serverSocket_note;
	static Socket socket_note;
	public static OutputStream out_note;
	public static InputStream in_note;
	public static boolean continue_sending = true;
	
	public static boolean notify = false;
	public static boolean warn_level1 = false;
	public static boolean warn_level2 = false;
	
	public static boolean ThreadStopped = true;
	public static boolean Bg_changed = false;
	public NotificationThread(){
		try {
			serverSocket_note = new ServerSocket(port_note);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(String.format("problem2"));
		}
		
	}
	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(0, 10000);
				
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			if(Bg_changed){
				
			}
			if (notify) {
				System.out.println("Face is detected......................");
				//SendMail.sendmail_notif=true;
				try {
					Main.alert1given = true;
					while(continue_sending){
						socket_note = serverSocket_note.accept();
						out_note = socket_note.getOutputStream();
						in_note = socket_note.getInputStream();
						out_note.write(1);
						out_note.flush();
						int p = in_note.read();
						socket_note.close();
						if(p==9){
						continue_sending = false;
						}
					}
						continue_sending = true;
					 System.out.println(String.format(".....................................................................................connected level 1"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println(String.format("connection_prob2"));
					e.printStackTrace();
				}
				notify = false;
			}
			if (warn_level1) {
				System.out.println("alert level 1...................");
				try {
					while(continue_sending){
						socket_note = serverSocket_note.accept();
						out_note = socket_note.getOutputStream();
						in_note = socket_note.getInputStream();
						out_note.write(2);
						out_note.flush();
						int p = in_note.read();
						socket_note.close();
						if(p==9){
						continue_sending = false;
						}
					}
					
					continue_sending = true;
					 System.out.println(String.format(".....................................................................................connected level 1"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println(String.format("connection_prob3"));
					e.printStackTrace();
				}
				warn_level1 = false;
			}
			if (warn_level2) {
				System.out.println("alert level 2...................");
				//SendMail.sendmail_notif=true;
				try {
					socket_note = serverSocket_note.accept();
					out_note = socket_note.getOutputStream();
					out_note.write(3);
					out_note.flush();
					socket_note.close();
					System.out.println(String.format("..................................................................................connected level 2"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println(String.format("connection_prob4"));
					e.printStackTrace();
				}
				warn_level2 = false;
			}
			ThreadStopped = false;
		}
	}
}*/

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

import javax.imageio.ImageIO;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class NotificationThread extends Thread {

	static int port_note = 6667;
	static int port_frame = 6669;
	private static String servername = Main.servername;
	static Socket socket_note, socket_frame;
	static OutputStream out_note, out_frame;
	public InputStream in_note;
	public byte p;
	public int myNotifId ;
	public BufferedImage notifFrame;
	public static int memoryLeft;
	
	public static String fcm_token; //= "c-UYQcaz-aE:APA91bE5oeWOoSzMGggYLL7FfezyfK7Ed8-w0EUADWW5Uwlo_PjrAnXBVrUNEil146wrsxISlRrnDOoAicrI6l2is_uuz1uIBIgQ81DHz76CGx3gp8ZLG3HYsE4PgkYjQUiXL0_lAhnV";					//remove hard coded value
	public static Boolean readyForNotifs = false;	// Turns true when FCM reg token of app is received!
	public static Boolean sendNotif = false;	
	static String serverKey = "AIzaSyD5bIjH30FEMF2hmTrRzjRVGSU2NYFYJKg";
	final static private String FCM_URL = "https://fcm.googleapis.com/fcm/send";
	FileInputStream serviceAccount;

	public NotificationThread() { 
		try {
			//serviceAccount = new FileInputStream("F:\\GitHub\\ARVIS-rasp-3b\\arvis-aws-rasp-3b-firebase-adminsdk-2pzq2-f1b0d0db80.json");
			serviceAccount = new FileInputStream(Main.ROOT_DIR +"//arvis-aws-rasp-3b-firebase-adminsdk-2pzq2-f1b0d0db80.json");
			FirebaseOptions options = new FirebaseOptions.Builder()
				    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
				    .setDatabaseUrl("https://arvis-aws-rasp-3b.firebaseio.com/")
				    .build();
			FirebaseApp.initializeApp(options);
			System.out.println("...Firebase initialized...");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void run() {
		while (true) {
			if (sendNotif) {
				System.out.println(".....notif sending started.....");
				if(readyForNotifs)
					send_FCM_Notification(fcm_token,serverKey,p,Main.store_activityname,myNotifId,notifFrame);
					sendNotif = false;
					/*socket_note = new Socket(servername,port_note);
					System.out.println("######################################.......................Client Sapadla!!!!!!");
					out_note = socket_note.getOutputStream();
					in_note = socket_note.getInputStream();
					out_note.write(p);
					out_note.flush();
					in_note.read();
					System.out.println("........................still sending p.........................."+ p);
					if (p == Main.BYTE_FACEFOUND_VDOGENERATING || p == Main.BYTE_ALERT1) {
						System.out.println("1st notif sent..........................");
					
						socket_frame = new Socket(servername,port_frame);
						out_frame = socket_frame.getOutputStream();
						ImageIO.write(notifFrame, "jpg", out_frame);
						socket_frame.close();
					}
					if (p == Main.BYTE_FACEFOUND_VDOGENERATED || p == Main.BYTE_ALERT2 || p == Main.BYTE_ABRUPT_END || p == Main.BYTE_LIGHT_CHANGE){
						DataOutputStream dout_activity = new DataOutputStream(out_note);
						dout_activity.writeUTF(Main.store_activityname);
						dout_activity.flush();
						System.out.println("2nd vdo generated notif sent.......................");
					}
					DataOutputStream dout_note = new DataOutputStream(out_note);
					System.out.println("Notifthread value of notifId is " + myNotifId);
					dout_note.writeInt(myNotifId);
					dout_note.flush();
					sendNotif = false;
					int q = in_note.read();
					if (q == 9 ){
						System.out.println("sending notif loop exit:::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
					}else System.out.println("sending notif loop exit:::::::::::::::::::::::::::::::::::::::::::::::::::::::::" + q);
					socket_note.close();*/
			} else {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					System.out.println(String.format("connection_problem re bawa!!!"));
					e1.printStackTrace();
				}
			}
		}
	}
	static void send_FCM_Notification(String tokenId, String server_key, int p, String activityName, int NotifId, BufferedImage image){
		try{
			// Create URL instance.
			URL url = new URL(FCM_URL);
			
			// create connection.
			HttpURLConnection conn;
			conn = (HttpURLConnection) url.openConnection();
			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			//set method as POST or GET
			conn.setRequestMethod("POST");
			//pass FCM server key
			conn.setRequestProperty("Authorization","key="+server_key);
			//Specify Message Format
			conn.setRequestProperty("Content-Type","application/json");
			
			//Create JSON Object & pass value
			/*JSONObject infoJson = new JSONObject();
			infoJson.put("title","Notif title");
			infoJson.put("body", "Notif body");*/
			
			JSONObject dataJson = new JSONObject();
			dataJson.put("NotifByte", p);
			dataJson.put("NotifId", NotifId);
			dataJson.put("HashId", Main.HASH_ID);
			System.out.println("..........Prepared 1st notif json object for app");
			System.out.println("FCM Token : " + fcm_token);
			if (p == Main.BYTE_FACEFOUND_VDOGENERATED || p == Main.BYTE_ALERT2 || p == Main.BYTE_ABRUPT_END || p == Main.BYTE_LIGHT_CHANGE){	
				dataJson.put("date",activityName);
				System.out.println("..........Prepared 2nd notif json object for app");
			}else if(p == Main.BYTE_MEMORY_ALERT){
				dataJson.put("%memory", memoryLeft);
			}
			JSONObject json = new JSONObject();
			json.put("to",tokenId.trim());
			//json.put("notification", infoJson);
			json.put("data",dataJson);
			System.out.println("json length = " + json.length());

			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(json.toString());
			wr.flush();
			int status = 0;
			if( null != conn ){
				status = conn.getResponseCode();
			}
			if( status != 0){
				if( status == 200 ){
					//SUCCESS message
					BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					System.out.println("Android Notification Response : " + reader.readLine());
				}else if(status == 401){
					//client side error
					System.out.println("Notification Response : TokenId : " + tokenId + " Error occurred :");
				}else if(status == 501){
					//server side error
					System.out.println("Notification Response : [ errorCode=ServerError ] TokenId : " + tokenId);
				}else if( status == 503){
					//server side error
					System.out.println("Notification Response : FCM Service is Unavailable  TokenId : " + tokenId);
				}
			}
		}catch(MalformedURLException mlfexception){
			// Protocol Error
			System.out.println("Error occurred while sending push Notification!.." + mlfexception.getMessage());
		}catch(IOException mlfexception){
			//URL problem
			System.out.println("Reading URL, Error occurred while sending push Notification!.." + mlfexception.getMessage());
		}catch(JSONException jsonexception){
			//Message format error
			System.out.println("Message Format, Error occurred while sending push Notification!.." + jsonexception.getMessage());
		}catch (Exception exception) {
			//General Error or exception.
			System.out.println("Error occurred while sending push Notification!.." + exception.getMessage());
		}
		
		//write the frame
		/*if (p == Main.BYTE_FACEFOUND_VDOGENERATING || p == Main.BYTE_ALERT1){
			try {
				System.out.println("...Sending key image for notification to app...");
				socketFrame_mob = ssFrame_mob.accept();
				System.out.println("#######..........Client Sapadla!!!!!!");
				
				OutputStream out = socketFrame_mob.getOutputStream();
				ImageIO.write(image, "jpg", out);
				
				socketFrame_mob.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}*/
		
	}
}
