import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class NotificationThread extends Thread {

	static int port_note = 6667;
	static int port_frame = 6669;
	static Socket socket_note, socket_frame;
	static OutputStream out_note, out_frame;
	public InputStream in_note;
	public byte p;
	public int myNotifId ;
	public BufferedImage notifFrame;
	public static int memoryLeft;

	public static String fcm_token; 
	public static Boolean readyForNotifs = false;	// Turns true when FCM reg token of app is received!
	public static Boolean sendNotif = false;	
	static String serverKey = "AIzaSyD5bIjH30FEMF2hmTrRzjRVGSU2NYFYJKg";
	final static private String FCM_URL = "https://fcm.googleapis.com/fcm/send";
	FileInputStream serviceAccount;

	public NotificationThread() {
		try {
			serviceAccount = new FileInputStream(Main.ROOT_DIR +"//arvis-aws-rasp-3b-firebase-adminsdk-2pzq2-f1b0d0db80.json");
			FirebaseOptions options = new FirebaseOptions.Builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount))
					.setDatabaseUrl("https://arvis-aws-rasp-3b.firebaseio.com/")
					.build();
			FirebaseApp.initializeApp(options);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (true) {
			if (sendNotif) {
				if(readyForNotifs)
					send_FCM_Notification(fcm_token,serverKey,p,Main.store_activityname,myNotifId,notifFrame);
				sendNotif = false;
			} else {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
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

			JSONObject dataJson = new JSONObject();
			dataJson.put("NotifByte", p);
			dataJson.put("NotifId", NotifId);
			dataJson.put("HashId", Main.HASH_ID);
			long dateTime = System.currentTimeMillis();
			dataJson.put("time", dateTime);
			if (p == Main.BYTE_FACEFOUND_VDOGENERATED || p == Main.BYTE_ALERT2 || p == Main.BYTE_ABRUPT_END || p == Main.BYTE_LIGHT_CHANGE){	
				dataJson.put("date",activityName);
			}else if(p == Main.BYTE_MEMORY_ALERT){
				dataJson.put("%memory", memoryLeft);
			}
			JSONObject json = new JSONObject();
			json.put("to",tokenId.trim());
			//json.put("notification", infoJson);
			json.put("data",dataJson);
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
				}else if(status == 401){
					//client side error
				}else if(status == 501){
					//server side error
				}else if( status == 503){
					//server side error
				}
			}
		}catch(MalformedURLException mlfexception){
			// Protocol Error
			mlfexception.printStackTrace();
		}catch(IOException mlfexception){
			//URL problem
		}catch(JSONException jsonexception){
			//Message format error
		}catch (Exception exception) {
			exception.printStackTrace();
			//General Error or exception.
		}
	}
}
