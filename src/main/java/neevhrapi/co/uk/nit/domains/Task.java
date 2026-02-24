package neevhrapi.co.uk.nit.domains;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class Task {
    private int taskId;
    private String name;
    private List<MessageResponse.Schedule> schedule;
}