package com.arextest.web.core.business.config.replay;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.arextest.web.core.business.config.AbstractConfigurableHandler;
import com.arextest.web.core.repository.AppContractRepository;
import com.arextest.web.core.repository.ConfigRepositoryProvider;
import com.arextest.web.model.contract.contracts.common.enums.ExpirationType;
import com.arextest.web.model.contract.contracts.config.replay.AbstractComparisonDetailsConfiguration;
import com.arextest.web.model.dto.AppContractDto;
import com.arextest.web.model.enums.ContractTypeEnum;

/**
 * @author jmo
 * @since 2022/1/22
 */
public abstract class AbstractComparisonConfigurableHandler<T extends AbstractComparisonDetailsConfiguration>
    extends AbstractConfigurableHandler<T> {

    private AppContractRepository appContractRepository;

    protected AbstractComparisonConfigurableHandler(ConfigRepositoryProvider<T> repositoryProvider,
        AppContractRepository appContractRepository) {
        super(repositoryProvider);
        this.appContractRepository = appContractRepository;
    }

    @Override
    public List<T> useResultAsList(String appId) {
        List<T> comparisonDetails = repositoryProvider.listBy(appId);
        if (CollectionUtils.isNotEmpty(comparisonDetails)) {
            comparisonDetails.removeIf(this::removeDetailsExpired);
        }
        return comparisonDetails;
    }

    public List<T> useResultAsList(String appId, String operationId) {
        List<T> comparisonDetails = repositoryProvider.listBy(appId, operationId);
        if (CollectionUtils.isNotEmpty(comparisonDetails)) {
            comparisonDetails.removeIf(this::removeDetailsExpired);
        }
        return comparisonDetails;
    }

    public List<T> useResultAsList(String appId, int compareConfigType) {
        return this.useResultAsList(appId).stream().filter(config -> config.getCompareConfigType() == compareConfigType)
            .collect(Collectors.toList());
    }

    public List<T> queryByOperationIdAndInterfaceId(String interfaceId, String operationId) {
        List<T> comparisonDetails = repositoryProvider.queryByInterfaceIdAndOperationId(interfaceId, operationId);
        if (CollectionUtils.isNotEmpty(comparisonDetails)) {
            comparisonDetails.removeIf(this::removeDetailsExpired);
        }
        return comparisonDetails;
    }

    public abstract List<T> queryByInterfaceId(String interfaceId);

    public List<T> queryComparisonConfig(String appId, String operationId, String operationType, String operationName) {

        // query the config of dependency
        if (operationType != null || operationName != null) {
            AppContractDto appContractDto =
                appContractRepository.queryDependency(operationId, operationType, operationName);
            if (appContractDto == null) {
                return Collections.emptyList();
            }

            List<T> comparisonConfigList = this.useResultAsList(appId, operationId);
            return comparisonConfigList.stream()
                .filter(config -> Objects.equals(config.getDependencyId(), appContractDto.getId())).peek(item -> {
                    item.setOperationType(operationType);
                    item.setOperationName(operationName);
                }).collect(Collectors.toList());
        }

        // query the config of operation
        if (operationId != null) {
            List<T> comparisonConfigList = this.useResultAsList(appId, operationId);
            return comparisonConfigList.stream().filter(config -> Objects.equals(config.getDependencyId(), null))
                .collect(Collectors.toList());
        }

        // query the config of app global
        return this.useResultAsList(appId, null);
    }

    private boolean removeDetailsExpired(T comparisonDetails) {
        int expirationType = comparisonDetails.getExpirationType();
        if (expirationType == ExpirationType.ABSOLUTE_TIME_EXPIRED.getCodeValue()) {
            return comparisonDetails.getExpirationDate().getTime() < System.currentTimeMillis();
        }
        return expirationType != ExpirationType.PINNED_NEVER_EXPIRED.getCodeValue();
    }

    @Override
    public boolean insert(T comparisonDetail) {

        if (comparisonDetail.getExpirationDate() == null) {
            comparisonDetail.setExpirationDate(new Date());
        }
        addDependencyId(Collections.singletonList(comparisonDetail));
        return repositoryProvider.insert(comparisonDetail);
    }

    @Override
    public boolean insertList(List<T> configurationList) {
        List<T> configurations = Optional.ofNullable(configurationList).map(List::stream).orElse(Stream.empty())
            .filter(item -> item != null && StringUtils.isNotEmpty(item.getAppId())).peek(item -> {
                if (item.getExpirationDate() == null) {
                    item.setExpirationDate(new Date());
                }
            }).collect(Collectors.toList());
        addDependencyId(configurations);
        return repositoryProvider.insertList(configurations);
    }

    public boolean removeByAppId(String appId) {
        return repositoryProvider.listBy(appId).isEmpty() || repositoryProvider.removeByAppId(appId);
    }

    private void addDependencyId(List<T> comparisonDetails) {
        Map<AppContractDto, String> notFoundAppContractMap = new HashMap<>();
        for (T comparisonDetail : comparisonDetails) {
            if (comparisonDetail.getOperationType() == null && comparisonDetail.getOperationName() == null) {
                continue;
            }

            AppContractDto appContractDto = new AppContractDto();
            appContractDto.setAppId(comparisonDetail.getAppId());
            appContractDto.setOperationId(comparisonDetail.getOperationId());
            appContractDto.setOperationType(comparisonDetail.getOperationType());
            appContractDto.setOperationName(comparisonDetail.getOperationName());
            appContractDto.setContractType(ContractTypeEnum.DEPENDENCY.getCode());
            if (notFoundAppContractMap.containsKey(appContractDto)) {
                comparisonDetail.setDependencyId(notFoundAppContractMap.get(appContractDto));
            } else {
                AppContractDto andModifyAppContract = appContractRepository.findAndModifyAppContract(appContractDto);
                String dependencyId = andModifyAppContract.getId();
                notFoundAppContractMap.put(appContractDto, dependencyId);
                comparisonDetail.setDependencyId(dependencyId);
            }
        }
    }
}
