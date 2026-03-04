package org.polyclinic;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.ResultSet;

public class DoctorsView {
    public static void show(Stage stage, Integer patientId, String specialization) {
        stage.setTitle("Врачи - " + specialization);

        Button backButton = new Button("← Назад");
        backButton.setOnAction(e -> SpecializationsView.show(stage, patientId));

        HBox navBox = new HBox(backButton);
        navBox.setPadding(new javafx.geometry.Insets(10));

        TableView<String[]> table = new TableView<>();

        TableColumn<String[], String> nameCol = new TableColumn<>("Врач");
        TableColumn<String[], String> roomCol = new TableColumn<>("Кабинет");
        TableColumn<String[], String> nextDateCol = new TableColumn<>("Ближайшая запись");

        nameCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue()[1]));
        roomCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue()[2]));
        nextDateCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue()[3]));

        table.getColumns().addAll(nameCol, roomCol, nextDateCol);
        table.setPrefHeight(300);

        nameCol.prefWidthProperty().bind(table.widthProperty().multiply(0.4));
        roomCol.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
        nextDateCol.prefWidthProperty().bind(table.widthProperty().multiply(0.4));

        try {
            ResultSet rs = PolyclinicLogic.getDoctorsBySpecialization(specialization);
            ObservableList<String[]> data = FXCollections.observableArrayList();

            while (rs.next()) {
                String nearestDate = rs.getString("nearest_available_date");
                String displayText;

                if (nearestDate != null) {
                    try {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy");
                        java.util.Date date = sdf.parse(nearestDate);
                        displayText = sdf.format(date);
                    } catch (Exception e) {
                        displayText = nearestDate;
                    }
                } else {
                    displayText = PolyclinicLogic.getNearestAvailableDate(rs.getInt("doctor_id"));
                }

                String[] row = new String[]{
                        rs.getString("doctor_id"),
                        rs.getString("full_name"),
                        rs.getString("room"),
                        displayText
                };
                data.add(row);
            }
            table.setItems(data);
        } catch (Exception e) {
            e.printStackTrace();
        }

        table.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String[] selectedRow = table.getSelectionModel().getSelectedItem();
                if (selectedRow != null) {
                    int doctorId = Integer.parseInt(selectedRow[0]);
                    String doctorName = selectedRow[1];
                    ScheduleView.show(stage, patientId, doctorId, doctorName, specialization);
                }
            }
        });

        VBox root = new VBox(10, navBox, table);
        Scene scene = new Scene(root, 800, 600);

        stage.setScene(scene);
        stage.setMaximized(true);
    }
}