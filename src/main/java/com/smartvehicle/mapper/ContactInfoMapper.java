package com.smartvehicle.mapper;

import com.smartvehicle.entity.ContactInfo;
import com.smartvehicle.payload.request.ContactInfoCreateReq;
import com.smartvehicle.payload.response.ContactInfoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContactInfoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    ContactInfo toEntity(ContactInfoCreateReq request);

    ContactInfoResponse toResponse(ContactInfo contactInfo);
}


