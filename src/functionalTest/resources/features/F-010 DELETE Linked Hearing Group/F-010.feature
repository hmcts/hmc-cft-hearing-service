@F-010
Feature: F-010: Delete linked hearing group

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-010.1
  Scenario: List the expected parties as per the latest version of the hearing request-response together with any actuals currently captured.
    Given a user with [an active profile in CCD]
    And a successful call [to create a hearing request] as in [CreateLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateAnotherLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateLinkedHearingGroupRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Delete linked hearing groups] operation of [HMC CFT Hearing Service],
    Then a positive response is received

  @S-010.2
  Scenario: Should return 403 unauthorised
    Given a successful call [to create a hearing request] as in [CreateLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateAnotherLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateLinkedHearingGroupRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Delete linked hearing groups] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has 403 status code],
    And the response has all other details as expected.

  @S-010.3
  Scenario: Incorrect schema should return 400
    Given a user with [an active profile in CCD]
    And a successful call [to create a hearing request] as in [CreateLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateAnotherLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateLinkedHearingGroupRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Delete linked hearing groups] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has 404 status code],
    And the response has all other details as expected.
