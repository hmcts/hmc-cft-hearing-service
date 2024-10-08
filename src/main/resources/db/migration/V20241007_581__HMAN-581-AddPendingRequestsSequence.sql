CREATE SEQUENCE public.pending_requests_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.pending_requests_id_seq OWNED BY public.pending_requests.id;

ALTER TABLE ONLY public.pending_requests ALTER COLUMN id SET DEFAULT nextval('public.pending_requests_id_seq'::regclass);
