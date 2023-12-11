package com.amz.proxy.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProxyServer {
    private String username;
    private char[] password;
    private String host;
    private int port;
    private boolean available;
    private Long unavailableUntil;
}