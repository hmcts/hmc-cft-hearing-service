package uk.gov.hmcts.reform.hmc.service.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.domain.model.HearingStatusAuditContext;

import static uk.gov.hmcts.reform.hmc.constants.Constants.HMC;
import static uk.gov.hmcts.reform.hmc.constants.Constants.REQUEST_VERSION_UPDATE;

@Service
@Slf4j
public class HearingRequestVersionAuditService {

    private final HearingStatusAuditService hearingStatusAuditService;

    public HearingRequestVersionAuditService(HearingStatusAuditService hearingStatusAuditService) {
        this.hearingStatusAuditService = hearingStatusAuditService;
    }

    @Transactional
    public void auditChangeInRequestVersion(HearingEntity hearingEntity,
                                            int existingRequestVersion,
                                            String clientS2SToken,
                                            boolean useNow) {
        int updatedRequestVersion = hearingEntity.getLatestRequestVersion();
        if (updatedRequestVersion == existingRequestVersion) {
            return;
        }

        String versionMessage = existingRequestVersion > 0
            ? String.format("requestVersion updated from <%d> to <%d>", existingRequestVersion, updatedRequestVersion)
            : String.format("requestVersion set to <%d>", updatedRequestVersion);

        try {
            JsonNode otherInfo = new ObjectMapper().readTree("{\"" + REQUEST_VERSION_UPDATE + "\":" + " \""
                                                                 + versionMessage + "\"}");
            if (existingRequestVersion > 0) {
                HearingStatusAuditContext hearingStatusAuditContext =
                    HearingStatusAuditContext.builder()
                        .hearingEntity(hearingEntity)
                        .hearingEvent(REQUEST_VERSION_UPDATE)
                        .httpStatus(String.valueOf(HttpStatus.OK.value()))
                        .source(clientS2SToken)
                        .target(HMC)
                        .otherInfo(otherInfo)
                        .useCurrentTimestamp(useNow)
                        .build();
                hearingStatusAuditService.saveAuditTriageDetailsWithUpdatedDateOrCurrentDate(hearingStatusAuditContext);
            } else {
                HearingStatusAuditContext hearingStatusAuditContext =
                    HearingStatusAuditContext.builder()
                        .hearingEntity(hearingEntity)
                        .hearingEvent(REQUEST_VERSION_UPDATE)
                        .httpStatus(String.valueOf(HttpStatus.OK.value()))
                        .source(clientS2SToken)
                        .target(HMC)
                        .otherInfo(otherInfo)
                        .build();
                hearingStatusAuditService.saveAuditTriageDetailsWithCreatedDate(hearingStatusAuditContext);
            }
        } catch (JsonProcessingException e) {
            log.error("Unable to audit requestVersion update: {}", versionMessage);
        }
    }
}
