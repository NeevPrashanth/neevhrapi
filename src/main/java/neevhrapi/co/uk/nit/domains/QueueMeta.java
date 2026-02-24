package neevhrapi.co.uk.nit.domains;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class QueueMeta {
    private int totalQueueCount;
    private int maxRedEntityCount;
    private int criticalLimit;
}
