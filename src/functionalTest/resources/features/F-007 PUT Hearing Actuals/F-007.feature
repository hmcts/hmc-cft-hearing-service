@F-007
Feature: F-007: Amend hearing actuals

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-007.1
  Scenario: successfully amend hearing request
    Given a user with [an active profile in CCD]
    And a successful call [to create a hearing request] as in [CreateHearingActualRequest],
    And a wait time of [90] seconds [to wait for status to come back from hmi]
    And a successful call [to list the hearing] as in [ListHearingActualRequest],
    And a wait time of [90] seconds [to wait for status to come back from hmi]
    When a request is prepared with appropriate values,
    And it is submitted to call the [amend hearing actuals] operation of [HMC CFT Hearing Service],
    Then a positive response is received,
    And the response [has the 200 code],
    And the response has all other details as expected,
    And a call [to verify the party name has been updated] will get the expected response as in [S-007.1-get-hearing].

  @S-007.2
  Scenario: Should return 400 no such ID
    Given a user with [an active profile in CCD]
    And a successful call [to create a hearing request] as in [CreateHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [amend hearing actuals] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has 404 status code],
    And the response has all other details as expected.

  @S-007.3
  Scenario: Should return 401 unauthorised
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [amend hearing actuals] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has 401 status code],
    And the response has all other details as expected.

  @S-007.4
  Scenario: Should return 004 non-unique dates
    Given a user with [an active profile in CCD]
    And a successful call [to create a hearing request] as in [CreateHearingActualRequest],
    And a wait time of [90] seconds [to wait for status to come back from hmi]
    And a successful call [to list the hearing] as in [ListHearingActualRequest],
    And a wait time of [90] seconds [to wait for status to come back from hmi]
    When a request is prepared with appropriate values,
    And it is submitted to call the [amend hearing actuals] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has the 400 code],
    And the response has all other details as expected.

  @S-007.5
  Scenario: Should return 003 invalid date
    Given a user with [an active profile in CCD]
    And a successful call [to create a hearing request] as in [CreateHearingActualRequest],
    And a wait time of [90] seconds [to wait for status to come back from hmi]
    And a successful call [to list the hearing] as in [ListHearingActualRequest],
    And a wait time of [90] seconds [to wait for status to come back from hmi]
    When a request is prepared with appropriate values,
    And it is submitted to call the [amend hearing actuals] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has the 400 code],
    And the response has all other details as expected.

  @S-007.6
  Scenario: Should return 002 invalid status
    Given a user with [an active profile in CCD]
    And a successful call [to create a hearing request] as in [CreateHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [amend hearing actuals] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has the 400 code],
    And the response has all other details as expected.
