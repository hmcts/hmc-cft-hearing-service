@F-001
Feature: Create hearing request

  Background:
    Given an appropriate test context as detailed in the test data source
    Given a user with [an active profile in CCD]
    And a case that has just been created as in [CreateCase],

  @S-001.1
  Scenario: successfully create hearing request with all available data fields
    When a request is prepared with appropriate values
    And it is submitted to call the [create hearing] operation of [HMC CFT Hearing Service]
    Then a positive response is received
    And the response [has 201 Created code]
    And the response [has a versionNumber of 1]
    And the response [has a status of HEARING_REQUESTED]
    And the response has all other details as expected.

#  // hearing window alternative scenario
#  partyDetails.organisationdetails alternative sceanrio

#  Scenario: can successfully create hearing using only mandatory fields
#
#  Scenario: cannot send request with requestDetails missing
#  Scenario: cannot send request with hearingDetails missing
#  Scenario: cannot send request with partyDetails missing
