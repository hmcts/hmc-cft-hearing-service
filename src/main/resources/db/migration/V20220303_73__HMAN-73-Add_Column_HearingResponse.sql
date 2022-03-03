ALTER TABLE ONLY public.hearing_response
    ADD COLUMN IF NOT EXISTS  hearing_request_version integer NOT NULL;
