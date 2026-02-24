package neevhrapi.co.uk.nit.domains.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private int empId;
    private String empName;
}
