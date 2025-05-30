package uk.gov.hmcts.reform.hmc.data;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Data;

import java.time.LocalDateTime;

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
