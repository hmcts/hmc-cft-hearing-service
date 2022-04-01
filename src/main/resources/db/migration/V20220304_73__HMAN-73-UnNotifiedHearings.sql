ALTER TABLE ONLY public.hearing_response ALTER COLUMN request_version TYPE integer USING (request_version::integer);

ALTER TABLE ONLY public.hearing_response ALTER COLUMN response_version TYPE integer USING (response_version::integer);
