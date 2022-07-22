@F-009
Feature: F-009: Amend linked hearing group

  Background:
    Given an appropriate test context as detailed in the test data source
    Given a user with [an active profile in CCD]

  @S-009.1
  Scenario: List the expected parties as per the latest version of the hearing request-response together with any actuals currently captured.
    Given a successful call [to create a hearing request] as in [CreateLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateAnotherLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateThirdLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateLinkedHearingGroupRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Put linked hearing groups] operation of [HMC CFT Hearing Service],
    Then a positive response is received,
    And a call [to verify versionNumber=1 and status=HEARING_REQUESTED] will get the expected response as in [S-009.1-get-linked-hearing].

  @S-009.2
  Scenario: Should return 403 unauthorised
    Given a successful call [to create a hearing request] as in [CreateLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateAnotherLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateThirdLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateLinkedHearingGroupRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Put linked hearing groups] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has 403 status code],
    And the response has all other details as expected.

  @S-009.3
  Scenario: 001 insufficient requestIDs
    Given a successful call [to create a hearing request] as in [CreateLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateAnotherLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateLinkedHearingGroupRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Put linked hearing groups] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has 400 status code],
    And the response [shows there are insufficient requestIDs],
    And the response has all other details as expected.

  @S-009.4
  Scenario: 002 hearing request isLinked is FALSE
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    And another successful call [to create a hearing request] as in [CreateLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateAnotherLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateLinkedHearingGroupRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Put linked hearing groups] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has 400 status code],
    And the response [shows isLinked value on hearingRequest trying to be linked is FALSE],
    And the response has all other details as expected.

  @S-009.5
  Scenario: 003 hearing request already in a group
    Given a successful call [to create a hearing request] as in [CreateLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateAnotherLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateThirdLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateFourthLinkedHearingRequest],
    And another successful call [to create a hearing request] as in [CreateLinkedHearingGroupRequest],
    And another successful call [to create a hearing request] as in [CreateAnotherLinkedHearingGroupRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Put linked hearing groups] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has 400 status code],
    And the response [shows hearing request is already in a group],
    And the response has all other details as expected.
