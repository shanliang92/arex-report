package com.arextest.web.model.contract.contracts.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeEntity{
    private String nodeName;
    private int index;
}
