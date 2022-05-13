ALTER TABLE public.hearing_day_details ALTER COLUMN start_date_time DROP NOT NULL;
ALTER TABLE public.hearing_day_details ALTER COLUMN end_date_time DROP NOT NULL;
ALTER TABLE public.hearing_day_details ALTER COLUMN room_id DROP NOT NULL;
ALTER TABLE public.hearing_day_details ALTER COLUMN venue_id DROP NOT NULL;
ALTER TABLE public.hearing_response ALTER COLUMN listing_status DROP NOT NULL;
ALTER TABLE public.hearing_attendee_details ALTER COLUMN party_id DROP NOT NULL;
