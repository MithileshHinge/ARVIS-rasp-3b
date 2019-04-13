import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;


public class AudioPlaying extends Thread{
	public boolean play_alarm = false,system_ready=false; 
	AudioInputStream audioInputStream;
	static String filePath;
	private static int p = 0;
	// to store current position
    Long currentFrame;
    static Clip clip;
    
	public AudioPlaying(){}
	@Override
	public void run() {
		try {
			filePath=Main.ROOT_DIR +"//warning.wav";
			if(play_alarm){
				filePath = Main.ROOT_DIR +"//siren.wav";
				}
			if(system_ready){
				File noSpeaker = new File(Main.ROOT_DIR + "//noSpeaker.txt");
				Scanner scnr = new Scanner(noSpeaker);
				String valstr = scnr.nextLine();
				p = Integer.parseInt(valstr);
				FileWriter fw = new FileWriter(noSpeaker.getAbsoluteFile());
			    BufferedWriter bw = new BufferedWriter(fw);
			    bw.write("0");
			    bw.close();
			    
				filePath = Main.ROOT_DIR + "//System_ready.wav";
				system_ready=false;
			}
			 audioInputStream = 
		                AudioSystem.getAudioInputStream(new File(filePath).getAbsoluteFile());
		     
			 DataLine.Info info = new DataLine.Info(Clip.class, audioInputStream.getFormat());
			 
		     // create clip reference
		     clip = (Clip) AudioSystem.getLine(info);
		     
		     // open audioInputStream to the clip
		     if (p == 0){
		     clip.open(audioInputStream);
		     }
		     p=0;
		     if(play_alarm){
		    	 play_alarm = false;
		    	 clip.loop(Clip.LOOP_CONTINUOUSLY);
		     }else{
		    	 clip.loop(0);
		     }
		     
			} catch (Exception e ) {
				e.printStackTrace();
			}

		}
		
	}