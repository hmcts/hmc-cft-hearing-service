insert into hearing ( hearing_id, status) values ('2000000000', 'AWAITING_LISTING');

insert into case_hearing_request (
 auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_id, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range, request_timestamp)
values ( 't'	,'hearingType1',	60,	'Priority type1',	4,'f','f','AB123',null,'ABA1'	,9372710950276233,	'2021-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC123',	't',	'2021-10-10 00:00:00',	1,	2000000000	,'t'	,	'Some listing comments1',	'Some judge1',	'2021-11-01 00:00:00',	'2021-11-12 00:00:00',	'2021-08-10 11:20:00');

insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, response_version, request_version)
values (61, '2000000000', '2020-08-10 11:20:00', 'listingStatus1-1', 'caselistingStatus1-1', '2', '1');

insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, list_assist_session_id, venue_id, room_id)
values (71, 61, '2021-01-10 11:20:00', '2021-08-10 11:20:00', 'session1-1', 'venue1-1', 'room1-1');
insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, list_assist_session_id, venue_id, room_id)
values (72, 61, '2020-01-10 11:20:00', '2020-08-10 11:20:00', 'session1-2', 'venue1-2', 'room1-2');

insert into hearing_day_panel(id, hearing_day_id, panel_user_id,is_presiding) values (81, 71, 'panel1-1','true');
insert into hearing_day_panel(id, hearing_day_id, panel_user_id,is_presiding) values (82, 71, 'panel1-2','false');

insert into hearing_attendee_details(id, hearing_day_id, party_id, party_sub_channel_type) values (91, 71, 'party1-1','subChannel1-1');
insert into hearing_attendee_details(id, hearing_day_id, party_id, party_sub_channel_type) values (92, 71, 'party1-2','subChannel1-2');
