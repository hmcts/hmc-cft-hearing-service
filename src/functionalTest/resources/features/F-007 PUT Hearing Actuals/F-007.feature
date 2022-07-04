@F-007
Feature: F-007: PUT hearing actuals

  Background:
    Given an appropriate test context as detailed in the test data source
    Given a user with [an active profile in CCD]
    And a case that has just been created as in [CreateCase],

  @S-007.1
  #    todo cant get case into UPDATE_REQUESTED state
  Scenario: successfully amend hearing request
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    And a successful call [to amend a hearing request] as in [AmendHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [amend hearing actuals] operation of [HMC CFT Hearing Service],
    Then a positive response is received,
    And the response [has the 201 code],
    And the response [has a status of UPDATE_REQUESTED],
    And the response has all other details as expected,
    And a call [to verify the party name has been updated] will get the expected response as in [S-004.1-get-hearing].

  @S-007.2
  Scenario: Should return 400 no such ID
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [amend hearing actuals] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has 404 status code],
    And the response has all other details as expected.

  @S-007.3
  Scenario: Should return 403 unauthorised
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [amend hearing actuals] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has 403 status code],
    And the response has all other details as expected.

  @S-007.4
  #    todo cant get case into UPDATE_REQUESTED state
  Scenario: successfully amend hearing request
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    And a successful call [to amend a hearing request] as in [AmendHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [amend hearing actuals] operation of [HMC CFT Hearing Service],
    Then a positive response is received,
    And the response [has the 400 code],
    And the response [has a status of UPDATE_REQUESTED],
    And the response has all other details as expected.

  @S-007.5
  #    todo cant get case into UPDATE_REQUESTED state
  Scenario: successfully amend hearing request
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    And a successful call [to amend a hearing request] as in [AmendHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [amend hearing actuals] operation of [HMC CFT Hearing Service],
    Then a positive response is received,
    And the response [has the 400 code],
    And the response [has a status of UPDATE_REQUESTED],
    And the response has all other details as expected.
