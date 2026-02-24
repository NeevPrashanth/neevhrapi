package neevhrapi.co.uk.nit.dao.jwtauth;

import neevhrapi.co.uk.nit.domains.jwtauth.AuthRequest;
import neevhrapi.co.uk.nit.domains.jwtauth.AuthResponse;

public interface AuthDao {

    public AuthResponse authenticateAndGenerateToken(AuthRequest request);
}
