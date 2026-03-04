package org.polyclinic;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class ScheduleGenerator {

    public static void generateSchedule(int daysAhead) {
        String sql = "SELECT generate_doctor_schedule(CURRENT_DATE, CURRENT_DATE + ?)";

        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, daysAhead);
            ps.execute();
            System.out.println("Расписание сгенерировано на " + daysAhead + " дней вперед");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void initSchedule() {
        generateSchedule(60);
    }
}