CREATE TABLE public.cancellation_reasons (
                         id bigint not null,
                         case_hearing_id bigint not null,
                         cancellation_reason_Type varchar(100) not null
);

ALTER TABLE ONLY public.cancellation_reasons
    ADD CONSTRAINT cancellation_reasons_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.cancellation_reasons
    ADD CONSTRAINT fk_cancellation_reasons_case_hearing_request FOREIGN KEY (case_hearing_id) REFERENCES public.case_hearing_request(case_hearing_id);

CREATE SEQUENCE public.cancellation_reasons_id_seq
    start with 1
    increment by 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.cancellation_reasons_id_seq OWNED BY public.cancellation_reasons.id;

ALTER TABLE ONLY public.cancellation_reasons ALTER COLUMN id SET DEFAULT nextval('public.cancellation_reasons_id_seq'::regclass);
