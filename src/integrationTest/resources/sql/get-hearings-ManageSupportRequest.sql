insert into hearing ( hearing_id, status, last_good_status) values ('2000000000', 'EXCEPTION', 'CANCELLATION_SUBMITTED');
insert into hearing ( hearing_id, status, last_good_status) values ('2000000001', 'EXCEPTION', 'AWAITING_LISTING');
insert into hearing ( hearing_id, status, last_good_status) values ('2000000002', 'EXCEPTION', 'CANCELLATION_SUBMITTED');

insert into case_hearing_request (
 auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
values ( 't'	,'hearingType1',	60,	'Priority type1',	4,'f','f','AB123',null,'TEST'	,9856815055686759,	'2021-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC123',	't',	'2021-10-10 00:00:00',	1,	2000000000	,'t'	,	'Some listing comments1',	'Some judge1',	'2021-11-01 00:00:00',	'2021-11-12 00:00:00');

insert into case_hearing_request (
 auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
values ( 't'	,'hearingType3',	63,	'Priority type3',	6,'t','t','AB123',null,'TEST'	,9856815055686759,	'2020-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC333',	't',	'2021-10-10 00:00:00',	1,	2000000001	,'f'	,	'Some listing comments3',	'Some judge3',	'2021-11-01 00:00:00',	'2021-11-12 00:00:00');

insert into case_hearing_request (
  auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
values ( 't'	,'hearingType3',	63,	'Priority type3',	6,'t','t','AB123',null,'TEST'	,9372710950276233,	'2020-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC333',	't',	'2021-10-10 00:00:00',	1,	2000000002	,'f'	,	'Some listing comments3',	'Some judge3',	'2021-11-01 00:00:00',	'2021-11-12 00:00:00');

insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version)
values (1, '2000000000', '2020-08-10 11:20:00', 'listingStatus1-1', 'caselistingStatus1-1', '1');

insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version)
values (2, '2000000001', '2020-08-10 11:20:00', 'listingStatus1-1', 'caselistingStatus1-1', '1');

insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, request_version)
values (3, '2000000002', '2020-08-10 11:20:00', 'listingStatus1-1', 'caselistingStatus1-1', '1');

insert into actual_hearing(hearing_response_id, actual_hearing_id, actual_hearing_type, actual_hearing_is_final_flag,hearing_result_type, hearing_result_date)
values (2, 1, 'witness hearing', 'true', 'COMPLETED', '2022-02-15');

insert into actual_hearing(hearing_response_id, actual_hearing_id, actual_hearing_type, actual_hearing_is_final_flag,hearing_result_type, hearing_result_date)
values (3, 3, 'witness hearing', 'true', 'ADJOURNED', '2022-02-15');
