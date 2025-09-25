package uk.gov.hmcts.reform.hmc;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLib;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLibConfigurer;

@Component
public class CftLibConfig implements CFTLibConfigurer {

    @Override
    public void configure(CFTLib lib) {
        createCcdRoles(lib);
        createIdamUsers(lib);
    }

    private void createCcdRoles(CFTLib lib) {
        lib.createRoles(
            "caseworker",
            "caseworker-approver",
            "caseworker-autotest1",
            "caseworker-autotest1-private",
            "caseworker-autotest1-senior",
            "caseworker-autotest1-solicitor",
            "caseworker-autotest2",
            "caseworker-autotest2-private",
            "caseworker-autotest2-senior",
            "caseworker-autotest2-solicitor",
            "caseworker-befta_jurisdiction_1",
            "caseworker-befta_jurisdiction_2",
            "caseworker-befta_jurisdiction_2-solicitor_1",
            "caseworker-befta_jurisdiction_2-solicitor_2",
            "caseworker-befta_jurisdiction_2-solicitor_3",
            "caseworker-befta_jurisdiction_3",
            "caseworker-befta_jurisdiction_3-solicitor",
            "caseworker-befta_master",
            "caseworker-befta_master-junior",
            "caseworker-befta_master-manager",
            "caseworker-befta_master-solicitor",
            "caseworker-befta_master-solicitor_1",
            "caseworker-befta_master-solicitor_2",
            "caseworker-befta_master-solicitor_3",
            "caseworker-caa",
            "ccd-import",
            "citizen",
            "pui-caa"
        );
    }

    private void createIdamUsers(CFTLib lib) {
        lib.createIdamUser("befta.pui.caa.1@gmail.com", "pui-caa");
        lib.createIdamUser("ccd.docker.default@hmcts.net", "ccd-import");

        lib.createIdamUser("auto.test.cnp@gmail.com", "caseworker", "caseworker-autotest1", "ccd-import");
        lib.createIdamUser("auto.test.cnp+private@gmail.com", "caseworker", "caseworker-autotest1",
                           "caseworker-autotest1-private");
        lib.createIdamUser("auto.test.cnp+senior@gmail.com", "caseworker", "caseworker-autotest1",
                           "caseworker-autotest1-senior");
        lib.createIdamUser("auto.test.cnp+solc@gmail.com", "caseworker", "caseworker-autotest1",
                           "caseworker-autotest1-solicitor");
        lib.createIdamUser("auto.test2.cnp@gmail.com", "caseworker", "caseworker-autotest2");
        lib.createIdamUser("auto.test2.cnp+private@gmail.com", "caseworker", "caseworker-autotest2",
                           "caseworker-autotest2-private");
        lib.createIdamUser("auto.test2.cnp+senior@gmail.com", "caseworker", "caseworker-autotest2",
                           "caseworker-autotest2-senior");
        lib.createIdamUser("auto.test2.cnp+solc@gmail.com", "caseworker", "caseworker-autotest2",
                           "caseworker-autotest2-solicitor");
        lib.createIdamUser("auto.test12.cnp@gmail.com", "caseworker", "caseworker-autotest1", "caseworker-autotest2");
        lib.createIdamUser("auto.test12.cnp+private@gmail.com", "caseworker", "caseworker-autotest1",
                           "caseworker-autotest1-private", "caseworker-autotest2", "caseworker-autotest2-private");
        lib.createIdamUser("auto.test12.cnp+solc@gmail.com", "caseworker", "caseworker-autotest1",
                           "caseworker-autotest1-solicitor", "caseworker-autotest2", "caseworker-autotest2-solicitor");
        lib.createIdamUser("auto.test12.cnp+senior@gmail.com", "caseworker", "caseworker-autotest1",
                           "caseworker-autotest1-senior", "caseworker-autotest2", "caseworker-autotest2-senior");
        lib.createIdamUser("ccdimportdomain@gmail.com", "caseworker", "caseworker-autotest1",
                           "caseworker-autotest1-senior");

        lib.createIdamUser("befta.citizen.2@gmail.com", "citizen");
        lib.createIdamUser("befta.citizen.3@gmail.com", "citizen");

        lib.createIdamUser("befta.caseworker.1@gmail.com", "caseworker", "caseworker-befta_jurisdiction_1");
        lib.createIdamUser("befta.caseworker.1.noprofile@gmail.com", "caseworker", "caseworker-befta_jurisdiction_1");

        lib.createIdamUser("befta.caseworker.2@gmail.com", "caseworker", "caseworker-befta_jurisdiction_2");
        lib.createIdamUser("befta.caseworker.2.solicitor.1@gmail.com", "caseworker", "caseworker-befta_jurisdiction_2",
                           "caseworker-befta_jurisdiction_2-solicitor_1");
        lib.createIdamUser("befta.caseworker.2.solicitor.2@gmail.com", "caseworker", "caseworker-befta_jurisdiction_2",
                           "caseworker-befta_jurisdiction_2-solicitor_2");
        lib.createIdamUser("befta.caseworker.2.solicitor.3@gmail.com", "caseworker", "caseworker-befta_jurisdiction_2",
                           "caseworker-befta_jurisdiction_2-solicitor_3");

        lib.createIdamUser("befta.caseworker.3@gmail.com", "caseworker", "caseworker-befta_jurisdiction_3");
        lib.createIdamUser("befta.solicitor.3@gmail.com", "caseworker", "caseworker-befta_jurisdiction_3",
                           "caseworker-befta_jurisdiction_3-solicitor");
        lib.createIdamUser("befta.solicitor.4@gmail.com", "caseworker", "caseworker-befta_jurisdiction_3",
                           "caseworker-befta_jurisdiction_3-solicitor");

        lib.createIdamUser("master.caseworker@gmail.com", "caseworker", "caseworker-befta_master");

        lib.createIdamUser("master.solicitor.1@gmail.com", "caseworker", "caseworker-befta_master",
                           "caseworker-befta_master-solicitor_1");
        lib.createIdamUser("master.solicitor.2@gmail.com", "caseworker", "caseworker-befta_master",
                           "caseworker-befta_master-solicitor_2");
        lib.createIdamUser("master.solicitor.3@gmail.com", "caseworker", "caseworker-befta_master",
                           "caseworker-befta_master-solicitor_3");

        lib.createIdamUser("befta.master.solicitor.becky@gmail.com", "caseworker", "caseworker-befta_master",
                           "caseworker-befta_master-solicitor");
        lib.createIdamUser("befta.master.solicitor.benjamin@gmail.com", "caseworker", "caseworker-befta_master",
                           "caseworker-befta_master-solicitor");
        lib.createIdamUser("befta.master.solicitor.bill@gmail.com", "caseworker", "caseworker-befta_master",
                           "caseworker-befta_master-solicitor");
        lib.createIdamUser("befta.master.solicitor.emma@gmail.com", "caseworker", "caseworker-befta_master",
                           "caseworker-befta_master-solicitor");
        lib.createIdamUser("befta.master.solicitor.jane@gmail.com", "caseworker", "caseworker-befta_master",
                           "caseworker-befta_master-solicitor", "pui-caa");
        lib.createIdamUser("befta.master.solicitor.david@gmail.com", "caseworker", "caseworker-befta_master",
                           "caseworker-befta_master-solicitor");

        lib.createIdamUser("befta.master.solicitor.mutlu@gmail.com", "caseworker", "caseworker-befta_master",
                           "caseworker-befta_master-solicitor");
        lib.createIdamUser("befta.master.solicitor.richard@gmail.com", "caseworker", "caseworker-befta_master",
                           "caseworker-befta_master-solicitor");
        lib.createIdamUser("befta.master.solicitor.tony@gmail.com", "caseworker", "caseworker-befta_master",
                           "caseworker-befta_master-solicitor");

        lib.createIdamUser("befta.jurisdiction.3.solicitor.alice@gmail.com", "caseworker",
                           "caseworker-befta_jurisdiction_3", "caseworker-befta_jurisdiction_3-solicitor", "pui-caa");

        lib.createIdamUser("befta.caseworker.caa@gmail.com", "caseworker", "caseworker-caa");

        lib.createIdamUser("role.assignment.admin@gmail.com", "caseworker");
        lib.createIdamUser("data.store.idam.system.user@gmail.com", "caseworker");

        lib.createIdamUser("ccd.ac.superuser@gmail.com", "caseworker");
        lib.createIdamUser("ccd.ac.solicitor1@gmail.com", "caseworker");
        lib.createIdamUser("ccd.ac.solicitor2@gmail.com", "caseworker");
        lib.createIdamUser("ccd.ac.staff1@gmail.com", "caseworker");
        lib.createIdamUser("ccd.ac.staff2@gmail.com", "caseworker");
        lib.createIdamUser("ccd.ac.staff3@gmail.com", "caseworker");
        lib.createIdamUser("ccd.ac.staff5@gmail.com", "caseworker");
        lib.createIdamUser("ccd.ac.staff6@gmail.com", "caseworker");
        lib.createIdamUser("ccd.ac.staff7@gmail.com", "caseworker");
        lib.createIdamUser("ccd.ac.staff8@gmail.com", "caseworker");
        lib.createIdamUser("ccd.ac.other1@gmail.com", "caseworker");
        lib.createIdamUser("hmc.superuser@gmail.com", "caseworker", "caseworker-befta_master");
        lib.createIdamUser("hmc.hearing-manager@gmail.com", "hearing-manager");
        lib.createIdamUser("hmc.hearing-viewer@gmail.com", "hearing-viewer");
        lib.createIdamUser("hmc.listed-hearing-viewer@gmail.com", "listed-hearing-viewer");
    }
}
