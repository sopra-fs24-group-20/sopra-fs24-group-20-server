package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * DTOMapper
 * This class is responsible for generating classes that will automatically
 * transform/map the internal representation
 * of an entity (e.g., Player) to the external/API representation (e.g.,
 * PlayerGetDTO for getting, PlayerPostDTO for creating)
 * and vice versa.
 * Additional mappers can be defined for new entities.
 * Always create one mapper for getting information (GET) and one mapper for
 * creating or updating information (POST, PUT).
 */
@Mapper
public interface DTOMapper {

    DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

    @Mapping(source = "lobbyName",target = "lobbyName") // Ignore players list in the lobby DTO
    @Mapping(source = "roundDuration",target = "roundDuration")
    @Mapping(source = "categories",target = "categories")
    @Mapping(source = "rounds",target = "rounds")
    @Mapping(source = "gameMode",target = "gameMode")
    @Mapping(source = "autoCorrectMode",target = "autoCorrectMode")
    LobbyGetDTO convertEntityToLobbyGetDTO(Lobby lobby);
    @Mapping(source = "lobbyName",target = "lobbyName") // Ignore players list in the lobby DTO
    @Mapping(source = "lobbyPassword",target = "lobbyPassword") // Ignore players list in the lobby DTO
    Lobby convertLobbyPostDTOtoEntity(LobbyPostDTO lobbyPostDTO); // New method for conversion
    @Mapping(target = "ready", ignore = true)
    @Mapping(target = "stats", ignore = true)
    @Mapping(target = "token", ignore = true)
    @Mapping(target = "lobby", ignore = true)
    @Mapping(source = "username", target = "username")
    @Mapping(source = "password", target = "password")
    Player convertPlayerPostDTOtoEntity(PlayerPostDTO playerPostDTO);

    @Mapping(source = "username", target = "username")
    @Mapping(source = "ready", target = "ready")
    @Mapping(source = "stats", target = "stats")
    PlayerGetDTO convertEntityToPlayerGetDTO(Player player);

    // Mapping for updating player profile
    @Mapping(target = "password", ignore = true)
    @Mapping(source = "ready", target = "ready")
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "token", ignore = true)
    @Mapping(target = "lobby", ignore = true)
    @Mapping(source = "stats", target = "stats")
    Player convertPlayerPutDTOtoEntity(PlayerPutDTO playerPutDTO);
}
