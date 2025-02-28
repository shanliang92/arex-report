package com.arextest.web.core.business.util;

import com.arextest.web.common.LogUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class SchemaUtils {
    private static final String DEFAULT_STR = "String";
    private static final int DEFAULT_INT = 1;
    private static final double DEFAULT_DOUBLE = 1.0;
    private static final String NULL_STR = "null";
    private static final String VALUE_WITH_SYMBOL = "%value%";

    public static void mergeMap(Map<String, Object> contract, Map<String, Object> model) {
        model.forEach((key, value) -> mergeEntry(contract, key, value));
    }

    public static String mergeJson(String contract, String model) {
        if (model == null) {
            return contract;
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> contractMap = contract == null ? new HashMap<>() : objectMapper.readValue(contract, Map.class);
            Map<String, Object> modelMap = objectMapper.readValue(model, Map.class);
            mergeMap(contractMap, modelMap);
            return objectMapper.writeValueAsString(contractMap);
        } catch (JsonProcessingException e) {
            LogUtils.error(LOGGER, "objectMapper readValue failed! contract:{}, model:{}", contract, model);
        }
        return null;
    }

    private static void mergeEntry(Map<String, Object> contract, String key, Object value) {
        Object contractItem = contract.get(key);
        if (value instanceof Map<?, ?>) {
            Map<String, Object> mapContract;
            if (contractItem instanceof Map<?, ?>) {
                mapContract = (Map<String, Object>) contractItem;
            } else {
                mapContract = new HashMap<>();
            }
            mergeMap(mapContract, (Map<String, Object>) value);
            contract.put(key, mapContract);
        } else if (value instanceof List) {
            List<Object> listContract;
            if (contractItem instanceof List) {
                listContract = (List<Object>) contractItem;
            } else {
                listContract = new ArrayList<>();
            }
            mergeList(listContract, (List<Object>) value);
            contract.put(key, listContract);
        } else {
            if (contract.get(key) == null) {
                contract.put(key, handlePrimaryItem(value));
            }
        }
    }

    private static void mergeList(List<Object> contract, List<Object> model) {
        Object contractItem = null;
        if (!contract.isEmpty()) {
            contractItem = contract.remove(0);
        }

        for (Object item : model) {
            if (item instanceof Map<?, ?>) {
                if (!(contractItem instanceof Map<?, ?>)) {
                    contractItem = new HashMap<>();
                }
                mergeMap((Map<String, Object>)contractItem, (Map<String, Object>) item);
            } else if (item instanceof List) {
                if (!(contractItem instanceof List)) {
                    contractItem = new ArrayList<>();
                }
                mergeList((List<Object>) contractItem, (List<Object>) item);
            } else {
                if (contractItem == null) {
                    Object primaryItem = handlePrimaryItem(item);
                    contractItem = new HashMap<String, Object>();
                    ((Map<String, Object>) contractItem).put(VALUE_WITH_SYMBOL, primaryItem);
                }
            }
        }
        if (contractItem != null) {
            contract.add(contractItem);
        }
    }

    private static Object handlePrimaryItem(Object item) {
        if (item == null) {
            return NULL_STR;
        }
        if (item instanceof Integer || item instanceof Long) {
            return DEFAULT_INT;
        }
        if (item instanceof Double || item instanceof Float) {
            return DEFAULT_DOUBLE;
        }
        if (item instanceof Boolean) {
            return Boolean.TRUE;
        }
        if (item instanceof String) {
            return DEFAULT_STR;
        }
        LogUtils.error(LOGGER, "Unsupported Type, item:{}, class:{}", item, item.getClass());
        return null;
    }
}
