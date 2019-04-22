package com.ktar5.texturesplitter;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;

public class Main extends Application {
    public static Window window;
    public Pane mainPane;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("TileMapTest");

        //this makes all stages close and the app exit when the main stage is closed
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });

        mainPane = new Pane();

        //Initialize primary stage window and set to view scene
        Scene scene = new Scene(mainPane, 600, 600);
        primaryStage.setScene(scene);
        primaryStage.setWidth(1280);
        primaryStage.setHeight(720);
        primaryStage.setMinWidth(350);
        primaryStage.setMinHeight(150);
        primaryStage.show();

        CreateWholeTileset.create();

        //Initialize window
        window = scene.getWindow();
    }


}
