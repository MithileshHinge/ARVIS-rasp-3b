import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;

public class Main {

	//static boolean LightChange = false;
    //static int h;
    //static int w;
    //static int grid_bc = 0;
    //static int blk_grid = 0;
    //static int grid_length = 0;
    //static int itr = 0;
	
	private static CascadeClassifier frontal_face_cascade;
	private static CascadeClassifier mouthCascade;
	static int frame_no = 0;
	private static boolean detectFace = true;
	private static boolean faceNotCovered = false;
	
	/*public static final String outputFilename = "//home//pi//arvis//videos//";
	public static final String outputFilename4android = "//home//pi//arvis//videos4android//";*/
	public static final String outputFilename = "C://Users//Sibhali//Desktop//videos//";
	public static final String outputFilename4android = "C://Users//Sibhali//Desktop//videos4android//";
	public static VideoWriter writer;
	public static boolean startStoring = true;
	public static long startTime;
	public static long startTime4android;
	public static Date dNow;
	public static SimpleDateFormat ft = new SimpleDateFormat("yyyy_MM_dd'at'hh_mm_ss_a");
	public static boolean writer_close = false;
	public static String store_name;
	public static String store_file_name;
	static OutputStream out;
	public static int myNotifId = 1;
	
	public static final byte 
		BYTE_FACEFOUND_VDOGENERATING = 1, 
		BYTE_FACEFOUND_VDOGENERATED = 2, 
		BYTE_ALERT1 = 3, 
		BYTE_ALERT2 = 4, 
		BYTE_ABRUPT_END = 5, 
		BYTE_LIGHT_CHANGE = 6,
		BYTE_CAMERA_INACTIVE = 7;
	
	public static VideoWriter writer4android;
	public static boolean writer_close4android = false;
	public static String store_name4android;
	public static String store_activityname;
	public static boolean once =false;
	static long timeNow1, timeNow2;
	static long time3, time4;
	public static long timeAndroidVdoStarted = -1;
	public static boolean j = true;
	public static boolean checkonce =true;
	public static Process proc;
	//Disable auto focus of camera through terminal
	
	public static boolean alert2given = false;
	public static boolean alert1given = false;
	public static int framesRead = 0;
	
	public static boolean Surv_Mode=true;
	//public static String fourcc = "X264";    /linux environ
	public static String fourcc = "XVID";
	public volatile static ConcurrentHashMap<Integer, String> notifId2filepaths = new ConcurrentHashMap<>();
	private static boolean give_system_ready_once = true;
	public static SendingFrame sendingFrame;
	public static SendingAudio sendingAudio;
	public static Listen listen;
	public static final String servername = "192.168.1.103";
	//public static final String HASH_ID = "2eab13847fe70c2e59dc588f299224aa";
	public static final String HASH_ID = "vv";
	public static String username, password;
	public static NotificationThread notifThread;
	public static SendingVideo sendingVideo;
	public static SendMail sendMail;
	/*private static int cameraErrorCount = 0;
	private static int cameraErrorCountThresh = 15;		// check for 5 seconds considering 3 fps
	*/
	public static int contoursCheck = 0;
	public static Mat saveImage;
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	public static void main(String[] args) {
		startProgram();
		
	}
	
	public static MatOfRect detect(Mat inputframe) {
		faceNotCovered=false;
		Mat mRgba = new Mat();
		Mat mGrey = new Mat();
		MatOfRect front_faces = new MatOfRect();
		// MatOfRect side_faces = new MatOfRect();
		inputframe.copyTo(mRgba);
		inputframe.copyTo(mGrey);
		Imgproc.cvtColor(mRgba, mGrey, Imgproc.COLOR_BGR2GRAY);
		Imgproc.equalizeHist(mGrey, mGrey);
		frontal_face_cascade.detectMultiScale(mGrey, front_faces, 1.1, 3, 0, new Size(30, 30), new Size());

		
		Rect[] facesArray = front_faces.toArray();

    	for (int i = 0; i < facesArray.length; i++) {
    	    Point centre1 = new Point(facesArray[i].x + facesArray[i].width * 0.5,facesArray[i].y + facesArray[i].height * 0.5);
    	    //Core.ellipse(mRgba, centre1, new Size(facesArray[i].width * 0.5, facesArray[i].height * 0.5), 0, 0, 360,new Scalar(192, 202, 235), 4, 8, 0);
    	    //Core.ellipse(front_faces, centre1, new Size(facesArray[i].width * 0.5, facesArray[i].height * 0.5), 0, 0, 360,new Scalar(192, 202, 235), 4, 8, 0);
    	    Mat faceROI = mGrey.submat(facesArray[i]);
    	    MatOfRect mouth = new MatOfRect();

    	    //mouthCascade.detectMultiScale(faceROI, mouth, 1.1, 2, 0, new Size(30, 30), new Size());
    	    mouthCascade.detectMultiScale(faceROI, mouth);
    	    Rect[] mouthArray = mouth.toArray();

    	    for (int k = 0 ; k < mouthArray.length; k++) {
    	        Point centre3 = new Point(facesArray[i].x + mouthArray[k].x + mouthArray[k].width * 0.5,
    	                facesArray[i].y + mouthArray[k].y + mouthArray[k].height * 0.5);
    	        if (centre3.y > centre1.y ){
    	        	faceNotCovered=true;
    	        //Core.ellipse(mRgba, centre3, new Size(mouthArray[k].width * 0.5, mouthArray[k].height * 0.5), 0, 0, 360,new Scalar(177, 138, 255), 4, 8, 0);
    	        //Core.ellipse(front_faces, centre3, new Size(mouthArray[k].width * 0.5, mouthArray[k].height * 0.5), 0, 0, 360,new Scalar(177, 138, 255), 4, 8, 0);
    	        //System.out.println(String.format("Detected %s Mouth(s)", mouth.toArray().length));
    	        }
    	    }
    	    if(faceNotCovered){
    	    	System.out.println(String.format("Detected %s face(s)", front_faces.toArray().length));
    	    	//FacenotCovered=false;
    	    }else{
    	    	System.out.println(String.format("Detected people = 0"));
    	    	//break;                                                                    ///add break for multiple faces or else no need	
    	    }
    	}
    	return front_faces;
    	//return mRgba;
	}
	
	public static Mat bufferedImageToMat(BufferedImage bi) {
		  Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
		  byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
		  mat.put(0, 0, data);
		  return mat;
		}
	
	private static BufferedImage matToBufferedImage(Mat frame) {
		int type = 0;
		if (frame.channels() == 1) {
			type = BufferedImage.TYPE_BYTE_GRAY;
		} else if (frame.channels() == 3) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		BufferedImage image = new BufferedImage(frame.width(), frame.height(), type);
		WritableRaster raster = image.getRaster();
		DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
		byte[] data = dataBuffer.getData();
		frame.get(0, 0, data);
		return image;
		
	}
	private static BufferedImage timestampIt(BufferedImage toEdit){
		BufferedImage dest = new BufferedImage(toEdit.getWidth(), toEdit.getHeight(),  BufferedImage.TYPE_3BYTE_BGR);
		
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    String dateTime = sdf.format(Calendar.getInstance().getTime()); // reading local time in the system
	    
	    Graphics2D g2 = dest.createGraphics();
	    //Color darkgreen= new Color(28,89,71);
	    Color darkgreen= new Color(0,0,0);
	    g2.drawImage(toEdit, 0, 0, toEdit.getWidth(), toEdit.getHeight(), null);
	    g2.setColor(darkgreen);
	    g2.setFont(new Font("TimesRoman", Font.PLAIN, 25)); 
	    g2.drawString(dateTime, 350, 450);
	    return dest;
	}
	
	private static int CalcContours(Mat frame){
		//Mat imageBlurr = new Mat();
		//Imgproc.GaussianBlur(fgMask, imageBlurr, new Size(3,3), 0);
		Imgproc.medianBlur(frame, frame, 9);
		//frame=imageBlurr;
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();    
	    Imgproc.findContours(frame, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
	    //System.out.println("Contours = " + contours.size());
	    for (int i = 0; i < contours.size(); i++) {
	    	if(contours.size()<2) break;
	    	Imgproc.drawContours(frame, contours, 1, new Scalar(0,0,255));
	    }
	    return contours.size();
	}
	
	
	public static void startProgram(){
		
		frame_no = 0;
		detectFace = true;
		faceNotCovered = false;
		
		startStoring = true;
		writer_close = false;
		myNotifId = 1;
		
		writer_close4android = false;
		once =false;
		timeAndroidVdoStarted = -1;
		j = true;
		checkonce =true;
		
		//Disable auto focus of camera through terminal
		
		alert2given = false;
		alert1given = false;
		framesRead = 0;
		
		Surv_Mode=true;
		notifId2filepaths = new ConcurrentHashMap<>();
		give_system_ready_once = true;
		
		notifThread = new NotificationThread();
		//sendingVideo = new SendingVideo();
		
		ConnectThread connThread = new ConnectThread();
		connThread.start();
		
		
		//sendingFrame = new SendingFrame();
		//sendingFrame.start();
		
		//SendingAudio audio = new SendingAudio();
		//audio.start();
		
		//SendingVideo sendingVideo = new SendingVideo(notifId2filepaths);
		//sendingVideo.start();
		
		
		//notifThread.start();
		
		SendMail t3 = new SendMail();
		t3.start();
		
		AudioPlaying audioPlaying = new AudioPlaying();
		
		VideoCapture capture = new VideoCapture(0);
		if (!capture.isOpened()) {
			System.out.println("Error - cannot open camera!");
			return;
		}
		
		BackgroundSubtractorMOG2 backgroundSubtractorMOG = Video.createBackgroundSubtractorMOG2(333, 16, false);
		
		
		/*frontal_face_cascade = new CascadeClassifier("//home//pi//arvis//haarcascades//haarcascade_frontalface_alt.xml");
		mouthCascade = new CascadeClassifier("//home//pi//arvis//haarcascades//Mouth.xml");*/
		
		frontal_face_cascade = new CascadeClassifier("C:\\Users\\Sibhali\\Desktop\\haarcascades\\haarcascade_frontalface_alt.xml");
		mouthCascade = new CascadeClassifier("C:\\Users\\Sibhali\\Desktop\\haarcascades\\Mouth.xml");
		if (frontal_face_cascade.empty()) {
			System.out.println("--(!)Error loading Front Face Cascade\n");
			return;
		} else System.out.println("Front Face classifier loaded");
		
		if(mouthCascade.empty()){
			System.out.println("--(!)Error loading Mouth Cascade\n");
			return;
		}else System.out.println("Mouth classifier loaded");
		
		
		
		int faceDetectionsCounter = 0;
		boolean noFaceAlert = true;
		Mat blackFrame = Mat.zeros(480, 640, CvType.CV_8UC1);

		while(true){
			timeNow1 = System.currentTimeMillis();
			Mat camImage = new Mat();
			capture.read(camImage);
			if (camImage.empty()){
				System.out.println(" --(!) No captured frame -- Break!");
			
				// Send notif that camera is off
				System.out.println(".....cameraInactive alert......");
				notifThread.p = BYTE_CAMERA_INACTIVE;
				notifThread.myNotifId = myNotifId;
				System.out.println("value of notifId is " + myNotifId);
				notifThread.sendNotif = true;
				
				// Send mail that camera is off
				SendMail.sendmail_notif = true;
				SendMail.sendmail_vdo = true;
				SendMail.sendmail = true;
				SendMail.whichMail = 2;
				
				// Sound a warning alarm that camera is disconnected
				
				return;
			}

			//Send frame via live-feed
			BufferedImage cam_img = matToBufferedImage(camImage);
			BufferedImage camimg = timestampIt(cam_img);
			sendingFrame.frame = camimg;
			
			if (!Surv_Mode && checkonce){
				System.out.println("..........................recording started...................................");
				time3 = System.currentTimeMillis();
				store_name = outputFilename + ft.format(dNow) + ".mp4";
				store_file_name = ft.format(dNow);
				writer =new VideoWriter(store_name, VideoWriter.fourcc( fourcc.charAt(0), fourcc.charAt(1), fourcc.charAt(2), fourcc.charAt(3)), 20, new Size(640,480), true);
				//writer = ToolFactory.makeWriter(store_name);
				//writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, 640, 480);
				startTime = System.nanoTime();
				checkonce =false;
			}
			if (!Surv_Mode){
				//saveImage=bufferedImageToMat(camimg);
				//writer.write(saveImage);
				System.out.println("writing image into video file !surv");
				writer.write(camImage);
				//writer.encodeVideo(0, camimg, System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
			}
			
			//Background subtraction without learning background
			Mat fgMask = new Mat();
			Mat frameRef = new Mat();
			
			if (j) {
				backgroundSubtractorMOG.apply(camImage, fgMask, -1);
				j = false;
			}else backgroundSubtractorMOG.apply(camImage, fgMask, 0);
			
			//Calculate black percentage
			byte[] buff = new byte[(int) (fgMask.total() * fgMask.channels())];
			fgMask.get(0, 0, buff);
			int blackCount = 0;
			for (int i = 0; i < buff.length; i++) {
				if (buff[i] == 0) {
					blackCount++;
				}
			}
			final int blackCountPercent = 100*blackCount/buff.length;
			//System.out.println("" + (blackCountPercent) + "%");
			Mat output = new Mat();
			camImage.copyTo(output, fgMask);
			
			//Get the number of contours
			int noOfContours = CalcContours(fgMask);
			//System.out.println("Contours = " + noOfContours + " BlackCountPercentage = " + blackCountPercent + "%");
			System.out.println(blackCountPercent+"%");
			
			//To give system is ready
			if(framesRead==200 && give_system_ready_once){
				give_system_ready_once = false;
				System.out.println("SYSTEM is Ready");
				audioPlaying.system_ready=true;
				audioPlaying.start();
			}
			//Consider background change if black % is less that 90
			if (blackCountPercent < 90 && framesRead > 100) {
				
				//Start recording video just after bg changes
				if (startStoring && Surv_Mode){
					System.out.println("..........................recording started...................................");
					time3 = System.currentTimeMillis();
					store_name = outputFilename + ft.format(dNow) + ".mp4";
					store_file_name = ft.format(dNow);
					SendMail.sendmail_vdo = false;
					writer =new VideoWriter(store_name, VideoWriter.fourcc( fourcc.charAt(0), fourcc.charAt(1), fourcc.charAt(2), fourcc.charAt(3)), 20, new Size(640,480), true);
					//writer = ToolFactory.makeWriter(store_name);
					//writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, 640, 480);
					System.out.println("''''''''''''''writer created succesfully''''''''''''''''''''''''");
					startTime = System.nanoTime();
					writer_close = true;
					startStoring = false;
					
				}
				
				//Write frame to video only when surveillance mode is ON
				if(Surv_Mode){
					//saveImage=bufferedImageToMat(camimg);
					//writer.write(saveImage);
					//System.out.println("writing image into video file");
					writer.write(camImage);
					//writer.encodeVideo(0, camimg, System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
				}
				//If writer4android is open, write frame to android video also
				if (writer4android != null){
					if(writer4android.isOpened()){
						if (timeAndroidVdoStarted!=-1 && (System.currentTimeMillis()-timeAndroidVdoStarted)/1000 >= 3){
							//writer4android.release();
							writer4android.release();
							notifId2filepaths.put(new Integer(myNotifId), store_name4android);
							notifThread.p = BYTE_FACEFOUND_VDOGENERATED;
							notifThread.myNotifId = myNotifId;
							notifThread.sendNotif = true;
							if(myNotifId<99)
								myNotifId++;
							else
								myNotifId = 1;
						}else {
							saveImage=bufferedImageToMat(camimg);
							writer4android.write(saveImage);
							//writer4android.write(camImage);
							//writer4android.encodeVideo(0, camimg, System.nanoTime() - startTime4android, TimeUnit.NANOSECONDS);
						}
					}
				}
				frame_no++;
				
				//Detect face every 3rd frame
				if (detectFace && frame_no==3){
					frame_no=0;
					System.out.println("Face Detecting now!");
					
					MatOfRect front_faces = detect(output);
					Mat outputFaces = new Mat();
					output.copyTo(outputFaces);

					for (Rect rect : front_faces.toArray()) {
						Point center = new Point(rect.x + rect.width * 0.5, rect.y + rect.height * 0.5);
						Imgproc.ellipse(camImage, center, new Size(rect.width * 0.5, rect.height * 0.5), 0, 0, 360,
								new Scalar(0, 255, 0), 4, 8, 0);
					}
					
					
					if (front_faces.toArray().length > 0 && faceNotCovered){
						faceDetectionsCounter++;
						
						//Consider face detected if detected more than twice
						if (faceDetectionsCounter >= 3){
							faceDetectionsCounter = 0;
							noFaceAlert = false;
							detectFace = false;
							SendMail.sendmail_notif=true;
							
							//If alert1 is not given, then start storing video4android | else, close the writer
							if (!alert1given){
								notifThread.notifFrame = camimg;
								notifThread.p = BYTE_FACEFOUND_VDOGENERATING;
								notifThread.myNotifId = myNotifId;
								System.out.println("value of notifId is " + myNotifId);
								notifThread.sendNotif = true;
								
								store_name4android = outputFilename4android + ft.format(dNow) + ".mp4";
								store_activityname = ft.format(dNow);
								//writer4android = ToolFactory.makeWriter(store_name4android);
								//writer4android.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, 640, 480);
								writer4android = new VideoWriter(store_name4android, VideoWriter.fourcc( fourcc.charAt(0), fourcc.charAt(1), fourcc.charAt(2), fourcc.charAt(3)), 20, new Size(640,480), true);
								startTime4android = System.nanoTime();
								timeAndroidVdoStarted = System.currentTimeMillis();
								
							}else {
								writer4android.release();
								notifId2filepaths.put(new Integer(myNotifId), store_name4android);
								notifThread.sendNotif = true;
								notifThread.p = BYTE_FACEFOUND_VDOGENERATED;
								notifThread.myNotifId = myNotifId;
								if(myNotifId < 99)
									myNotifId++;
								else
									myNotifId = 1;
							}
						}
					}
				}
				
				
				//Give alert1 and start writer4android
				if (noFaceAlert && !alert1given && blackCountPercent<85 && (time4-time3)/1000 > 5 ){            //notifthrad dependent
					alert1given = true;
					System.out.println("warn level 1.......................");
					
					notifThread.notifFrame = camimg;
					notifThread.p = BYTE_ALERT1;
					notifThread.myNotifId = myNotifId;
					notifThread.sendNotif = true;
					System.out.println("alert level 1 value of notifId is " + myNotifId);
					
					
					store_name4android = outputFilename4android + ft.format(dNow) + ".mp4";
					store_activityname = ft.format(dNow);
					writer4android = new VideoWriter(store_name4android, VideoWriter.fourcc( fourcc.charAt(0), fourcc.charAt(1), fourcc.charAt(2), fourcc.charAt(3)), 20, new Size(640,480), true);
					//writer4android = ToolFactory.makeWriter(store_name4android);
					//writer4android.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, 640, 480);
					startTime4android = System.nanoTime();
					time3 = System.currentTimeMillis();
					timeAndroidVdoStarted = -1;
					
					//AudioPlaying audioPlaying = new AudioPlaying();
					//audioPlaying.start();
				}
				
				//Give alert2 and close writer4android
				if ((time4 - time3)/1000 > 15 && noFaceAlert && alert1given && !alert2given){
					alert2given = true;
					System.out.println("warn level 2........................");
					writer4android.release();
					notifId2filepaths.put(new Integer(myNotifId), store_name4android);
					notifThread.sendNotif = true;
					notifThread.p = BYTE_ALERT2;
					notifThread.myNotifId = myNotifId;
					System.out.println("alert level 2 value of notifId is " + myNotifId);
					if(myNotifId<99)
						myNotifId++;
					else
						myNotifId = 1;
					noFaceAlert = false;
					detectFace = false;
					SendMail.sendmail_notif = true;
					System.out.println("NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN");
				}
				
				/*//Check for Illumination change
				//if(notifThread.p == BYTE_FACEFOUND_VDOGENERATING | notifThread.p == BYTE_FACEFOUND_VDOGENERATED){
					int contoursPerPercentChange = 0;
					if(blackCountPercent != 0)
						contoursPerPercentChange = (noOfContours/blackCountPercent)*10;
					else
						contoursPerPercentChange = (noOfContours/1)*10;
					System.out.println("....................................contoursPerPercentChange = "+contoursPerPercentChange);
					if(contoursPerPercentChange < 6)
						contoursCheck--;
					if(contoursPerPercentChange > 5)
						contoursCheck++;
					if(contoursCheck>5){
						contoursCheck = 0;
						System.out.println(".............LIGHT CHANGE.............");
						framesRead = 10;
					}
			//	}
			*/
				
				
			}else {
				contoursCheck = 0;
				frameRef = camImage;
				
				//LightChange = false;
				
				dNow = new Date();
				startStoring = true;
				
				if(notifThread.p ==BYTE_FACEFOUND_VDOGENERATING || notifThread.p==BYTE_ALERT1){
					System.out.println("abrupt end...........................");
					writer4android.release();
					notifId2filepaths.put(new Integer(myNotifId), store_name4android);
					notifThread.p = 5;
					notifThread.myNotifId = myNotifId;
					notifThread.sendNotif = true;
					SendMail.sendmail_notif = true;
					SendMail.sendmail_vdo = true;
					SendMail.whichMail = 1;
					System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
					System.out.println("abrupt end value of notifId is " + myNotifId);
					if(myNotifId<99)
						myNotifId++;
					else
						myNotifId = 1;
				}
				
				//Writer close once bg becomes normal
				if (writer_close){
					if (writer != null){
						if(writer.isOpened()){
							writer.release();
							System.out.println("writer has been closed #chillax");
						}
					}
					//writer.close();                               see if this can be used directly
					writer_close = false; 
					alert1given = false;
					alert2given = false;
					noFaceAlert = true;
					timeAndroidVdoStarted = -1;
					SendMail.sendmail_vdo = true;
					System.out.println("VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV");
					once = false;
				}
				
				detectFace = true;
				faceDetectionsCounter = 0;
				frame_no = 0;
				
				//apply bgsubtraction while learning background
				backgroundSubtractorMOG.apply(camImage, fgMask, -1);
			}
			
			if (framesRead < 110) {
				framesRead++;
				System.out.println("frmes_read" + framesRead);
			}
			time4 = System.currentTimeMillis();
			timeNow2 = System.currentTimeMillis();
			System.out.println("                        time : " + (timeNow2 - timeNow1));
			
			//System.out.println("frmes_read" + framesRead);
			timeNow1 = timeNow2;
		}
	}

}
