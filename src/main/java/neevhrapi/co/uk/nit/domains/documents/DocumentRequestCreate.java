package neevhrapi.co.uk.nit.domains.documents;

import lombok.Data;

@Data
public class DocumentRequestCreate {
    private Integer employeeId;
    private String documentName;
}
