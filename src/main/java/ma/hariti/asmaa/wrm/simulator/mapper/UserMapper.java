package ma.hariti.asmaa.wrm.simulator.mapper;

import ma.hariti.asmaa.wrm.simulator.dto.request.CreateUserRequest;
import ma.hariti.asmaa.wrm.simulator.dto.response.UserResponse;
import ma.hariti.asmaa.wrm.simulator.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(CreateUserRequest request);

    UserResponse toResponse(User user);
}
