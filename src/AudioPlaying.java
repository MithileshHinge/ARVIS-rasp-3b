import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;


public class AudioPlaying extends Thread{
	public boolean play_alarm = false,system_ready=false; 
	AudioInputStream audioInputStream;
	static String filePath;
		
	// to store current position
    Long currentFrame;
    static Clip clip;
    
	public AudioPlaying(){}
	@Override
	public void run() {
		try {
			//filePath="//home//pi//Desktop//warning.wav";
			filePath="C:\\Users\\Sibhali\\Desktop\\Audio\\warning.wav";
			if(play_alarm){
				//filePath = "//home//pi//Desktop//siren.wav";
				filePath = "C:\\Users\\Sibhali\\Desktop\\Audio\\siren.wav";
			}
			if(system_ready){
				//filePath = "//home//pi//Desktop//System_ready.wav";
				filePath = "C:\\Users\\Sibhali\\Desktop\\Audio\\System_ready.wav";
				system_ready=false;
			}
			 audioInputStream = 
		                AudioSystem.getAudioInputStream(new File(filePath).getAbsoluteFile());
		     
			 DataLine.Info info = new DataLine.Info(Clip.class, audioInputStream.getFormat());
			 
		     // create clip reference
		     clip = (Clip) AudioSystem.getLine(info);
		     
		     // open audioInputStream to the clip
		     clip.open(audioInputStream);
		     
		     if(play_alarm){
		    	 play_alarm = false;
		    	 clip.loop(Clip.LOOP_CONTINUOUSLY);
		     }else{
		    	 clip.loop(0);
		     }
		     
			} catch (Exception e ) {
				System.out.println("Error with playing sound.");
				e.printStackTrace();
			}

		}
		
	}