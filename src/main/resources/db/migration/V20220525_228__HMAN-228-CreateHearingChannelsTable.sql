CREATE TABLE public.hearing_channels (
                        hearing_channels_id bigint not null,
                        hearing_channel_type varchar(70) not null,
                        case_hearing_id bigint not null
);

ALTER TABLE ONLY public.hearing_channels
    ADD CONSTRAINT hearing_channels_pkey PRIMARY KEY (hearing_channels_id);

CREATE SEQUENCE public.hearing_channels_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.hearing_channels_id_seq OWNED BY public.hearing_channels.hearing_channels_id;

ALTER TABLE ONLY public.hearing_channels ALTER COLUMN hearing_channels_id SET DEFAULT nextval('public.hearing_channels_id_seq'::regclass);

ALTER TABLE ONLY public.hearing_channels
    ADD CONSTRAINT fk_hearing_channels_case_hearing_request FOREIGN KEY (case_hearing_id) REFERENCES public.case_hearing_request(case_hearing_id);

ALTER TABLE ONLY public.hearing_channels ADD COLUMN created_date_time timestamp without time zone;