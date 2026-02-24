package neevhrapi.co.uk.nit.constants;

public class QueryConstants {
    public static final String FETCH_PROJECTS =
            "SELECT " +
                    "p.id AS project_id, " +
                    "p.name AS project_name, " +
                    "p.client AS client_name, " +
                    "p.start_date, " +
                    "p.end_date, " +
                    "ps.status AS project_status, " +
                    "p.notes, " +
                    "GROUP_CONCAT(u.username SEPARATOR ', ') AS project_resources " +
                    "FROM projects p " +
                    "JOIN project_status ps ON p.status_id = ps.id " +
                    "LEFT JOIN project_resource pr ON p.id = pr.project_id " +
                    "LEFT JOIN users u ON pr.user_id = u.id " +
                    "GROUP BY p.id, p.name, p.client, p.start_date, p.end_date, ps.status, p.notes " +
                    "ORDER BY p.id;";

    public static final String FETCH_USERS_EXCLUDING_MANAGER =
            "SELECT DISTINCT u.id, u.username " +
                    "FROM users u " +
                    "JOIN user_roles ur ON u.id = ur.user_id " +
                    "WHERE ur.role != 'Manager'";

    public static final String FETCH_PROJECT_STATUSES =
            "SELECT id, status FROM project_status";

    public static final String INSERT_PROJECT_RESOURCE =
            "INSERT INTO project_resource (user_id, project_id) VALUES (?, ?)";
    public static final String INSERT_PROJECT =
            "INSERT INTO projects (name, client, start_date, end_date, status_id, notes) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

    public static final String FETCH_TASKS_BY_PROJECT_ID =
            "SELECT " +
                    "t.id AS task_id, " +
                    "t.name AS task_name, " +
                    "t.start_date, " +
                    "t.end_date, " +
                    "t.notes, " +
                    "GROUP_CONCAT(u.username SEPARATOR ', ') AS task_resources " +
                    "FROM project_tasks pt " +
                    "JOIN task t ON pt.task_id = t.id " +
                    "LEFT JOIN task_resource tr ON t.id = tr.task_id " +
                    "LEFT JOIN users u ON tr.user_id = u.id " +
                    "WHERE pt.project_id = ? " +
                    "GROUP BY t.id, t.name, t.start_date, t.end_date, t.notes " +
                    "ORDER BY t.id;";

    public static final String INSERT_TASK =
            "INSERT INTO task (name, start_date, end_date, notes) VALUES (?, ?, ?, ?)";

    public static final String INSERT_TASK_RESOURCE =
            "INSERT INTO task_resource (user_id, task_id) VALUES (?, ?)";

    public static final String INSERT_TASK_PROJECT =
            "INSERT INTO project_tasks (project_id, task_id) VALUES (?, ?)";

    public static final String UPDATE_TASK =
            "UPDATE task SET name = ?, start_date = ?, end_date = ?, notes = ? WHERE id = ?";

    public static final String DELETE_TASK_RESOURCES =
            "DELETE FROM task_resource WHERE task_id = ?";

    public static final String UPDATE_PROJECT =
            "UPDATE projects SET name = ?, client = ?, start_date = ?, end_date = ?, status_id = ?, notes = ? WHERE id = ?";

    public static final String DELETE_PROJECT_RESOURCES =
            "DELETE FROM project_resource WHERE project_id = ?";

    public static final String ARCHIVE_PROJECT =
            "INSERT INTO project_history (project_id, name, client, start_date, end_date, status_id, notes) " +
                    "SELECT id, name, client, start_date, end_date, status_id, notes FROM projects WHERE id = ?";

    public static final String ARCHIVE_PROJECT_RESOURCES =
            "INSERT INTO project_resource_history (project_id, user_id) " +
                    "SELECT project_id, user_id FROM project_resource WHERE project_id = ?";

}