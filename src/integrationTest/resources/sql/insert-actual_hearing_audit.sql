INSERT INTO hearing ( hearing_id, status, created_date_time, updated_date_time) VALUES ('2000000000', 'HEARING_REQUESTED', '2021-08-10 11:20:00','2021-08-12 13:30:00');
INSERT INTO hearing ( hearing_id, status) VALUES ('2000000001', 'LISTED');

insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version)
values (1, '2000000000', '2026-05-10 11:20:00', 'listingStatus1-1', 'caselistingStatus1-1', '1');
insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version)
values (2, '2000000001', '2026-05-10 11:20:00', 'listingStatus1-1', 'caselistingStatus1-1', '1');

insert into actual_hearing_audit (
  id, hearing_id, hearing_response_id, audit_create_date_time, actual_hearing_audit_record)
values (1, 2000000000, 1, '2026-05-10 11:20:00',
        '{"auditDetails": "Some audit details for hearing 2000000000"}');
insert into actual_hearing_audit (
  id, hearing_id, hearing_response_id, audit_create_date_time, actual_hearing_audit_record)
values (2, 2000000001, 2, '2026-05-10 11:20:00',
        '{"auditDetails": "Some audit details for hearing 2000000001"}');
