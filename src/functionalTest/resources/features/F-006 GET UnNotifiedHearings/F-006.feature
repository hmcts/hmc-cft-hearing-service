@F-006
Feature: F-006: Search unnotified Hearings

  Background:
    Given an appropriate test context as detailed in the test data source
    Given a user with [an active profile in CCD]
    And a case that has just been created as in [CreateCase],



  @S-006.4
  # AC04: Return empty result set when existing MIN hearingDayDetails.startDateTime is lesser than provided hearingStartDateFrom
  Scenario: Successfully search for hearings of a case reference that has 1 hearing requests
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    And a wait time of [90] seconds [to allow for outbound service to process all messages]
    And another successful call [listing a hearing] as in [ListingHearing],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Search unnotified Hearings] operation of [HMC CFT Hearing Service],
    Then a positive response is received,
    And the response [has 200 status code],
