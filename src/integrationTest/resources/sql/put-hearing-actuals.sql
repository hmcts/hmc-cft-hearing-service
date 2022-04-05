INSERT INTO hearing ( hearing_id, status) VALUES ('2000000000', 'HEARING_REQUESTED');
INSERT INTO hearing ( hearing_id, status) VALUES ('2000000100', 'LISTED');
INSERT INTO hearing ( hearing_id, status) VALUES ('2000000200', 'AWAITING_LISTING');

-------------
INSERT INTO hearing (hearing_id, status) VALUES ('2000000302', 'UPDATE_REQUESTED');

insert into case_hearing_request (auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
VALUES ( 't'	,'Some hearing type',	60,	'Priority type',	4,'f','f','AB123',null,'ABA1'	,1111222233334444,	'2021-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC123',	't',	'2021-10-10 00:00:00',	1,	2000000302	,'t'	,	'Some listing comments',	'Some judge',	'2021-11-01 00:00:00',	'2021-11-12 00:00:00');

INSERT INTO hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, response_version, request_version)
VALUES (1, '2000000302', '2021-08-10 11:20:00', 'listingStatus1-1', 'caselistingStatus1-1', '2', '1');

INSERT INTO hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, list_assist_session_id, venue_id, room_id)
VALUES (1, '1', '2022-01-31 11:20:00', '2022-01-31 14:20:00', 'session1-1', 'venue1-1', 'room1-1');
INSERT INTO hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, list_assist_session_id, venue_id, room_id)
VALUES (2, '1', '2022-02-12 11:21:00', '2022-02-12 14:20:00', 'session1-1', 'venue1-1', 'room1-1');

-------------
INSERT INTO hearing ( hearing_id, status) VALUES ('2000001000', 'LISTED');

insert into case_hearing_request (auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
VALUES ( 't'	,'Some hearing type',	60,	'Priority type',	4,'f','f','AB123',null,'ABA1'	,1111222233334444,	'2021-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC123',	't',	'2021-10-10 00:00:00',	1,	2000001000	,'t'	,	'Some listing comments',	'Some judge',	'2021-11-01 00:00:00',	'2021-11-12 00:00:00');

INSERT INTO hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, response_version, request_version)
VALUES (2, '2000001000', '2021-08-10 11:20:00', 'listingStatus1-1', 'caselistingStatus1-1', '2', '1');

INSERT INTO hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, list_assist_session_id, venue_id, room_id)
VALUES (3, '2', '2022-01-15 11:20:00', '2022-01-15 14:20:00', 'session1-1', 'venue1-1', 'room1-1');

-------------
INSERT INTO hearing ( hearing_id, status) VALUES ('2000001100', 'UPDATE_REQUESTED');

insert into case_hearing_request (auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
VALUES ( 't'	,'Some hearing type',	60,	'Priority type',	4,'f','f','AB123',null,'ABA1'	,1111222233334444,	'2021-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC123',	't',	'2021-10-10 00:00:00',	1,	2000001100	,'t'	,	'Some listing comments',	'Some judge',	'2021-11-01 00:00:00',	'2021-11-12 00:00:00');

INSERT INTO hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, response_version, request_version)
VALUES (3, '2000001100', '2021-08-10 11:20:00', 'listingStatus1-1', 'caselistingStatus1-1', '2', '1');

INSERT INTO hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, list_assist_session_id, venue_id, room_id)
VALUES (4, '3', '2022-01-15 11:20:00', '2022-01-15 14:20:00', 'session1-1', 'venue1-1', 'room1-1');

-------------
INSERT INTO hearing ( hearing_id, status) VALUES ('2000001200', 'UPDATE_SUBMITTED');

insert into case_hearing_request (auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
VALUES ( 't'	,'Some hearing type',	60,	'Priority type',	4,'f','f','AB123',null,'ABA1'	,1111222233334444,	'2021-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC123',	't',	'2021-10-10 00:00:00',	1,	2000001200	,'t'	,	'Some listing comments',	'Some judge',	'2021-11-01 00:00:00',	'2021-11-12 00:00:00');

INSERT INTO hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, response_version, request_version)
VALUES (4, '2000001200', '2021-08-10 11:20:00', 'listingStatus1-1', 'caselistingStatus1-1', '2', '1');

INSERT INTO hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, list_assist_session_id, venue_id, room_id)
VALUES (5, '4', '2022-01-15 11:20:00', '2022-01-15 14:20:00', 'session1-1', 'venue1-1', 'room1-1');
