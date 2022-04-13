package uk.gov.hmcts.reform.hmc;

import com.microsoft.applicationinsights.core.dependencies.google.common.io.Resources;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLib;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLibConfigurer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class CFTLibConfig implements CFTLibConfigurer {

    @Override
    public void configure(CFTLib lib) throws IOException {
        // Create a CCD user profile
        lib.createProfile("banderous","DIVORCE", "NO_FAULT_DIVORCE", "Submitted");
        // Create roles
        lib.createRoles(
            "caseworker-divorce"
        );

        lib.createIdamUser("user1@gmail.com", "caseworker-divorce");
        // Configure the AM role assignment service
//        var json = Resources.toString(Resources.getResource("cftlib-am-role-assignments.json"), StandardCharsets.UTF_8);
//        lib.configureRoleAssignments(json);

        // Import a CCD definition xlsx
//        var def = getClass().getClassLoader().getResourceAsStream("NFD-dev.xlsx").readAllBytes();
//        lib.importDefinition(def);
    }
}
