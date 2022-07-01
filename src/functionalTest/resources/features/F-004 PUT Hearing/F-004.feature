@F-004
Feature: F-004: Amend hearing request

  Background:
    Given an appropriate test context as detailed in the test data source
    Given a user with [an active profile in CCD],
    And a case that has just been created as in [CreateCase],

    @S-004.1
  Scenario: successfully amend hearing request
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [amend hearing] operation of [HMC CFT Hearing Service],
    Then a positive response is received,
    And the response [has the 201 code],
    And the response [has a versionNumber of 2],
    And the response [has a status of HEARING_REQUESTED],
    And the response has all other details as expected,
    And a call [to verify the party name has been updated] will get the expected response as in [S-004.1-get-hearing].

  @S-004.2
  Scenario: Cannot amend hearing with incorrect version number
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    When a request is prepared with appropriate values,
    And the request [has an incorrect version number of 2],
    And it is submitted to call the [amend hearing] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has the 400 code],
    And the response has all other details as expected,
    And a call [to get hearing to show version number hasn't been incremented] will get the expected response as in [S-004.2-get-hearing].

  @S-004.3
  Scenario: Cannot amend hearing with incorrect hearing id
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    When a request is prepared with appropriate values,
    And the request [has an incorrect hearing id],
    And it is submitted to call the [amend hearing] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has the 404 code],
    And the response has all other details as expected.

  @S-004.4 @Ignore
#    todo cant get case into UPDATE_REQUESTED state
  Scenario: successfully amend hearing request in the UPDATE_REQUESTED state
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    And a successful call [to amend a hearing request] as in [AmendHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [amend hearing] operation of [HMC CFT Hearing Service],
    Then a positive response is received,
    And the response [has the 201 code],
    And the response [has a versionNumber of 3],
    And the response [has a status of UPDATE_REQUESTED],
    And the response has all other details as expected,
#    And a call [to verify the values have been updated] will get the expected response as in [S-004.1-get-hearing].

  @S-004.5
  Scenario: cannot amend hearing request in the CANCELLATION_REQUESTED state
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    And a successful call [to delete a hearing request] as in [deleteHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [amend hearing] operation of [HMC CFT Hearing Service],
    Then a negative response is received,
    And the response [has the 400 code],
    And the response has all other details as expected.

  @S-004.6
  Scenario: can successfully create hearing using only mandatory fields
    Given a successful call [to create a hearing request] as in [CreateHearingRequest],
    When a request is prepared with appropriate values,
    And it is submitted to call the [amend hearing] operation of [HMC CFT Hearing Service],
    And the request [contains only the mandatory fields],
    Then a positive response is received,
    And the response [has the 201 code],
    And the response [has a versionNumber of 2],
    And the response [has a status of HEARING_REQUESTED],
    And the response has all other details as expected.
