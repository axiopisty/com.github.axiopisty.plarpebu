package com.github.axiopisty.plarpebu.javafx.launcher.api;


import org.osgi.annotation.versioning.ProviderType;

import javafx.stage.Stage;

@ProviderType
public interface StageService {
	
	Stage getInstance();
	
}
