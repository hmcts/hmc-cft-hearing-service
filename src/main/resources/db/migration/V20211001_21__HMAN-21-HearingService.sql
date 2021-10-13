CREATE TABLE public.case_hearing_request (
                         case_hearing_id bigint not null,
                         auto_list_flag boolean not null,
                         hearing_type varchar(40) not null,
                         required_duration_in_minutes integer not null,
                         hearing_priority_type varchar(60) not null,
                         number_of_physical_attendees integer,
                         hearing_in_welsh_flag boolean,
                         private_hearing_required_flag boolean,
                         lead_judge_contract_type varchar(70),
                         first_date_time_of_hearing_must_be timestamp without time zone,
                         hmcts_service_id varchar(4) not null,
                         case_reference varchar not null,
                         hearing_request_received_date_time timestamp without time zone not null,
                         external_case_reference varchar(70),
                         case_url_context_path varchar(1024) not null,
                         hmcts_internal_case_name varchar(1024) not null,
                         public_case_name varchar(1024) not null,
                         additional_security_required_flag boolean,
                         owning_location_id varchar(40) not null,
                         case_restricted_flag boolean not null,
                         case_sla_start_date timestamp without time zone not null,
                         version_number integer not null,
                         hearing_id varchar(10) not null,
                         interpreter_booking_required_flag boolean,
                         is_linked_flag boolean,
                         listing_comments varchar(5000),
                         requester varchar(60),
                         hearing_window_start_date_range timestamp without time zone,
                         hearing_window_end_date_range timestamp without time zone,
                         request_timestamp timestamp without time zone not null
);

ALTER TABLE ONLY public.case_hearing_request
    ADD CONSTRAINT case_hearing_pkey PRIMARY KEY (case_hearing_id);

CREATE SEQUENCE public.case_hearing_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.case_hearing_id_seq OWNED BY public.case_hearing_request.case_hearing_id;


CREATE TABLE public.non_standard_durations (
                        case_hearing_id bigint not null,
                        non_standard_hearing_duration_reason_type varchar(70)
);

ALTER TABLE ONLY public.non_standard_durations
    ADD CONSTRAINT fk_non_standard_durations_case_hearing_request FOREIGN KEY (case_hearing_id) REFERENCES public.case_hearing_request(case_hearing_id);

CREATE TABLE public.required_facilities (
                      case_hearing_id bigint not null,
                      facility_type varchar(70)
);

ALTER TABLE ONLY public.required_facilities
    ADD CONSTRAINT fk_required_facilities_case_hearing_request FOREIGN KEY (case_hearing_id) REFERENCES public.case_hearing_request(case_hearing_id);

CREATE TABLE public.hearing (
                      hearing_id bigint not null,
                      status varchar(20) not null
);

ALTER TABLE ONLY public.hearing
    ADD CONSTRAINT hearing_pkey PRIMARY KEY (hearing_id);

CREATE SEQUENCE public.hearing_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.hearing_id_seq OWNED BY public.hearing.hearing_id;

CREATE TYPE public.locationid AS ENUM (
    'COURT',
    'CLUSTER',
    'REGION'
);

CREATE TYPE public.requirementtype AS ENUM (
    'MUSTINC',
    'OPTINC',
    'EXCLUDE'
);

CREATE TYPE public.casecategorytype AS ENUM (
    'CASETYPE',
    'CASESUBTYPE'
);

CREATE TYPE public.partytype AS ENUM (
      'IND',
      'ORG'
);

CREATE TYPE public.dayofweekunavailable AS ENUM (
      'MONDAY',
      'TUESDAY',
      'WEDNESDAY',
      'THURSDAY',
      'FRIDAY',
      'SATURDAY',
      'SUNDAY'
);

CREATE TYPE public.dayofweekunavailabletype AS ENUM (
      'AM',
      'PM',
      'ALL'
);

CREATE TABLE public.required_locations (
                      case_hearing_id bigint not null,
                      location_level_type varchar not null,
                      location_id public.locationid not null
);

ALTER TABLE ONLY public.required_locations
    ADD CONSTRAINT fk_required_locations_case_hearing_request FOREIGN KEY (case_hearing_id) REFERENCES public.case_hearing_request(case_hearing_id);

CREATE TABLE public.panel_requirements (
                      case_hearing_id bigint not null,
                      role_type varchar(70)
);

ALTER TABLE ONLY public.panel_requirements
    ADD CONSTRAINT fk_panel_requirements_case_hearing_request FOREIGN KEY (case_hearing_id) REFERENCES public.case_hearing_request(case_hearing_id);

CREATE TABLE public.panel_authorisation_requirements (
                      case_hearing_id bigint not null,
                      authorisation_type varchar(70),
                      authorisation_subtype varchar(70)
);

ALTER TABLE ONLY public.panel_authorisation_requirements
    ADD CONSTRAINT fk_panel_authorisation_requirements_case_hearing_request FOREIGN KEY (case_hearing_id) REFERENCES public.case_hearing_request(case_hearing_id);

CREATE TABLE public.panel_specialisms (
                      case_hearing_id bigint not null,
                      specialism_type varchar(70)
);

ALTER TABLE ONLY public.panel_specialisms
    ADD CONSTRAINT fk_panel_specialisms_case_hearing_request FOREIGN KEY (case_hearing_id) REFERENCES public.case_hearing_request(case_hearing_id);

CREATE TABLE public.panel_user_requirements (
                      case_hearing_id bigint not null,
                      judicial_user_id varchar(70) not null,
                      user_type varchar(70),
                      requirement_type public.requirementtype not null
);

ALTER TABLE ONLY public.panel_user_requirements
    ADD CONSTRAINT fk_panel_user_requirements_case_hearing_request FOREIGN KEY (case_hearing_id) REFERENCES public.case_hearing_request(case_hearing_id);

CREATE TABLE public.case_categories (
                      case_hearing_id bigint not null,
                      case_category_type public.casecategorytype,
                      case_category_value varchar(70)
);

ALTER TABLE ONLY public.case_categories
    ADD CONSTRAINT fk_case_categories_case_hearing_request FOREIGN KEY (case_hearing_id) REFERENCES public.case_hearing_request(case_hearing_id);


CREATE TABLE public.hearing_party (
                      case_hearing_id bigint not null,
                      tech_party_id bigint not null,
                      party_reference varchar(40) not null,
                      party_type public.partytype not null,
                      party_role_type varchar(6)
);

ALTER TABLE ONLY public.hearing_party
    ADD CONSTRAINT hearing_party_pkey PRIMARY KEY (tech_party_id);

CREATE SEQUENCE public.tech_party_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.tech_party_id_seq OWNED BY public.hearing_party.tech_party_id;

ALTER TABLE ONLY public.hearing_party
    ADD CONSTRAINT fk_hearing_party_case_hearing_request FOREIGN KEY (case_hearing_id) REFERENCES public.case_hearing_request(case_hearing_id);

CREATE TABLE public.individual_detail (
                      tech_party_id bigint not null,
                      related_party_relationship_type varchar(2000) not null,
                      related_party_id varchar(2000) not null,
                      vulnerability_details varchar(256),
                      vulnerable_flag boolean,
                      interpreter_language varchar(10),
                      channel_type varchar(70),
                      last_name varchar(100) not null,
                      first_name varchar(100) not null,
                      title varchar(40) not null
);

ALTER TABLE ONLY public.individual_detail
    ADD CONSTRAINT fk_individual_detail_hearing_party FOREIGN KEY (tech_party_id) REFERENCES public.hearing_party(tech_party_id);

CREATE TABLE public.reasonable_adjustments (
                      tech_party_id bigint not null,
                      reasonable_adjustment_code varchar(10)
);

ALTER TABLE ONLY public.reasonable_adjustments
    ADD CONSTRAINT fk_reasonable_adjustments_hearing_party FOREIGN KEY (tech_party_id) REFERENCES public.hearing_party(tech_party_id);

CREATE TABLE public.organisation_detail (
                      tech_party_id bigint not null,
                      organisation_name varchar(2000) not null,
                      organisation_type_code varchar(60) not null,
                      hmcts_organisation_reference varchar(60) not null
);

ALTER TABLE ONLY public.organisation_detail
    ADD CONSTRAINT fk_organisation_detail_hearing_party FOREIGN KEY (tech_party_id) REFERENCES public.hearing_party(tech_party_id);

CREATE TABLE public.unavailability (
                      tech_party_id bigint not null,
                      day_of_week_unavailable public.dayofweekunavailable not null,
                      day_of_week_unavailable_type public.dayofweekunavailabletype not null,
                      start_date timestamp without time zone not null,
                      end_date timestamp without time zone not null
);

ALTER TABLE ONLY public.unavailability
    ADD CONSTRAINT fk_unavailability_hearing_party FOREIGN KEY (tech_party_id) REFERENCES public.hearing_party(tech_party_id);


CREATE TABLE public.contact_details (
                      tech_party_id bigint not null,
					            contact_type varchar(30),
					            contact_details varchar(120)
);

ALTER TABLE ONLY public.contact_details
    ADD CONSTRAINT fk_contact_details_hearing_party FOREIGN KEY (tech_party_id) REFERENCES public.hearing_party(tech_party_id);
