package neevhrapi.co.uk.nit.domains.documents;

import lombok.Data;

@Data
public class AddressUpsertRequest {
    private String addressType;
    private String line1;
    private String line2;
    private String city;
    private String postalCode;
    private String country;
}
