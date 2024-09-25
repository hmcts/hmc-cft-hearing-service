ALTER TABLE public.pending_requests
    ALTER COLUMN hearing_id DROP NOT NULL,
    ALTER COLUMN version_number DROP NOT NULL,
    ALTER COLUMN submitted_date_time DROP NOT NULL,
    ALTER COLUMN retry_count DROP NOT NULL,
    ALTER COLUMN last_tried_date_time DROP NOT NULL,
    ALTER COLUMN status DROP NOT NULL,
    ADD COLUMN IF NOT EXISTS message TEXT;
