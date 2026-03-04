package org.polyclinic;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ProfileView {
    public static void show(Stage stage, Integer patientId) {
        stage.setTitle("Редактирование профиля");

        String[] patientData = getPatientData(patientId);
        if (patientData == null) {
            showAlert("Ошибка", "Не удалось загрузить данные профиля");
            return;
        }

        Button backButton = new Button("← Назад");
        backButton.setOnAction(e -> MainMenu.show(stage, patientId, AuthView.getCurrentUserName()));

        HBox navBox = new HBox(backButton);
        navBox.setPadding(new Insets(10));

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text title = new Text("Редактирование профиля");
        title.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(title, 0, 0, 2, 1);

        // Логин (только для просмотра)
        Label loginLabel = new Label("Логин:");
        grid.add(loginLabel, 0, 1);

        TextField loginField = new TextField(patientData[0]);
        loginField.setEditable(false);
        grid.add(loginField, 1, 1);

        // ФИО
        Label nameLabel = new Label("ФИО:");
        grid.add(nameLabel, 0, 2);

        TextField nameField = new TextField(patientData[1]);
        grid.add(nameField, 1, 2);

        Label phoneLabel = new Label("Телефон:");
        grid.add(phoneLabel, 0, 3);

        TextField phoneField = new TextField(patientData[2]);
        grid.add(phoneField, 1, 3);

        Label policyLabel = new Label("Номер полиса:");
        grid.add(policyLabel, 0, 4);

        TextField policyField = new TextField(patientData[3] != null ? patientData[3] : "");
        grid.add(policyField, 1, 4);

        Label birthLabel = new Label("Дата рождения:");
        grid.add(birthLabel, 0, 5);

        TextField birthField = new TextField(patientData[4] != null ? patientData[4] : "");
        grid.add(birthField, 1, 5);
        birthField.setPromptText("ДД.ММ.ГГГГ");

        Button saveButton = new Button("Сохранить изменения");
        saveButton.setOnAction(e -> {
            if (updatePatientData(patientId, nameField.getText(), phoneField.getText(),
                    policyField.getText(), birthField.getText())) {
                showAlert("Успех", "Данные профиля успешно обновлены!");
                AuthView.setCurrentUserName(nameField.getText()); // Обновляем имя в системе
                MainMenu.show(stage, patientId, nameField.getText()); // Возвращаемся в меню
            }
        });
        grid.add(saveButton, 1, 7);

        VBox root = new VBox(navBox, grid);
        Scene scene = new Scene(root, 500, 500);
        stage.setScene(scene);
        stage.show();
    }

    private static String[] getPatientData(Integer patientId) {
        String sql = "SELECT login, full_name, phone, policy_number, " +
                "TO_CHAR(birth_date, 'DD.MM.YYYY') as birth_date " +
                "FROM patients WHERE patient_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new String[]{
                        rs.getString("login"),
                        rs.getString("full_name"),
                        rs.getString("phone"),
                        rs.getString("policy_number"),
                        rs.getString("birth_date")
                };
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean updatePatientData(Integer patientId, String fullName,
                                             String phone, String policyNumber,
                                             String birthDate) {
        if (fullName.isEmpty()) {
            showAlert("Ошибка", "Поле ФИО обязательно для заполнения");
            return false;
        }

        String sql = "UPDATE patients SET full_name = ?, phone = ?, " +
                "policy_number = ?, birth_date = ? WHERE patient_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, fullName);
            pstmt.setString(2, phone);
            pstmt.setString(3, policyNumber.isEmpty() ? null : policyNumber);

            if (!birthDate.isEmpty()) {
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy");
                    java.util.Date date = sdf.parse(birthDate);
                    pstmt.setDate(4, new java.sql.Date(date.getTime()));
                } catch (Exception e) {
                    showAlert("Ошибка", "Неверный формат даты рождения. Используйте ДД.ММ.ГГГГ");
                    return false;
                }
            } else {
                pstmt.setDate(4, null);
            }

            pstmt.setInt(5, patientId);
            pstmt.executeUpdate();
            return true;

        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось обновить данные профиля");
            e.printStackTrace();
            return false;
        }
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}