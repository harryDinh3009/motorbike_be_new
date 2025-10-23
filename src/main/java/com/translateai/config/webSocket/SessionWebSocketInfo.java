package com.translateai.config.webSocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class SessionWebSocketInfo {

    private String jwtToken;

    private String fullName;

    private String userName;

    private String avatar;

    private String email;

    private String id;

}
