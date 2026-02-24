package neevhrapi.co.uk.nit.service;

import neevhrapi.co.uk.nit.domains.tasktracker.Task;
import neevhrapi.co.uk.nit.domains.tasktracker.TaskResponse;
import neevhrapi.co.uk.nit.domains.tasktracker.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.time.format.DateTimeFormatter;


@Service
public class TaskService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int save(Task task) {
        String sql = "INSERT INTO task_schedule (userid, from_time, to_time, task_date, task) VALUES (?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                task.getUserid(),
                task.getFromTime(),
                task.getToTime(),
                task.getTaskDate(),
                task.getTask());
    }

    public Integer getUserIdByUsername(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";

        try {
            return jdbcTemplate.queryForObject(sql, Integer.class, username);
        } catch (EmptyResultDataAccessException e) {
            // Username not found, return null or throw exception
            return 0;
        }
    }

    public List<TaskResponse> findByUserIdAndDate(int userid, LocalDate date) {
        String sql = "SELECT userid,from_time,to_time,task_date,task FROM task_schedule WHERE userid = ? AND task_date = ? order by from_time desc";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return jdbcTemplate.query(sql, new Object[]{userid, date}, new RowMapper<TaskResponse>() {
            @Override
            public TaskResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
                TaskResponse task = new TaskResponse();
                task.setUserid(rs.getInt("userid"));
                task.setFromTime(rs.getTime("from_time").toLocalTime().format(formatter));
                task.setToTime(rs.getTime("to_time").toLocalTime().format(formatter));
                task.setTaskDate(rs.getDate("task_date").toLocalDate());
                task.setTask(rs.getString("task"));
                return task;
            }
        });
    }

    public String addUser(User user) {
        String checkSql = "SELECT COUNT(*) FROM users WHERE username = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, user.getUsername());

        if (count != null && count > 0) {
            return "Username already exists";
        }

        String insertUserSql = "INSERT INTO users (username, password) VALUES (?, ?)";
        jdbcTemplate.update(insertUserSql, user.getUsername(), user.getPassword());

        String getIdSql = "SELECT id FROM users WHERE username = ?";
        Integer userId = jdbcTemplate.queryForObject(getIdSql, Integer.class, user.getUsername());

        String insertRoleSql = "INSERT INTO user_roles (user_id, role) VALUES (?, 'Developer')";
        jdbcTemplate.update(insertRoleSql, userId);

        return "User added successfully";
    }
}
