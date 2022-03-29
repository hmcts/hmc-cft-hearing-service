package uk.gov.hmcts.reform.hmc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TestFixtures {

    private TestFixtures() {
    }

    public static String fromFileAsString(final String filePath) {
        StringBuilder json = new StringBuilder();
        try (final InputStreamReader inputStreamReader = new InputStreamReader(getInputStream(filePath));
             final BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                json.append(str);
            }
        } catch (IOException e) {
            throw new RuntimeException("Caught exception reading resource " + filePath, e);
        }
        return json.toString();
    }

    private static InputStream getInputStream(final String filename) {
        return TestFixtures.class.getClassLoader()
            .getResourceAsStream(filename);
    }
}
