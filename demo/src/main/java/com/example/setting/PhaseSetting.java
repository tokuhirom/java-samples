package com.example.setting;

import lombok.Getter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@ToString
@Component
public class PhaseSetting {
    @Value("${spring.profiles.active}")
    private String activeProfile;

    public boolean isRelease() {
        return activeProfile.contains("release");
    }

    public boolean isLocal() {
        return activeProfile.contains("local");
    }
}
