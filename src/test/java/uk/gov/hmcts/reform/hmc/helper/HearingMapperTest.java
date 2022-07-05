package uk.gov.hmcts.reform.hmc.helper;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HearingMapperTest {

    @Mock
    private CaseHearingRequestMapper caseHearingRequestMapper;

    @Mock
    private PartyDetailMapper partyDetailMapper;

    @Mock
    private HearingDetailsMapper hearingDetailsMapper;


    @Test
    void modelToEntityTest() {

        val hearingMapper = new HearingMapper(caseHearingRequestMapper, partyDetailMapper, hearingDetailsMapper);

        assertEquals(365, hearingMapper.roundUpDuration(361));
        assertEquals(370, hearingMapper.roundUpDuration(369));
        assertEquals(725, hearingMapper.roundUpDuration(725));
        assertEquals(730, hearingMapper.roundUpDuration(730));
        assertEquals(425, hearingMapper.roundUpDuration(425));
        assertEquals(480, hearingMapper.roundUpDuration(476));
        assertEquals(485, hearingMapper.roundUpDuration(485));
        assertEquals(490, hearingMapper.roundUpDuration(487));
    }
}
