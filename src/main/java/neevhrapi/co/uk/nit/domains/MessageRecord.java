package neevhrapi.co.uk.nit.domains;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageRecord {
    private String sessionId;
    private String mrn;
    private String timestamp;
    private String status;
    private String info;
}

