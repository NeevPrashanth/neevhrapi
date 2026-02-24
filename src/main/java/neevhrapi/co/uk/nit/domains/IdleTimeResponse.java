package neevhrapi.co.uk.nit.domains;

import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdleTimeResponse {
    private List<IdleTimeDetail> idealTimeDetails;
    private IdleTimeMeta meta;
}
