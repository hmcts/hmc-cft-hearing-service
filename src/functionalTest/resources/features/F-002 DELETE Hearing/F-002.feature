@F-002
Feature: F-002: Delete hearing request

  Background:
    Given an appropriate test context as detailed in the test data source
    Given a user with [an active profile in CCD],

  @S-002.1
  Scenario: successfully delete hearing request
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [delete hearing] operation of [HMC CFT Hearing Service],
    Then a positive response is received,
    And the response [has the 200 OK code],
    And the response [has a versionNumber of 2],
    And the response [has a status of CANCELLATION_REQUESTED],
    And the response has all other details as expected,

    And a wait time of [120] seconds [to wait for status to come back from hmi]
    And a call [to verify versionNumber=2 and status=CANCELLATION_SUBMITTED] will get the expected response as in [S-002.1-get-hearing].


  @S-002.2
  Scenario: successfully delete hearing request in UPDATE_SUBMITTED state
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    And a wait time of [120] seconds [to wait for status to come back from hmi]

    And a successful call [to amend a hearing request] as in [AmendHearingRequest],
    And a wait time of [120] seconds [to wait for status to come back from hmi]
    And a successful call [to check the hearing status is UPDATE_SUBMITTED] as in [GetHearingRequestUpdateSubmittedStatus],

    When a request is prepared with appropriate values,
    And it is submitted to call the [delete hearing] operation of [HMC CFT Hearing Service],
    Then a positive response is received,
    And the response [has the 200 OK code],
    And the response [has a versionNumber of 3],
    And the response [has a status of CANCELLATION_REQUESTED],
    And the response has all other details as expected,

    And a wait time of [120] seconds [to wait for status to come back from hmi]
    And a successful call [to check the hearing status is CANCELLATION_SUBMITTED] as in [GetHearingRequestCancellationSubmittedStatus],

  @S-002.3
  Scenario: cannot delete hearing that is in CANCELLATION_REQUESTED state
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    And a wait time of [120] seconds [to wait for status to come back from hmi]

    And a successful call [to delete a hearing request] as in [deleteHearingRequest],
    And a wait time of [120] seconds [to wait for status to come back from hmi]

    When a request is prepared with appropriate values,
    And it is submitted to call the [delete hearing] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has a status of CANCELLATION_REQUESTED],
    And the response has all other details as expected,

    And a wait time of [120] seconds [to wait for status to come back from hmi]
    And a call [to verify versionNumber=2 and status=CANCELLATION_SUBMITTED] will get the expected response as in [S-002.1-get-hearing]

