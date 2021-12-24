package uk.gov.hmcts.reform.hmc.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Builder
@Value
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleAssignmentRequestResource {

    RoleRequestResource roleRequest;

    List<RoleAssignmentResource> requestedRoles;

}
