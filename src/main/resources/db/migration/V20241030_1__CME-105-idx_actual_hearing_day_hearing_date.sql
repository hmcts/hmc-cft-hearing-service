CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_hearing_status ON public.hearing (status);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_case_hearing_request_service_code ON public.case_hearing_request (hmcts_service_code);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_hearing_response_hearing_id_response_id ON public.hearing_response (hearing_id, hearing_response_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_linked_group_details_request_id ON public.linked_group_details (request_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_hearing_day_details_startDateTime ON public.hearing_day_details (start_date_time);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_case_hearing_request_case_reference ON public.case_hearing_request (case_reference);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_case_hearing_request_hearing_id ON public.case_hearing_request (hearing_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_case_hearing_request_created_date_time ON public.case_hearing_request (created_date_time);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_actual_hearing_day_hearing_date ON public.actual_hearing_day (actual_hearing_id, hearing_date);