package neevhrapi.co.uk.nit.domains.projectmgt;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimesheetEntryDTO {
    private int projectId;
    private int userId;
    private String projectName;
    private int taskId;
    private String taskName;
    private LocalDate taskStartDate;
    private LocalDate taskEndDate;
    private LocalDate date;
    private Double hoursSpent;
    private String description;
    private int status;
}



