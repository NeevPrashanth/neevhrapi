package neevhrapi.co.uk.nit.domains.projectmgt;

import lombok.Data;

@Data
public class TaskResponseDTO {
    private int taskId;
    private String taskName;
    private String startDate;
    private String endDate;
    private String notes;
    private String taskResources; // Comma-separated usernames
}