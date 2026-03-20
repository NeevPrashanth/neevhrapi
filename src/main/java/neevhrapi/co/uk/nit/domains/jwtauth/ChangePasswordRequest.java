package neevhrapi.co.uk.nit.domains.jwtauth;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangePasswordRequest {
    private Long userId;
    private String currentPassword;
    private String newPassword;
}

