@F-005
Feature: F-005: Search for hearings

  Background:
    Given an appropriate test context as detailed in the test data source
    Given a user with [an active profile in CCD]

  @S-005.1
  Scenario: Successfully search for hearings of a case reference using the status LISTED parameter
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    And a wait time of [15] seconds [to wait for status to come back from hmi]
    And a successful call [to list the hearing] as in [ListHearingRequest],
    And a wait time of [15] seconds [to wait for status to come back from hmi]
    When a request is prepared with appropriate values,
    And it is submitted to call the [Search for hearings] operation of [HMC CFT Hearing Service],
    Then a positive response is received,
    And the response [has 200 status code],
    And the response [contains the hearing with LISTED status],
    And the response has all other details as expected.

  @S-005.2
  Scenario: Successfully search for a hearing using the HEARING_REQUESTED parameter, hearing with CANCELLATION_REQUESTED status is excluded
     Given a successful call [to create a hearing request] as in [CreateHearingRequest],
     And another successful call [to create a second hearing request] as in [CreateSecondHearingRequest],
     And a successful call [to delete a hearing request] as in [deleteHearingRequest],

     When a request is prepared with appropriate values,
     And it is submitted to call the [Search for hearings] operation of [HMC CFT Hearing Service],
     Then a positive response is received,
     And the response [has 200 status code],
     And the response [contains only the hearing with HEARING_REQUESTED status],
     And the response has all other details as expected.

  @S-005.3
  Scenario: Successfully search for LISTED hearings of a case reference
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    And a wait time of [15] seconds [to wait for status to come back from hmi]
    And a successful call [to list the hearing] as in [ListHearingRequest],
    And a wait time of [15] seconds [to wait for status to come back from hmi]
    When a request is prepared with appropriate values,
    And it is submitted to call the [Search for hearings] operation of [HMC CFT Hearing Service],
    Then a positive response is received,
    And the response [has 200 status code],
    And the response [contains the hearing with LISTED status],
    And the response has all other details as expected.
