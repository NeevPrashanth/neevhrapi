package neevhrapi.co.uk.nit.domains.jwtauth;

import lombok.*;

import java.util.List;
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private Long id;

    private String username;
    private String password;
    private List<String> roles;

}
