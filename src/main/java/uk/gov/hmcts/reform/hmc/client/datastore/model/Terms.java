package uk.gov.hmcts.reform.hmc.client.datastore.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Terms {

    public List<String> reference;

}
