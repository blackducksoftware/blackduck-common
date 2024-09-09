package com.blackduck.integration.blackduck.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.blackduck.integration.blackduck.http.BlackDuckUrl;
import com.blackduck.integration.blackduck.http.BlackDuckUrlSearchTerm;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

public class BlackDuckUrlTest {
    @Test
    public void testParsingComponentVersionIds() throws IntegrationException {
        HttpUrl componentVersion = new HttpUrl("https://blackduckserver/api/"
                                                   + "components/07731f32-a0f0-4485-8d90-1f0bbdc8185d/"
                                                   + "versions/cf1fd627-04db-4754-be67-dc0127c772d2");

        BlackDuckUrl blackDuckUrl = new BlackDuckUrl(componentVersion);

        assertEquals("07731f32-a0f0-4485-8d90-1f0bbdc8185d", blackDuckUrl.parseId(searchTerms("components")));
        assertEquals("cf1fd627-04db-4754-be67-dc0127c772d2", blackDuckUrl.parseId(searchTerms("components", "versions")));
    }

    @Test
    public void testParsingComponentVersionIdsWithTokens() throws IntegrationException {
        HttpUrl componentVersion = new HttpUrl("https://blackduckserver/api/"
                                                   + "components/07731f32-a0f0-4485-8d90-1f0bbdc8185d/"
                                                   + "versions/cf1fd627-04db-4754-be67-dc0127c772d2");

        BlackDuckUrl blackDuckUrl = new BlackDuckUrl(componentVersion);

        assertEquals("07731f32-a0f0-4485-8d90-1f0bbdc8185d", blackDuckUrl.parseId(Arrays.asList(BlackDuckUrlSearchTerm.COMPONENTS)));
        assertEquals("cf1fd627-04db-4754-be67-dc0127c772d2", blackDuckUrl.parseId(Arrays.asList(BlackDuckUrlSearchTerm.COMPONENTS, BlackDuckUrlSearchTerm.VERSIONS)));
    }

    @Test
    public void testParsingBomComponentVersionIds() throws IntegrationException {
        HttpUrl bomComponentVersionMatchedFiles = new HttpUrl("https://blackduckserver/api/"
                                                                  + "projects/687f92c5-a90f-4d16-9f30-573ac27b8eae/"
                                                                  + "versions/c58fb845-f47b-487e-b15e-86b8762ec2db/"
                                                                  + "components/07731f32-a0f0-4485-8d90-1f0bbdc8185d/"
                                                                  + "versions/cf1fd627-04db-4754-be67-dc0127c772d2/"
                                                                  + "origins/bc02ac05-af16-42da-9e91-a705f7c2b5b3/"
                                                                  + "matched-files");

        BlackDuckUrl blackDuckUrl = new BlackDuckUrl(bomComponentVersionMatchedFiles);

        assertEquals("687f92c5-a90f-4d16-9f30-573ac27b8eae", blackDuckUrl.parseId(searchTerms("projects")));
        assertEquals("c58fb845-f47b-487e-b15e-86b8762ec2db", blackDuckUrl.parseId(searchTerms("projects", "versions")));

        assertEquals("07731f32-a0f0-4485-8d90-1f0bbdc8185d", blackDuckUrl.parseId(searchTerms("projects", "versions", "components")));
        assertEquals("07731f32-a0f0-4485-8d90-1f0bbdc8185d", blackDuckUrl.parseId(searchTerms("components")));

        assertEquals("cf1fd627-04db-4754-be67-dc0127c772d2", blackDuckUrl.parseId(searchTerms("versions", "versions")));
        assertEquals("cf1fd627-04db-4754-be67-dc0127c772d2", blackDuckUrl.parseId(searchTerms("components", "versions")));

        assertEquals("bc02ac05-af16-42da-9e91-a705f7c2b5b3", blackDuckUrl.parseId(searchTerms("projects", "versions", "components", "versions", "origins")));
        assertEquals("bc02ac05-af16-42da-9e91-a705f7c2b5b3", blackDuckUrl.parseId(searchTerms("components", "versions", "origins")));
        assertEquals("bc02ac05-af16-42da-9e91-a705f7c2b5b3", blackDuckUrl.parseId(searchTerms("origins")));
    }

    private List<BlackDuckUrlSearchTerm> searchTerms(String... terms) {
        return Arrays
                   .stream(terms)
                   .map(BlackDuckUrlSearchTerm::new)
                   .collect(Collectors.toList());
    }

}
