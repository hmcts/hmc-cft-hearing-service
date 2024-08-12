ALTER TABLE
    public.pending_requests
ADD
    COLUMN IF NOT EXISTS message text;