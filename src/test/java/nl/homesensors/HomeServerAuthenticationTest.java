package nl.homesensors;

import org.apache.http.client.methods.HttpPost;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomeServerAuthenticationTest {

    @Test
    void givenNoUserNameSetWhenSetAuthorizationHeaderThenNoInteractionsWithRequest() {
        // given
        final HomeServerAuthentication homeServerAuthentication = new HomeServerAuthentication(null, "secret");
        final HttpPost request = mock(HttpPost.class);

        // when
        homeServerAuthentication.setAuthorizationHeader(request);

        // then
        verifyNoInteractions(request);
    }

    @Test
    void givenNoPasswordSetWhenSetAuthorizationHeaderThenNoInteractionsWithRequest() {
        // given
        final HomeServerAuthentication homeServerAuthentication = new HomeServerAuthentication("BadAssUser", null);
        final HttpPost request = mock(HttpPost.class);

        // when
        homeServerAuthentication.setAuthorizationHeader(request);

        // then
        verifyNoInteractions(request);
    }

    @Test
    void givenBothUsernameAndPasswordSetWhenSetAuthorizationHeaderThenHeaderAdded() {
        // given
        final HomeServerAuthentication homeServerAuthentication = new HomeServerAuthentication("BadAssUser", "secret");
        final HttpPost request = mock(HttpPost.class);

        // when
        homeServerAuthentication.setAuthorizationHeader(request);

        // then
        verify(request).setHeader("Authorization", "Basic QmFkQXNzVXNlcjpzZWNyZXQ=");
    }
}
