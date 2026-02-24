package neevhrapi.co.uk.nit.domains;
import lombok.*;

import java.util.List;
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageTrendData {
    private List<String> labels;
    private List<Dataset> datasets;
}
