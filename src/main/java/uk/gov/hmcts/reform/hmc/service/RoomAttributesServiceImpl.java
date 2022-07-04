package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.model.RoomAttribute;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.hasText;

@Service
@Slf4j
public class RoomAttributesServiceImpl implements RoomAttributesService {

    private final Map<String, RoomAttribute> roomAttributeByReasonableAdjustmentCode;
    private final Map<String, RoomAttribute> roomAttributeByRoomAttributeCode;

    public RoomAttributesServiceImpl(@Value("${room-attributes.file}") Resource roomAttributesFile)
        throws IOException {
        List<RoomAttribute> roomAttributes = new ObjectMapper()
            .readValue(roomAttributesFile.getFile(), new TypeReference<>(){});

        roomAttributeByReasonableAdjustmentCode = roomAttributes.stream()
            .filter(roomAttribute -> hasText(roomAttribute.getReasonableAdjustmentCode()))
            .collect(Collectors.toMap(RoomAttribute::getReasonableAdjustmentCode, Function.identity()));
        roomAttributeByRoomAttributeCode = roomAttributes.stream()
            .collect(Collectors.toMap(RoomAttribute::getRoomAttributeCode, Function.identity()));

        log.info("[Room Attributes] Loaded file: {} - total records: {}; with reasonable adjustment codes: {}",
                 roomAttributesFile.getFilename(),
                 roomAttributes.size(),
                 roomAttributeByReasonableAdjustmentCode.size());
    }

    @Override
    public Optional<RoomAttribute> findByReasonableAdjustmentCode(String reasonableAdjustmentCode) {
        return Optional.ofNullable(roomAttributeByReasonableAdjustmentCode.get(reasonableAdjustmentCode));
    }

    @Override
    public Optional<RoomAttribute> findByRoomAttributeCode(String roomAttributeCode) {
        return Optional.ofNullable(roomAttributeByRoomAttributeCode.get(roomAttributeCode));
    }
}
