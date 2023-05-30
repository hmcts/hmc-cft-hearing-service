package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.hmc.domain.model.enums.LinkType;

import java.io.Serializable;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class PreviousLinkedGroupDetails extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 9069607205338784728L;

    private Long linkedGroupId;
    private String requestId;
    private String requestName;
    private LocalDateTime requestDateTime;
    private LinkType linkType;
    private String reasonForLink;
    private String status;
    private String linkedComments;
    private Long linkedGroupLatestVersion;
}
