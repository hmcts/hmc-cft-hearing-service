package uk.gov.hmcts.reform.hmc.hmi.befta;

import uk.gov.hmcts.befta.BeftaMain;

public class CftHearingServiceBeftaMain {

    private CftHearingServiceBeftaMain() {
        // Hide Utility Class Constructor :
        // Utility classes should not have a public or default constructor (squid:S1118)
    }

    public static void main(String[] args) {

        BeftaMain.main(args, new CftHearingServiceTestAutomationAdapter());
    }

}
