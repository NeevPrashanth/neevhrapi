package neevhrapi.co.uk.nit.domains.timesheet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetData {
    private String empname;
    private List<Integer> id;
    private List<DayEntry> emptimes;
}