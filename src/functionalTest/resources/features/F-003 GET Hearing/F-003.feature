@F-003
Feature: Get hearing request

  Background:
    Given an appropriate test context as detailed in the test data source
    Given a user with [an active profile in CCD]
    And a case that has just been created as in [CreateCase],

    @S-003.1
#      TODO CHECK HOW DIFFERENCE RESPONSE SHOULD BE TO INPUT DATA FROM CREATE HEARING
  Scenario: successfully get hearing request
    Given a successful call [to create a hearing request] as in [CreateHearingRequest]
    When a request is prepared with appropriate values
    And it is submitted to call the [delete hearing] operation of [HMC CFT Hearing Service]
    Then a positive response is received
    And the response [has the 200 OK code]
    And the response has all other details as expected.

#  Scenario: retrieving a non extant hearing returns empty list
#  Scenario: getting a hearing with the ?isValid param returns a 204 with no payload
