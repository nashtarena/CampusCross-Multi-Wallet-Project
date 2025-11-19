package com.campuscross.wallet.dto;

public record RegisterRequest(
        String email,
        String password,
        String firstName,
        String lastName,
        String phoneNumber,
        String studentId,
        String campusName,
        String role
) {}
