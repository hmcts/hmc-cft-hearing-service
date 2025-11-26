package uk.gov.hmcts.reform.hmc.data;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;

@Data
@MappedSuperclass
public class AuditBaseEntity {

    @Column(name = "http_status")
    private String httpStatus;

    @Column(name = "source")
    private String source;

    @Column(name = "target")
    private String target;

    @Column(name = "error_description", columnDefinition = "jsonb")
    @Convert(converter = JsonDataConverter.class)
    @SuppressWarnings("java:S2789")
    private JsonNode errorDescription;

    @Column(name = "other_info", columnDefinition = "jsonb")
    @Convert(converter = JsonDataConverter.class)
    @SuppressWarnings("java:S2789")
    private JsonNode otherInfo = null;

}
