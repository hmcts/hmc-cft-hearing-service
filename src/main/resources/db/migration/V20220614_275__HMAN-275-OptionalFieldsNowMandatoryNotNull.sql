ALTER TABLE ONLY public.actual_hearing ALTER COLUMN actual_hearing_type SET NOT NULL;

ALTER TABLE ONLY public.actual_hearing ALTER COLUMN hearing_final_flag SET NOT NULL;

ALTER TABLE ONLY public.actual_hearing ALTER COLUMN hearing_result SET NOT NULL;

ALTER TABLE ONLY public.actual_hearing ALTER COLUMN hearing_result_reason_type SET NOT NULL;

ALTER TABLE ONLY public.actual_hearing ALTER COLUMN hearing_result_date SET NOT NULL;
