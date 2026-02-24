package neevhrapi.co.uk.nit.domains.tasktracker;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Data;

@Data
public class Task {
    private int userid;
    private LocalTime fromTime;
    private LocalTime toTime;
    private LocalDate taskDate;
    private String task;
}
