package com.github.axiopisty.plarpebu.mediaplayer.api;

import java.io.File;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface MediaPlayer {
	
	void load(File file);
	
	void play();
	
	void pause();
	
	void stop();
	
}
