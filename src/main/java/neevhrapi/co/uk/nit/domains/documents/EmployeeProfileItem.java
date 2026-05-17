package neevhrapi.co.uk.nit.domains.documents;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class EmployeeProfileItem {
    private Integer userId;
    private String firstName;
    private String lastName;
    private String dob;
    private String emailId;
    private List<AddressItem> approvedAddresses;
    private List<AddressItem> pendingAddresses;
}
