CREATE TABLE public.change_reasons (id bigint not null,
                                    case_hearing_id bigint not null,
                                    change_reason_type varchar(70) not null,
                                    created_date_time timestamp without time zone
);

ALTER TABLE ONLY public.change_reasons
     ADD CONSTRAINT change_reasons_pkey PRIMARY KEY (id);

CREATE SEQUENCE public.change_reasons_id_seq
    start with 1
    increment by 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.change_reasons_id_seq OWNED BY public.change_reasons.id;

ALTER TABLE ONLY public.change_reasons ALTER COLUMN id SET DEFAULT nextval('public.cancellation_reasons_id_seq'::regclass);

ALTER TABLE ONLY public.change_reasons
    ADD CONSTRAINT fk_change_reasons_case_hearing_request FOREIGN KEY (case_hearing_id)
        REFERENCES public.case_hearing_request(case_hearing_id);