package com.github.axiopisty.plarpebu.mediaplayer.mp3.provider;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import static java.util.Optional.*;

import org.osgi.service.component.annotations.Component;

import com.github.axiopisty.plarpebu.mediaplayer.api.MediaPlayer;
import com.github.axiopisty.plarpebu.mediaplayer.mp3.provider.internal.MP3Player;

import osgi.enroute.debug.api.Debug;

@Component(
	name = "com.github.axiopisty.plarpebu.mediaplayer.mp3",
	service = MediaPlayer.class, 
	immediate = true,
	property = {
		Debug.COMMAND_SCOPE + "=mp3",
		Debug.COMMAND_FUNCTION + "=load",
		Debug.COMMAND_FUNCTION + "=play",
		Debug.COMMAND_FUNCTION + "=pause",
		Debug.COMMAND_FUNCTION + "=stop",
		Debug.COMMAND_FUNCTION + "=enableDynamicWeaving",
	}
)
public class KaraokePlayer implements MediaPlayer {
	
	private MP3Player player;
	private File mp3;
	private Thread thread;
	private boolean enableDynamicWeaving = false;
	
	public void load(String path) {
		load(new File(path));
	}

	@Override
	public void load(File file) {
		initialize(file);
	}
	
	private synchronized void initialize(File mp3) {
		if(mp3 == null) {
			return;
		}
		ofNullable(player).ifPresent(p -> {
			p.stop();
			clear();
		});

		try {
			this.mp3 = mp3;
			player = new MP3Player(mp3, enableDynamicWeaving);
			thread = new Thread(() -> {
				player.run();
				clear();
			});
			thread.start();
		} catch (LineUnavailableException | IOException | UnsupportedAudioFileException  e) {
			e.printStackTrace();
			clear();
		} catch (Exception e) {
			e.printStackTrace();
			clear();
		}
	}
	
	private void clear() {
		player = null; 
		mp3 = null;
		thread = null; 
	}
	
	@Override
	public void play() {
		ofNullable(player).ifPresent(MP3Player::play);
	}

	@Override
	public void pause() {
		ofNullable(player).ifPresent(MP3Player::pause);
	}

	@Override
	public void stop() {
		ofNullable(player).ifPresent(MP3Player::stop);
	}
	
	@Override
	synchronized public void enableDynamicWeaving(boolean enabled) {
		this.enableDynamicWeaving = enabled;
		initialize(mp3);
	}

}
