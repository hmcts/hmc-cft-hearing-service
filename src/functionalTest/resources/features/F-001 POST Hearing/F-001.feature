@F-001
Feature: F-001: Create hearing request

  Background:
    Given an appropriate test context as detailed in the test data source
    Given a user with [an active profile in CCD]
    And a case that has just been created as in [CreateCase],

  @S-001.1
  Scenario: successfully create hearing request with all available data fields
    When a request is prepared with appropriate values,
    And it is submitted to call the [create hearing] operation of [HMC CFT Hearing Service],
    Then a positive response is received,
    And the response [has 201 Created code],
    And the response [has a versionNumber of 1],
    And the response [has a status of HEARING_REQUESTED],
    And the response has all other details as expected,
    And a call [to verify versionNumber=1 and status=HEARING_REQUESTED] will get the expected response as in [S-001.1-get-hearing].

  @S-001.2
  Scenario: successfully create hearing request with only mandatory fields
    When a request is prepared with appropriate values,
    And it is submitted to call the [create hearing] operation of [HMC CFT Hearing Service],
    Then a positive response is received,
    And the response [has 201 Created code],
    And the response [has a versionNumber of 1],
    And the response [has a status of HEARING_REQUESTED],
    And the response has all other details as expected,
    And a call [to verify versionNumber=1 and status=HEARING_REQUESTED] will get the expected response as in [S-001.1-get-hearing].

  @S-001.3
  Scenario: successfully create hearing request using organisationDetails instead of individualDetails
    When a request is prepared with appropriate values,
    And it is submitted to call the [create hearing] operation of [HMC CFT Hearing Service],
    Then a positive response is received,
    And the response [has 201 Created code],
    And the response [has a versionNumber of 1],
    And the response [has a status of HEARING_REQUESTED],
    And the response has all other details as expected,
    And a call [to verify versionNumber=1 and status=HEARING_REQUESTED] will get the expected response as in [S-001.1-get-hearing].

