package neevhrapi.co.uk.nit.domains.timesheet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DayEntry {
    private String day;
    private int totalHours;
    private List<ProjectEntry> projects;

}