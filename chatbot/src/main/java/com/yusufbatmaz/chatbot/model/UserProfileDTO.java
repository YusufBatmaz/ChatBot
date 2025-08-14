package com.yusufbatmaz.chatbot.model;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO for updating user profile without exposing entity internals.
 */
@Getter
@Setter
public class UserProfileDTO {
    private String nickname;
    private String occupation;
    private String personality;
    private Set<String> traits;
    private String additionalInfo;
    private String preferredLanguage;
    private Boolean enableForNewChats;
}


