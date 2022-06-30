@F-003
Feature: F-003: Get hearing request

  Background:
    Given an appropriate test context as detailed in the test data source
    Given a user with [an active profile in CCD],
    And a case that has just been created as in [CreateCase],

    @S-003.1
  Scenario: successfully get hearing request
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [get hearing] operation of [HMC CFT Hearing Service],
    Then a positive response is received,
    And the response [has the 200 OK code],
    And the response has all other details as expected.

  @S-003.2
  Scenario: Getting a hearing using the ?isValid=true param returns a 204 with no payload
      Given a successful call [to create a hearing request] as in [CreateHearingRequest],
      When a request is prepared with appropriate values,
      And the request [uses the query param isValid=true],
      And it is submitted to call the [get hearing] operation of [HMC CFT Hearing Service],
      Then a positive response is received,
      And the response [has the 204 OK code],
      And the response [has no payload],
      And the response has all other details as expected.

  @S-003.3
  Scenario: Getting a hearing that doesn't exist returns 404
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    When a request is prepared with appropriate values,
    And the request [tries to get a non extant hearing],
    And it is submitted to call the [get hearing] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has the 404 code],
    And the response has all other details as expected.

