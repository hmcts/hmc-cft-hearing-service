package uk.gov.hmcts.reform.hmc.service;

import com.microsoft.applicationinsights.core.dependencies.apachecommons.lang3.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.exceptions.BadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.PartiesNotifiedBadRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.PartiesNotifiedNotFoundException;
import uk.gov.hmcts.reform.hmc.model.partiesnotified.PartiesNotified;
import uk.gov.hmcts.reform.hmc.repository.HearingRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static uk.gov.hmcts.reform.hmc.constants.Constants.HEARING_ID_MAX_LENGTH;
import static uk.gov.hmcts.reform.hmc.exceptions.ValidationError.INVALID_HEARING_ID_DETAILS;

@Service
@Component
@Slf4j
public class PartiesNotifiedServiceImpl implements PartiesNotifiedService {

    private final HearingRepository hearingRepository;

    @Autowired
    public PartiesNotifiedServiceImpl(HearingRepository hearingRepository) {
        this.hearingRepository = hearingRepository;
    }

    @Override
    public void getPartiesNotified(Long hearingId, int responseVersion, PartiesNotified partiesNotified) {
        isValidFormat(hearingId.toString());
        if (!hearingRepository.existsById(hearingId)) {
            throw new PartiesNotifiedNotFoundException("001 No such id: %s", hearingId);
        } else {
            Optional<HearingEntity> hearingEntity = hearingRepository.findById(hearingId);
            if (hearingEntity.isPresent()) {
                HearingEntity hearingEntityToSave = hearingEntity.get();
                if (hearingEntityToSave.getHearingResponses().get(0).getResponseVersion()
                    != responseVersion) {
                    throw new PartiesNotifiedNotFoundException("002 No such response version", null);
                } else if (Integer.valueOf(hearingEntityToSave.getHearingResponses()
                                               .get(0).getResponseVersion()).equals(responseVersion)
                    && hearingEntityToSave.getHearingResponses().get(0).getPartiesNotifiedDateTime() != null) {
                    throw new PartiesNotifiedBadRequestException("003 Already set", null);
                } else {
                    hearingEntityToSave.getHearingResponses().get(0).setPartiesNotifiedDateTime(LocalDateTime.now());
                    hearingEntityToSave.getHearingResponses()
                        .get(0).setRequestVersion(partiesNotified.getRequestVersion());
                    hearingEntityToSave.getHearingResponses().get(0).setServiceData(partiesNotified.getServiceData());
                    //hearingRepository.save(hearingEntityToSave);
                }
            }

        }

    }

    private void isValidFormat(String hearingIdStr) {
        if (hearingIdStr.length() != HEARING_ID_MAX_LENGTH || !StringUtils.isNumeric(hearingIdStr)
            || hearingIdStr.charAt(0) != '2') {
            throw new BadRequestException(INVALID_HEARING_ID_DETAILS);
        }
    }


}
