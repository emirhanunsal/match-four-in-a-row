package me.github.emirhanunsal.matchfourinarow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinRoomRequest {
    @NotBlank(message = "Player ID is required")
    private String playerId;
}
