ALTER TABLE ONLY public.case_hearing_request
    ADD CONSTRAINT uc_hearing_id_hearing_request_version UNIQUE (hearing_id,hearing_request_version);