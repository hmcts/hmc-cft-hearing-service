package uk.gov.hmcts.reform.hmc.hmi.befta;

import uk.gov.hmcts.befta.BeftaTestDataLoader;
import uk.gov.hmcts.befta.TestAutomationAdapter;
import uk.gov.hmcts.befta.data.UserData;
import uk.gov.hmcts.befta.player.BackEndFunctionalTestScenarioContext;

import java.util.concurrent.ExecutionException;

public class CftHearingServiceTestAutomationAdapter implements TestAutomationAdapter {


    @Override
    public String getNewS2SToken() {
        return null;
    }

    @Override
    public String getNewS2SToken(String clientId) {
        return null;
    }

    @Override
    public void authenticate(UserData user, String preferredTokenClientId) throws ExecutionException {

    }

    @Override
    public Object calculateCustomValue(BackEndFunctionalTestScenarioContext scenarioContext, Object key) {
        return null;
    }

    @Override
    public BeftaTestDataLoader getDataLoader() {
        return new BeftaTestDataLoader() {
            @Override
            public void loadTestDataIfNecessary() {

            }

            @Override
            public boolean isTestDataLoadedForCurrentRound() {
                return false;
            }
        };
    }
}
