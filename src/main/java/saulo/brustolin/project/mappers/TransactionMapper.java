package saulo.brustolin.project.mappers;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import saulo.brustolin.project.dtos.transactions.UpdateTransactionDTO;
import saulo.brustolin.project.entities.Transaction;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(UpdateTransactionDTO dto, @MappingTarget Transaction entity);
}
