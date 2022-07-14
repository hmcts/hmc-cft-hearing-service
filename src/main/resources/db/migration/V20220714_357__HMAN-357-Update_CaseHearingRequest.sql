ALTER TABLE ONLY public.case_hearing_request
  ADD COLUMN IF NOT EXISTS listing_auto_change_reason_code varchar(70);