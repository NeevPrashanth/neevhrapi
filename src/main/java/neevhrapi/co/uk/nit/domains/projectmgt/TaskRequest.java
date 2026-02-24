package neevhrapi.co.uk.nit.domains.projectmgt;

import lombok.Data;
import java.util.List;

@Data
public class TaskRequest {
    private String name;
    private String startDate; // Format: YYYY-MM-DD
    private String endDate;   // Format: YYYY-MM-DD
    private String notes;
    private List<Integer> resourceUserIds; // List of user IDs to assign
}