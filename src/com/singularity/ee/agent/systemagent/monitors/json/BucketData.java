/**
 * Copyright 2013 AppDynamics Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.singularity.ee.agent.systemagent.monitors.json;

import org.apache.commons.lang.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BucketData {

    @SuppressWarnings("UnusedDeclaration")
    private String name;
    @SuppressWarnings("UnusedDeclaration")
    private int id;
    private MeasureData perf_data;
    private MeasureData avail_data;
    private MeasureData data_count;

    private static final String DATE_FORMAT = "yyyy-MMM-dd hh:mm a";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

    public BucketData() {
        perf_data = new MeasureData();
        avail_data = new MeasureData();
        data_count = new MeasureData();
    }

    public String getName() {
        return name;
    }

    public Date getDate() throws ParseException {
        return dateFormat.parse(name);
    }

    public int getId() {
        return id;
    }

    public MeasureData getPerfData() {
        return perf_data;
    }

    public MeasureData getAvailData() {
        return avail_data;
    }

    public MeasureData getDataCount() {
        return data_count;
    }

    public long getIsReporting() {
        return StringUtils.isEmpty(perf_data.getValue()) || StringUtils.isEmpty(avail_data.getValue()) ? 0 : 1;
    }

    @Override
    public String toString() {
        return String.format("{%d %s}", id, name);
    }
}
