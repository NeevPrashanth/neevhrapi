package neevhrapi.co.uk.nit.domains.timesheet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetRow {
    private String user;
    private String project;
    private String task;
    private int id;
    private String description;
    private int hours;
    private String date;

}
