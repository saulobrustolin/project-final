package saulo.brustolin.project.mappers;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import saulo.brustolin.project.dtos.users.UpdateUserDTO;
import saulo.brustolin.project.entities.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @Mapping(target = "cpf", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateEntityFromDto(UpdateUserDTO dto, @MappingTarget User entity);
}
