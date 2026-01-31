package com.algorena.games.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ChessGameStateDTO.class, name = "CHESS"),
        @JsonSubTypes.Type(value = Connect4GameStateDTO.class, name = "CONNECT_FOUR")
})
public interface GameStateDTO {
}
