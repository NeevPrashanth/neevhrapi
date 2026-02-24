package neevhrapi.co.uk.nit.domains.timesheet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEntry {
    private String project;
    private List<TaskEntry> tasks;
}