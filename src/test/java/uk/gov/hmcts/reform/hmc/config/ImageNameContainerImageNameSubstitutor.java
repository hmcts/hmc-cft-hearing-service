package uk.gov.hmcts.reform.hmc.config;

import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.ImageNameSubstitutor;


public class ImageNameContainerImageNameSubstitutor extends ImageNameSubstitutor{
    private final DockerImageName hmctsPostgresDockerImage = DockerImageName
        .parse("hmctspublic.azurecr.io/imported/postgres:15-alpine")
        .asCompatibleSubstituteFor("postgres");

    @Override
    public DockerImageName apply(DockerImageName original) {

        if (original.asCanonicalNameString().contains("postgres")) {
            return hmctsPostgresDockerImage;
        }

        return original;
    }

    @Override
    protected String getDescription() {
        return "hmcts hmc substitutor";
    }
}
