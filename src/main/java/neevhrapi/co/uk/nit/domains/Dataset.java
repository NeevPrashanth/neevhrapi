package neevhrapi.co.uk.nit.domains;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dataset {
    private String label;
    private List<Integer> data;
    private String borderColor;
    private boolean fill;
}
