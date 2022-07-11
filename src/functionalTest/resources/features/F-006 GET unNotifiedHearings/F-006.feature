@F-006
Feature: F-006: Search unnotified Hearings

  Background:
    Given an appropriate test context as detailed in the test data source
    Given a user with [an active profile in CCD]
    And a case that has just been created as in [CreateCase],
    And a successful call [to create a hearing request] as in [CreateHearingRequest],
    And a successful call [post a listg] as in [in boudn adpater],


  @S-006.1
  # AC01: Validation error  - Return 400 error message
  Scenario: Successfully search for hearings of a case reference that has 2 hearing requests
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    And another successful call [to create a hearing request] as in [CreateHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Search unnotified Hearings] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has the 400 code],


  @S-006.2
  # AC02: User does not have rights Role Assignment - Return 403 error message
  Scenario: Successfully search for hearings of a case reference that has 2 hearing requests
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    And another successful call [to create a hearing request] as in [CreateHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Search unnotified Hearings] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has the 403 code],


  @S-006.3
  # AC03: 400 when set when hearing.hmctsServiceCode does not match provided value
  Scenario: Successfully search for hearings of a case reference that has 2 hearing requests
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    And another successful call [to create a hearing request] as in [CreateHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Search unnotified Hearings] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has the 400 code],

  @S-006.4
  # AC04: Return empty result set when existing MIN hearingDayDetails.startDateTime is lesser than provided hearingStartDateFrom
  Scenario: Successfully search for hearings of a case reference that has 2 hearing requests
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    And another successful call [to create a hearing request] as in [CreateHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Search unnotified Hearings] operation of [HMC CFT Hearing Service],
    Then a positive response is received,
    And the response [has 200 status code],

  @S-006.5
  # AC05: Return empty result set when existing MAX hearingDayDetails.endDateTime is greater than provided hearingStartDateTo
  Scenario: Successfully search for hearings of a case reference that has 2 hearing requests
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    And another successful call [to create a hearing request] as in [CreateHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Search unnotified Hearings] operation of [HMC CFT Hearing Service],
    Then a positive response is received,
    And the response [has 200 status code],


  @S-006.6
  # AC06: Return empty result set if there is an outstanding request version after the most recent response from LA
  Scenario: Successfully search for hearings of a case reference that has 2 hearing requests
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    And another successful call [to create a hearing request] as in [CreateHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Search unnotified Hearings] operation of [HMC CFT Hearing Service],
    Then a positive response is received,
    And the response [has 200 status code],


  @S-006.7
  # AC07: Return empty result set if hearingResponse.partiesNotifiedDateTime is NULL
  Scenario: Successfully search for hearings of a case reference that has 2 hearing requests
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    And another successful call [to create a hearing request] as in [CreateHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Search unnotified Hearings] operation of [HMC CFT Hearing Service],
    Then a positive response is received,
    And the response [has 200 status code],
