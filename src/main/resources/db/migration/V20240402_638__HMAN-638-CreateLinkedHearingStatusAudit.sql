CREATE TABLE public.linked_hearing_status_audit (
                         id bigint not null,
                         hmcts_service_id varchar(4) not null,
                         linked_group_id varchar(20) not null,
                         linked_group_version varchar(5) not null,
                         linked_hearing_event_date_Time timestamp without time zone,
                         linked_hearing_event varchar(40) not null,
                         http_status varchar(3),
                         source varchar(15),
                         target varchar(15),
                         error_description jsonb,
                         other_info jsonb,
                         linked_group_hearings jsonb
);

ALTER TABLE ONLY public.linked_hearing_status_audit
    ADD CONSTRAINT linked_hearing_status_audit_pkey PRIMARY KEY (id);

CREATE SEQUENCE public.linked_hearing_status_audit_id_seq
    start with 1
    increment by 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.linked_hearing_status_audit_id_seq OWNED BY public.linked_hearing_status_audit.id;

ALTER TABLE ONLY public.linked_hearing_status_audit ALTER COLUMN id SET DEFAULT nextval('public.linked_hearing_status_audit_id_seq'::regclass);
