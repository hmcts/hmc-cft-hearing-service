CREATE TABLE public.hearing_status_audit (
                         id bigint not null,
                         hmcts_service_id varchar(4) not null,
                         hearing_id varchar(20) not null,
                         status varchar(30) not null,
                         status_update_date_time timestamp without time zone,
                         hearing_event varchar(40) not null,
                         http_status varchar(3),
                         source varchar(15),
                         target varchar(15),
                         error_description jsonb,
                         request_version varchar(5) not null,
                         response_date_time timestamp without time zone
);

ALTER TABLE ONLY public.hearing_status_audit
    ADD CONSTRAINT hearing_status_audit_pkey PRIMARY KEY (id);

CREATE SEQUENCE public.hearing_status_audit_id_seq
    start with 1
    increment by 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.hearing_status_audit_id_seq OWNED BY public.hearing_status_audit.id;

ALTER TABLE ONLY public.hearing_status_audit ALTER COLUMN id SET DEFAULT nextval('public.hearing_status_audit_id_seq'::regclass);

