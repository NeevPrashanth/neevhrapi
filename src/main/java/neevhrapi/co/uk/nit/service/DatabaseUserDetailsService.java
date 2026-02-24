package neevhrapi.co.uk.nit.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Fetch user
        String userQuery = "SELECT id, username, password FROM users WHERE username = ?";
        List<UserRecord> users = jdbcTemplate.query(userQuery, new Object[]{username}, (rs, rowNum) -> {
            return new UserRecord(rs.getInt("id"), rs.getString("username"), rs.getString("password"));
        });

        if (users.isEmpty()) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        UserRecord userRecord = users.get(0);

        // Fetch roles
        String roleQuery = "SELECT role FROM user_roles WHERE user_id = ?";
        List<GrantedAuthority> authorities = jdbcTemplate.query(roleQuery, new Object[]{userRecord.getId()},
                        (rs, rowNum) -> new SimpleGrantedAuthority("ROLE_" + rs.getString("role")))
                .stream().collect(Collectors.toList());

        return User.builder()
                .username(userRecord.getUsername())
                .password("{noop}" + userRecord.getPassword()) // Add encoding here later
                .authorities(authorities)
                .build();
    }

    // Helper class for user record
    private static class UserRecord {
        private final int id;
        private final String username;
        private final String password;

        public UserRecord(int id, String username, String password) {
            this.id = id;
            this.username = username;
            this.password = password;
        }

        public int getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }
}