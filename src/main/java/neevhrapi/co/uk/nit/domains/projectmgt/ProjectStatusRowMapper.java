package neevhrapi.co.uk.nit.domains.projectmgt;


import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProjectStatusRowMapper implements RowMapper<ProjectStatusDTO> {
    @Override
    public ProjectStatusDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        ProjectStatusDTO status = new ProjectStatusDTO();
        status.setId(rs.getInt("id"));
        status.setStatus(rs.getString("status"));
        return status;
    }
}