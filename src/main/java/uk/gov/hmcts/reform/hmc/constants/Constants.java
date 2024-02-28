package uk.gov.hmcts.reform.hmc.constants;

public final class Constants {

    private Constants() {
    }

    public static final String POST_HEARING_STATUS = "HEARING_REQUESTED";
    public static final String HEARING_STATUS_UPDATE_REQUESTED = "UPDATE_REQUESTED";
    public static final Integer VERSION_NUMBER_TO_INCREMENT = 1;
    public static final String EMAIL_TYPE = "EMAIL";
    public static final String PHONE_TYPE = "PHONE";
    public static final String UNAVAILABILITY_RANGE_TYPE = "Range";
    public static final String UNAVAILABILITY_DOW_TYPE = "DOW";
    public static final Integer HEARING_ID_VALID_LENGTH = 10;
    public static final String CANCELLATION_REQUESTED = "CANCELLATION_REQUESTED";
    public static final String MESSAGE_TYPE = "message_type";
    public static final String HEARING_ID = "hearing_id";
    public static final String CFT_HEARING_SERVICE = "<CFT Hearing Service>";
    public static final String WRITE = "<WRITE>";
    public static final String READ = "<READ>";
    public static final String HMC_TO_HMI = "hmc-to-hmi";
    public static final String HMC_FROM_HMI = "hmc-from-hmi";
    public static final String TOPIC_HMC_TO_CFT = "hmc-to-cft";

    public static final String ERROR_PROCESSING_MESSAGE = "Error occurred during service bus processing. "
        + "Service:{}. Entity:{}. Method:{}. Hearing ID: {}.";
    public static final String ERROR_SENDING_MESSAGE = "Error occurred during service bus sending message. "
        + "Service:{} . Entity: {}. Method: {}. Hearing ID: {}.";

    public static final String NO_DEFINED = "NO_DEFINED";
    public static final String REQUEST_HEARING = "REQUEST_HEARING";
    public static final String AMEND_HEARING = "AMEND_HEARING";
    public static final String DELETE_HEARING = "DELETE_HEARING";
    public static final String CASE_HQ = "CASEHQ";
    public static final String EPIMS = "EPIMS";
    public static final String Region = "Region";
    public static final String COURT = "Court";
    public static final String CLUSTER = "Cluster";
    public static final String REQUIRED = "Required";
    public static final String NOT_REQUIRED = "Not Required";
    public static final Integer UN_NOTIFIED_HEARINGS_LIMIT = 1000;
    public static final Integer FIRST_PAGE = 0;
    public static final String PENDING = "PENDING";
    public static final Integer DURATION_OF_DAY = 360;
    public static final String LIST_ASSIST = "ListAssist";
    public static final String ERROR = "ERROR";
    public static final String LIST_ASSIST_SUCCESSFUL_RESPONSE = "Response received from ListAssist successfully";
    public static final String CANCEL = "CNCL";
    public static final String AMEND_REASON_CODE = "AMEND";
    public static final String HMCTS_SERVICE_ID = "hmctsServiceId";
    public static final String HMCTS_DEPLOYMENT_ID = "hmctsDeploymentId";
    public static final String LATEST_HEARING_REQUEST_VERSION = "Latest-Hearing-Request-Version";
    public static final String LATEST_HEARING_STATUS = "Latest-Hearing-Status";
    public static final Integer HMCTS_DEPLOYMENT_ID_MAX_SIZE = 40;
    public static final String UPDATE_HEARING_REQUEST = "update-hearing-request";
    public static final String CREATE_HEARING_REQUEST = "create-hearing-request";
    public static final String DELETE_HEARING_REQUEST = "delete-hearing-request";
    public static final String POST_HEARING_ACTUALS_COMPLETION = "post-hearing-actuals-completion";
    public static final String PUT_HEARING_ACTUALS_COMPLETION = "put-hearing-actuals-completion";
    public static final String PUT_PARTIES_NOTIFIED = "put-parties-notified";
    public static final String LA_RESPONSE = "list-assist-response";
    public static final String LA_ACK = "list-assist-ack";
    public static final String HMI = "hmi";
    public static final String FH = "fh";
    public static final String HMC = "hmc";
    public static final String LA_SUCCESS_STATUS = "202";
    public static final String LA_FAILURE_STATUS = "400";
    public static final String SUCCESS_STATUS = "200";
}
