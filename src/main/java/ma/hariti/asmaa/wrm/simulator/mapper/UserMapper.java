package ma.hariti.asmaa.wrm.simulator.mapper;

import ma.hariti.asmaa.wrm.simulator.dto.request.UserDTO;
import ma.hariti.asmaa.wrm.simulator.dto.response.UserResponse;
import ma.hariti.asmaa.wrm.simulator.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserDTO toDTO(User user);

    User toEntity(UserDTO userDTO);

    List<UserDTO> toDTOList(List<User> users);
    UserResponse toResponse(User user);

    void updateUserFromDTO(UserDTO userDTO, @MappingTarget User user);
}