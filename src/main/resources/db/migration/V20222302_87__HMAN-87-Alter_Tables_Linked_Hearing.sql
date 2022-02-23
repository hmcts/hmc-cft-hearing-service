
--
-- Table: linked_group_details
--
CREATE SEQUENCE public.linked_group_details_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


CREATE TYPE public.link_type_enum AS enum (
      'Ordered',
      'Same Slot'
);

CREATE TABLE public.linked_group_details (
                                                 linked_group_id bigint not null default nextval('public.linked_group_details_id_seq'::regclass),
                                                 request_id varchar(50) not null,
                                                 request_name varchar(255),
                                                 request_date_time timestamp without time zone default now() not null,
                                                 link_type public.link_type_enum not null,
                                                 reason_for_link varchar(255) not null,
                                                 status varchar(40) not null default 'awaiting listing',
                                                 linked_comments varchar(4000)
);

ALTER SEQUENCE public.linked_group_details_id_seq OWNED BY public.linked_group_details.linked_group_id;

ALTER TABLE ONLY public.linked_group_details
    ADD CONSTRAINT linked_group_id_pkey PRIMARY KEY (linked_group_id);


--
-- Table: linked_hearing_details
--

CREATE SEQUENCE public.linked_hearing_details_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


CREATE TABLE public.linked_hearing_details (
                                                 linked_hearing_id bigint not null default nextval('public.linked_hearing_details_id_seq'::regclass),
                                                 linked_group_id bigint not null,
                                                 hearing_id bigint not null,
                                                 linked_order bigint

);

ALTER SEQUENCE public.linked_hearing_details_id_seq OWNED BY public.linked_hearing_details.linked_hearing_id;

ALTER TABLE ONLY public.linked_hearing_details
    ADD CONSTRAINT linked_hearing_id_pkey PRIMARY KEY (linked_hearing_id);


ALTER TABLE ONLY public.linked_hearing_details
ADD CONSTRAINT fk_linked_hearing_details_linked_group_id FOREIGN KEY (linked_group_id) REFERENCES public.linked_group_details(linked_group_id);

ALTER TABLE ONLY public.linked_hearing_details
ADD CONSTRAINT fk_linked_hearing_details_hearing_id FOREIGN KEY (hearing_id) REFERENCES public.hearing(hearing_id);
