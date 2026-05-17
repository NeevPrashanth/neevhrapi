package neevhrapi.co.uk.nit.domains.documents;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MyDocumentItem {
    private Integer requestId;
    private String documentName;
    private String status;
    private Integer uploadId;
    private String uploadedFileName;
    private String approvedFileName;
}
