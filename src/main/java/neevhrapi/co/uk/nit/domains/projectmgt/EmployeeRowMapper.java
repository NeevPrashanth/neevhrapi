package neevhrapi.co.uk.nit.domains.projectmgt;

import neevhrapi.co.uk.nit.domains.user.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EmployeeRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = User.builder()
                .empId(rs.getInt("id"))
                .empName(rs.getString("username"))
                .build();
        return user;
    }
}