CREATE INDEX IF NOT EXISTS idx_hearing_response_hearing_id_response_id ON public.hearing_response (hearing_id, hearing_response_id) CONCURRENTLY;
