CREATE TABLE public.hearing_response (
                         hearing_response_id bigint not null,
                         hearing_id bigint not null,
                         received_date_time timestamp without time zone not null,
                         listing_status varchar(60) not null,
                         lisitng_case_status varchar(60) not null
);

ALTER TABLE ONLY public.hearing_response
    ADD CONSTRAINT hearing_response_pkey PRIMARY KEY (hearing_response_id);

ALTER TABLE ONLY public.hearing_response
    ADD CONSTRAINT fk_hearing_response_hearing FOREIGN KEY (hearing_id) REFERENCES public.hearing(hearing_id);

CREATE SEQUENCE public.hearing_response_id_seq
    start with 1
    increment by 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.hearing_response_id_seq OWNED BY public.hearing_response.hearing_response_id;

ALTER TABLE ONLY public.hearing_response ALTER COLUMN hearing_response_id SET DEFAULT nextval('public.hearing_response_id_seq'::regclass);

CREATE TABLE public.hearing_day_details (
                         hearing_day_id bigint not null,
                         hearing_response_id bigint not null,
                         start_date_time timestamp without time zone not null,
                         end_date_time timestamp without time zone not null,
                         list_assist_session_id varchar(60) not null,
                         venue_id varchar(60) not null,
                         room_id varchar(60) not null
);

ALTER TABLE ONLY public.hearing_day_details
    ADD CONSTRAINT hearing_day_details_pkey PRIMARY KEY (hearing_day_id);

ALTER TABLE ONLY public.hearing_day_details
    ADD CONSTRAINT fk_hearing_day_details_hearing_response FOREIGN KEY (hearing_response_id) REFERENCES public.hearing_response(hearing_response_id);

CREATE SEQUENCE public.hearing_day_details_id_seq
    start with 1
    increment by 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.hearing_day_details_id_seq OWNED BY public.hearing_day_details.hearing_day_id;

ALTER TABLE ONLY public.hearing_day_details ALTER COLUMN hearing_day_id SET DEFAULT nextval('public.hearing_day_details_id_seq'::regclass);

CREATE TABLE public.hearing_day_panel (
                         id bigint not null,
                         hearing_day_id bigint not null,
                         panel_user_id varchar(60) not null
);

ALTER TABLE ONLY public.hearing_day_panel
    ADD CONSTRAINT hearing_day_panel_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.hearing_day_panel
    ADD CONSTRAINT fk_hearing_day_panel_hearing_day_details FOREIGN KEY (hearing_day_id) REFERENCES public.hearing_day_details(hearing_day_id);

CREATE SEQUENCE public.hearing_day_panel_id_seq
    start with 1
    increment by 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.hearing_day_panel_id_seq OWNED BY public.hearing_day_panel.id;

ALTER TABLE ONLY public.hearing_day_panel ALTER COLUMN id SET DEFAULT nextval('public.hearing_day_panel_id_seq'::regclass);

CREATE TABLE public.hearing_attendee_details (
                         id bigint not null,
                         hearing_day_id bigint not null,
                         party_id varchar(40) not null,
                         party_sub_channel_type varchar(60) not null
);

ALTER TABLE ONLY public.hearing_attendee_details
    ADD CONSTRAINT hearing_attendee_details_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.hearing_attendee_details
    ADD CONSTRAINT fk_hearing_attendee_details_hearing_day_details FOREIGN KEY (hearing_day_id) REFERENCES public.hearing_day_details(hearing_day_id);

CREATE SEQUENCE public.hearing_attendee_details_id_seq
    start with 1
    increment by 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.hearing_attendee_details_id_seq OWNED BY public.hearing_attendee_details.id;

ALTER TABLE ONLY public.hearing_attendee_details ALTER COLUMN id SET DEFAULT nextval('public.hearing_attendee_details_id_seq'::regclass);
