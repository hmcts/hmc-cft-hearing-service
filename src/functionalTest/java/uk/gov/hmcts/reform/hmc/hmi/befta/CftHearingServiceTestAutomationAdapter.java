package uk.gov.hmcts.reform.hmc.hmi.befta;

import uk.gov.hmcts.befta.BeftaTestDataLoader;
import uk.gov.hmcts.befta.DefaultBeftaTestDataLoader;
import uk.gov.hmcts.befta.DefaultTestAutomationAdapter;
import uk.gov.hmcts.befta.dse.ccd.DataLoaderToDefinitionStore;

public class CftHearingServiceTestAutomationAdapter extends DefaultTestAutomationAdapter {

    @Override
    protected BeftaTestDataLoader buildTestDataLoader() {
        return new DataLoaderToDefinitionStore(this,
                                               DataLoaderToDefinitionStore.VALID_CCD_TEST_DEFINITIONS_PATH) {
        };
    }
}
