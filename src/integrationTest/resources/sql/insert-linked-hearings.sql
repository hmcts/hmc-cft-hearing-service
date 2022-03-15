INSERT INTO linked_group_details (linked_group_id, status, link_type, request_id, request_date_time, reason_for_link, linked_group_latest_version) VALUES (7600000000, 'AWAITING_LISTING', 'Ordered', '44444', '2021-08-10 11:20:00', 'good reason', 1);

INSERT INTO hearing (hearing_id, is_linked_flag, linked_group_id, linked_order, status) VALUES (2000000005, 't', 7600000000, 1, 'HEARING_REQUESTED');
INSERT INTO hearing (hearing_id, is_linked_flag, linked_group_id, linked_order, status) VALUES (2000000006, 't', 7600000000, 2, 'HEARING_REQUESTED');

