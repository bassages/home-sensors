package nl.homesensors;

import static java.util.Objects.nonNull;

import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpPost;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class HomeServerAuthentication {

    private final String homeServerRestServiceBasicAuthUser;
    private final String homeServerRestServiceBasicAuthPassword;

    public HomeServerAuthentication(@Value("${home-server-local-rest-service-basic-auth-user:#{null}}")
                                    final String homeServerRestServiceBasicAuthUser,
                                    @Value("${home-server-local-rest-service-basic-auth-password:#{null}}")
                                    final String homeServerRestServiceBasicAuthPassword) {
        this.homeServerRestServiceBasicAuthUser = homeServerRestServiceBasicAuthUser;
        this.homeServerRestServiceBasicAuthPassword = homeServerRestServiceBasicAuthPassword;
    }

    void setAuthorizationHeader(final HttpPost request) {
        if (nonNull(homeServerRestServiceBasicAuthUser) && nonNull(homeServerRestServiceBasicAuthPassword)) {
            final String auth = homeServerRestServiceBasicAuthUser + ":" + homeServerRestServiceBasicAuthPassword;
            final byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
            final String authorizationHeader = "Basic " + new String(encodedAuth);
            request.setHeader(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }
    }
}
