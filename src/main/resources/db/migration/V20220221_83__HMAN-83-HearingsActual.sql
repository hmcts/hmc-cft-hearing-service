CREATE TYPE public.hearingresulttype AS ENUM (
    'COMPLETED',
    'ADJOURNED',
    'CANCELLED'
);

CREATE TABLE public.actual_hearing (
                         actual_hearing_id bigint not null,
                         hearing_response_id bigint not null,
                         actual_hearing_type varchar(40) not null,
                         actual_hearing_is_final_flag boolean not null,
                         hearing_result_type public.hearingresulttype not null,
                         hearing_result_reason_type varchar(70),
                         hearing_result_date timestamp without time zone not null
);

ALTER TABLE ONLY public.actual_hearing
    ADD CONSTRAINT actual_hearing_pkey PRIMARY KEY (actual_hearing_id);

ALTER TABLE ONLY public.actual_hearing
    ADD CONSTRAINT fk_actual_hearing_hearing_response FOREIGN KEY (hearing_response_id) REFERENCES public.hearing_response(hearing_response_id);

CREATE SEQUENCE public.actual_hearing_id_seq
    start with 1
    increment by 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.actual_hearing_id_seq OWNED BY public.actual_hearing.actual_hearing_id;

ALTER TABLE ONLY public.actual_hearing ALTER COLUMN actual_hearing_id SET DEFAULT nextval('public.actual_hearing_id_seq'::regclass);

CREATE TABLE public.actual_hearing_day (
                         actual_hearing_day_id bigint not null,
                         actual_hearing_id bigint not null,
                         hearing_date date not null,
                         start_date_time timestamp without time zone not null,
                         end_date_time timestamp without time zone not null
);

ALTER TABLE ONLY public.actual_hearing_day
    ADD CONSTRAINT actual_hearing_day_pkey PRIMARY KEY (actual_hearing_day_id);

ALTER TABLE ONLY public.actual_hearing_day
    ADD CONSTRAINT fk_actual_hearing_day_actual_hearing FOREIGN KEY (actual_hearing_id) REFERENCES public.actual_hearing(actual_hearing_id);

ALTER TABLE ONLY public.actual_hearing_day
    ADD CONSTRAINT uc_actual_hearing_day_actual_hearing_hearing_date UNIQUE (actual_hearing_id,hearing_date);

CREATE SEQUENCE public.actual_hearing_day_id_seq
    start with 1
    increment by 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.actual_hearing_day_id_seq OWNED BY public.actual_hearing_day.actual_hearing_day_id;

ALTER TABLE ONLY public.actual_hearing_day ALTER COLUMN actual_hearing_day_id SET DEFAULT nextval('public.actual_hearing_day_id_seq'::regclass);

CREATE TABLE public.actual_hearing_day_pauses (
                         pauses_id bigint not null,
                         actual_hearing_day_id bigint not null,
                         pause_date_time timestamp without time zone not null,
                         resume_date_time timestamp without time zone not null
);

ALTER TABLE ONLY public.actual_hearing_day_pauses
    ADD CONSTRAINT actual_hearing_day_pauses_pkey PRIMARY KEY (pauses_id);

ALTER TABLE ONLY public.actual_hearing_day_pauses
    ADD CONSTRAINT fk_actual_hearing_day_pauses_actual_hearing_day FOREIGN KEY (actual_hearing_day_id) REFERENCES public.actual_hearing_day(actual_hearing_day_id);

CREATE SEQUENCE public.pauses_id_seq
    start with 1
    increment by 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.pauses_id_seq OWNED BY public.actual_hearing_day_pauses.pauses_id;

ALTER TABLE ONLY public.actual_hearing_day_pauses ALTER COLUMN pauses_id SET DEFAULT nextval('public.pauses_id_seq'::regclass);

CREATE TABLE public.actual_hearing_party (
                         actual_party_id bigint not null,
                         actual_hearing_day_id bigint not null,
                         party_id varchar(40),
                         actual_party_role_type varchar(40) not null,
                         did_not_attend_flag boolean
);

ALTER TABLE ONLY public.actual_hearing_party
    ADD CONSTRAINT actual_hearing_party_pkey PRIMARY KEY (actual_party_id);

ALTER TABLE ONLY public.actual_hearing_party
    ADD CONSTRAINT fk_actual_hearing_party_actual_hearing_day FOREIGN KEY (actual_hearing_day_id) REFERENCES public.actual_hearing_day(actual_hearing_day_id);

CREATE SEQUENCE public.actual_party_id_seq
    start with 1
    increment by 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.actual_party_id_seq OWNED BY public.actual_hearing_party.actual_party_id;

ALTER TABLE ONLY public.actual_hearing_party ALTER COLUMN actual_party_id SET DEFAULT nextval('public.actual_party_id_seq'::regclass);

CREATE TABLE public.actual_attendee_individual_detail (
                         actual_attendee_individual_detail_id bigint not null,
                         actual_party_id bigint not null,
                         first_name varchar(100),
                         last_name varchar(100),
                         party_organisation_name varchar(2000),
                         party_actual_sub_channel_type varchar(70) not null
);

ALTER TABLE ONLY public.actual_attendee_individual_detail
    ADD CONSTRAINT actual_attendee_individual_detail_pkey PRIMARY KEY (actual_attendee_individual_detail_id);

ALTER TABLE ONLY public.actual_attendee_individual_detail
    ADD CONSTRAINT fk_actual_attendee_individual_detail_actual_party FOREIGN KEY (actual_party_id) REFERENCES public.actual_hearing_party(actual_party_id);

CREATE SEQUENCE public.actual_attendee_individual_detail_id_seq
    start with 1
    increment by 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.actual_attendee_individual_detail_id_seq OWNED BY public.actual_attendee_individual_detail.actual_attendee_individual_detail_id;

ALTER TABLE ONLY public.actual_attendee_individual_detail ALTER COLUMN actual_attendee_individual_detail_id SET DEFAULT nextval('public.actual_attendee_individual_detail_id_seq'::regclass);

CREATE TABLE public.actual_party_relationship_detail (
                         actual_party_relationship_id bigint not null,
                         source_actual_party_id bigint not null,
                         target_actual_party_id bigint not null
);

ALTER TABLE ONLY public.actual_party_relationship_detail
    ADD CONSTRAINT actual_party_relationship_detail_pkey PRIMARY KEY (actual_party_relationship_id);

ALTER TABLE ONLY public.actual_party_relationship_detail
    ADD CONSTRAINT fk_actual_party_relationship_detail_actual_party FOREIGN KEY (source_actual_party_id) REFERENCES public.actual_hearing_party(actual_party_id);

CREATE SEQUENCE public.actual_party_relationship_id_seq
    start with 1
    increment by 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.actual_party_relationship_id_seq OWNED BY public.actual_party_relationship_detail.actual_party_relationship_id;

ALTER TABLE ONLY public.actual_party_relationship_detail ALTER COLUMN actual_party_relationship_id SET DEFAULT nextval('public.actual_party_relationship_id_seq'::regclass);
