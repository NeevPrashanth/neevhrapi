package neevhrapi.co.uk.nit.domains;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MessageResponse {
    private List<MessageRecord> records;
    private int totalPages;
    private int totalRecords;
    private int currentPage;

    @Data
    @Builder
    public static class Schedule {
        private String day;
        private String date;
        private int hours;
        private boolean active;
        private String description ;
    }
}

