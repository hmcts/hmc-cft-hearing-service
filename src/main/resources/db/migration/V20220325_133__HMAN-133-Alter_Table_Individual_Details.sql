ALTER TABLE public.individual_detail ADD COLUMN IF NOT EXISTS other_reasonable_adjustment_details varchar(200);

ALTER TABLE public.individual_detail ADD COLUMN IF NOT EXISTS custody_status varchar(80);

ALTER TABLE public.case_hearing_request ADD COLUMN IF NOT EXISTS amend_reason_code varchar(70);
