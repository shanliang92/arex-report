package com.arextest.web.core.business.config.replay;

import com.arextest.web.core.business.config.application.ApplicationOperationConfigurableHandler;
import com.arextest.web.core.repository.AppContractRepository;
import com.arextest.web.core.repository.ConfigRepositoryProvider;
import com.arextest.web.core.repository.FSInterfaceRepository;
import com.arextest.web.model.contract.contracts.config.application.ApplicationOperationConfiguration;
import com.arextest.web.model.contract.contracts.config.replay.ComparisonEncryptionConfiguration;
import com.arextest.web.model.contract.contracts.config.replay.ComparisonExclusionsConfiguration;
import com.arextest.web.model.dto.filesystem.FSInterfaceDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class ComparisonEncryptionConfigurableHandler
        extends AbstractComparisonConfigurableHandler<ComparisonEncryptionConfiguration>{
    protected ComparisonEncryptionConfigurableHandler(
            @Autowired ConfigRepositoryProvider<ComparisonEncryptionConfiguration> repositoryProvider,
            @Autowired AppContractRepository appContractRepository) {
        super(repositoryProvider, appContractRepository);
    }

    @Resource
    FSInterfaceRepository fsInterfaceRepository;

    @Resource
    ApplicationOperationConfigurableHandler applicationOperationConfigurableHandler;


    @Override
    public List<ComparisonEncryptionConfiguration> queryByInterfaceId(String interfaceId) {

        // get operationId
        FSInterfaceDto fsInterfaceDto = fsInterfaceRepository.queryInterface(interfaceId);
        String operationId = fsInterfaceDto == null ? null : fsInterfaceDto.getOperationId();

        List<ComparisonEncryptionConfiguration> result =
                this.queryByOperationIdAndInterfaceId(interfaceId, operationId);
        if (StringUtils.isNotEmpty(operationId)) {
            ApplicationOperationConfiguration applicationOperationConfiguration =
                    applicationOperationConfigurableHandler.useResultByOperationId(operationId);
            if (applicationOperationConfiguration != null) {
                List<ComparisonEncryptionConfiguration> globalConfig =
                        this.useResultAsList(applicationOperationConfiguration.getAppId(), null);
                result.addAll(globalConfig);
            }
        }
        return result;
    }
}

