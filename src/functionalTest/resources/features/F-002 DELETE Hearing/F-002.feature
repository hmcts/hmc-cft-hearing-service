@F-002
Feature: Delete hearing request

  Background:
    Given an appropriate test context as detailed in the test data source

    @S-002.1
  Scenario: successfully delete hearing request
    Given a successful call [to create a hearing request] as in [CreateHearingRequest]
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And it is submitted to call the [delete hearing] operation of [HMC CFT Hearing Service]
    Then a positive response is received
    And the response [has the 200 OK code]
    And the response has all other details as expected.
