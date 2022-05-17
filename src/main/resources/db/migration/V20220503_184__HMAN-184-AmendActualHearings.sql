ALTER TABLE ONLY public.actual_party_relationship_detail
    ADD CONSTRAINT fk_actual_party_relationship_detail_target_actual_party FOREIGN KEY (target_actual_party_id)
        REFERENCES public.actual_hearing_party(actual_party_id);
