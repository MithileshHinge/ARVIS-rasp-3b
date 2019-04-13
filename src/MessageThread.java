import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;


public class MessageThread extends Thread{

	private int port = 6676;
	private Socket socket;
	private String servername=Main.servername ;
	final byte BYTE_SURV_MODE_ON = 1,
			BYTE_SURV_MODE_OFF = 3, 
			BYTE_EMAIL_NOTIF_ON = 9, 
			BYTE_EMAIL_NOTIF_OFF = 10,
			BYTE_PLAY_ALARM=7, 
			BYTE_STOP_ALARM=8,
			//BYTE_STOP_LIVEFEED = 4, 
			BYTE_START_LIVEFEED=2, 
			BYTE_RESTART=11,
			BYTE_START_AUDIO = 13,
			BYTE_START_VIDEO_DOWNLOAD = 14;
		
	volatile boolean end = false;
	
	public MessageThread(){	}

	public void run(){
		while(!end){
			try{
				int p;
				socket = new Socket(servername,port);
				socket.setSoTimeout(10000);
				try{
					p = socket.getInputStream().read();
				}catch(SocketTimeoutException e){
					e.printStackTrace();
					socket.close();
					continue;
				}
				AudioPlaying audioPlaying1 = new AudioPlaying();

				switch(p){
				case BYTE_SURV_MODE_ON:
					Main.Surv_Mode=true;
					if (Main.writer != null){
						if(Main.writer.isOpened()){
							Main.writer.release();
						}
					}
					break;
				case BYTE_SURV_MODE_OFF:
					Main.Surv_Mode=false;
					Main.checkonce=true;
					break;
					
				case BYTE_EMAIL_NOTIF_ON:
					SendMail.sendmail = true;
					break;

				case BYTE_EMAIL_NOTIF_OFF:
					SendMail.sendmail = false;
					break;

					/*case BYTE_STOP_LIVEFEED:
					System.out.println("@@@@@@@@@@@@@@@@@Live Feed off kela..........................");
					SendingFrame.livefeed = false;
					break;*/
				case BYTE_START_LIVEFEED:
					//SendingFrame sendingFrame = new SendingFrame();
					Main.sendingFrame = new SendingFrame();
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
					audioPlaying1.play_alarm=true;
					audioPlaying1.start();
					break;
				case BYTE_STOP_ALARM:
					if(AudioPlaying.clip.isOpen() || AudioPlaying.clip.isActive())
					{
						AudioPlaying.clip.stop();
						AudioPlaying.clip.close();
					}
					
					break;
				case BYTE_RESTART:
					break;
					
				case BYTE_START_AUDIO :
					Main.sendingAudio = new SendingAudio();
					Main.sendingAudio.start();
					break;
					
				case BYTE_START_VIDEO_DOWNLOAD :
					Main.sendingVideo = new SendingVideo();
					Main.sendingVideo.start();
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
	
	public void end(){
		this.end = true;
	}
}
