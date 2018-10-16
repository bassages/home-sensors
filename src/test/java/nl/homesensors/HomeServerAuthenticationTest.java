package nl.homesensors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.apache.http.client.methods.HttpPost;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HomeServerAuthenticationTest {

    @Test
    public void givenNoUserNameSetWhenSetAuthorizationHeaderThenNoInteractionsWithRequest() {
        final HomeServerAuthentication homeServerAuthentication = new HomeServerAuthentication(null, "secret");

        final HttpPost request = mock(HttpPost.class);

        homeServerAuthentication.setAuthorizationHeader(request);

        verifyZeroInteractions(request);
    }

    @Test
    public void givenNoPasswordSetWhenSetAuthorizationHeaderThenNoInteractionsWithRequest() {
        final HomeServerAuthentication homeServerAuthentication = new HomeServerAuthentication("BadAssUser", null);

        final HttpPost request = mock(HttpPost.class);

        homeServerAuthentication.setAuthorizationHeader(request);

        verifyZeroInteractions(request);
    }

    @Test
    public void givenBothUsernameAndPasswordSetWhenSetAuthorizationHeaderThenHeaderAdded() {
        final HomeServerAuthentication homeServerAuthentication = new HomeServerAuthentication("BadAssUser", "secret");

        final HttpPost request = mock(HttpPost.class);

        homeServerAuthentication.setAuthorizationHeader(request);

        verify(request).setHeader("Authorization", "Basic QmFkQXNzVXNlcjpzZWNyZXQ=");
    }
}