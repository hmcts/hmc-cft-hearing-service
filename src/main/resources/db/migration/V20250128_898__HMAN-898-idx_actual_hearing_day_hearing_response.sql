ALTER TABLE ONLY public.actual_hearing
    ADD CONSTRAINT uc_actual_hearing_id_hearing_response_id UNIQUE (hearing_response_id);

