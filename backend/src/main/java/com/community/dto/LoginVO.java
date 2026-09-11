package com.community.dto;

import lombok.Data;

@Data
public class LoginVO {

    private String token;

    private Long id;

    private String username;

    private Integer role;

    private String phone;

    private String building;

    private String roomNo;
}
