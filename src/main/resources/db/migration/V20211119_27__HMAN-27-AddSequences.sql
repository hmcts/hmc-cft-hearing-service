ALTER SEQUENCE IF EXISTS hearing_id_seq RESTART WITH 2000000000;

ALTER TABLE ONLY public.hearing ALTER COLUMN hearing_id SET DEFAULT nextval('public.hearing_id_seq'::regclass);

ALTER TABLE ONLY public.case_hearing_request ALTER COLUMN case_hearing_id SET DEFAULT nextval('public.case_hearing_id_seq'::regclass);

ALTER TABLE ONLY public.hearing_party ALTER COLUMN tech_party_id SET DEFAULT nextval('public.tech_party_id_seq'::regclass);

CREATE SEQUENCE public.non_standard_durations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.non_standard_durations_id_seq OWNED BY public.non_standard_durations.id;

ALTER TABLE ONLY public.non_standard_durations ALTER COLUMN id SET DEFAULT nextval('public.non_standard_durations_id_seq'::regclass);

CREATE SEQUENCE public.required_locations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.required_locations_id_seq OWNED BY public.required_locations.id;

ALTER TABLE ONLY public.required_locations ALTER COLUMN id SET DEFAULT nextval('public.required_locations_id_seq'::regclass);


CREATE SEQUENCE public.required_facilities_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.required_facilities_id_seq OWNED BY public.required_facilities.id;

ALTER TABLE ONLY public.required_facilities ALTER COLUMN id SET DEFAULT nextval('public.required_facilities_id_seq'::regclass);

CREATE SEQUENCE public.case_categories_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.case_categories_id_seq OWNED BY public.case_categories.id;

ALTER TABLE ONLY public.case_categories ALTER COLUMN id SET DEFAULT nextval('public.case_categories_id_seq'::regclass);

ALTER TABLE ONLY public.hearing_party ALTER COLUMN tech_party_id SET DEFAULT nextval('public.tech_party_id_seq'::regclass);

CREATE SEQUENCE public.individual_detail_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.individual_detail_id_seq OWNED BY public.individual_detail.id;

ALTER TABLE ONLY public.individual_detail ALTER COLUMN id SET DEFAULT nextval('public.individual_detail_id_seq'::regclass);

CREATE SEQUENCE public.organisation_detail_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.organisation_detail_id_seq OWNED BY public.organisation_detail.id;

ALTER TABLE ONLY public.organisation_detail ALTER COLUMN id SET DEFAULT nextval('public.organisation_detail_id_seq'::regclass);

CREATE SEQUENCE public.unavailability_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.unavailability_id_seq OWNED BY public.unavailability.id;

ALTER TABLE ONLY public.unavailability ALTER COLUMN id SET DEFAULT nextval('public.unavailability_id_seq'::regclass);

CREATE SEQUENCE public.contact_details_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.contact_details_id_seq OWNED BY public.contact_details.id;

ALTER TABLE ONLY public.contact_details ALTER COLUMN id SET DEFAULT nextval('public.contact_details_id_seq'::regclass);

CREATE SEQUENCE public.reasonable_adjustments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.reasonable_adjustments_id_seq OWNED BY public.reasonable_adjustments.id;

ALTER TABLE ONLY public.reasonable_adjustments ALTER COLUMN id SET DEFAULT nextval('public.individual_detail_id_seq'::regclass);

CREATE SEQUENCE public.panel_requirements_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.panel_requirements_id_seq OWNED BY public.panel_requirements.id;

ALTER TABLE ONLY public.panel_requirements ALTER COLUMN id SET DEFAULT nextval('public.panel_requirements_id_seq'::regclass);

CREATE SEQUENCE public.panel_authorisation_requirements_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.panel_authorisation_requirements_id_seq OWNED BY public.panel_authorisation_requirements.id;

ALTER TABLE ONLY public.panel_authorisation_requirements ALTER COLUMN id SET DEFAULT nextval('public.panel_authorisation_requirements_id_seq'::regclass);

CREATE SEQUENCE public.panel_specialisms_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.panel_specialisms_id_seq OWNED BY public.panel_specialisms.id;

ALTER TABLE ONLY public.panel_specialisms ALTER COLUMN id SET DEFAULT nextval('public.panel_specialisms_id_seq'::regclass);

CREATE SEQUENCE public.panel_user_requirements_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.panel_user_requirements_id_seq OWNED BY public.panel_user_requirements.id;

ALTER TABLE ONLY public.panel_user_requirements ALTER COLUMN id SET DEFAULT nextval('public.panel_user_requirements_id_seq'::regclass);
