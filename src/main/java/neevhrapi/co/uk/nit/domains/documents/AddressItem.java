package neevhrapi.co.uk.nit.domains.documents;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressItem {
    private Integer id;
    private Integer userId;
    private String addressType;
    private String line1;
    private String line2;
    private String city;
    private String postalCode;
    private String country;
    private String status;
}
