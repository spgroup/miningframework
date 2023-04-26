package com.alibaba.excel.enums;

import java.util.HashMap;
import java.util.Map;
import com.alibaba.excel.util.StringUtils;

public enum CellDataTypeEnum {

    STRING,
    DIRECT_STRING,
    NUMBER,
    BOOLEAN,
    EMPTY,
    ERROR,
    DATE,
    RICH_TEXT_STRING,;

    private static final Map<String, CellDataTypeEnum> TYPE_ROUTING_MAP = new HashMap<String, CellDataTypeEnum>(16);

    static {
        TYPE_ROUTING_MAP.put("s", STRING);
        TYPE_ROUTING_MAP.put("str", DIRECT_STRING);
        TYPE_ROUTING_MAP.put("inlineStr", DIRECT_STRING);
        TYPE_ROUTING_MAP.put("e", ERROR);
        TYPE_ROUTING_MAP.put("b", BOOLEAN);
        TYPE_ROUTING_MAP.put("n", NUMBER);
    }

    public static CellDataTypeEnum buildFromCellType(String cellType) {
        if (StringUtils.isEmpty(cellType)) {
            return EMPTY;
        }
        return TYPE_ROUTING_MAP.get(cellType);
    }
}