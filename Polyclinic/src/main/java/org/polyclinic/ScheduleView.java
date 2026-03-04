package org.polyclinic;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ScheduleView {
    public static void show(Stage stage, Integer patientId, Integer doctorId,
                            String doctorName, String specialization) {
        stage.setTitle("Запись к врачу: " + doctorName);

        Button backButton = new Button("← Назад");
        backButton.setOnAction(e -> DoctorsView.show(stage, patientId, specialization));

        HBox navBox = new HBox(backButton);
        navBox.setPadding(new Insets(10));

        Label titleLabel = new Label("Врач: " + doctorName + " (" + specialization + ")");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label infoLabel = new Label("Кабинет: " + getDoctorRoom(doctorId));
        infoLabel.setStyle("-fx-font-size: 14px;");

        ComboBox<String> dateComboBox = new ComboBox<>();
        dateComboBox.setPromptText("Выберите дату");

        Label dateLabel = new Label("Доступные даты для записи:");
        VBox dateBox = new VBox(5, dateLabel, dateComboBox);
        dateBox.setPadding(new Insets(10));

        ListView<String> timeListView = new ListView<>();
        timeListView.setPrefHeight(200);

        Label timeLabel = new Label("Доступное время:");
        VBox timeBox = new VBox(5, timeLabel, timeListView);
        timeBox.setPadding(new Insets(10));

        Button bookButton = new Button("Записаться на прием");
        bookButton.setDisable(true);

        try {
            List<String> availableDates = PolyclinicLogic.getAvailableDates(doctorId);
            ObservableList<String> dateItems = FXCollections.observableArrayList(availableDates);
            dateComboBox.setItems(dateItems);

            if (availableDates.isEmpty()) {
                dateComboBox.setPromptText("Нет доступных дат для записи");
                dateComboBox.setDisable(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        dateComboBox.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null && !newDate.isEmpty()) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                    LocalDate selectedLocalDate = LocalDate.parse(newDate, formatter);
                    Date sqlDate = Date.valueOf(selectedLocalDate);

                    updateAvailableTimes(timeListView, doctorId, sqlDate, bookButton);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        timeListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldTime, newTime) -> {
                    bookButton.setDisable(newTime == null || newTime.startsWith("Нет"));
                }
        );

        bookButton.setOnAction(e -> {
            String selectedTime = timeListView.getSelectionModel().getSelectedItem();
            String selectedDateStr = dateComboBox.getValue();

            if (selectedTime != null && selectedDateStr != null && !selectedTime.startsWith("Нет")) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                    LocalDate selectedLocalDate = LocalDate.parse(selectedDateStr, formatter);
                    Date sqlDate = Date.valueOf(selectedLocalDate);
                    Time sqlTime = Time.valueOf(selectedTime + ":00");

                    boolean success = PolyclinicLogic.bookAppointment(
                            patientId, doctorId, sqlDate, sqlTime
                    );

                    if (success) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Успех");
                        alert.setHeaderText(null);
                        alert.setContentText("Вы успешно записались на прием!\n" +
                                "Врач: " + doctorName + "\n" +
                                "Дата: " + selectedDateStr + "\n" +
                                "Время: " + selectedTime + "\n" +
                                "Кабинет: " + getDoctorRoom(doctorId));
                        alert.showAndWait();

                        updateAvailableTimes(timeListView, doctorId, sqlDate, bookButton);
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Ошибка");
                        alert.setHeaderText(null);
                        alert.setContentText("Это время уже занято. Пожалуйста, выберите другое время.");
                        alert.showAndWait();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        VBox bookingBox = new VBox(15, titleLabel, infoLabel, dateBox, timeBox, bookButton);
        bookingBox.setPadding(new Insets(20));

        VBox root = new VBox(navBox, bookingBox);
        Scene scene = new Scene(root, 500, 550);
        stage.setScene(scene);
    }

    private static void updateAvailableTimes(ListView<String> listView,
                                             Integer doctorId,
                                             Date date,
                                             Button bookButton) {
        try {
            List<String> availableTimes = PolyclinicLogic.getAvailableTimes(doctorId, date);

            ObservableList<String> times = FXCollections.observableArrayList(availableTimes);
            listView.setItems(times);
            listView.getSelectionModel().clearSelection();
            bookButton.setDisable(true);
            
            if (times.isEmpty()) {
                times.add("Нет свободного времени на эту дату");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getDoctorRoom(Integer doctorId) {
        String sql = "SELECT room FROM doctors WHERE doctor_id = ?";
        try (var conn = db.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            var rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("room");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Не указан";
    }
}