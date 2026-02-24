package neevhrapi.co.uk.nit.domains;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailDetails {
    private String to;
    private String cc;
    private String subject;
    private String body;


}
