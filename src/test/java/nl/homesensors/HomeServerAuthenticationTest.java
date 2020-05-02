package nl.homesensors;

import org.apache.http.client.methods.HttpPost;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HomeServerAuthenticationTest {

    @Test
    public void givenNoUserNameSetWhenSetAuthorizationHeaderThenNoInteractionsWithRequest() {
        final HomeServerAuthentication homeServerAuthentication = new HomeServerAuthentication(null, "secret");

        final HttpPost request = mock(HttpPost.class);

        homeServerAuthentication.setAuthorizationHeader(request);

        verifyNoInteractions(request);
    }

    @Test
    public void givenNoPasswordSetWhenSetAuthorizationHeaderThenNoInteractionsWithRequest() {
        final HomeServerAuthentication homeServerAuthentication = new HomeServerAuthentication("BadAssUser", null);

        final HttpPost request = mock(HttpPost.class);

        homeServerAuthentication.setAuthorizationHeader(request);

        verifyNoInteractions(request);
    }

    @Test
    public void givenBothUsernameAndPasswordSetWhenSetAuthorizationHeaderThenHeaderAdded() {
        final HomeServerAuthentication homeServerAuthentication = new HomeServerAuthentication("BadAssUser", "secret");

        final HttpPost request = mock(HttpPost.class);

        homeServerAuthentication.setAuthorizationHeader(request);

        verify(request).setHeader("Authorization", "Basic QmFkQXNzVXNlcjpzZWNyZXQ=");
    }
}
