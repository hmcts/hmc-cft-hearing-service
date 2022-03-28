package uk.gov.hmcts.reform.hmc.helper.hmi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ListingOtherConsiderationsMapperTest {

    @Test
    void shouldReturnHearingInWelsh() {
        Boolean hearingInWelsh = Boolean.TRUE;
        List<String> facilityTypes = new ArrayList<>();
        ListingOtherConsiderationsMapper listingOtherConsiderationsMapper = new ListingOtherConsiderationsMapper();
        List<String> otherConsiderations =
                listingOtherConsiderationsMapper.getListingOtherConsiderations(hearingInWelsh, facilityTypes);
        assertEquals(1, otherConsiderations.size());
        assertTrue(otherConsiderations.get(0).contains("true"));
    }

    @Test
    void shouldNotReturnHearingInWelsh() {
        Boolean hearingInWelsh = Boolean.FALSE;
        List<String> facilityTypes = new ArrayList<>();
        ListingOtherConsiderationsMapper listingOtherConsiderationsMapper = new ListingOtherConsiderationsMapper();
        List<String> otherConsiderations =
                listingOtherConsiderationsMapper.getListingOtherConsiderations(hearingInWelsh, facilityTypes);
        assertEquals(0, otherConsiderations.size());
    }

    @Test
    void shouldNotReturnHearingInWelshForNullEntry() {
        Boolean hearingInWelsh = null;
        List<String> facilityTypes = new ArrayList<>();
        ListingOtherConsiderationsMapper listingOtherConsiderationsMapper = new ListingOtherConsiderationsMapper();
        List<String> otherConsiderations =
                listingOtherConsiderationsMapper.getListingOtherConsiderations(hearingInWelsh, facilityTypes);
        assertEquals(0, otherConsiderations.size());
    }

    @Test
    void shouldReturnNoFacilityTypesForNullEntry() {
        Boolean hearingInWelsh = Boolean.FALSE;
        List<String> facilityTypes = null;
        ListingOtherConsiderationsMapper listingOtherConsiderationsMapper = new ListingOtherConsiderationsMapper();
        List<String> otherConsiderations =
                listingOtherConsiderationsMapper.getListingOtherConsiderations(hearingInWelsh, facilityTypes);
        assertEquals(0, otherConsiderations.size());
    }

    @Test
    void shouldReturnNoFacilityTypesForEmptyEntry() {
        Boolean hearingInWelsh = Boolean.FALSE;
        List<String> facilityTypes = new ArrayList<>();
        ListingOtherConsiderationsMapper listingOtherConsiderationsMapper = new ListingOtherConsiderationsMapper();
        List<String> otherConsiderations =
                listingOtherConsiderationsMapper.getListingOtherConsiderations(hearingInWelsh, facilityTypes);
        assertEquals(facilityTypes.size(), otherConsiderations.size());
    }

    @Test
    void shouldReturnFacilityTypes() {
        Boolean hearingInWelsh = Boolean.FALSE;
        List<String> facilityTypes = new ArrayList<>();
        facilityTypes.add("Test 1");
        facilityTypes.add("Test 2");
        facilityTypes.add("Test 3");
        ListingOtherConsiderationsMapper listingOtherConsiderationsMapper = new ListingOtherConsiderationsMapper();
        List<String> otherConsiderations =
                listingOtherConsiderationsMapper.getListingOtherConsiderations(hearingInWelsh, facilityTypes);
        assertEquals(facilityTypes.size(), otherConsiderations.size());
        assertTrue(otherConsiderations.contains(facilityTypes.get(0)));
        assertTrue(otherConsiderations.contains(facilityTypes.get(1)));
        assertTrue(otherConsiderations.contains(facilityTypes.get(2)));
    }


}
