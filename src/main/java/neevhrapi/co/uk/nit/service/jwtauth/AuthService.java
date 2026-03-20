package neevhrapi.co.uk.nit.service.jwtauth;

import neevhrapi.co.uk.nit.domains.jwtauth.AuthRequest;
import neevhrapi.co.uk.nit.domains.jwtauth.AuthResponse;
import neevhrapi.co.uk.nit.domains.jwtauth.ChangePasswordRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private AuthDaoImpl userRepository;

    private final String SECRET_KEY = "your-very-secret-key";

    public AuthResponse authenticateAndGenerateToken(AuthRequest request) {
        // Validate applicationKey (can be hardcoded or from DB)
        if (!"APP-123456".equals(request.getApplicationKey())) {
            throw new RuntimeException("Invalid application key");
        }

        return userRepository.authenticateAndGenerateToken(request);
    }

    public void changePassword(ChangePasswordRequest request, String authenticatedUsername) {
        if (request == null || request.getUserId() == null || request.getCurrentPassword() == null || request.getNewPassword() == null) {
            throw new RuntimeException("userId, currentPassword and newPassword are required");
        }
        if (request.getNewPassword().trim().isEmpty()) {
            throw new RuntimeException("newPassword cannot be empty");
        }
        userRepository.changePassword(request.getUserId(), request.getCurrentPassword(), request.getNewPassword(), authenticatedUsername);
    }
}

