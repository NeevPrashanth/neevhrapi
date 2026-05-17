package neevhrapi.co.uk.nit.domains.documents;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FinanceDocumentItem {
    private Integer id;
    private Integer employeeId;
    private Integer year;
    private String docType;
    private String originalFileName;
}
