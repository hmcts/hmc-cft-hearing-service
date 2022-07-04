@F-006
Feature: F-006: Search unnotified Hearings

  Background:
    Given an appropriate test context as detailed in the test data source
    Given a user with [an active profile in CCD]
    And a case that has just been created as in [CreateCase],


  @S-006.1
  # AC01: Validation error  - Return 400 error message TODO
  Scenario: Successfully search for hearings of a case reference that has 2 hearing requests
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    And another successful call [to create a hearing request] as in [CreateHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Search unnotified Hearings] operation of [HMC CFT Hearing Service],
    Then a positive response is received,
    And the response [has 200 status code],
