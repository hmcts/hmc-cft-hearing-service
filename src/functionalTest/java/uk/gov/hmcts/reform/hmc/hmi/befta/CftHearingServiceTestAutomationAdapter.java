package uk.gov.hmcts.reform.hmc.hmi.befta;

import uk.gov.hmcts.befta.BeftaTestDataLoader;
import uk.gov.hmcts.befta.DefaultBeftaTestDataLoader;
import uk.gov.hmcts.befta.DefaultTestAutomationAdapter;
import uk.gov.hmcts.befta.dse.ccd.DataLoaderToDefinitionStore;
import uk.gov.hmcts.befta.util.BeftaUtils;

public class CftHearingServiceTestAutomationAdapter extends DefaultTestAutomationAdapter {

    @Override
    protected BeftaTestDataLoader buildTestDataLoader() {
        return new DataLoaderToDefinitionStore(this,
                                               DataLoaderToDefinitionStore.VALID_CCD_TEST_DEFINITIONS_PATH) {

            @Override
            protected void createRoleAssignment(String resource, String filename) {
                // Do not create role assignments.
                BeftaUtils.defaultLog("Will NOT create role assignments!");
            }

        };
    }

    @Override
    public BeftaTestDataLoader getDataLoader() {
        return new DefaultBeftaTestDataLoader() {
            @Override
            public void doLoadTestData() {

            }
        };
    }
}
