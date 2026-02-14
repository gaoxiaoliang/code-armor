package com.code.armor.mapper;

import com.code.armor.dto.AppDto;
import com.code.armor.entity.App;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AppMapper {
    AppDto toDto(App app);
}
