package com.arextest.report.model.api.contracts.login;

import lombok.Data;

/**
 * Created by rchen9 on 2022/8/4.
 */
@Data
public class VerifyResponseType {
    private boolean success;
    private String accessToken;
    private String refreshToken;
}
