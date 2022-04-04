ALTER TABLE ONLY public.hearing_response ADD COLUMN IF NOT EXISTS cancellation_reason_type varchar(70);
