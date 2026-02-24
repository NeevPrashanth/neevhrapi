package neevhrapi.co.uk.nit.domains;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LastEmail {
    private boolean sent;
    private String time;
}
