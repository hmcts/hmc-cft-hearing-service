CREATE SEQUENCE public.party_relationship_details_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.party_relationship_details (party_relationship_details_id bigint not null default nextval('public.party_relationship_details_id_seq'::regclass),
                                             source_tech_party_id bigint not null,
                                             target_tech_party_id bigint not null,
                                             relationship_type varchar(2000)
);

ALTER SEQUENCE public.party_relationship_details_id_seq OWNED BY public.party_relationship_details.party_relationship_details_id;

ALTER TABLE ONLY public.party_relationship_details
    ADD CONSTRAINT party_relationship_details_pkey PRIMARY KEY (party_relationship_details_id);

ALTER TABLE ONLY public.party_relationship_details
    ADD CONSTRAINT fk_party_relationship_details_source_hearing_party FOREIGN KEY (source_tech_party_id)
        REFERENCES public.hearing_party(tech_party_id);

ALTER TABLE ONLY public.party_relationship_details
    ADD CONSTRAINT fk_party_relationship_details_dest_hearing_party FOREIGN KEY (target_tech_party_id)
        REFERENCES public.hearing_party(tech_party_id);
