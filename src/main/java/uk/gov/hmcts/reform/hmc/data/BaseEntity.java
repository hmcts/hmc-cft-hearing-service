package uk.gov.hmcts.reform.hmc.data;

import lombok.Data;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;

@Data
@MappedSuperclass
public class BaseEntity {

    @Column(name = "created_date_time")
    private LocalDateTime createdDateTime;

    @PrePersist
    public void prePersist() {
        createdDateTime = LocalDateTime.now();
    }

}
