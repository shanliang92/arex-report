package com.arextest.web.model.mapper;

import com.arextest.web.model.contract.contracts.QuerySceneInfoResponseType;
import com.arextest.web.model.dto.iosummary.SubSceneInfo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Created by rchen9 on 2023/6/9.
 */
@Mapper
public interface SubSceneInfoMapper {
    SubSceneInfoMapper INSTANCE = Mappers.getMapper(SubSceneInfoMapper.class);

    QuerySceneInfoResponseType.SubSceneInfoType contractFromDto(SubSceneInfo subSceneInfo);
}
