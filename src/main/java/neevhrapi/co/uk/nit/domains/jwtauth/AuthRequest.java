package neevhrapi.co.uk.nit.domains.jwtauth;

import lombok.*;
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthRequest {
    private String applicationKey;
    private String username;
    private String password;
    // Getters and setters
}