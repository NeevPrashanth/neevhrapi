package neevhrapi.co.uk.nit.domains.projectmgt;

import neevhrapi.co.uk.nit.domains.Task;

import java.util.List;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class WeekTimesheet {
    private int projectId;
    private String projectName;
    private List<Task> tasks;
}
