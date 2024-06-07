CREATE OR REPLACE VIEW public.max_hearing_request_version AS
    SELECT
        hearing_id,
        MAX(hearing_request_version) AS max_hearing_request_version
    FROM
        case_hearing_request
    GROUP BY
        hearing_id;
