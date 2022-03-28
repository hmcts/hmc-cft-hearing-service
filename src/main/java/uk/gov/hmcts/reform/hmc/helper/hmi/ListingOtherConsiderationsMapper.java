package uk.gov.hmcts.reform.hmc.helper.hmi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ListingOtherConsiderationsMapper {

    public List<String> getListingOtherConsiderations(Boolean hearingInWelshFlag,
                                                      List<String> facilityTypes) {

        List<String> listOtherConsiderations = new ArrayList<>();
        if (null != hearingInWelshFlag && hearingInWelshFlag) {
            listOtherConsiderations.add(hearingInWelshFlag.toString());
        }
        if (null != facilityTypes) {
            listOtherConsiderations.addAll(facilityTypes);
        }
        return listOtherConsiderations;
    }
}
