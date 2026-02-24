package neevhrapi.co.uk.nit.domains.timesheet;

import lombok.Data;

import java.util.List;

@Data
public class TimesheetStatusUpdateRequest {
    private List<Integer> id;
}