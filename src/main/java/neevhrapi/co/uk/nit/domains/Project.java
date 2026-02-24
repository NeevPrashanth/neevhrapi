package neevhrapi.co.uk.nit.domains;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class Project {
    private int projectId;
    private String projectName;
    private List<Task> tasks;
}