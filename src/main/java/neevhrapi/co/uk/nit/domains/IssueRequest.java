package neevhrapi.co.uk.nit.domains;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssueRequest {
    private String startDate;
    private String endDate;
    private Integer interfaceId;

}
