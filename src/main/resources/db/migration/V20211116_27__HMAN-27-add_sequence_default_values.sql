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

ALTER TABLE ONLY public.individual_detail ALTER COLUMN tech_party_id SET DEFAULT nextval('public.individual_detail_id_seq'::regclass);
