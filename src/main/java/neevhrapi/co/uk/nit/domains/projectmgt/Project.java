package neevhrapi.co.uk.nit.domains.projectmgt;

import lombok.Data;

@Data
public class Project {
    private int projectId;
    private String projectName;
    private String clientName;
    private String startDate;
    private String endDate;
    private String projectStatus;
    private String notes;
    private String projectResources;
}