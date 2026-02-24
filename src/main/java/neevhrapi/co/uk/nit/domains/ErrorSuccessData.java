package neevhrapi.co.uk.nit.domains;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorSuccessData {
    private List<String> labels;
    private List<ErrorDataset> datasets;
}
