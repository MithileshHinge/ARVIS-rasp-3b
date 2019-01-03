import java.io.File;
import java.util.Arrays;

public class CheckSpace extends Thread {

	private static File rootDir = new File("/");
	private static File pendriveDir = new File("/media/usb1"); //use when pendriceconnected..do check path
	private static File videosDir = new File("/home/pi/Desktop/videos");
	private static File videos4androidDir = new File("/home/pi/Desktop/videos4android");
	private static long checkTime = 1000;
	private static boolean notifyOnce = true;
	public void run(){
		String totalspacestring = Long.toString(rootDir.getTotalSpace()/1073741824);
		int totalspace = Integer.parseInt(totalspacestring);
		String freespacestring = Long.toString(rootDir.getFreeSpace()/1073741824);
		int freespace  = Integer.parseInt(freespacestring);
		double freeSpacePercent =((double)freespace/(double)totalspace)*100;
		System.out.println("freespace : " + freeSpacePercent);
		
		if (freeSpacePercent < 11){
			//give notif		
			System.out.println(".......................freeSpacePercent < 10");
			Main.notifThread.p = Main.BYTE_MEMORY_ALERT;
			Main.notifThread.memoryLeft = 90;
			
			if(freeSpacePercent < 6){
				Main.notifThread.memoryLeft = 95;
				notifyOnce = true;
				
			}
			Main.notifThread.myNotifId = Main.myNotifId;
			System.out.println("value of notifId is " + Main.myNotifId);
			if(notifyOnce){
				notifyOnce = false;
				Main.notifThread.sendNotif = true;
				if(Main.myNotifId < 99)
					Main.myNotifId++;
				else
					Main.myNotifId = 1;
				// send mail
				SendMail.sendmail_notif = true;
				SendMail.sendmail_vdo = true;
				SendMail.sendmail = true;
				SendMail.whichMail = 3;
				System.out.println(".............CS: whichMail = "+SendMail.whichMail);
			}

		}

		if(freeSpacePercent>80) checkTime=1000*60*60*72;
		else if(freeSpacePercent>60) checkTime=3600000*48;
		else if(freeSpacePercent>40) checkTime=3600000*24;
		else if(freeSpacePercent>20) checkTime=3600000*12;
		else checkTime=3600000;


		try {
			System.out.println("checkTime : "  + checkTime);
			Thread.sleep(checkTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
