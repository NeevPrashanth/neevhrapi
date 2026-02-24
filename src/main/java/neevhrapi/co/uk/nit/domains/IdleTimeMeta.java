package neevhrapi.co.uk.nit.domains;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class IdleTimeMeta {
    private int totalCount;
    private int maxRedEntityCount;
    private int criticalLimit;
}
