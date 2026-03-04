# Polyclinic

## 📌 Описание

Учебный проект на Java для автоматизации работы поликлиники.
Приложение реализует управление пациентами, врачами и записями на приём с использованием реляционной базы данных.

Проект демонстрирует работу с JDBC и подключение к базе данных.

---

## 🛠️ Используемые технологии

* Java 11+
* PostgreSQL
* JDBC
* Maven
* Git

---

## ⚙️ Требования

Перед запуском необходимо установить:

* Java 11 или выше
* Maven
* PostgreSQL

Проверить версии можно командами:

```bash
java -version
mvn -version
```

---

## 🗄️ Настройка базы данных

1. Запустить PostgreSQL
2. Создать базу данных:

```sql
CREATE DATABASE clinicdb;
```

3. В коде проекта указать параметры подключения:

```java
String url = "jdbc:postgresql://localhost:5432/clinicdb";
String user = "your_username";
String password = "your_password";
```

---

## 🚀 Сборка проекта

Перейти в папку проекта:

```bash
cd Polyclinic
```

Собрать проект:

```bash
mvn clean package
```

После успешной сборки в папке `target/` появится `.jar` файл.

---

## ▶ Запуск проекта

### Вариант 1 — через Maven

```bash
mvn exec:java
```

(если в pom.xml настроен exec-plugin)

---

### Вариант 2 — через jar-файл

```bash
java -jar target/Polyclinic-1.0-SNAPSHOT.jar
```

(название jar может немного отличаться — смотри в папке target)

---

## 📁 Структура проекта

```
Polyclinic/
├── src/
│   └── main/
│       ├── java/
│       └── resources/
├── pom.xml
├── README.md
└── LICENSE
```

---

## 👨‍💻 Автор

Стрилько Денис
Java backend trainee

---

## 📄 Лицензия

Проект распространяется под лицензией MIT.
