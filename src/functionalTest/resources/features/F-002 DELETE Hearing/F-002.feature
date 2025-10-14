@F-002
Feature: F-002: Delete hearing request

  Background:
    Given an appropriate test context as detailed in the test data source
    Given a user with [an active profile in CCD],
    And a case that has just been created as in [CreateCase],

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
    And a call [to verify versionNumber=2 and status=CANCELLATION_REQUESTED] will get the expected response as in [S-002.1-get-hearing].

  @S-002.2
  Scenario: successfully delete hearing request in UPDATE_REQUESTED state
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    And a successful call [to amend a hearing request] as in [AmendHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [delete hearing] operation of [HMC CFT Hearing Service],
    Then a positive response is received,
    And the response [has the 200 OK code],
    And the response [has a versionNumber of 3],
    And the response [has a status of CANCELLATION_REQUESTED],
    And the response has all other details as expected,
    And a call [to verify versionNumber=3 and status=CANCELLATION_REQUESTED] will get the expected response as in [S-002.2-get-hearing].

  @S-002.3
  Scenario: cannot delete hearing that is in CANCELLATION_REQUESTED state
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    And a successful call [to delete a hearing request] as in [deleteHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [delete hearing] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has a status of CANCELLATION_REQUESTED],
    And the response has all other details as expected,
    And a call [to verify versionNumber=2 and status=CANCELLATION_REQUESTED] will get the expected response as in [S-002.1-get-hearing]
