package uk.gov.hmcts.reform.hmc.helper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;

@Mapper(componentModel = "spring")
public interface HearingMapper {

    @Mapping(target = "id", ignore = true)
    HearingEntity modelToEntity(String status);
}
