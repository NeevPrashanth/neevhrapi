package neevhrapi.co.uk.nit.domains.tasktracker;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class TaskResponse {
    private int userid;
    private String fromTime;
    private String toTime;
    private LocalDate taskDate;
    private String task;
}
