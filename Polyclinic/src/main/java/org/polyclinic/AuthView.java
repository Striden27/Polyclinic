package org.polyclinic;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AuthView {
    private static Integer currentUserId = null;
    private static String currentUserName = null;

    public static void show(Stage stage) {
        stage.setTitle("Поликлиника - Вход");

        TabPane tabPane = new TabPane();

        Tab loginTab = new Tab("Вход", createLoginForm(stage));
        loginTab.setClosable(false);

        Tab registerTab = new Tab("Регистрация", createRegisterForm(stage));
        registerTab.setClosable(false);

        tabPane.getTabs().addAll(loginTab, registerTab);

        Scene scene = new Scene(tabPane, 400, 350);
        stage.setScene(scene);
        stage.show();
    }

    private static GridPane createLoginForm(Stage stage) {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text title = new Text("Вход в систему");
        title.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(title, 0, 0, 2, 1);

        Label userLabel = new Label("Логин:");
        grid.add(userLabel, 0, 1);

        TextField userTextField = new TextField();
        grid.add(userTextField, 1, 1);

        Label pwLabel = new Label("Пароль:");
        grid.add(pwLabel, 0, 2);

        PasswordField pwBox = new PasswordField();
        grid.add(pwBox, 1, 2);

        Button loginBtn = new Button("Войти");
        loginBtn.setOnAction(e -> {
            if (authenticate(userTextField.getText(), pwBox.getText())) {
                MainMenu.show(stage, currentUserId, currentUserName);
            } else {
                showAlert("Ошибка", "Неверный логин или пароль");
            }
        });
        grid.add(loginBtn, 1, 4);

        return grid;
    }

    private static GridPane createRegisterForm(Stage stage) {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text title = new Text("Регистрация");
        title.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(title, 0, 0, 2, 1);

        Label userLabel = new Label("Логин:");
        grid.add(userLabel, 0, 1);

        TextField loginField = new TextField();
        grid.add(loginField, 1, 1);

        Label pwLabel = new Label("Пароль:");
        grid.add(pwLabel, 0, 2);

        PasswordField pwBox = new PasswordField();
        grid.add(pwBox, 1, 2);

        Label nameLabel = new Label("ФИО:");
        grid.add(nameLabel, 0, 3);

        TextField nameField = new TextField();
        grid.add(nameField, 1, 3);

        Label phoneLabel = new Label("Телефон:");
        grid.add(phoneLabel, 0, 4);

        TextField phoneField = new TextField();
        grid.add(phoneField, 1, 4);

        Button registerBtn = new Button("Зарегистрироваться");
        registerBtn.setOnAction(e -> {
            if (registerUser(loginField.getText(), pwBox.getText(),
                    nameField.getText(), phoneField.getText())) {
                showAlert("Успех", "Регистрация прошла успешно! Теперь войдите в систему.");
            }
        });
        grid.add(registerBtn, 1, 6);

        return grid;
    }

    private static boolean authenticate(String login, String password) {
        String sql = "SELECT patient_id, full_name FROM patients WHERE login = ? AND password = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, login);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                currentUserId = rs.getInt("patient_id");
                currentUserName = rs.getString("full_name");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean registerUser(String login, String password, String fullName, String phone) {
        if (login.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            showAlert("Ошибка", "Заполните все обязательные поля");
            return false;
        }

        String sql = "INSERT INTO patients (login, password, full_name, phone) VALUES (?, ?, ?, ?)";

        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, login);
            pstmt.setString(2, password);
            pstmt.setString(3, fullName);
            pstmt.setString(4, phone);
            pstmt.executeUpdate();
            return true;

        } catch (Exception e) {
            showAlert("Ошибка", "Логин уже занят или произошла ошибка");
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

    public static Integer getCurrentUserId() {
        return currentUserId;
    }

    public static String getCurrentUserName() {
        return currentUserName;
    }

    public static void setCurrentUserName(String userName) {
        currentUserName = userName;
    }

    public static void logout() {
        currentUserId = null;
        currentUserName = null;
    }
}