package neevhrapi.co.uk.nit.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class InitialUserSeeder implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public InitialUserSeeder(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Integer totalUsers = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        if (totalUsers != null && totalUsers == 0) {
            String hashedPassword = passwordEncoder.encode("Welcome123");
            jdbcTemplate.update("INSERT INTO users (username, password) VALUES (?, ?)", "abhi", hashedPassword);

            Integer userId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE username = ?", Integer.class, "abhi");
            if (userId != null) {
                jdbcTemplate.update("INSERT INTO user_roles (user_id, role) VALUES (?, 'Manager')", userId);
            }
        }
    }
}
