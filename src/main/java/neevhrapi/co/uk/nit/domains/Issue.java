package neevhrapi.co.uk.nit.domains;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Issue {
    private String issue;
    private String action;
    //2025-03-20
    private String date;
    private Integer interfaceId;
    private String username;
}
