ALTER TABLE public.linked_group_details ADD COLUMN linked_group_latest_version bigint not null;

--
-- Table: linked_hearing_details_audit
--

CREATE SEQUENCE public.linked_hearing_details_audit_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.linked_hearing_details_audit (
                                                 linked_hearing_details_audit_id bigint not null default nextval('public.linked_hearing_details_audit_id_seq'::regclass),
                                                 linked_group_id bigint not null,
                                                 linked_group_version bigint not null,
                                                 hearing_id bigint not null,
                                                 linked_order bigint
);

ALTER SEQUENCE public.linked_hearing_details_audit_id_seq OWNED BY public.linked_hearing_details_audit.linked_hearing_details_audit_id;

ALTER TABLE ONLY public.linked_hearing_details_audit
    ADD CONSTRAINT linked_hearing_details_audit_id_pkey PRIMARY KEY (linked_hearing_details_audit_id);

--
-- Table: linked_group_details_audit
--
CREATE SEQUENCE public.linked_group_details_audit_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


CREATE TABLE public.linked_group_details_audit (
                                                 linked_group_details_audit_id bigint not null default nextval('public.linked_hearing_details_audit_id_seq'::regclass),
                                                 linked_group_id bigint not null,
                                                 linked_group_version bigint not null,
                                                 request_id varchar(50) not null,
                                                 request_name varchar(255),
                                                 request_date_time timestamp without time zone default now() not null,
                                                 link_type public.link_type_enum not null,
                                                 reason_for_link varchar(255) not null,
                                                 status varchar(40) not null default 'Awaiting listing',
                                                 linked_comments varchar(4000)
);

ALTER SEQUENCE public.linked_group_details_audit_id_seq OWNED BY public.linked_group_details_audit.linked_group_details_audit_id;

ALTER TABLE ONLY public.linked_group_details_audit
    ADD CONSTRAINT linked_group_details_audit_id_pkey PRIMARY KEY (linked_group_details_audit_id);

--
-- Table: hearing
--
ALTER TABLE public.hearing ADD COLUMN linked_group_id bigint;
ALTER TABLE public.hearing ADD COLUMN linked_order bigint;
ALTER TABLE public.hearing ADD COLUMN is_linked_flag boolean;

--
-- Table: case_hearing_requests
--
ALTER TABLE public.case_hearing_request DROP COLUMN is_linked_flag;

DROP TABLE IF EXISTS LINKED_HEARING_DETAILS;


ALTER TABLE ONLY public.linked_group_details_audit
ADD CONSTRAINT fk_linked_group_details_audit_linked_group_id FOREIGN KEY (linked_group_id) REFERENCES public.linked_group_details(linked_group_id);

ALTER TABLE ONLY public.linked_hearing_details_audit
ADD CONSTRAINT fk_linked_hearing_details_audit_linked_group_id FOREIGN KEY (linked_group_id) REFERENCES public.linked_group_details(linked_group_id);

ALTER TABLE ONLY public.linked_hearing_details_audit
ADD CONSTRAINT fk_linked_hearing_details_audit_hearing_id FOREIGN KEY (hearing_id) REFERENCES public.hearing(hearing_id);

ALTER TABLE ONLY public.hearing
ADD CONSTRAINT fk_hearing_link_group_id FOREIGN KEY (linked_group_id) REFERENCES public.linked_group_details(linked_group_id);
