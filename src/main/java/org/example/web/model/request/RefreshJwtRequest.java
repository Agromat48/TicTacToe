package org.example.web.model.request;

public class RefreshJwtRequest {

    private String refreshToken;

    public RefreshJwtRequest() {
    }

    public RefreshJwtRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
