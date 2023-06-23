@F-008
Feature: F-008: Create linked hearing group

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-008.1
  Scenario: List the expected parties as per the latest version of the hearing request-response together with any actuals currently captured.
    Given a user with [an active profile in CCD]
    And a successful call [to create a hearing request] as in [CreateLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateAnotherLinkedHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Create a group of linked hearing requests] operation of [HMC CFT Hearing Service],
    Then a positive response is received,
    And the response [has 201 status code],
    And the response has all other details as expected.

  @S-008.2
  Scenario: Incorrect schema should return 400 - 002 hearing request isLinked is False
    Given a user with [an active profile in CCD]
    And a successful call [to create a hearing request] as in [CreateHearingRequest],
    And another successful call [to create a hearing request] as in [CreateAnotherLinkedHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Create a group of linked hearing requests] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has 400 status code],
    And the response [shows isLinked value on hearingRequest trying to be linked is FALSE],
    And the response has all other details as expected.

  @S-008.3
  Scenario: Should return 401 unauthorised
    Given a successful call [to create a hearing request] as in [CreateLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateAnotherLinkedHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Create a group of linked hearing requests] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has 401 status code],
    And the response has all other details as expected.

  @S-008.4
  Scenario: 001 insufficient requestIDs
    Given a user with [an active profile in CCD]
    And a successful call [to create a hearing request] as in [CreateLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateAnotherLinkedHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Create a group of linked hearing requests] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has 400 status code],
    And the response [shows there are insufficient requestIDs],
    And the response has all other details as expected.

  @S-008.5
  Scenario: 003 hearing request already in a group
    Given a user with [an active profile in CCD]
    And a successful call [to create a hearing request] as in [CreateLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateAnotherLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateLinkedHearingGroupRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Create a group of linked hearing requests] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has 400 status code],
    And the response [shows hearing request is already in a group],
    And the response has all other details as expected.
