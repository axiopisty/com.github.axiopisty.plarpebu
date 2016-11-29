package com.github.axiopisty.plarpebu.application;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import com.github.axiopisty.plarpebu.javafx.launcher.api.StageService;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import osgi.enroute.debug.api.Debug;

@Component(
	name="com.github.axiopisty.plarpebu", 
	service = Plarpebu.class, 
	immediate = true,
	property = {
		Debug.COMMAND_SCOPE + "=plarpebu", 
		Debug.COMMAND_FUNCTION + "=show", 
		Debug.COMMAND_FUNCTION + "=hide" 
	}
)
public class Plarpebu {

	@interface Config {
		String message() default "Welcome to the Plarpebu Karaoke Application!";
		String title() default "Plarpebu";
	}

	@Reference StageService stageService;
	
	@Reference LogService log;
	
	private FileChooser chooser;

	@Activate
	void activate(Config config) {
		System.out.println(config.message());
		Platform.runLater(() -> {
			Stage primaryStage = stageService.getInstance();
			primaryStage.setTitle(config.title());

			chooser = new FileChooser();
			chooser.setTitle("Select an MP3 file.");
			chooser.getExtensionFilters().addAll(
				new ExtensionFilter("Audio Files", "*.mp3")
			);
			
			final Label loadLbl = new Label();
			
			final Button loadBtn = new Button();
			loadBtn.setText("Load");
			loadBtn.setOnAction(event -> {
				File mp3 = chooser.showOpenDialog(primaryStage);
				if(mp3 != null) {
					String file = toString(mp3);
					loadLbl.setText(file);
					log.log(LogService.LOG_DEBUG, "Selected file: " + file);
				}
			});
			
			final Button playBtn = new Button();
			playBtn.setText("Play");
			playBtn.setOnAction(event -> {
				log.log(LogService.LOG_DEBUG, "pressed: play");
			});
			
			final Button pauseBtn = new Button();
			pauseBtn.setText("Pause");
			pauseBtn.setOnAction(event -> {
				log.log(LogService.LOG_DEBUG, "pressed: pause");
			});
			
			final Button stopBtn = new Button();
			stopBtn.setText("Stop");
			stopBtn.setOnAction(event -> {
				log.log(LogService.LOG_DEBUG, "pressed: stop");
			});

			HBox searchBox = new HBox();
			searchBox.getChildren().addAll(loadBtn, loadLbl);
			
			HBox controls = new HBox();
			controls.getChildren().addAll(playBtn, pauseBtn, stopBtn);
			
			VBox pane = new VBox();
			pane.getChildren().addAll(searchBox, controls);
			
			StackPane root = new StackPane();
			root.getChildren().add(pane);

			Scene scene = new Scene(root, 500, 75);
			javafxOsgiHack(scene);
			primaryStage.setScene(scene);
		});
		show();
	}
	
	private String toString(File file) {
		String string;
		try {
			string = file.getCanonicalPath();
		} catch (IOException e) {
			string = file.getName();
		}
		return string;
	} 

	/**
	 * This hack is used to get the CSS styles to work appropriately inside the
	 * OSGi environment. I'm not sure why this is needed, but without it, the
	 * css file is not loaded and the following warning is displayed in the
	 * console:
	 * 
	 * com.sun.javafx.css.StyleManager loadStylesheetUnPrivileged WARNING:
	 * Resource "com/sun/javafx/scene/control/skin/modena/modena.css" not found.
	 * 
	 * {@link http://stackoverflow.com/q/30550688/328275}
	 * 
	 * @param scene
	 */
	private void javafxOsgiHack(Scene scene) {
		Bundle systemBundle = FrameworkUtil.getBundle(getClass()).getBundleContext().getBundle(0);
		URL modena = systemBundle.getResource("com/sun/javafx/scene/control/skin/modena/modena.css");
		scene.getStylesheets().add(modena.toExternalForm());
	}

	@Deactivate
	void deactivate() {
		hide();
	}

	public void show() {
		Platform.runLater(() -> stageService.getInstance().show());
	}

	public void hide() {
		Platform.runLater(() -> stageService.getInstance().hide());
	}
}
