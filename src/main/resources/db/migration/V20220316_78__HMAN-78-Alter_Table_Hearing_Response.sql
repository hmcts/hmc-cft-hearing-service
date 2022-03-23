ALTER TABLE ONLY public.hearing_response ADD COLUMN IF NOT EXISTS cancellation_reason_type varchar(70);

ALTER TABLE ONLY public.hearing_response ADD COLUMN IF NOT EXISTS translator_required boolean;

ALTER TABLE ONLY public.hearing_response ADD COLUMN IF NOT EXISTS listing_transaction_id varchar(60);

ALTER TABLE ONLY public.hearing ADD COLUMN IF NOT EXISTS error_code int;

ALTER TABLE ONLY public.hearing ADD COLUMN IF NOT EXISTS error_description varchar(100);

ALTER TABLE ONLY public.hearing_response ALTER COLUMN response_version DROP NOT null;

ALTER TABLE ONLY public.hearing_day_details ALTER COLUMN list_assist_session_id DROP NOT null;

