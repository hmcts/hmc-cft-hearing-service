ALTER TABLE ONLY public.hearing_response
    ADD COLUMN IF NOT EXISTS cancellation_reason_type varchar(70);

ALTER TABLE ONLY public.hearing_response
    ADD COLUMN IF NOT EXISTS translator_required boolean;

ALTER TABLE ONLY public.hearing_response
    ADD COLUMN IF NOT EXISTS listing_transaction_id varchar(60);
