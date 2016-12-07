package com.github.axiopisty.plarpebu.mediaplayer.mp3.provider.internal;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

public class MP3Player implements Runnable {
	
	private final Object stateLock;

	private MP3Reader reader;

	private PlayerState state;

	public MP3Player(File mp3, boolean enableDynamicWeaving) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
		stateLock = new Object();
		reader = new MP3Reader(mp3, enableDynamicWeaving);

		state = PlayerState.INITIALIZED;
		System.out.println("state = PlayerState.INITIALIZED");
	}
	
	public void play() {
		synchronized(stateLock) {
			state = PlayerState.PLAYING;
			System.out.println("state = PlayerState.PLAYING");
		}
	}
	
	public synchronized void pause() {
		synchronized(stateLock) {
			state = PlayerState.PAUSED;
			System.out.println("state = PlayerState.PAUSED");
		}
	}
	
	public synchronized void stop() {
		synchronized(stateLock) {
			state = PlayerState.STOPPED;
			System.out.println("state = PlayerState.STOPPED");
		}
	}

	@Override
	public void run() {
		System.out.println("MP3Player run entered.");
		reader.openLine();
		
		int bytes = 1;
		byte[] buffer = new byte[16000];
		while (bytes != -1) {
			final PlayerState temp;
			synchronized(stateLock) {
				temp = state;
			}
			
			if (temp == PlayerState.INITIALIZED || temp == PlayerState.PAUSED) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
					Thread.currentThread().interrupt();
				}
				continue;
			} else if (temp == PlayerState.STOPPED) {
				break;
			} else if (temp == PlayerState.PLAYING) {
				bytes = reader.readFromLine(buffer);
			}
		}
		
		reader.closeLine();
		reader.closeAudioInputStream();
		
		System.out.println("MP3Player run exited.");
	}

	private class MP3Reader {

		private boolean dynamicWeavingEnabled;
		
		private AudioInputStream decodedAudioInputStream;
		private AudioFormat decodedAudioFormat;
		private SourceDataLine line;
		
		public MP3Reader(File mp3, boolean dynamicWeavingEnabled) throws UnsupportedAudioFileException, IOException {
			System.out.println("MP3Reader::load: " + mp3.getName());
			this.dynamicWeavingEnabled = dynamicWeavingEnabled;
			System.out.println("MP3Reader::dynamicWeavingEnabled::" + dynamicWeavingEnabled);
			
			final AudioInputStream in = AudioSystem.getAudioInputStream(mp3);
			final AudioFormat format = in.getFormat();
			
			if(dynamicWeavingEnabled) {
				decodedAudioInputStream = AudioSystem.getAudioInputStream(
					new AudioFormat(
						AudioFormat.Encoding.PCM_SIGNED, 
						format.getSampleRate(), 
						16,
						format.getChannels(),
						format.getChannels() * 2,
						format.getSampleRate(),
						false
					),
					in
				);
				
				decodedAudioFormat = decodedAudioInputStream.getFormat();
				
				DataLine.Info info = new DataLine.Info(
					SourceDataLine.class, 
					decodedAudioFormat, 
					AudioSystem.NOT_SPECIFIED
				);
				
				SourceDataLine temp;
				try {
					temp = (SourceDataLine) AudioSystem.getLine(info);
				} catch (LineUnavailableException e) {
					e.printStackTrace();
					temp = null;
				}
				line = temp;
				
			} else {
				decodedAudioInputStream = null;
				decodedAudioFormat = null;
				line = null;
			}
			System.out.println("MP3Reader::constructed");
		}
		
		public void openLine() {
			if(dynamicWeavingEnabled) {
				if (line != null) {
					int bufferSize = line.getBufferSize();
					try {
						line.open(decodedAudioFormat, bufferSize);
						line.start();
					} catch (LineUnavailableException e) {
						e.printStackTrace();
					}
				}
			};
		}
		
		public int readFromLine(final byte[] buffer) {
			int bytes = 0;
			if(dynamicWeavingEnabled) {
				try {
					bytes = decodedAudioInputStream.read(buffer, 0, buffer.length);
					if (bytes >= 0) {
						byte[] pcm = new byte[bytes];
						System.arraycopy(buffer, 0, pcm, 0, bytes);
						line.write(buffer, 0, bytes);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return bytes;
		}

		public void closeLine() {
			if(dynamicWeavingEnabled) {
				line.drain();
				line.stop();
				line.close();
			}
		}

		public void closeAudioInputStream() {
			if(dynamicWeavingEnabled) {
				try {
					if (decodedAudioInputStream != null) {
						decodedAudioInputStream.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

}