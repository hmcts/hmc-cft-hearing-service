package uk.gov.hmcts.reform.hmc.domain.model.enums;

import java.util.Arrays;
import java.util.Locale;

public enum ListAssistCaseStatus {
    AWAITING_LISTING("Awaiting Listing"),
    CASE_CLOSED("Case Closed"),
    CASE_CREATED("Case Created"),
    HEARING_COMPLETED("Hearing Completed"),
    LISTED("Listed"),
    PENDING_RELISTING("Pending Relisting");

    public final String label;

    ListAssistCaseStatus(String label) {
        this.label = label;
    }

    public static ListAssistCaseStatus getByLabel(String label) {
        return Arrays.stream(ListAssistCaseStatus.values())
                .filter(eachStatus -> eachStatus.label.toLowerCase(Locale.ROOT)
                        .equals(label.toLowerCase(Locale.ROOT))).findAny().orElse(null);
    }

    public static String getLabel(String name) {
        ListAssistCaseStatus status = getByName(name);
        return null == status ? null : status.label;
    }

    public static ListAssistCaseStatus getByName(String name) {
        return Arrays.stream(ListAssistCaseStatus.values())
                .filter(eachStatus -> eachStatus.name().toLowerCase(Locale.ROOT)
                        .equals(name.toLowerCase(Locale.ROOT))).findAny().orElse(null);
    }

    public static boolean isValidLabel(String label) {
        return Arrays.stream(values())
                .anyMatch(eachStatus -> eachStatus.label.toLowerCase(Locale.ROOT)
                                .equals(label.toLowerCase(Locale.ROOT)));
    }

    public static boolean isValidName(String name) {
        return Arrays.stream(values())
                .anyMatch(eachStatus -> eachStatus.name().equals(name));
    }

}
