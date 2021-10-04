CREATE TABLE public.case_hearing_request (
                         auto_list_flag boolean not null,
                         hearing_type varchar(40) not null,
                         required_duration_in_minutes integer not null,
                         hearing_priority_type varchar(60) not null,
                         number_of_physical_attendees integer,
                         hearing_in_welsh_flag boolean,
                         private_hearing_required_flag boolean,
                         lead_judge_contract_type varchar(70),
                         first_date_time_of_hearing_must_be timestamp without time zone,
                         hmcts_service_id varchar not null,
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
                         hearing_id integer not null,
                         interpreter_booking_required_flag boolean

);

CREATE TABLE public.non_standard_durations (
                        non_standard_hearing_duration_reason_type varchar(70)
);

CREATE TABLE public.required_facilities (
                      facility_type varchar(70),
                      id uuid not null primary key

);

CREATE TABLE public.hearing_request (
                      listing_comments varchar(5000),
                      requester varchar(60),
                      hearing_window_start_date_range timestamp without time zone,
                      hearing_window_end_date_range timestamp without time zone
);

CREATE TABLE public.hearing (
                      status varchar(20) not null
);

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
                      location_level_type varchar not null,
                      location_id public.locationid NOT NULL
);

CREATE TABLE public.panel_requirements (
                      role_type varchar(70)
);

CREATE TABLE public.panel_authorisation_requirements (
                      authorisation_type varchar(70),
                      authorisation_subtype varchar(70)
);

CREATE TABLE public.panel_specialisms (
                      specialism_type varchar(70)
);

CREATE TABLE public.panel_user_requirements (
                      judicial_user_id varchar(70) not null,
                      user_type varchar(70),
                      requirement_type public.requirementtype not null
);

CREATE TABLE public.case_categories (
                      case_category_type public.casecategorytype,
                      case_category_value varchar(70)
);

CREATE TABLE public.hearing_party (
                      party_reference varchar(40) not null,
                      party_type public.partytype not null,
                      party_role_type varchar(6)
);

CREATE TABLE public.individual_detail (
                      related_party_relationship_type varchar(2000) not null,
                      related_party_id varchar(2000) not null,
                      vulnerability_details varchar(256),
                      vulnerable_flag boolean,
                      interpreter_language varchar(10),
                      channel_type varchar(40),
                      last_name varchar(100) not null,
                      first_name varchar(100) not null,
                      title varchar(40) not null
);

CREATE TABLE public.reasonable_adjustments (
                      reasonable_adjustment_code varchar(10)
);

CREATE TABLE public.organisation_detail (
                      organisation_name varchar(2000) not null,
                      organisation_type_code varchar(60) not null,
                      hmcts_organisation_reference varchar(60) not null
);

CREATE TABLE public.unavailability (
                      day_of_week_unavailable public.dayofweekunavailable not null,
                      day_of_week_unavailable_type public.dayofweekunavailabletype not null,
                      start_date timestamp without time zone not null,
                      end_date timestamp without time zone not null
);

CREATE TABLE public.contact_details (
					            contact_type varchar(20),
					            contact_details varchar(120)
);
