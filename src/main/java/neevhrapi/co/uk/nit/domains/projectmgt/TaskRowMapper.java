package neevhrapi.co.uk.nit.domains.projectmgt;


import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TaskRowMapper implements RowMapper<TaskResponseDTO> {
    @Override
    public TaskResponseDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        TaskResponseDTO task = new TaskResponseDTO();
        task.setTaskId(rs.getInt("task_id"));
        task.setTaskName(rs.getString("task_name"));
        task.setStartDate(rs.getString("start_date"));
        task.setEndDate(rs.getString("end_date"));
        task.setNotes(rs.getString("notes"));
        task.setTaskResources(rs.getString("task_resources"));
        return task;
    }
}