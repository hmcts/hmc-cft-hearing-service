@F-001
Feature: Create hearing request

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-001.1
  Scenario: successfully create hearing request
    Given a user with [an active profile in CCD]
    And a case that has just been created as in [CreateCase],
    When a request is prepared with appropriate values
    And it is submitted to call the [create hearing] operation of [HMC CFT Hearing Service]
    Then a positive response is received
    And the response [has the 200 OK code]
    And the response has all other details as expected.
