ALTER TABLE ONLY public.hearing_response
    ADD CONSTRAINT uc_hearing_id_request_version_received_date_time UNIQUE (hearing_id,request_version,received_date_time);
