package neevhrapi.co.uk.nit.domains.jwtauth;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String refresh_token;

}
