package com.arextest.web.api.service.controller.config;

import com.arextest.common.model.response.Response;
import com.arextest.common.utils.ResponseUtils;
import com.arextest.web.core.business.config.ConfigurableHandler;
import com.arextest.web.core.business.config.replay.ComparisonReferenceConfigurableHandler;
import com.arextest.web.model.contract.contracts.config.replay.ComparisonReferenceConfiguration;
import com.arextest.web.model.contract.contracts.config.replay.QueryComparisonRequestType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * Created by rchen9 on 2022/9/16.
 */
@Controller
@RequestMapping("/api/config/comparison/reference")
public class ComparisonReferenceController extends AbstractConfigurableController<ComparisonReferenceConfiguration> {
    protected ComparisonReferenceController(
        @Autowired ConfigurableHandler<ComparisonReferenceConfiguration> configurableHandler) {
        super(configurableHandler);
    }

    @Resource
    ComparisonReferenceConfigurableHandler comparisonReferenceConfigurableHandler;

    @Deprecated
    @RequestMapping("/useResultAsList")
    @ResponseBody
    public final Response useResultList(@RequestParam String appId,
        @RequestParam(required = false) String operationId) {
        if (StringUtils.isEmpty(appId)) {
            return InvalidResponse.REQUESTED_APP_ID_IS_EMPTY;
        }
        return ResponseUtils
            .successResponse(this.comparisonReferenceConfigurableHandler.useResultAsList(appId, operationId));
    }

    @RequestMapping("/queryByInterfaceId")
    @ResponseBody
    public final Response queryByInterfaceId(@RequestParam String interfaceId) {
        if (StringUtils.isEmpty(interfaceId)) {
            return InvalidResponse.REQUESTED_INTERFACE_ID_IS_EMPTY;
        }

        return ResponseUtils
            .successResponse(this.comparisonReferenceConfigurableHandler.queryByInterfaceId(interfaceId));
    }

    @PostMapping("/queryComparisonConfig")
    @ResponseBody
    public Response queryComparisonConfig(@RequestBody QueryComparisonRequestType request) {
        return ResponseUtils.successResponse(this.comparisonReferenceConfigurableHandler.queryComparisonConfig(
            request.getAppId(), request.getOperationId(), request.getOperationType(), request.getOperationName()));
    }

}
