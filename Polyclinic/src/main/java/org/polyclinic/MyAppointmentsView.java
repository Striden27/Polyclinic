package org.polyclinic;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.ResultSet;

public class MyAppointmentsView {
    public static void show(Stage stage, Integer patientId) {
        stage.setTitle("Мои записи");

        Button backButton = new Button("← Назад");
        backButton.setOnAction(e -> MainMenu.show(stage, patientId, AuthView.getCurrentUserName()));

        Button refreshButton = new Button("Обновить");
        refreshButton.setOnAction(e -> show(stage, patientId));

        HBox navBox = new HBox(10, backButton, refreshButton);
        navBox.setPadding(new Insets(10));

        TableView<String[]> table = new TableView<>();

        TableColumn<String[], String> doctorCol = new TableColumn<>("Врач");
        TableColumn<String[], String> specCol = new TableColumn<>("Специализация");
        TableColumn<String[], String> dateCol = new TableColumn<>("Дата");
        TableColumn<String[], String> timeCol = new TableColumn<>("Время");
        TableColumn<String[], String> statusCol = new TableColumn<>("Статус");
        TableColumn<String[], String> roomCol = new TableColumn<>("Кабинет");

        doctorCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue()[1]));
        specCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue()[2]));
        dateCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue()[3]));
        timeCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue()[4]));
        statusCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue()[5]));
        roomCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue()[6]));

        table.getColumns().addAll(doctorCol, specCol, dateCol, timeCol, statusCol, roomCol);

        try {
            ResultSet rs = PolyclinicLogic.getPatientAppointments(patientId);
            ObservableList<String[]> data = FXCollections.observableArrayList();

            while (rs.next()) {
                String[] row = new String[]{
                        rs.getString("appointment_id"),
                        rs.getString("doctor_name"),
                        rs.getString("specialization"),
                        rs.getString("appointment_date"),
                        rs.getString("appointment_time"),
                        rs.getString("status"),
                        rs.getString("room")
                };
                data.add(row);
            }
            table.setItems(data);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Button cancelButton = new Button("Отменить запись");
        cancelButton.setOnAction(e -> {
            String[] selectedRow = table.getSelectionModel().getSelectedItem();
            if (selectedRow != null) {
                int appointmentId = Integer.parseInt(selectedRow[0]);
                boolean success = PolyclinicLogic.cancelAppointment(appointmentId);

                if (success) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Успех");
                    alert.setHeaderText(null);
                    alert.setContentText("Запись успешно отменена");
                    alert.showAndWait();
                    show(stage, patientId);
                }
            }
        });

        VBox buttonsBox = new VBox(10, cancelButton);
        buttonsBox.setPadding(new Insets(10));

        VBox root = new VBox(10, navBox, table, buttonsBox);
        Scene scene = new Scene(root, 800, 500);
        stage.setScene(scene);
    }
}