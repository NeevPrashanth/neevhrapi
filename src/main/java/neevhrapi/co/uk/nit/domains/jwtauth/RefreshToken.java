package neevhrapi.co.uk.nit.domains.jwtauth;

import lombok.*;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@Builder
public class RefreshToken {
    private String refresh_token;
    public RefreshToken(String refresh_token) {
        this.refresh_token = refresh_token;
    }
}
