package uk.gov.hmcts.reform.hmc.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

@Table(name = "hearing_response")
@Entity
@Data
@SecondaryTable(name = "hearing",
    pkJoinColumns = {
        @PrimaryKeyJoinColumn(name = "hearing_id")})
public class HearingResponseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,
        generator = "hearing_response_id_seq")
    @Column(name = "hearing_response_id")
    private Long hearingResponseId;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Column(name = "received_date_time", nullable = false)
    private LocalDateTime requestTimeStamp;

    @Column(name = "listing_status", nullable = false)
    private String listingStatus;

    @Column(name = "listing_case_status", nullable = false)
    private String listingCaseStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hearing_id")
    private HearingEntity hearing;

    @OneToMany(mappedBy = "hearingResponse")
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<HearingDayDetailsEntity> hearingDayDetails;

    @Column(name = "request_version", nullable = false)
    private String requestVersion;

    @Column(name = "response_version", nullable = false)
    private String responseVersion;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Column(name = "parties_notified_datetime")
    private LocalDateTime partiesNotifiedDateTime;

    @Column(name = "service_data", columnDefinition = "jsonb")
    @Convert(converter = JsonDataConverter.class)
    private JsonNode serviceData;
}
