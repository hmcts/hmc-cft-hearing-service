package uk.gov.hmcts.reform.hmc.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Builder
@Data
public class RoleAssignment {
    private String id;
    private String actorIdType; // currently IDAM
    private String actorId;
    private String roleType; // ORGANISATION, CASE
    private String roleName;
    private String classification;
    private String grantType; // BASIC, STANDARD, SPECIFIC, CHALLENGED, EXCLUDED
    private String roleCategory; // JUDICIAL, STAFF
    private Boolean readOnly;
    private Instant beginTime;
    private Instant endTime;
    private Instant created;
    private List<String> authorisations;
    private RoleAssignmentAttributes attributes;
}
