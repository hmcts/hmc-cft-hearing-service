CREATE TABLE public.actual_hearing_audit (
                                           id bigint not null,
                                           hearing_response_id bigint not null,
                                           hearing_id bigint not null,
                                           audit_create_date_time timestamp without time zone,
                                           actual_hearing_audit_record jsonb
);

ALTER TABLE ONLY public.actual_hearing_audit
  ADD CONSTRAINT actual_hearing_audit_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.actual_hearing_audit
  ADD CONSTRAINT fk_actual_hearing_audit_hearing FOREIGN KEY (hearing_id) REFERENCES public.hearing(hearing_id);

ALTER TABLE ONLY public.actual_hearing_audit
  ADD CONSTRAINT fk_actual_hearing_audit_hearing_response_id FOREIGN KEY (hearing_response_id) REFERENCES public.hearing_response(hearing_response_id);

CREATE SEQUENCE public.actual_hearing_audit_id_seq
  start with 1
  increment by 1
  NO MINVALUE
  NO MAXVALUE
  CACHE 1;

ALTER SEQUENCE public.actual_hearing_audit_id_seq OWNED BY public.actual_hearing_audit.id;

ALTER TABLE ONLY public.actual_hearing_audit ALTER COLUMN id SET DEFAULT nextval('public.actual_hearing_audit_id_seq'::regclass);
