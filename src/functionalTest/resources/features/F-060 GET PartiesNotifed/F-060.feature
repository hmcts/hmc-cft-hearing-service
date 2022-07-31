@F-060
Feature: F-060: Search for Parties Notified

  Background:
    Given an appropriate test context as detailed in the test data source
    Given a user with [an active profile in CCD]

  @S-060.1
  Scenario: Should return 400 validation error
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Search for parties notified] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has the 400 status code].

  @S-060.2
  Scenario: Should return 403 invalid role
    Given a successful call [to create a hearing request] as in [CreateHearingRequest]
    When a request is prepared with appropriate values,
    And it is submitted to call the [Search for parties notified] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has the 403 status code].

  @S-060.3
  Scenario: List the expected parties of the hearing request.
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    And a successful call [to amend a hearing request] as in [AmendHearingRequest],
    And a successful call to [get hearing] as in [GetHearingRequestPositiveGeneric],
#    And  a successful call [to put parties notified responses] as in [CreatePartiesNotifiedRequest]
    When a request is prepared with appropriate values,
    And it is submitted to call the [Search for parties notified] operation of [HMC CFT Hearing Service],
    Then a positive response is received,
    And the response [has the 200 status code],
    And the response has all other details as expected.
