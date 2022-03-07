package uk.gov.hmcts.reform.hmc.helper;

import uk.gov.hmcts.reform.hmc.data.HearingAttendeeDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayPanelEntity;
import uk.gov.hmcts.reform.hmc.model.Attendee;
import uk.gov.hmcts.reform.hmc.model.HearingDaySchedule;

import java.util.ArrayList;
import java.util.List;

public class GetHearingResponseCommonCode {

    protected HearingDaySchedule setHearingDayScheduleDetails(HearingDayDetailsEntity detailEntity) {
        HearingDaySchedule hearingDaySchedule = new HearingDaySchedule();
        hearingDaySchedule.setHearingStartDateTime(detailEntity.getStartDateTime());
        hearingDaySchedule.setHearingEndDateTime(detailEntity.getEndDateTime());
        hearingDaySchedule.setListAssistSessionId(detailEntity.getListAssistSessionId());
        hearingDaySchedule.setHearingVenueId(detailEntity.getVenueId());
        hearingDaySchedule.setHearingRoomId(detailEntity.getRoomId());
        return hearingDaySchedule;
    }

    protected void setAttendeeDetails(List<HearingAttendeeDetailsEntity> attendeeDetailsEntities,
                                    HearingDaySchedule hearingDaySchedule) {
        List<Attendee> attendeeList = new ArrayList<>();
        for (HearingAttendeeDetailsEntity attendeeDetailEntity : attendeeDetailsEntities) {
            Attendee attendee = new Attendee();
            attendee.setPartyId(attendeeDetailEntity.getPartyId());
            attendee.setHearingSubChannel(attendeeDetailEntity.getPartySubChannelType());
            attendeeList.add(attendee);
        }
        hearingDaySchedule.setAttendees(attendeeList);
    }

    protected void setHearingJudgeAndPanelMemberIds(HearingDayPanelEntity hearingDayPanelEntity,
                                                  HearingDaySchedule hearingDaySchedule) {
        if (null == hearingDayPanelEntity.getIsPresiding() || !hearingDayPanelEntity.getIsPresiding()) {
            hearingDaySchedule.setPanelMemberId(hearingDayPanelEntity.getPanelUserId());
        } else {
            hearingDaySchedule.setHearingJudgeId(hearingDayPanelEntity.getPanelUserId());
        }
    }

}
