package neevhrapi.co.uk.nit.domains.jwtauth;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenReq {
    private String refreshToken;

}
