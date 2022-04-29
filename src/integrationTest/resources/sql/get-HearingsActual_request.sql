insert into hearing ( hearing_id, status) values ('2000000000', 'UPDATE_SUBMITTED');

insert into case_hearing_request (
 case_hearing_id, auto_list_flag, hearing_type, required_duration_in_minutes, hearing_priority_type, number_of_physical_attendees, hearing_in_welsh_flag, private_hearing_required_flag, lead_judge_contract_type, first_date_time_of_hearing_must_be, hmcts_service_code, case_reference, hearing_request_received_date_time, external_case_reference, case_url_context_path, hmcts_internal_case_name, public_case_name, additional_security_required_flag, owning_location_id, case_restricted_flag, case_sla_start_date, hearing_request_version, hearing_id, interpreter_booking_required_flag, listing_comments, requester, hearing_window_start_date_range, hearing_window_end_date_range)
values ( 1, 't'	,'Witness Hearing',	60,	'Priority type1',	4,'f','f','AB123',null,'ABA1'	,9372710950276233,	'2021-08-10 11:20:00','EXT/REF123',	'https://www.google.com',	'Internal case name','Public case name',	't'	,'CMLC123',	't',	'2021-10-10 00:00:00',	1,	2000000000	,'t'	, 'Some listing comments1',	'Some judge1',	'2021-11-01 00:00:00',	'2021-11-12 00:00:00');

insert into case_categories(case_hearing_id, case_category_type, case_category_value)
values (1, 'CASETYPE', 'CaseType1');
insert into case_categories(case_hearing_id, case_category_type, case_category_value)
values (1, 'CASETYPE', 'CaseType2');

insert into hearing_response(hearing_response_id, hearing_id, received_date_time, listing_status, listing_case_status, response_version, request_version)
values (1, '2000000000', '2020-08-10 11:20:00', 'listingStatus1-1', 'caselistingStatus1-1', '2', '1');

insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (1, 1, '2021-01-10 11:20:00', '2021-08-10 11:20:00','venue1-1', 'room1-1');
insert into hearing_day_details(hearing_day_id, hearing_response_id, start_date_time, end_date_time, venue_id, room_id)
values (2, 1, '2020-01-10 11:20:00', '2020-08-10 11:20:00', 'venue1-2', 'room1-2');

insert into hearing_attendee_details(id, hearing_day_id, party_id, party_sub_channel_type)
values (1, 1, 'party1-1','subChannel1-1');
insert into hearing_attendee_details(id, hearing_day_id, party_id, party_sub_channel_type)
values (2, 1, 'party1-2','subChannel1-2');

insert into hearing_party(tech_party_id, case_hearing_id, party_reference, party_type, party_role_type)
values (1, 1, 1,'IND', 'ROLE1');

insert into individual_detail(tech_party_id, title, first_name, last_name)
values (1, 'Mr', 'TestFirstName','testLastName');

insert into actual_hearing(hearing_response_id, actual_hearing_id, actual_hearing_type, actual_hearing_is_final_flag,hearing_result_type, hearing_result_date)
values (1, 1, 'witness hearing', 'true', 'COMPLETED', '2022-02-15');

insert into actual_hearing_day(actual_hearing_id, hearing_date, start_date_time,end_date_time, actual_hearing_day_id)
values (1, '2022-02-05', '2022-02-05T10:00:00', '2022-02-05T15:00:00', 1);
insert into actual_hearing_day(actual_hearing_id, hearing_date, start_date_time,end_date_time, actual_hearing_day_id)
values (1, '2022-02-06', '2022-02-06T10:00:00', '2022-02-06T16:00:00', 2);

insert into actual_hearing_day_pauses(actual_hearing_day_id, pauses_id, pause_date_time, resume_date_time)
values (1, 1, '2022-02-05T12:00:00','2022-02-05T12:30:00');
insert into actual_hearing_day_pauses(actual_hearing_day_id, pauses_id, pause_date_time, resume_date_time)
values (1, 2, '2022-02-06T12:00:00','2022-02-06T12:30:00');

insert into actual_hearing_party(actual_hearing_day_id, actual_party_id, party_id, actual_party_role_type, did_not_attend_flag)
values (1, 1, 1, 'RoleType1','false');
insert into actual_hearing_party(actual_hearing_day_id, actual_party_id, party_id, actual_party_role_type, did_not_attend_flag)
values (2, 2, 1, 'RoleType1','true');
insert into actual_hearing_party(actual_hearing_day_id, actual_party_id, party_id, actual_party_role_type, did_not_attend_flag)
values (2, 3, 2, 'RoleType2','false');

insert into actual_attendee_individual_detail(actual_attendee_individual_detail_id, actual_party_id, first_name, last_name, party_organisation_name, party_actual_sub_channel_type)
values (1, 1, 'testFirstName', 'testLastName', null, 'subType1');
insert into actual_attendee_individual_detail(actual_attendee_individual_detail_id, actual_party_id, first_name, last_name, party_organisation_name, party_actual_sub_channel_type)
values (2, 2, 'testFirstName', 'testLastName', null, 'subType2');
insert into actual_attendee_individual_detail(actual_attendee_individual_detail_id, actual_party_id, first_name, last_name, party_organisation_name, party_actual_sub_channel_type)
values (3, 3, 'testRepFirstName', 'testRepLastName', null, 'subType2');

insert into actual_party_relationship_detail(actual_party_relationship_id, source_actual_party_id, target_actual_party_id)
values (1, 2, 3);
