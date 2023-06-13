@F-006
Feature: F-006: Search for hearing actuals

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-006.1
  Scenario: List the expected parties as per the latest version of the hearing request-response together with any actuals currently captured.
    Given a user with [an active profile in CCD]
    And a successful call [to create a hearing request] as in [CreateHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Search for hearing actuals] operation of [HMC CFT Hearing Service],
    Then a positive response is received,
    And the response [has 200 status code],
    And the response [contains both hearings],
    And the response has all other details as expected.

  @S-006.2
  Scenario: Should return 400 no such ID
    Given a user with [an active profile in CCD]
    And a successful call [to create a hearing request] as in [CreateHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Search for hearing actuals] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has 404 status code],
    And the response has all other details as expected.

  @S-006.3
  Scenario: Should return 401 unauthorised
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Search for hearing actuals] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has 403 status code],
    And the response has all other details as expected.
