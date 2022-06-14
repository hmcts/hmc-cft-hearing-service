ALTER TABLE ONLY public.actual_hearing ALTER COLUMN actual_hearing_type DROP NOT NULL;

ALTER TABLE ONLY public.actual_hearing ALTER COLUMN actual_hearing_is_final_flag DROP NOT NULL;

ALTER TABLE ONLY public.actual_hearing ALTER COLUMN hearing_result_type DROP NOT NULL;

ALTER TABLE ONLY public.actual_hearing ALTER COLUMN hearing_result_reason_type DROP NOT NULL;

ALTER TABLE ONLY public.actual_hearing ALTER COLUMN hearing_result_date DROP NOT NULL;
