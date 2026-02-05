package me.github.emirhanunsal.matchfourinarow.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    private String playerId;
    private Disc disc;
}
