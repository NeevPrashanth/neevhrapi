package neevhrapi.co.uk.nit.service.jwtauth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import neevhrapi.co.uk.nit.dao.jwtauth.AuthDao;
import neevhrapi.co.uk.nit.domains.jwtauth.AuthRequest;
import neevhrapi.co.uk.nit.domains.jwtauth.AuthResponse;
import neevhrapi.co.uk.nit.domains.jwtauth.User;
import neevhrapi.co.uk.nit.service.CredentialStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Repository
public class AuthDaoImpl implements AuthDao {
    private static final Logger logger = LogManager.getLogger(AuthDaoImpl.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    CredentialStore credentialStore;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User findByUsername(String username) {
        String userSql = "SELECT id, username, password FROM users WHERE username = ?";
        String rolesSql = "SELECT role FROM user_roles WHERE user_id = ?";

        try {
            // Fetch user data
            User user = jdbcTemplate.queryForObject(userSql, new Object[]{username}, (rs, rowNum) -> {
                User u = new User();
                u.setId(rs.getLong("id"));
                u.setUsername(rs.getString("username"));
                u.setPassword(rs.getString("password"));
                return u;
            });

            // Fetch roles
            List<String> roles = jdbcTemplate.query(rolesSql, new Object[]{user.getId()},
                    (rs, rowNum) -> rs.getString("role"));

            user.setRoles(roles);
            logger.info("{}", user);
            return user;

        } catch (EmptyResultDataAccessException e) {
            return null; // or throw custom NotFoundException
        }
    }
    private User findByUserId(Long userId) {
        String sql = "SELECT id, username, password FROM users WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{userId}, (rs, rowNum) -> {
                User u = new User();
                u.setId(rs.getLong("id"));
                u.setUsername(rs.getString("username"));
                u.setPassword(rs.getString("password"));
                return u;
            });
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private final String SECRET_KEY = "your-very-secret-key";

    @Override
    public AuthResponse authenticateAndGenerateToken(AuthRequest request) {
        // Validate applicationKey (can be hardcoded or from DB)
        if (!"APP-123456".equals(request.getApplicationKey())) {
            throw new RuntimeException("Invalid application key");
        }

        // Find user from DB
        User user = findByUsername(request.getUsername());
        if (Objects.isNull(user)) {
            throw  new RuntimeException("User not found");
        }
        // Validate password using hash match.
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // Generate token with username and roles
        String token = Jwts.builder()
                .setSubject(user.getUsername())
                .claim("roles", user.getRoles()) // Assuming roles is a List<String>
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() +86400000)) // 1 day expiry
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();

        // Store credentials and generate refresh token
        String refreshToken = credentialStore.storeCredentials(user.getUsername(), request.getApplicationKey());

        return AuthResponse.builder()
                .token(token)
                .refresh_token(refreshToken)
                .build();
    }

    public AuthResponse generateTokenFromRefreshContext(String username, String applicationKey) {
        if (!"APP-123456".equals(applicationKey)) {
            throw new RuntimeException("Invalid application key");
        }

        User user = findByUsername(username);
        if (Objects.isNull(user)) {
            throw new RuntimeException("User not found");
        }

        String token = Jwts.builder()
                .setSubject(user.getUsername())
                .claim("roles", user.getRoles())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();

        String refreshToken = credentialStore.storeCredentials(user.getUsername(), applicationKey);
        return AuthResponse.builder()
                .token(token)
                .refresh_token(refreshToken)
                .build();
    }
    @Override
    public void changePassword(Long userId, String currentPassword, String newPassword, String authenticatedUsername) {
        User user = findByUserId(userId);
        if (Objects.isNull(user)) {
            throw new RuntimeException("User not found");
        }
        if (!user.getUsername().equals(authenticatedUsername)) {
            throw new RuntimeException("Not authorized to change this user password");
        }
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        String hashedNewPassword = passwordEncoder.encode(newPassword);
        int updated = jdbcTemplate.update("UPDATE users SET password = ? WHERE id = ?", hashedNewPassword, userId);
        if (updated != 1) {
            throw new RuntimeException("Unable to change password");
        }
    }

    public boolean validateToken(String token) {
        try {
            logger.info("validating token: {}", token);
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            logger.info("received exception while validating it");
            return false;
        }
    }

    public String extractUsername(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

}

