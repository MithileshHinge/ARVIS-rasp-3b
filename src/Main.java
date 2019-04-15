import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
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

	private static CascadeClassifier frontal_face_cascade;
	private static CascadeClassifier mouthCascade;
	static int frame_no = 0;
	private static boolean detectFace = true;
	private static boolean faceNotCovered = false;
	public static String ROOT_DIR = "";
	public static String outputFilename = "";
	public static String outputFilename4android = "";
	public static VideoWriter writer;
	public static boolean startStoring = true;
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
	BYTE_CAMERA_INACTIVE = 7,
	BYTE_MEMORY_ALERT = 8;
	public static VideoWriter writer4android;
	public static boolean writer_close4android = false;
	public static String store_name4android;
	public static String store_activityname;
	public static boolean once =false;

	static long time3, time4;
	public static long timeAndroidVdoStarted = -1;
	public static boolean j = true;
	public static boolean checkonce =true;
	public static Process proc;
	public static boolean alert2given = false;
	public static boolean alert1given = false;
	public static volatile int framesRead = 0;
	public static boolean Surv_Mode=true;
	public static String fourcc = "X264";   
	public volatile static ConcurrentHashMap<Integer, String> notifId2filepaths = new ConcurrentHashMap<>();
	private static boolean give_system_ready_once = true;
	public static SendingFrame sendingFrame;
	public static SendingAudio sendingAudio;
	public static String servername = "13.233.111.181";
	public static String HASH_ID;
	public static String username, password;
	public static NotificationThread notifThread;
	public static SendingVideo sendingVideo;
	public static SendMail sendMail;
	public static int contoursCheck = 0;
	public static Mat saveImage;
	public static ArrayList<Mat> past5frames;
	public static DetectPerson detectPerson;
	public static int lightChangeVerified;
	public static long lightChangeDecisionOutdatedTimer;
	public static boolean flagRecordingStarted = false;
	static long recordingTimeStart, currentRecordingTime;
	public static final int FRAMES_TO_LEARN = 60;
	public static int blackCountThres;
	public static int systemNormalCountdown = 0;
	public static boolean systemStagnantcheck = true;
	static long stagnantStart;
	public static String configFile = "/home/pi/arvis/config.txt";

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public static void main(String[] args) throws Exception {
		if (args.length >0)
			configFile = args[0];
		getConfig(configFile);
		resetAutoExp();
		startProgram();
	}

	public static void getConfig(String configFile) throws FileNotFoundException{
		File config = new File(configFile);
		Scanner scnr = new Scanner(config);
		servername = scnr.nextLine();
		HASH_ID = scnr.nextLine();		
		ROOT_DIR = scnr.nextLine();
		System.out.println(ROOT_DIR);
		outputFilename = ROOT_DIR + "//videos//";
		outputFilename4android = ROOT_DIR + "//videos4android//";
		String ThresVal = scnr.nextLine();
		scnr.close();
		blackCountThres = Integer.parseInt(ThresVal);
	}

	public static MatOfRect detect(Mat inputframe) {
		faceNotCovered=false;
		Mat mRgba = new Mat();
		Mat mGrey = new Mat();
		MatOfRect front_faces = new MatOfRect();
		inputframe.copyTo(mRgba);
		inputframe.copyTo(mGrey);
		Imgproc.cvtColor(mRgba, mGrey, Imgproc.COLOR_BGR2GRAY);
		Imgproc.equalizeHist(mGrey, mGrey);
		frontal_face_cascade.detectMultiScale(mGrey, front_faces, 1.1, 3, 0, new Size(30, 30), new Size());
		Rect[] facesArray = front_faces.toArray();
		for (int i = 0; i < facesArray.length; i++) {
			Point centre1 = new Point(facesArray[i].x + facesArray[i].width * 0.5,facesArray[i].y + facesArray[i].height * 0.5);
			Mat faceROI = mGrey.submat(facesArray[i]);
			MatOfRect mouth = new MatOfRect();
			mouthCascade.detectMultiScale(faceROI, mouth);
			Rect[] mouthArray = mouth.toArray();
			for (int k = 0 ; k < mouthArray.length; k++) {
				Point centre3 = new Point(facesArray[i].x + mouthArray[k].x + mouthArray[k].width * 0.5,
						facesArray[i].y + mouthArray[k].y + mouthArray[k].height * 0.5);
				if (centre3.y > centre1.y ){
					faceNotCovered=true;
				}
			}
		}
		return front_faces;
	}

	public static Mat bufferedImageToMat(BufferedImage bi) {
		Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
		byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
		mat.put(0, 0, data);
		return mat;
	}

	public static BufferedImage matToBufferedImage(Mat frame) {
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
		String dateTime = sdf.format(Calendar.getInstance().getTime()); 
		Graphics2D g2 = dest.createGraphics();
		Color darkgreen= new Color(255,255,255);
		g2.drawImage(toEdit, 0, 0, toEdit.getWidth(), toEdit.getHeight(), null);
		g2.setColor(darkgreen);
		g2.setFont(new Font("TimesRoman", Font.PLAIN, 25)); 
		g2.drawString(dateTime, 350, 450);
		return dest;
	}

	public static void startProgram() throws Exception{

		frame_no = 0;
		detectFace = true;
		faceNotCovered = false;
		startStoring = true;
		writer_close = false;
		myNotifId = 1;
		writer_close4android = false;
		timeAndroidVdoStarted = -1;
		j = true;
		checkonce = true;
		alert2given = false;
		alert1given = false;
		framesRead = 0;
		notifId2filepaths = new ConcurrentHashMap<>();
		give_system_ready_once = true;
		past5frames = new ArrayList<Mat>(5);
		lightChangeVerified = 0;
		notifThread = new NotificationThread();
		notifThread.start();
		ConnectThread connThread = new ConnectThread();
		connThread.start();
		CheckSpace checkSpace = new CheckSpace();
		checkSpace.start();
		SendMail t3 = new SendMail();
		t3.start();
		AudioPlaying audioPlaying = new AudioPlaying();
		VideoCapture capture = new VideoCapture(0);
		if (!capture.isOpened()) {
			return;
		}
		BackgroundSubtractorMOG2 backgroundSubtractorMOG = Video.createBackgroundSubtractorMOG2(FRAMES_TO_LEARN, 16, false);
		frontal_face_cascade = new CascadeClassifier(ROOT_DIR + "//haarcascades//haarcascade_frontalface_alt.xml");
		mouthCascade = new CascadeClassifier(ROOT_DIR + "//haarcascades//Mouth.xml");
		if (frontal_face_cascade.empty()) {
			return;
		}
		if(mouthCascade.empty()){
			return;
		}
		int faceDetectionsCounter = 0;
		boolean noFaceAlert = true;
		
		while(true){
			Mat camImage = new Mat();
			capture.read(camImage);
			if (camImage.empty()){
				if (writer != null){
					if(writer.isOpened()){
						writer.release();
						}
				}
				if (writer4android != null){
					if(writer4android.isOpened()){
						writer4android.release();
						}
				}

				notifThread.p = BYTE_CAMERA_INACTIVE;
				notifThread.myNotifId = myNotifId;
				notifThread.sendNotif = true;
				if(myNotifId < 99)
					myNotifId++;
				else
					myNotifId = 1;
				SendMail.sendmail_notif = true;
				SendMail.sendmail_vdo = true;
				SendMail.sendmail = true;
				SendMail.whichMail = 2;
				System.out.println(".............Main: whichMail = "+SendMail.whichMail);
				
				// TODO: Sound a warning alarm that camera is disconnected 

				return;
			}
			past5frames.add(camImage);
			if (past5frames.size() > 5)
				past5frames.remove(0);

			if (!Surv_Mode && checkonce){
				time3 = System.currentTimeMillis();                            
				store_name = outputFilename + ft.format(dNow) + ".mp4";
				store_file_name = ft.format(dNow);
				writer =new VideoWriter(store_name, VideoWriter.fourcc( fourcc.charAt(0), fourcc.charAt(1), fourcc.charAt(2), fourcc.charAt(3)), 20, new Size(640,480), true);
				checkonce =false;
			}
			if (!Surv_Mode){
				if (writer != null){
					if(writer.isOpened()){
						writer.write(camImage);
					}
				}
			}
			Mat fgMask = new Mat();

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
			Mat output = new Mat();
			camImage.copyTo(output, fgMask);
			
			BufferedImage cam_img = matToBufferedImage(camImage);
			BufferedImage camimg = timestampIt(cam_img);
			SendingFrame.frame = camimg;

			if(framesRead==(FRAMES_TO_LEARN - 5) && give_system_ready_once){
				give_system_ready_once = false;
				audioPlaying.system_ready=true;
				audioPlaying.start();
			}
			if (blackCountPercent < blackCountThres && framesRead >= FRAMES_TO_LEARN) {
				systemNormalCountdown = 0;
				if(flagRecordingStarted){
					currentRecordingTime = System.currentTimeMillis();
					}
				
				if (startStoring && Surv_Mode){
					flagRecordingStarted = true;
					recordingTimeStart = System.currentTimeMillis();
					time3 = System.currentTimeMillis();
					store_name = outputFilename + ft.format(dNow) + ".mp4";
					store_file_name = ft.format(dNow);
					SendMail.sendmail_vdo = false;
					writer =new VideoWriter(store_name, VideoWriter.fourcc( fourcc.charAt(0), fourcc.charAt(1), fourcc.charAt(2), fourcc.charAt(3)), 20, new Size(640,480), true);
					writer_close = true;
					startStoring = false;

				}

				if (blackCountPercent < 70 && lightChangeVerified == 0){
					Mat camImageGray = new Mat();
					Imgproc.cvtColor(camImage, camImageGray, Imgproc.COLOR_BGR2GRAY);
					Mat camImageGrayInitial = new Mat();
					Imgproc.cvtColor(past5frames.get(2), camImageGrayInitial, Imgproc.COLOR_BGR2GRAY);
					final int patchSize = 40;
					final double changeThresh = 5;
					final double numBlocksThreshPercent = 0.7;
					int changedBlocks = 0;

					for (int i=0; i<camImageGray.rows()/patchSize; i++){
						for (int j=0; j<camImageGray.cols()/patchSize; j++){
							Mat patch1 = camImageGray.submat(i*patchSize, (i+1)*patchSize, j*patchSize, (j+1)*patchSize);
							Mat patch2 = camImageGrayInitial.submat(i*patchSize, (i+1)*patchSize, j*patchSize, (j+1)*patchSize);

							Scalar mean1 = Core.mean(patch1);
							Scalar mean2 = Core.mean(patch2);

							if (Math.abs(mean1.val[0] - mean2.val[0]) > changeThresh){
								changedBlocks++;
							}
						}
					}

					if (changedBlocks > numBlocksThreshPercent * (camImage.rows()/patchSize * camImage.cols()/patchSize)){
						lightChangeVerified = 1;
						
						framesRead=0;
						resetAutoExp();
						notifThread.p = BYTE_LIGHT_CHANGE;
						notifThread.myNotifId = myNotifId;
						notifThread.sendNotif = true;
					}else {
						lightChangeVerified = -1;
						lightChangeDecisionOutdatedTimer = System.currentTimeMillis();
					}
				}

				if (lightChangeVerified == -1 && (System.currentTimeMillis() - lightChangeDecisionOutdatedTimer)/1000 > 5*60){ //recheck light change after 5 mins
					lightChangeVerified = 0;
				}

				if(Surv_Mode){
					if (writer != null){
						if(writer.isOpened()){
							writer.write(bufferedImageToMat(camimg));
						}
					}
				}
				if (writer4android != null){
					if(writer4android.isOpened()){
						if (timeAndroidVdoStarted!=-1 && (System.currentTimeMillis()-timeAndroidVdoStarted)/1000 >= 3){
							writer4android.release();
							notifId2filepaths.put(new Integer(myNotifId), store_name4android);
							notifThread.p = BYTE_FACEFOUND_VDOGENERATED;
							notifThread.myNotifId = myNotifId;
							notifThread.sendNotif = true;
							if(myNotifId < 99)
								myNotifId++;
							else
								myNotifId = 1;
						}else {
							saveImage=bufferedImageToMat(camimg);
							if (writer4android != null){
								if(writer4android.isOpened()){
									writer4android.write(saveImage);
								}
							}
						}
					}
				}
				frame_no++;

				if (detectFace && frame_no==3){
					frame_no=0;
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
						if (faceDetectionsCounter >= 3){
							faceDetectionsCounter = 0;
							noFaceAlert = false;
							detectFace = false;
							SendMail.sendmail_notif=true;
							if (!alert1given){
								notifThread.notifFrame = camimg;
								notifThread.p = BYTE_FACEFOUND_VDOGENERATING;
								notifThread.myNotifId = myNotifId;
								notifThread.sendNotif = true;

								store_name4android = outputFilename4android + ft.format(dNow) + ".mp4";
								store_activityname = ft.format(dNow);
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
				if (noFaceAlert && !alert1given && blackCountPercent<93 && (time4-time3)/1000 > 5 ){            //notifthrad dependent
					alert1given = true;
					notifThread.notifFrame = camimg;
					notifThread.p = BYTE_ALERT1;
					notifThread.myNotifId = myNotifId;
					notifThread.sendNotif = true;
					store_name4android = outputFilename4android + ft.format(dNow) + ".mp4";
					store_activityname = ft.format(dNow);
					writer4android = new VideoWriter(store_name4android, VideoWriter.fourcc( fourcc.charAt(0), fourcc.charAt(1), fourcc.charAt(2), fourcc.charAt(3)), 20, new Size(640,480), true);
					startTime4android = System.nanoTime();
					time3 = System.currentTimeMillis();
					timeAndroidVdoStarted = -1;
				}

				//Give alert2 and close writer4android
				if ((time4 - time3)/1000 > 15 && noFaceAlert && alert1given && !alert2given){
					alert2given = true;
					writer4android.release();
					notifId2filepaths.put(new Integer(myNotifId), store_name4android);
					notifThread.sendNotif = true;
					notifThread.p = BYTE_ALERT2;
					notifThread.myNotifId = myNotifId;
					if(myNotifId < 99)
						myNotifId++;
					else
						myNotifId = 1;
					noFaceAlert = false;
					detectFace = false;
					SendMail.sendmail_notif = true;
					}

				}else {

				if(systemNormalCountdown<8){
					systemNormalCountdown++;
				}else {
					if(System.currentTimeMillis() - stagnantStart > 1000*3600){
						stagnantStart = System.currentTimeMillis();
						framesRead = 0;
						resetAutoExp();
						}

					flagRecordingStarted = false;
					startStoring = true;
					//Writer close once bg becomes normal
					if (writer_close ){
						stagnantStart = System.currentTimeMillis();
						if (writer != null){
							if(writer.isOpened()){
								writer.release();
								}
						}
						writer_close = false; 
						alert1given = false;
						alert2given = false;
						noFaceAlert = true;
						timeAndroidVdoStarted = -1;
						SendMail.whichMail = 1;
						SendMail.sendmail_vdo = true;
						}
				}

				contoursCheck = 0;

				dNow = new Date();

				if(notifThread.p ==BYTE_FACEFOUND_VDOGENERATING || notifThread.p==BYTE_ALERT1){
					writer4android.release();
					notifId2filepaths.put(new Integer(myNotifId), store_name4android);
					notifThread.p = 5;
					notifThread.myNotifId = myNotifId;
					notifThread.sendNotif = true;
					if(myNotifId < 99)
						myNotifId++;
					else
						myNotifId = 1;
				}


				detectFace = true;
				faceDetectionsCounter = 0;
				frame_no = 0;

				lightChangeVerified = 0;
				if (detectPerson != null){
					detectPerson.detect = false;
					detectPerson = null;
				}

				backgroundSubtractorMOG.apply(camImage, fgMask, -1);
			}
			System.out.println("total recording time :" + (currentRecordingTime-recordingTimeStart)/1000);
			if((currentRecordingTime-recordingTimeStart)/60000 > 2){
				recordingTimeStart = currentRecordingTime;
				framesRead = 0;
				resetAutoExp();
			}
			if (framesRead < FRAMES_TO_LEARN) {
				framesRead++;
				}
			time4 = System.currentTimeMillis();
			
		}
	}

	public static void resetAutoExp(){
		int retVal = 1;
		while(retVal != 0){
			try{
				String[] command = { "v4l2-ctl", "-c", "exposure_auto=3" };
				ProcessBuilder pb = new ProcessBuilder(command);
				pb.redirectErrorStream(true);
				Process proc = pb.start();
				BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				String line;
				while ((line = reader.readLine()) != null)
				retVal = proc.waitFor();
				Thread.sleep(700);					
			}catch (InterruptedException | IOException e1){
				e1.printStackTrace();
			}
		}
		retVal = 1;
		while(retVal != 0){
			String[] command2 = { "v4l2-ctl", "-c", "exposure_auto=1" };
			try {

				ProcessBuilder pb = new ProcessBuilder(command2);
				pb.redirectErrorStream(true);
				Process proc = pb.start();
				BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				String line;
				while ((line = reader.readLine()) != null)
				retVal = proc.waitFor();
				System.out.println(retVal);
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			}
		}
	}
}
