ALTER TABLE public.linked_group_details ALTER COLUMN request_id TYPE bigint using request_id::bigint;

ALTER TABLE ONLY public.linked_group_details
ALTER COLUMN request_id SET NOT NULL;

ALTER TABLE ONLY public.linked_group_details
ADD CONSTRAINT uc_linked_group_details_request_id UNIQUE (request_id);

CREATE SEQUENCE public.linked_group_details_request_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE ONLY public.linked_group_details
ALTER COLUMN request_id SET DEFAULT nextval('public.linked_group_details_request_id_seq');

ALTER SEQUENCE public.linked_group_details_request_id_seq OWNED BY public.linked_group_details.request_id;
