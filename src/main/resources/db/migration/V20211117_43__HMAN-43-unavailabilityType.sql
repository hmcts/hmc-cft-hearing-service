
ALTER TABLE public.unavailability ADD unavailability_type varchar(10);

ALTER TABLE public.unavailability ALTER COLUMN day_of_week_unavailable DROP NOT NULL;
ALTER TABLE public.unavailability ALTER COLUMN day_of_week_unavailable_type DROP NOT NULL;
ALTER TABLE public.unavailability ALTER COLUMN start_date DROP NOT NULL;
ALTER TABLE public.unavailability ALTER COLUMN end_date DROP NOT NULL;
