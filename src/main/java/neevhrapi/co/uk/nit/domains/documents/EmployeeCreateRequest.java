package neevhrapi.co.uk.nit.domains.documents;

import lombok.Data;

@Data
public class EmployeeCreateRequest {
    private String firstName;
    private String lastName;
    private String dob;
    private String emailId;
}
