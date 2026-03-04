package org.polyclinic;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        ScheduleGenerator.initSchedule();

        stage.setTitle("Поликлиника - Система записи");
        stage.setMaximized(true);

        AuthView.show(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}