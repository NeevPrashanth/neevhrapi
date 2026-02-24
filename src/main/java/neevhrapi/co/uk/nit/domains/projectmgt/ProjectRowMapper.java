package neevhrapi.co.uk.nit.domains.projectmgt;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProjectRowMapper implements RowMapper<Project> {
    @Override
    public Project mapRow(ResultSet rs, int rowNum) throws SQLException {
        Project project = new Project();
        project.setProjectId(rs.getInt("project_id"));
        project.setProjectName(rs.getString("project_name"));
        project.setClientName(rs.getString("client_name"));
        project.setStartDate(rs.getString("start_date"));
        project.setEndDate(rs.getString("end_date"));
        project.setProjectStatus(rs.getString("project_status"));
        project.setNotes(rs.getString("notes"));
        project.setProjectResources(rs.getString("project_resources"));
        return project;
    }
}