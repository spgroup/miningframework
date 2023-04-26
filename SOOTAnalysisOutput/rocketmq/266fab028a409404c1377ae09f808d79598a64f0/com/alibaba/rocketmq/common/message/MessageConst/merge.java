/**
 * Copyright (C) 2010-2013 Alibaba Group Holding Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.rocketmq.common.message;

import java.util.HashSet;


public class MessageConst {
    /**
     * 消�?�关键�?，多个Key用KEY_SEPARATOR隔开（查询消�?�使用）
     */
    public static final String PROPERTY_KEYS = "KEYS";
    /**
     * 消�?�标签，�?�支�?设置一个Tag（�?务端消�?�过滤使用）
     */
    public static final String PROPERTY_TAGS = "TAGS";
    /**
     * 是�?�等待�?务器将消�?�存储完毕�?返回（�?�能是等待刷盘完�?或者等待�?�步�?制到其他�?务器）
     */
    public static final String PROPERTY_WAIT_STORE_MSG_OK = "WAIT";
    /**
     * 消�?�延时投递时间级别，0表示�?延时，大于0表示特定延时级别（具体级别在�?务器端定义）
     */
    public static final String PROPERTY_DELAY_TIME_LEVEL = "DELAY";

    /**
     * 内部使用
     */
    public static final String PROPERTY_RETRY_TOPIC = "RETRY_TOPIC";
    public static final String PROPERTY_REAL_TOPIC = "REAL_TOPIC";
    public static final String PROPERTY_REAL_QUEUE_ID = "REAL_QID";
    public static final String PROPERTY_TRANSACTION_PREPARED = "TRAN_MSG";
    public static final String PROPERTY_PRODUCER_GROUP = "PGROUP";
    public static final String PROPERTY_MIN_OFFSET = "MIN_OFFSET";
    public static final String PROPERTY_MAX_OFFSET = "MAX_OFFSET";
    public static final String PROPERTY_BUYER_ID = "BUYER_ID";
    public static final String PROPERTY_ORIGIN_MESSAGE_ID = "ORIGIN_MESSAGE_ID";
    public static final String PROPERTY_TRANSFER_FLAG = "TRANSFER_FLAG";
    public static final String PROPERTY_CORRECTION_FLAG = "CORRECTION_FLAG";
    public static final String PROPERTY_MQ2_FLAG = "MQ2_FLAG";
    public static final String PROPERTY_RECONSUME_TIME = "RECONSUME_TIME";
<<<<<<< HEAD
    public static final String PROPERTY_MSG_REGION = "MSG_REGION";
=======
    public static final String PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX="UNIQ_KEY";

>>>>>>> origin/norepeat
    public static final String KEY_SEPARATOR = " ";

    public static final HashSet<String> systemKeySet = new HashSet<String>();
    static {
        systemKeySet.add(PROPERTY_MSG_REGION);
        systemKeySet.add(PROPERTY_KEYS);
        systemKeySet.add(PROPERTY_TAGS);
        systemKeySet.add(PROPERTY_WAIT_STORE_MSG_OK);
        systemKeySet.add(PROPERTY_DELAY_TIME_LEVEL);
        systemKeySet.add(PROPERTY_RETRY_TOPIC);
        systemKeySet.add(PROPERTY_REAL_TOPIC);
        systemKeySet.add(PROPERTY_REAL_QUEUE_ID);
        systemKeySet.add(PROPERTY_TRANSACTION_PREPARED);
        systemKeySet.add(PROPERTY_PRODUCER_GROUP);
        systemKeySet.add(PROPERTY_MIN_OFFSET);
        systemKeySet.add(PROPERTY_MAX_OFFSET);
        systemKeySet.add(PROPERTY_BUYER_ID);
        systemKeySet.add(PROPERTY_ORIGIN_MESSAGE_ID);
        systemKeySet.add(PROPERTY_TRANSFER_FLAG);
        systemKeySet.add(PROPERTY_CORRECTION_FLAG);
        systemKeySet.add(PROPERTY_MQ2_FLAG);
        systemKeySet.add(PROPERTY_RECONSUME_TIME);
        systemKeySet.add(PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX);
    }
}
