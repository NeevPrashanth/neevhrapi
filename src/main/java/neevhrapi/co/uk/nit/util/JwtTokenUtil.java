package neevhrapi.co.uk.nit.util;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenUtil {
    private static final Logger logger = LogManager.getLogger(JwtTokenUtil.class);

    private final String SECRET = "your-very-secret-key";


    public boolean validateToken(String token) {
        try {
            logger.info("validating token: {}", token);
            Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("received exception while validating it");
            return false;
        }
    }

    public String extractUsername(String token) {
        return Jwts.parser().setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}

