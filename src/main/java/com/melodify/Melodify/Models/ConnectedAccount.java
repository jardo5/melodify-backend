package com.melodify.Melodify.Models;

import lombok.Data;


@Data
public class ConnectedAccount {
    private String provider;
    private String accessToken;
    private String refreshToken;
    private String expiresAt;
}
