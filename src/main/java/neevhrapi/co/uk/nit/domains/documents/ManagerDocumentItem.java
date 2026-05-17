package neevhrapi.co.uk.nit.domains.documents;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ManagerDocumentItem {
    private Integer uploadId;
    private Integer requestId;
    private Integer employeeId;
    private String employeeName;
    private String documentName;
    private String originalFileName;
    private String status;
}
