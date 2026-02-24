package neevhrapi.co.uk.nit.domains.timesheet;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TimesheetRowMapper implements RowMapper<TimesheetRow> {
    @Override
    public TimesheetRow mapRow(ResultSet rs, int rowNum) throws SQLException {
        TimesheetRow row = new TimesheetRow();
        row.setUser(rs.getString("user"));
        row.setProject(rs.getString("project"));
        row.setTask(rs.getString("task"));
        row.setId(rs.getInt("id"));
        row.setDescription(rs.getString("description"));
        row.setHours(rs.getInt("hours_spent"));
        row.setDate(rs.getString("date"));
        return row;
    }
}