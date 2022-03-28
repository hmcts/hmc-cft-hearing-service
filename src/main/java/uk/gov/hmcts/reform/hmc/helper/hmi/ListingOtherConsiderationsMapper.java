package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ListingOtherConsiderationsMapper {

    public List<String> getListingOtherConsiderations(Boolean hearingInWelshFlag,
                                                      List<String> facilityTypes) {
        List<String> listOtherConsiderations = new ArrayList<>();
        if (null != hearingInWelshFlag) {
            listOtherConsiderations.add(hearingInWelshFlag.toString());
        }
        if (null != facilityTypes) {
            facilityTypes.stream().forEach(listOtherConsiderations::add);
        }
        return listOtherConsiderations;
    }
}
