package neevhrapi.co.uk.nit.domains;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class QueueDetail {
    private String name;
    private int count;
    private int interfaceId;
    private boolean critical;
}
