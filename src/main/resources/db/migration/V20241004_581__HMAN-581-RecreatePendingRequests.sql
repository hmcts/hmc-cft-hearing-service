DROP TABLE public.pending_requests;

CREATE TABLE public.pending_requests (
                         id bigint not null,
                         hearing_id bigint not null,
                         version_number integer not null,
                         submitted_date_time timestamp without time zone not null,
                         retry_count integer not null,
                         last_tried_date_time timestamp without time zone not null,
                         status varchar(20) not null,
                         incident_flag boolean,
                         message text
);
