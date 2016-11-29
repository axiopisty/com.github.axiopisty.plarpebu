package com.github.axiopisty.plarpebu.javafx.launcher.provider;

import com.github.axiopisty.plarpebu.javafx.launcher.api.StageService;

import javafx.stage.Stage;

public class StageProvider implements StageService {

	private final Stage stage;

	public StageProvider(Stage stage) {
		this.stage = stage;
	}

	@Override
	public Stage getInstance() {
		return stage;
	}
	
}