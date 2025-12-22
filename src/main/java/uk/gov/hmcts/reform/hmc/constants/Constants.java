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
    public static final String AWAITING_ACTUALS = "AWAITING_ACTUALS";
    public static final String DELETE_HEARING = "DELETE_HEARING";
    public static final String CASE_HQ = "CASEHQ";
    public static final String EPIMS = "EPIMS";
    public static final String OVERRIDE_URL = "OVERRIDE_URL";
    public static final String REGION = "Region";
    public static final String COURT = "Court";
    public static final String CLUSTER = "Cluster";
    public static final String REQUIRED = "Required";
    public static final String NOT_REQUIRED = "Not Required";
    public static final Integer UN_NOTIFIED_HEARINGS_LIMIT = 1000;
    public static final String PENDING = "PENDING";
    public static final Integer DURATION_OF_DAY = 360;
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
    public static final String REQUEST_VERSION_UPDATE = "request-version-update";
    public static final String LA_RESPONSE = "list-assist-response";
    public static final String LA_ACK = "list-assist-ack";
    public static final String OVERRIDE_URL_EVENT = "override-url";
    public static final String HMI = "hmi";
    public static final String FH = "fh";
    public static final String HMC = "hmc";
    public static final String LA_SUCCESS_STATUS = "202";
    public static final String LA_FAILURE_STATUS = "400";
    public static final String LA_FAILURE_SERVER_STATUS = "500";
    public static final String SUCCESS_STATUS = "200";
    public static final String UPDATE_LINKED_HEARING_REQUEST = "update-linked_hearing-request";
    public static final String CREATE_LINKED_HEARING_REQUEST = "create-linked_hearing-request";
    public static final String DELETE_LINKED_HEARING_REQUEST = "delete-linked_hearing-request";
    public static final String EXCEPTION_STATUS = "Exception";
    public static final String CANCELLED = "CANCELLED";
    public static final String SERVICE_CODE_ABA1 = "ABA1";
    public static final String EXCEPTION_MESSAGE = "Hearing id: {} with Case reference: {} , Service Code: {}"
        + " and Error Description: {} updated to status {}";
    public static final String AMQP_CACHE = "com.azure.core.amqp.cache";
    public static final String AMQP_CACHE_VALUE = "true";
    public static final Integer ELASTIC_QUERY_DEFAULT_SIZE = 10;
    public static final String MANAGE_EXCEPTION_SUCCESS_MESSAGE = "successfully transitioned hearing : %s, "
        +  "from state : %s to state: %s";
    public static final String MANAGE_EXCEPTION_AUDIT_EVENT = "tech_support";
    public static final String IDAM_TECH_ADMIN_ROLE = "hmc_tech_admin";
    public static final String TECH_ADMIN_UI_SERVICE = "tech_admin_ui";
    public static final int MAX_HEARING_REQUESTS = 100;
    public static final String MANAGE_EXCEPTION_COMMIT_FAIL_EVENT = "tech_support_commit_fail";
    public static final String MANAGE_EXCEPTION_COMMIT_FAIL = "Database commit failed";
    public static final String FINAL_STATE_MESSAGE = "Hearing id: {} with Case reference: {} , Service Code: {}"
        + " and Response received but current hearing status: {}; LA status: {} no further action taken ";
    public static final String HEARING_FINAL_STATE = "hearing-is-in-final-state";
    public static final String HEARING_STATE = "finalState";
}
