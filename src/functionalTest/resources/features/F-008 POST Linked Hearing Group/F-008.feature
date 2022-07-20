@F-008
Feature: F-008: Search for hearings

  Background:
    Given an appropriate test context as detailed in the test data source
    Given a user with [an active profile in CCD]

  @S-008.1
  Scenario: List the expected parties as per the latest version of the hearing request-response together with any actuals currently captured.
    Given a successful call [to create a hearing request] as in [CreateLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateAnotherLinkedHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Create a group of linked hearing requests] operation of [HMC CFT Hearing Service],
    Then a positive response is received,
    And the response [has 201 status code],
    And the response has all other details as expected.

  @S-008.2
  Scenario: Incorrect schema should return 400
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    And another successful call [to create a hearing request] as in [CreateAnotherLinkedHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Create a group of linked hearing requests] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has 400 status code],
    And the response has all other details as expected.

  @S-008.3
  Scenario: Should return 403 unauthorised
    Given a successful call [to create a hearing request] as in [CreateLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateAnotherLinkedHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Create a group of linked hearing requests] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has 403 status code],
    And the response has all other details as expected.

  @S-008.4
  Scenario: 001 insufficient requestIDs
    Given a successful call [to create a hearing request] as in [CreateLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateAnotherLinkedHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Create a group of linked hearing requests] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has 400 status code],
    And the response has all other details as expected.

  @S-008.5 003 hearing request already in a group
  Scenario: Should return 400 insufficient requestIDs
    Given a successful call [to create a hearing request] as in [CreateLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateAnotherLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateLinkedHearingGroupRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Create a group of linked hearing requests] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has 400 status code],
    And the response has all other details as expected.

