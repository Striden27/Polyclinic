package org.polyclinic;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainMenu {
    private static Stage mainStage;
    private static Integer currentUserId;
    private static String currentUserName;

    public static void show(Stage stage, Integer userId, String userName) {
        mainStage = stage;
        currentUserId = userId;
        currentUserName = userName;

        stage.setTitle("Поликлиника - Главное меню");

        Label welcomeLabel = new Label("Добро пожаловать, " + userName + "!");
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button specButton = new Button("Записаться к врачу");
        specButton.setPrefSize(200, 50);
        specButton.setOnAction(e -> SpecializationsView.show(stage, userId));

        Button myAppointmentsButton = new Button("Мои записи");
        myAppointmentsButton.setPrefSize(200, 50);
        myAppointmentsButton.setOnAction(e -> MyAppointmentsView.show(stage, userId));

        Button profileButton = new Button("Редактировать профиль");
        profileButton.setPrefSize(200, 50);
        profileButton.setOnAction(e -> ProfileView.show(stage, userId));

        Button logoutButton = new Button("Выйти");
        logoutButton.setPrefSize(200, 30);
        logoutButton.setOnAction(e -> {
            AuthView.logout();
            AuthView.show(stage);
        });

        HBox navBox = new HBox(10);
        navBox.setPadding(new Insets(10));

        VBox mainBox = new VBox(20, welcomeLabel, specButton, myAppointmentsButton, profileButton, logoutButton);
        mainBox.setPadding(new Insets(40));
        mainBox.setAlignment(javafx.geometry.Pos.CENTER);

        VBox root = new VBox(navBox, mainBox);
        Scene scene = new Scene(root, 500, 450);
        stage.setScene(scene);
        stage.show();
    }

    public static void addBackButton(Stage stage, Runnable backAction) {
        Button backButton = new Button("← Назад");
        backButton.setOnAction(e -> backAction.run());

        HBox navBox = new HBox(backButton);
        navBox.setPadding(new Insets(10));
    }
}