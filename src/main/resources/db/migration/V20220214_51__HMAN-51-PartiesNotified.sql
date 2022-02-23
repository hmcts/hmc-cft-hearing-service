ALTER TABLE ONLY public.hearing_response
    ADD COLUMN IF NOT EXISTS response_version varchar(60) not null;

ALTER TABLE ONLY public.hearing_response
    ADD COLUMN IF NOT EXISTS parties_notified_datetime timestamp without time zone;

ALTER TABLE ONLY public.hearing_response
    ADD COLUMN IF NOT EXISTS service_data jsonb;
