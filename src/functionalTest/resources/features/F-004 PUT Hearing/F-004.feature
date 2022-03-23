@F-004
Feature: Amend hearing request

  Background:
    Given an appropriate test context as detailed in the test data source
    Given a user with [an active profile in CCD]
    And a case that has just been created as in [CreateCase],

    @S-004.1
  Scenario: successfully amend hearing request
    Given a successful call [to create a hearing request] as in [CreateHearingRequest]
    When a request is prepared with appropriate values
    And it is submitted to call the [amend hearing] operation of [HMC CFT Hearing Service]
    Then a positive response is received
    And the response [has the 201 code]
    And the response [has a versionNumber of 2]
    And the response [has a status of UPDATE_REQUESTED]
    And the response has all other details as expected.

#  Scenario: Cannot amend hearing with incorrect version number
#     -version number is not incremented on unsuccessful request
#  Scenario: Cannot amend hearing with incorrect hearing ID
#  Scenario: Can amend hearing in the  UPDATE_REQUESTED state
#  Scenario: Cannot amend hearing in the  CANCELLATION_REQUESTED state
#
#  Scenario: can successfully create hearing using only mandatory fields
#
#  Scenario: cannot send request with requestDetails missing
#  Scenario: cannot send request with hearingDetails missing
#  Scenario: cannot send request with partyDetails missing
