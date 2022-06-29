INSERT INTO hearing (hearing_id, status)
VALUES ('2000000011', 'HEARING_AMENDED'),
       ('2000000012', 'UPDATE_REQUESTED');

INSERT INTO case_hearing_request (auto_list_flag,
                                  hearing_type,
                                  required_duration_in_minutes,
                                  hearing_priority_type,
                                  number_of_physical_attendees,
                                  hearing_in_welsh_flag,
                                  private_hearing_required_flag,
                                  lead_judge_contract_type,
                                  first_date_time_of_hearing_must_be,
                                  hmcts_service_code,
                                  case_reference,
                                  hearing_request_received_date_time,
                                  external_case_reference,
                                  case_url_context_path,
                                  hmcts_internal_case_name,
                                  public_case_name,
                                  additional_security_required_flag,
                                  owning_location_id,
                                  case_restricted_flag,
                                  case_sla_start_date,
                                  hearing_request_version,
                                  hearing_id,
                                  interpreter_booking_required_flag,
                                  listing_comments, requester,
                                  hearing_window_start_date_range,
                                  hearing_window_end_date_range)
VALUES ('t', 'Some hearing type2', 62, 'Priority type2', 5, 't', 'f', 'AB333', null, 'XYZ1', 1111222233335555,
        '2020-08-10 11:20:00', 'EXT/REF783', 'https://www.google.com', 'Internal case name2', 'Public case name2', 'f',
        'CMLC333', 't', '2020-10-10 00:00:00', 1, 2000000011, 't', 'Some listing comments2', 'Some judge2',
        '2021-11-01 00:00:00', '2021-11-12 00:00:00'),

       ('t', 'Some hearing type', 60, 'Priority type', 4, 'f', 'f', 'AB123', null, 'ABA1', 1111222233334455,
        '2021-08-10 11:20:00', 'EXT/REF123', 'https://www.google.com', 'Internal case name', 'Public case name', 't',
        'CMLC123', 't', '2021-10-10 00:00:00', 1, 2000000012, 't', 'Some listing comments', 'Some judge',
        '2021-11-01 00:00:00', '2021-11-12 00:00:00');

INSERT INTO change_reasons (id, change_reason_type, case_hearing_id)
VALUES (1, 'reason 1', 1),
       (2, 'reason 2', 1),
       (3, 'reason 3', 1),
       (4, 'reason A', 2),
       (5, 'reason B', 2)

