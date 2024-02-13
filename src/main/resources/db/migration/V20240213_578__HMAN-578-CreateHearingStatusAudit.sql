CREATE TABLE public.hearing_status_audit (
                         hmcts_service_id varchar(4) not null,
                         hearing_id varchar(20) not null,
                         status varchar(20) not null,
                         status_update_date_time timestamp without time zone not null,
                         hearing_event varchar(40) not null,
                         http_status varchar(3) not null,
                         source varchar(15) not null,
                         target varchar(15) not null,
                         error_description jsonb,
                         request_version varchar(5) not null,
                         response_date_time timestamp without time zone not null
);
