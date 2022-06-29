ALTER TABLE ONLY public.actual_hearing_day ADD COLUMN not_required boolean;

ALTER TABLE ONLY public.actual_hearing_day ALTER COLUMN start_date_time DROP NOT null;

ALTER TABLE ONLY public.actual_hearing_day ALTER COLUMN end_date_time DROP NOT null;
