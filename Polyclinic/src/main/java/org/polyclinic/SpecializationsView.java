package org.polyclinic;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class SpecializationsView {
    public static void show(Stage stage, Integer patientId) {
        stage.setTitle("Выбор специализации");

        Button backButton = new Button("← Назад");
        backButton.setOnAction(e -> MainMenu.show(stage, patientId, AuthView.getCurrentUserName()));

        HBox navBox = new HBox(backButton);
        navBox.setPadding(new javafx.geometry.Insets(10));

        ListView<String> listView = new ListView<>();
        listView.setPrefSize(300, 300);
        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        ObservableList<String> items = FXCollections.observableArrayList();
        try {
            List<String> specializations = PolyclinicLogic.getSpecializations();
            items.addAll(specializations);
            listView.setItems(items);
        } catch (Exception e) {
            e.printStackTrace();
        }

        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String selectedSpec = listView.getSelectionModel().getSelectedItem();
                if (selectedSpec != null) {
                    DoctorsView.show(stage, patientId, selectedSpec);
                }
            }
        });

        VBox root = new VBox(10, navBox, listView);
        Scene scene = new Scene(root, 400, 400);
        stage.setScene(scene);
    }
}