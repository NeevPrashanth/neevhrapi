package neevhrapi.co.uk.nit.service.jwtauth;

import neevhrapi.co.uk.nit.domains.jwtauth.AuthRequest;
import neevhrapi.co.uk.nit.domains.jwtauth.AuthResponse;
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


}

