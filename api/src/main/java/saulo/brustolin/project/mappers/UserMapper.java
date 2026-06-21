package saulo.brustolin.project.mappers;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import saulo.brustolin.project.dtos.users.UpdateUserDTO;
import saulo.brustolin.project.entities.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
    void updateEntityFromDto(UpdateUserDTO dto, @MappingTarget User entity);
}
