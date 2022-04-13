package uk.gov.hmcts.reform.hmc.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.hmc.ApplicationParams;
import uk.gov.hmcts.reform.hmc.BaseTest;
import uk.gov.hmcts.reform.hmc.exceptions.BadFutureHearingRequestException;
import uk.gov.hmcts.reform.hmc.exceptions.FutureHearingServerException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubDeleteMethodThrowingError;
import static uk.gov.hmcts.reform.hmc.WiremockFixtures.stubSuccessfullyDeleteLinkedHearingGroups;
import static uk.gov.hmcts.reform.hmc.client.futurehearing.FutureHearingErrorDecoder.INVALID_REQUEST;
import static uk.gov.hmcts.reform.hmc.client.futurehearing.FutureHearingErrorDecoder.SERVER_ERROR;

public class FutureHearingRepositoryIT extends BaseTest {

    private static final String TOKEN = "example-token";
    private static final String GET_TOKEN_URL = "/FH_GET_TOKEN_URL";
    private static final String HMI_REQUEST_URL = "/resources/linked-hearing-group";
    private static final String REQUEST_ID = "12345";
    private static final String DELETE_HEARING_DATA_SCRIPT = "classpath:sql/delete-hearing-tables.sql";
    private static final String INSERT_LINKED_HEARINGS_DATA_SCRIPT = "classpath:sql/insert-linked-hearings.sql";

    @Autowired
    private ApplicationParams applicationParams;

    @Autowired
    private DefaultFutureHearingRepository defaultFutureHearingRepository;

    @Nested
    @DisplayName("Delete Linked Hearing Group")
    class DeleteLinkedHearingGroup {

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
        void shouldSuccessfullyDeleteLinkedHearingGroup() {
            stubSuccessfullyDeleteLinkedHearingGroups(TOKEN, REQUEST_ID);
            defaultFutureHearingRepository.deleteLinkedHearingGroup(REQUEST_ID);
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
        void shouldThrow400AuthenticationException() {
            stubDeleteMethodThrowingError(400, HMI_REQUEST_URL + "/" + REQUEST_ID);
            assertThatThrownBy(() -> defaultFutureHearingRepository.deleteLinkedHearingGroup(REQUEST_ID))
                .isInstanceOf(BadFutureHearingRequestException.class)
                .hasMessageContaining(INVALID_REQUEST);
        }

        @Test
        @Sql(scripts = {DELETE_HEARING_DATA_SCRIPT, INSERT_LINKED_HEARINGS_DATA_SCRIPT})
        void shouldThrow500AuthenticationException() {
            stubDeleteMethodThrowingError(500, HMI_REQUEST_URL + "/" + REQUEST_ID);
            assertThatThrownBy(() -> defaultFutureHearingRepository.deleteLinkedHearingGroup(REQUEST_ID))
                .isInstanceOf(FutureHearingServerException.class)
                .hasMessageContaining(SERVER_ERROR);
        }
    }
}
