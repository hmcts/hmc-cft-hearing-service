@F-011
Feature: F-011: Search for linked hearing group

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-011.1
  Scenario: Successfully search for linked hearing group
    Given a user with [an active profile in CCD]
    And a successful call [to create a hearing request] as in [CreateLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateAnotherLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateLinkedHearingGroupRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Search linked hearing groups] operation of [HMC CFT Hearing Service],
    Then a positive response is received

  @S-011.2
  Scenario: Should return 401 unauthorised
    Given a successful call [to create a hearing request] as in [CreateLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateAnotherLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateLinkedHearingGroupRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Search linked hearing groups] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has 401 status code],
    And the response has all other details as expected.

  @S-011.3
  Scenario: Incorrect schema should return 404
    Given a user with [an active profile in CCD]
    And a successful call [to create a hearing request] as in [CreateLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateAnotherLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateLinkedHearingGroupRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Search linked hearing groups] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has 404 status code],
    And the response has all other details as expected.
