package neevhrapi.co.uk.nit.domains;
import lombok.*;

import java.util.List;
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDetails {
    private List<RecentAction> recentActions;
    private LastEmail lastEmail;
    private MessageTrendData messageTrendData;
    private ErrorSuccessData errorSuccessData;
    private String status;
}
