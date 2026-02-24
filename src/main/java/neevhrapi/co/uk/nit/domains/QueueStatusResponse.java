package neevhrapi.co.uk.nit.domains;

import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueueStatusResponse {
    private List<QueueDetail> queueDetails;
    private QueueMeta meta;
}
