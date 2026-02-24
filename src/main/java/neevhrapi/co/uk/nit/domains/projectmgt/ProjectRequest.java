package neevhrapi.co.uk.nit.domains.projectmgt;

import lombok.Data;
import java.util.List;

@Data
public class ProjectRequest {
    private String name;
    private String client;
    private String startDate; // format: YYYY-MM-DD
    private String endDate;   // format: YYYY-MM-DD
    private int statusId;
    private String notes;
    private List<Integer> resourceUserIds; // New field for user IDs
}