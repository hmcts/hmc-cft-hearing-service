package uk.gov.hmcts.reform.hmc.hmi.befta;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import uk.gov.hmcts.befta.BeftaMain;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = "json:target/cucumber.json",
    glue = {"uk.gov.hmcts.befta.player"},
    features = { "classpath:features" },
    tags = "(not @Ignore) or (not @elasticsearch)")
public class CftHearingServiceBeftaRunner {

    private CftHearingServiceBeftaRunner() {
        // Hide Utility Class Constructor :
        // Utility classes should not have a public or default constructor (squid:S1118)
    }

    @BeforeClass
    public static void setUp() {
        BeftaMain.setUp(new CftHearingServiceTestAutomationAdapter());
    }

    @AfterClass
    public static void tearDown() {
        BeftaMain.tearDown();
    }

}
