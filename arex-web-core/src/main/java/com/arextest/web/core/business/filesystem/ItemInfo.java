package com.arextest.web.core.business.filesystem;


import com.arextest.web.model.dto.filesystem.FSItemDto;

import java.util.List;
import java.util.Set;

public interface ItemInfo {
    String initItem(String parentId, Integer parentNodeType, String workspaceId, String name);
    String saveItem(FSItemDto dto);
    Boolean removeItem(String infoId);
    Boolean removeItems(Set<String> infoIds);
    String duplicate(String parentId, String infoId, String name);
    List<FSItemDto> queryByIds(List<String> ids);
    FSItemDto queryById(String id);
}
