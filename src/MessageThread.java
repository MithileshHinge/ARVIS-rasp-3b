import java.io.IOException;
//import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;


public class MessageThread extends Thread{

	private int port = 6676;
	private Socket socket;
	private String servername=Main.servername ;
	//final byte BYTE_STOP_LIVEFEED = 4, BYTE_START_LISTEN = 5, BYTE_STOP_LISTEN = 6,BYTE_PLAY_ALARM=7, BYTE_STOP_ALARM=8;
	final byte BYTE_SURV_MODE_ON = 1, BYTE_SURV_MODE_OFF = 3, BYTE_EMAIL_NOTIF_ON = 9, BYTE_EMAIL_NOTIF_OFF = 10,BYTE_PLAY_ALARM=7, BYTE_STOP_ALARM=8,BYTE_STOP_LIVEFEED = 4, BYTE_START_LIVEFEED=2;

	//private static DatagramSocket dataSocket;

	public MessageThread(){	}

	public void run(){
		while(true){
			try{
				socket = new Socket(servername,port);

				int p = socket.getInputStream().read();
				AudioPlaying audioPlaying1 = new AudioPlaying();

				switch(p){
				case BYTE_SURV_MODE_ON:
					System.out.println("this is surveillance mode");
					Main.Surv_Mode=true;
					if (Main.writer != null){
						if(Main.writer.isOpen()){
							Main.writer.close();
						}
					}
					break;
				case BYTE_SURV_MODE_OFF:
					System.out.println("Normal CCTV recording mode");
					Main.Surv_Mode=false;
					Main.checkonce=true;
					break;
				case BYTE_EMAIL_NOTIF_ON:
					SendMail.sendmail = true;
					System.out.println("......email notif turned ON.....");
					break;

				case BYTE_EMAIL_NOTIF_OFF:
					SendMail.sendmail = false;
					System.out.println("......email notif turned OFF.....");
					break;

					/*case BYTE_STOP_LIVEFEED:
					System.out.println("@@@@@@@@@@@@@@@@@Live Feed off kela..........................");
					SendingFrame.livefeed = false;
					break;*/
				case BYTE_START_LIVEFEED:
					System.out.println("@@@@@@@@@@@@@@@@@Live Feed on kela..........................");
					//SendingFrame sendingFrame = new SendingFrame();
					Main.sendingFrame.start();
					break;
					/*case BYTE_START_LISTEN:
					System.out.println("@@@@@@@@@@@@@@@@@Listen on kela.............................");
					SendingFrame.listen = true;
					break;
				case BYTE_STOP_LISTEN:
					System.out.println("@@@@@@@@@@@@@@@@@Listen off kela.............................");
					SendingFrame.listen = false;
					break;*/
				case BYTE_PLAY_ALARM:
					System.out.println("#########################   Alarm on kela ");
					audioPlaying1.play_alarm=true;
					audioPlaying1.start();
					break;
				case BYTE_STOP_ALARM:
					System.out.println("#########################   Alarm off kela ");

					AudioPlaying.clip.stop();
					AudioPlaying.clip.close();
					break;
				}

				socket.getOutputStream().write(1);
				socket.getOutputStream().flush();

				socket.close();
			}catch (IOException e){
				e.printStackTrace();
				continue;
			}
		}
	}
}
