package neevhrapi.co.uk.nit.domains;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class IdleTimeDetail {
    private String name;
    private int idleTime;
    private boolean critical;
    private int interfaceId;
}
