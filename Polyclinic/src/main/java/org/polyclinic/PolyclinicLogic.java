package org.polyclinic;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class PolyclinicLogic {

    public static List<String> getSpecializations() throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT specialization_name FROM specializations ORDER BY specialization_name";

        try (Connection c = db.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(rs.getString(1));
            }
        }
        return list;
    }

    public static ResultSet getDoctorsBySpecialization(String specialization) throws SQLException {
        String sql = "SELECT d.doctor_id, d.full_name, d.room, " +
                "COALESCE( " +
                "(SELECT MIN(ds.work_date) " +
                "FROM doctor_schedule ds " +
                "LEFT JOIN appointments a ON ds.doctor_id = a.doctor_id " +
                "AND ds.work_date = a.appointment_date " +
                "AND a.status = 'запланирован' " +
                "WHERE ds.doctor_id = d.doctor_id " +
                "AND ds.work_date >= CURRENT_DATE " +
                "AND ds.is_available = true " +
                "AND a.appointment_id IS NULL " +  // нет записи на это время
                "LIMIT 1), " +
                "(SELECT MIN(ds.work_date) " +
                "FROM doctor_schedule ds " +
                "WHERE ds.doctor_id = d.doctor_id " +
                "AND ds.work_date >= CURRENT_DATE " +
                "AND ds.is_available = true " +
                "LIMIT 1)) as nearest_available_date " +
                "FROM doctors d " +
                "WHERE d.specialization_id = ( " +
                "SELECT specialization_id FROM specializations " +
                "WHERE specialization_name = ? " +
                ") AND d.is_active = true " +
                "ORDER BY nearest_available_date NULLS LAST, d.full_name";

        Connection c = db.getConnection();
        PreparedStatement ps = c.prepareStatement(sql);
        ps.setString(1, specialization);

        return ps.executeQuery();
    }

    public static List<String> getAvailableTimes(Integer doctorId, Date date) throws SQLException {
        List<String> availableTimes = new ArrayList<>();

        String checkScheduleSql = "SELECT start_time, end_time FROM doctor_schedule " +
                "WHERE doctor_id = ? AND work_date = ? AND is_available = true";

        Time workStart = null;
        Time workEnd = null;
        int slotDuration = 30;

        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(checkScheduleSql)) {
            ps.setInt(1, doctorId);
            ps.setDate(2, date);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                workStart = rs.getTime("start_time");
                workEnd = rs.getTime("end_time");
            } else {
                return availableTimes;
            }
        }

        if (workStart == null || workEnd == null) {
            return availableTimes;
        }

        String sqlBusy = "SELECT appointment_time " +
                "FROM appointments " +
                "WHERE doctor_id = ? " +
                "AND appointment_date = ? " +
                "AND status = 'запланирован' " +
                "ORDER BY appointment_time";

        List<Time> busyTimes = new ArrayList<>();
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sqlBusy)) {
            ps.setInt(1, doctorId);
            ps.setDate(2, date);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                busyTimes.add(rs.getTime("appointment_time"));
            }
        }

        LocalTime currentTime = workStart.toLocalTime();
        LocalTime endTime = workEnd.toLocalTime();

        while (currentTime.plusMinutes(slotDuration).isBefore(endTime.plusMinutes(1))) {
            Time slotTime = Time.valueOf(currentTime);

            boolean isBusy = false;
            for (Time busyTime : busyTimes) {
                if (busyTime.equals(slotTime)) {
                    isBusy = true;
                    break;
                }
            }

            if (!isBusy) {
                availableTimes.add(currentTime.toString().substring(0, 5));
            }

            currentTime = currentTime.plusMinutes(slotDuration);
        }

        return availableTimes;
    }

    public static List<String> getAvailableDates(Integer doctorId) throws SQLException {
        List<String> availableDates = new ArrayList<>();

        String sql = "SELECT work_date FROM doctor_schedule " +
                "WHERE doctor_id = ? " +
                "AND work_date >= CURRENT_DATE " +
                "AND is_available = true " +
                "AND work_date NOT IN ( " +
                "SELECT DISTINCT appointment_date FROM appointments " +
                "WHERE doctor_id = ? AND status = 'запланирован' " +
                "GROUP BY appointment_date " +
                "HAVING COUNT(*) >= 16 " +
                ") " +
                "ORDER BY work_date " +
                "LIMIT 14";

        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setInt(2, doctorId);
            ResultSet rs = ps.executeQuery();

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy");

            while (rs.next()) {
                Date date = rs.getDate("work_date");
                availableDates.add(sdf.format(date));
            }
        }

        return availableDates;
    }


    public static boolean bookAppointment(Integer patientId, Integer doctorId, Date date, Time time) {

        String checkScheduleSql = "SELECT 1 FROM doctor_schedule " +
                "WHERE doctor_id = ? AND work_date = ? AND is_available = true";

        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(checkScheduleSql)) {
            ps.setInt(1, doctorId);
            ps.setDate(2, date);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                return false;
            }
        } catch (SQLException e) {
            return false;
        }

        String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, status) " +
                "VALUES (?, ?, ?, ?, 'запланирован') " +
                "ON CONFLICT (doctor_id, appointment_date, appointment_time) DO NOTHING " +
                "RETURNING appointment_id";

        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            ps.setInt(2, doctorId);
            ps.setDate(3, date);
            ps.setTime(4, time);

            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            return false;
        }
    }


    public static ResultSet getPatientAppointments(Integer patientId) throws SQLException {
        String sql = "SELECT a.appointment_id, d.full_name as doctor_name, " +
                "s.specialization_name as specialization, " +
                "TO_CHAR(a.appointment_date, 'DD.MM.YYYY') as appointment_date, " +
                "TO_CHAR(a.appointment_time, 'HH24:MI') as appointment_time, " +
                "a.status, d.room " +
                "FROM appointments a " +
                "JOIN doctors d ON a.doctor_id = d.doctor_id " +
                "JOIN specializations s ON d.specialization_id = s.specialization_id " +
                "WHERE a.patient_id = ? " +
                "AND a.appointment_date >= CURRENT_DATE " +
                "AND a.status = 'запланирован' " +
                "ORDER BY a.appointment_date, a.appointment_time";

        Connection c = db.getConnection();
        PreparedStatement ps = c.prepareStatement(sql);
        ps.setInt(1, patientId);

        return ps.executeQuery();
    }

    public static boolean cancelAppointment(Integer appointmentId) {
        String sql = "UPDATE appointments SET status = 'отменен' WHERE appointment_id = ?";

        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, appointmentId);
            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getNearestAvailableDate(Integer doctorId) {
        String sql = "SELECT MIN(ds.work_date) as nearest_date " +
                "FROM doctor_schedule ds " +
                "LEFT JOIN appointments a ON ds.doctor_id = a.doctor_id " +
                "AND ds.work_date = a.appointment_date " +
                "AND a.status = 'запланирован' " +
                "WHERE ds.doctor_id = ? " +
                "AND ds.work_date >= CURRENT_DATE " +
                "AND ds.is_available = true " +
                "AND a.appointment_id IS NULL " +
                "LIMIT 1";

        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Date date = rs.getDate("nearest_date");
                if (date != null) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy");
                    return sdf.format(date);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String nextWorkDateSql = "SELECT MIN(work_date) as next_work_date " +
                "FROM doctor_schedule " +
                "WHERE doctor_id = ? " +
                "AND work_date >= CURRENT_DATE " +
                "AND is_available = true " +
                "LIMIT 1";

        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(nextWorkDateSql)) {
            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Date date = rs.getDate("next_work_date");
                if (date != null) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy");
                    return "Свободных записей нет, след. работа: " + sdf.format(date);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "Нет расписания";
    }

    public static boolean updatePatientProfile(Integer patientId, String fullName,
                                               String phone, String policyNumber) {
        String sql = "UPDATE patients SET full_name = ?, phone = ?, " +
                "policy_number = ? WHERE patient_id = ?";

        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, fullName);
            ps.setString(2, phone);
            ps.setString(3, policyNumber.isEmpty() ? null : policyNumber);
            ps.setInt(4, patientId);

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}