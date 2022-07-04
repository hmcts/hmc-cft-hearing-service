@F-007
Feature: F-007: PUT hearing actuals

  Background:
    Given an appropriate test context as detailed in the test data source
    Given a user with [an active profile in CCD]
    And a case that has just been created as in [CreateCase],

  @S-007.1
  Scenario: successfully amend hearing request
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [amend hearing actuals] operation of [HMC CFT Hearing Service],
    Then a positive response is received,
    And the response [has the 201 code],
    And the response [has a versionNumber of 2],
    And the response [has a status of HEARING_REQUESTED],
    And the response has all other details as expected,
    And a call [to verify the party name has been updated] will get the expected response as in [S-004.1-get-hearing].

  @S-007.2
  Scenario: Should return 400 no such ID
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Search for hearing actuals] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has 404 status code],
    And the response has all other details as expected.

  @S-007.3
  Scenario: Should return 401 unauthorised
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Search for hearing actuals] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has 401 status code],
    And the response has all other details as expected.

