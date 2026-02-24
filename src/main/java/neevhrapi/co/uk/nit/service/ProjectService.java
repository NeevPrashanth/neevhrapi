package neevhrapi.co.uk.nit.service;

import neevhrapi.co.uk.nit.constants.QueryConstants;
import neevhrapi.co.uk.nit.domains.MessageResponse;
import neevhrapi.co.uk.nit.domains.Project;
import neevhrapi.co.uk.nit.domains.Task;
import neevhrapi.co.uk.nit.domains.projectmgt.*;
import neevhrapi.co.uk.nit.domains.timesheet.*;
import neevhrapi.co.uk.nit.domains.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class ProjectService {

    public List<Project> getAllProjects() {
        return List.of(
                Project.builder()
                        .projectId(1)
                        .projectName("Project A")
                        .tasks(List.of(
                                Task.builder()
                                        .name("Deployment")
                                        .schedule(List.of(
                                                MessageResponse.Schedule.builder().day("Mon").date("2025-04-07").hours(3).active(true).build(),
                                                MessageResponse.Schedule.builder().day("Thu").date("2025-04-10").hours(3).active(true).build(),
                                                MessageResponse.Schedule.builder().day("Tue").date("2025-04-08").hours(2).active(false).build()
                                        )).build(),
                                Task.builder()
                                        .name("Testing")
                                        .schedule(List.of(
                                                MessageResponse.Schedule.builder().day("Tue").date("2025-04-08").hours(2).active(false).build(),
                                                MessageResponse.Schedule.builder().day("Wed").date("2025-04-09").hours(4).active(true).build(),
                                                MessageResponse.Schedule.builder().day("Fri").date("2025-04-11").hours(5).active(true).build()
                                        )).build(),
                                Task.builder()
                                        .name("Meetings")
                                        .schedule(List.of(
                                                MessageResponse.Schedule.builder().day("Mon").date("2025-04-07").hours(3).active(true).build(),
                                                MessageResponse.Schedule.builder().day("Thu").date("2025-04-10").hours(3).active(true).build(),
                                                MessageResponse.Schedule.builder().day("Wed").date("2025-04-09").hours(2).active(false).build()
                                        )).build()
                        )).build(),

                Project.builder()
                        .projectId(2)
                        .projectName("Project B")
                        .tasks(List.of(
                                Task.builder()
                                        .name("Design")
                                        .schedule(List.of(
                                                MessageResponse.Schedule.builder().day("Mon").date("2025-04-07").hours(3).active(true).build(),
                                                MessageResponse.Schedule.builder().day("Tue").date("2025-04-08").hours(4).active(true).build(),
                                                MessageResponse.Schedule.builder().day("Fri").date("2025-04-11").hours(5).active(true).build()
                                        )).build()
                        )).build(),
                Project.builder()
                        .projectId(3)
                        .projectName("NeevHRApp")
                        .tasks(List.of(
                                Task.builder()
                                        .name("Design")
                                        .schedule(List.of(
                                                MessageResponse.Schedule.builder().day("Mon").date("2025-04-07").hours(3).active(true).build(),
                                                MessageResponse.Schedule.builder().day("Tue").date("2025-04-08").hours(7).active(true).build(),
                                                MessageResponse.Schedule.builder().day("Fri").date("2025-04-11").hours(5).active(true).build()
                                        )).build()
                        )).build()

        );
    }


    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Map<String, List<neevhrapi.co.uk.nit.domains.projectmgt.Project>> getProjectsGroupedByStatus() {
        List<neevhrapi.co.uk.nit.domains.projectmgt.Project> allProjects = jdbcTemplate.query(QueryConstants.FETCH_PROJECTS, new ProjectRowMapper());

        Map<String, List<neevhrapi.co.uk.nit.domains.projectmgt.Project>> groupedProjects = allProjects.stream()
                .collect(Collectors.groupingBy(project -> {
                    String status = project.getProjectStatus();
                    if ("ACTIVE".equalsIgnoreCase(status)) return "Active Projects";
                    else if ("HOLD".equalsIgnoreCase(status)) return "OnHold Projects";
                    else return "Archieve Projects";
                }));

        // Ensure keys are always present, even if empty
        Map<String, List<neevhrapi.co.uk.nit.domains.projectmgt.Project>> response = new LinkedHashMap<>();
        response.put("Active Projects", groupedProjects.getOrDefault("Active Projects", new ArrayList<>()));
        response.put("OnHold Projects", groupedProjects.getOrDefault("OnHold Projects", new ArrayList<>()));
        response.put("Archieve Projects", groupedProjects.getOrDefault("Archieve Projects", new ArrayList<>()));

        return response;
    }

    public List<UserDTO> getUsersExcludingManager() {
        return jdbcTemplate.query(QueryConstants.FETCH_USERS_EXCLUDING_MANAGER, new UserRowMapper());
    }

    public List<ProjectStatusDTO> getAllProjectStatuses() {
        return jdbcTemplate.query(QueryConstants.FETCH_PROJECT_STATUSES, new ProjectStatusRowMapper());
    }

    @Transactional
    public int createProject(ProjectRequest request) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        // Insert into projects table
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(QueryConstants.INSERT_PROJECT, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, request.getName());
            ps.setString(2, request.getClient());
            ps.setDate(3, java.sql.Date.valueOf(request.getStartDate()));
            ps.setDate(4, java.sql.Date.valueOf(request.getEndDate()));
            ps.setInt(5, request.getStatusId());
            ps.setString(6, request.getNotes());
            return ps;
        }, keyHolder);

        int projectId = keyHolder.getKey().intValue();

        // Insert into project_resource table
        List<Integer> userIds = request.getResourceUserIds();
        if (userIds != null && !userIds.isEmpty()) {
            jdbcTemplate.batchUpdate(QueryConstants.INSERT_PROJECT_RESOURCE,
                    userIds,
                    userIds.size(),
                    (ps, userId) -> {
                        ps.setInt(1, userId);
                        ps.setInt(2, projectId);
                    }
            );
        }

        return projectId;
    }

    public List<TaskResponseDTO> getTasksByProjectId(int projectId) {
        return jdbcTemplate.query(QueryConstants.FETCH_TASKS_BY_PROJECT_ID, new TaskRowMapper(), projectId);
    }

    @Transactional
    public int createTask(int projectId,TaskRequest request) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        // Insert into task table
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(QueryConstants.INSERT_TASK, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, request.getName());
            ps.setDate(2, java.sql.Date.valueOf(request.getStartDate()));
            ps.setDate(3, java.sql.Date.valueOf(request.getEndDate()));
            ps.setString(4, request.getNotes());
            return ps;
        }, keyHolder);

        int taskId = keyHolder.getKey().intValue();

        // Insert into task_resource table
        List<Integer> userIds = request.getResourceUserIds();
        if (userIds != null && !userIds.isEmpty()) {
            jdbcTemplate.batchUpdate(QueryConstants.INSERT_TASK_RESOURCE,
                    userIds,
                    userIds.size(),
                    (ps, userId) -> {
                        ps.setInt(1, userId);
                        ps.setInt(2, taskId);
                    }
            );
        }

        if (projectId != 0 ) {
            jdbcTemplate.update(QueryConstants.INSERT_TASK_PROJECT,
                    projectId,
                    taskId);

        }

        return taskId;
    }

    @Transactional
    public void updateTask(int taskId, TaskRequest request) {
        // Step 1: Archive existing task data
        jdbcTemplate.update(
                "INSERT INTO task_history (task_id, name, start_date, end_date, notes) " +
                        "SELECT id, name, start_date, end_date, notes FROM task WHERE id = ?",
                taskId
        );

        // Step 2: Archive existing task resources
        jdbcTemplate.update(
                "INSERT INTO task_resource_history (task_id, user_id) " +
                        "SELECT task_id, user_id FROM task_resource WHERE task_id = ?",
                taskId
        );

        // Step 3: Update task details
        jdbcTemplate.update(QueryConstants.UPDATE_TASK,
                request.getName(),
                java.sql.Date.valueOf(request.getStartDate()),
                java.sql.Date.valueOf(request.getEndDate()),
                request.getNotes(),
                taskId
        );

        // Step 4: Delete old task resources
        jdbcTemplate.update(QueryConstants.DELETE_TASK_RESOURCES, taskId);

        // Step 5: Insert new task resources
        List<Integer> userIds = request.getResourceUserIds();
        if (userIds != null && !userIds.isEmpty()) {
            jdbcTemplate.batchUpdate(QueryConstants.INSERT_TASK_RESOURCE,
                    userIds,
                    userIds.size(),
                    (ps, userId) -> {
                        ps.setInt(1, userId);
                        ps.setInt(2, taskId);
                    }
            );
        }
    }

    public void updateProject(int projectId, ProjectRequest request) {
        // Step 1: Archive existing project data
        jdbcTemplate.update(QueryConstants.ARCHIVE_PROJECT, projectId);

        // Step 2: Archive existing project resources
        jdbcTemplate.update(QueryConstants.ARCHIVE_PROJECT_RESOURCES, projectId);

        // Step 3: Update project details
        jdbcTemplate.update(QueryConstants.UPDATE_PROJECT,
                request.getName(),
                request.getClient(),
                java.sql.Date.valueOf(request.getStartDate()),
                java.sql.Date.valueOf(request.getEndDate()),
                request.getStatusId(),
                request.getNotes(),
                projectId
        );

        // Step 4: Delete old project resources
        jdbcTemplate.update(QueryConstants.DELETE_PROJECT_RESOURCES, projectId);

        // Step 5: Insert new project resources
        List<Integer> userIds = request.getResourceUserIds();
        if (userIds != null && !userIds.isEmpty()) {
            jdbcTemplate.batchUpdate(QueryConstants.INSERT_PROJECT_RESOURCE,
                    userIds,
                    userIds.size(),
                    (ps, userId) -> {
                        ps.setInt(1, userId);
                        ps.setInt(2, projectId);
                    }
            );
        }
    }

    public List<TimesheetEntryDTO> getTimesheetEntriesForUser(String username, int weekFlag) {
        String sql = """
        SELECT 
            p.id AS project_id,
            p.name AS project_name,
            t.id AS task_id,
            t.name AS task_name,
            ts.date,
            ts.hours_spent,
            ts.description,
            ts.status
        FROM task_resource tr
        JOIN task t ON tr.task_id = t.id
        JOIN project_tasks pt ON t.id = pt.task_id
        JOIN projects p ON pt.project_id = p.id
        LEFT JOIN timesheet ts 
            ON ts.user_id = tr.user_id 
            AND ts.task_id = t.id 
            AND ts.project_id = p.id
            AND ts.date BETWEEN (
                CURDATE() - INTERVAL (WEEKDAY(CURDATE()) + (? * 7)) DAY
            ) AND (
                CURDATE() - INTERVAL (WEEKDAY(CURDATE()) - 6 + (? * 7)) DAY
            )
        WHERE tr.user_id = ?
        ORDER BY p.name, t.name, ts.date
        """;

        return jdbcTemplate.query(sql, rowMapper(), weekFlag, weekFlag, getUserIdByUsername(username));
    }

    private RowMapper<TimesheetEntryDTO> rowMapper() {
        return (rs, rowNum) -> {
            TimesheetEntryDTO dto = new TimesheetEntryDTO();
            dto.setProjectId(rs.getInt("project_id"));
            dto.setProjectName(rs.getString("project_name"));
            dto.setTaskId(rs.getInt("task_id"));
            dto.setTaskName(rs.getString("task_name"));
            dto.setDate((Objects.isNull(rs.getDate("date"))?null:rs.getDate("date").toLocalDate()));
            dto.setHoursSpent(rs.getObject("hours_spent") != null ? rs.getDouble("hours_spent") : null);
            dto.setDescription(rs.getString("description"));
            dto.setStatus(rs.getInt("status"));

            return dto;
        };
    }

    public List<LocalDate> getWeekDates(int weekFlag) {
        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.with(DayOfWeek.MONDAY).minusWeeks(weekFlag);
        return IntStream.range(0, 7)
                .mapToObj(startOfWeek::plusDays)
                .collect(Collectors.toList());
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

    public List<WeekTimesheet> mapToWeekTimesheet(List<TimesheetEntryDTO> entries, int weekFlag) {
        List<LocalDate> weekDates = getWeekDates(weekFlag);

        // Group by Project ID
        Map<Integer, Map<String, List<TimesheetEntryDTO>>> grouped = entries.stream()
                .collect(Collectors.groupingBy(
                        TimesheetEntryDTO::getProjectId,
                        Collectors.groupingBy(TimesheetEntryDTO::getTaskName)
                ));

        List<WeekTimesheet> result = new ArrayList<>();

        for (Map.Entry<Integer, Map<String, List<TimesheetEntryDTO>>> projectEntry : grouped.entrySet()) {
            Integer projectId = projectEntry.getKey();
            Map<String, List<TimesheetEntryDTO>> taskMap = projectEntry.getValue();

            WeekTimesheet.WeekTimesheetBuilder weekBuilder = WeekTimesheet.builder()
                    .projectId(projectId)
                    .projectName(taskMap.values().stream().flatMap(List::stream).findFirst().map(TimesheetEntryDTO::getProjectName).orElse(""));

            List<Task> tasks = new ArrayList<>();

            for (Map.Entry<String, List<TimesheetEntryDTO>> taskEntry : taskMap.entrySet()) {
                String taskName = taskEntry.getKey();
                List<TimesheetEntryDTO> taskEntries = taskEntry.getValue();

                List<MessageResponse.Schedule> schedules = new ArrayList<>();
                int taskId = taskEntries.get(0).getTaskId();
                for (LocalDate date : weekDates) {
                    Optional<TimesheetEntryDTO> matchingEntry = taskEntries.stream()
                            .filter(e -> e.getDate() != null && e.getDate().equals(date))
                            .findFirst();

                    boolean isFutureDate = date.isAfter(LocalDate.now());
                    boolean isStatusInactive = matchingEntry.map(e -> e.getStatus() != 0 && e.getStatus() != 1).orElse(false);
                    boolean isActive = !isFutureDate && !isStatusInactive;


                    schedules.add(MessageResponse.Schedule.builder()
                            .day(date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                            .date(date.toString())
                            .hours(matchingEntry.map(e -> e.getHoursSpent() != null ? e.getHoursSpent().intValue() : 0).orElse(0))
                            .active(isActive)
                                    .description(matchingEntry.map(e -> e.getDescription() != null ? e.getDescription() : "").orElse(""))
                            .build());
                }

                tasks.add(Task.builder()
                        .name(taskName)
                        .taskId(taskId)
                        .schedule(schedules)
                        .build());
            }

            weekBuilder.tasks(tasks);
            result.add(weekBuilder.build());
        }

        return result;
    }

    public void saveTimesheetEntries(List<TimesheetEntryDTO> entries) {
        String sql = """
        INSERT INTO timesheet (user_id, project_id, task_id, date, hours_spent, description)
        VALUES (?, ?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE
            hours_spent = VALUES(hours_spent),
            description = VALUES(description),
            updated_at = NOW()
        """;

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                TimesheetEntryDTO entry = entries.get(i);
                ps.setInt(1, entry.getUserId());
                ps.setInt(2, entry.getProjectId());
                ps.setInt(3, entry.getTaskId());
                ps.setDate(4, java.sql.Date.valueOf(String.valueOf(entry.getDate())));
                ps.setDouble(5, entry.getHoursSpent());
                ps.setString(6, entry.getDescription());

            }

            @Override
            public int getBatchSize() {
                return entries.size();
            }
        });
    }


    public void saveTimesheet(String username, List<WeekTimesheet> weekTimesheetList) {
        Integer userId = getUserIdByUsername(username);
        if (userId == null) {
            throw new IllegalArgumentException("Invalid user");
        }

        List<TimesheetEntryDTO> entriesToSave = new ArrayList<>();

        for (WeekTimesheet weekTimesheet : weekTimesheetList) {
            int projectId = weekTimesheet.getProjectId();

            for (Task task : weekTimesheet.getTasks()) {
                int taskId = task.getTaskId();

                for (MessageResponse.Schedule schedule : task.getSchedule()) {
                    // Ignore inactive or zero hour entries
                    if (!schedule.isActive() ) {
                        continue;
                    }

                    TimesheetEntryDTO dto = new TimesheetEntryDTO();
                    dto.setUserId(userId);
                    dto.setProjectId(projectId);
                    dto.setTaskId(taskId);
                    LocalDate localDate = LocalDate.parse(schedule.getDate());
                    dto.setDate(localDate);
                    dto.setHoursSpent((double) schedule.getHours());
                    dto.setDescription(schedule.getDescription());
                    entriesToSave.add(dto);
                }
            }
        }

        if (!entriesToSave.isEmpty()) {
            saveTimesheetEntries(entriesToSave);
        } else {
            throw new IllegalArgumentException("No valid timesheet entries to save.");
        }
    }

    public List<User> getAllUsers() {
        String sql = "SELECT id, username FROM users";
        return jdbcTemplate.query(sql, new EmployeeRowMapper());
    }

    public List<TimesheetRow> getUserTimesheet(int userId, String endDate) {
        String sql = "SELECT " +
                "t.id, u.username user, p.name project, tsk.name task, t.date, t.hours_spent, t.description " +
                "FROM timesheet t " +
                "JOIN users u ON u.id = t.user_id " +
                "JOIN projects p ON p.id = t.project_id " +
                "JOIN task tsk ON tsk.id = t.task_id " +
                "WHERE t.status != 2 AND t.user_id = ? AND t.date <= ? " +
                "ORDER BY t.user_id, t.project_id, t.task_id, t.date";

        return jdbcTemplate.query(sql, new TimesheetRowMapper(), userId, endDate);
    }

    public TimesheetData getTimesheetByUser(int userId, String endDate) {
        List<TimesheetRow> rows = getUserTimesheet(userId, endDate);
        if (rows.isEmpty()) return null;
        List<Integer> ids=new ArrayList<>();
        String empName = rows.get(0).getUser();
        Map<String, Map<String, List<TaskEntry>>> dayProjectTaskMap = new TreeMap<>();
        Map<String, Integer> totalHoursMap = new HashMap<>();

        for (TimesheetRow row : rows) {
            ids.add(row.getId());
            dayProjectTaskMap
                    .computeIfAbsent(row.getDate(), d -> new LinkedHashMap<>())
                    .computeIfAbsent(row.getProject(), p -> new ArrayList<>())
                    .add(new TaskEntry(row.getTask(), row.getId(), row.getDescription(), row.getHours()));

            totalHoursMap.merge(row.getDate(), row.getHours(), Integer::sum);
        }

        List<DayEntry> dayEntries = dayProjectTaskMap.entrySet().stream().map(dayEntry -> {
            String day = dayEntry.getKey();
            List<ProjectEntry> projects = dayEntry.getValue().entrySet().stream().map(pEntry ->
                    new ProjectEntry(pEntry.getKey(), pEntry.getValue())
            ).toList();
            int totalHours = totalHoursMap.getOrDefault(day, 0);
            return new DayEntry(day, totalHours,projects);
        }).toList();

        return new TimesheetData(empName,ids, dayEntries);
    }


    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public int updateStatusToApproved(List<Integer> ids) {
        System.out.println("ids: "+ids);
        String sql = "UPDATE timesheet SET status = 2 WHERE id IN (:ids)";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("ids", ids);
        return namedParameterJdbcTemplate.update(sql, parameters);
    }

}
