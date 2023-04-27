package com.amyojiakor.AccountMicroService.models.payloads;


import java.util.List;

public record UserDetailsResponse(String firstName, String lastName, String email, List<?> userAccounts){
}
