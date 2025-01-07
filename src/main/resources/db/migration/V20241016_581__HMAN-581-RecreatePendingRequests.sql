DROP SEQUENCE IF EXISTS public.pending_requests_seq;
DROP TABLE IF EXISTS public.pending_requests;

CREATE TABLE public.pending_requests (
                         id bigint not null,
                         hearing_id bigint not null,
                         version_number integer not null,
                         message_type varchar(20) not null,
                         submitted_date_time timestamp without time zone not null,
                         deployment_id varchar(20) not null,
                         retry_count integer not null,
                         last_tried_date_time timestamp without time zone not null,
                         status varchar(20) not null,
                         incident_flag boolean,
                         message text
);

CREATE SEQUENCE public.pending_requests_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.pending_requests_id_seq OWNED BY public.pending_requests.id;

ALTER TABLE ONLY public.pending_requests ALTER COLUMN id SET DEFAULT nextval('public.pending_requests_id_seq'::regclass);

