import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

import javax.imageio.ImageIO;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

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
	public Mat notifFrame;
	public static int memoryLeft;
	
	public static String fcm_token; //= "c-UYQcaz-aE:APA91bE5oeWOoSzMGggYLL7FfezyfK7Ed8-w0EUADWW5Uwlo_PjrAnXBVrUNEil146wrsxISlRrnDOoAicrI6l2is_uuz1uIBIgQ81DHz76CGx3gp8ZLG3HYsE4PgkYjQUiXL0_lAhnV";					//remove hard coded value
	public static boolean readyForNotifs = false;	// Turns true when FCM reg token of app is received!
	public static boolean sendNotif = false;	
	static String serverKey = "AIzaSyD5bIjH30FEMF2hmTrRzjRVGSU2NYFYJKg";
	final static private String FCM_URL = "https://fcm.googleapis.com/fcm/send";
	FileInputStream serviceAccount;
	public static byte[] imgSent;

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
	static void send_FCM_Notification(String tokenId, String server_key, int p, String activityName, int NotifId, Mat image){
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
			long dateTime = System.currentTimeMillis();
			dataJson.put("time", dateTime);
			System.out.println("..........Prepared 1st notif json object for app");
			System.out.println("FCM Token : " + fcm_token);
			if (p == Main.BYTE_FACEFOUND_VDOGENERATED || p == Main.BYTE_ALERT2 || p == Main.BYTE_ABRUPT_END || p == Main.BYTE_LIGHT_CHANGE){
				//Resize mat
				Imgproc.resize(image, image, new Size(70,70));
				
				//convert to buffered image
				BufferedImage resizedFrame = Main.matToBufferedImage(image);
				
				//convert to String
				String frame = imgToBase64String(resizedFrame,"jpg");
				
				double bytes = frame.length();
				double kilobytes = (bytes/1024);
				System.out.println("@#@#@#@#@@#@#@#@#@#@#@ size of image to be sent in KB : " + kilobytes +" frame string length " + frame.length());
				dataJson.put("Frame",frame);
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
			mlfexception.printStackTrace();
			System.out.println("Error occurred while sending push Notification!.." + mlfexception.getMessage());
		}catch(IOException mlfexception){
			//URL problem
			System.out.println("Reading URL, Error occurred while sending push Notification!.." + mlfexception.getMessage());
		}catch(JSONException jsonexception){
			//Message format error
			System.out.println("Message Format, Error occurred while sending push Notification!.." + jsonexception.getMessage());
		}catch (Exception exception) {
			exception.printStackTrace();
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
	
	public static String imgToBase64String(final BufferedImage img, final String formatName) {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		try{
		   ImageIO.write(img, formatName, os);
		   return Base64.getEncoder().encodeToString(os.toByteArray());
		} catch (final IOException ioe){
		    throw new UncheckedIOException(ioe);
		}
	}
}
