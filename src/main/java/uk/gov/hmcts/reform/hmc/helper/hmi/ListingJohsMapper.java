package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.model.PanelPreference;
import uk.gov.hmcts.reform.hmc.model.PanelRequirements;
import uk.gov.hmcts.reform.hmc.model.hmi.ListingJoh;

import java.util.ArrayList;
import java.util.List;

@Component
public class ListingJohsMapper {

    public List<ListingJoh> getListingJohs(PanelRequirements panelRequirements) {
        List<ListingJoh> listingJohs = new ArrayList<>();
        if (panelRequirements != null && panelRequirements.getPanelPreferences() != null) {
            for (PanelPreference panelPreference : panelRequirements.getPanelPreferences()) {
                ListingJoh listingJoh = ListingJoh.builder()
                    .listingJohId(panelPreference.getMemberID())
                    .listingJohPreference(panelPreference.getRequirementType())
                    .build();
                listingJohs.add(listingJoh);
            }
        }
        return listingJohs;
    }
}
