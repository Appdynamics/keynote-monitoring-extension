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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BucketData {
    private String name;
    private Date date;
    private int id;
    private long perfData;
    private long availData;
    private long isReporting;
    private static final Log logger = LogFactory.getLog(BucketData.class);
    private static final String DATE_FORMAT = "yyyy-MMM-dd hh:mm a";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

    public BucketData() {
    }

    private BucketData(JSONObject obj) throws JSONException {

        name = obj.getString("name");
        id = obj.getInt("id");
        isReporting = 1;

        try {
            date = dateFormat.parse(name);
        } catch (ParseException e) {
            logger.error("Error parsing date/time string: " + name, e);
        }

        JSONObject perfObj = obj.getJSONObject("perf_data");
        try {
            Double val = Double.valueOf(perfObj.getString("value")) * 1000.0;
            perfData = val.longValue();
        } catch (NumberFormatException e) {
//            logger.error("Error parsing performance data from Keynote: " + perfObj.toString(), e);
            isReporting = 0;
        }

        JSONObject availObj = obj.getJSONObject("avail_data");
        try {
            Double val = Double.valueOf(availObj.getString("value"));
            availData = val.longValue();
        } catch (NumberFormatException e) {
//            logger.error("Error parsing availability data from Keynote: " + availObj.toString(), e);
            isReporting = 0;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getPerfData() {
        return perfData;
    }

    public void setPerfData(long perfData) {
        this.perfData = perfData;
    }

    public long getAvailData() {
        return availData;
    }

    public void setAvailData(long availData) {
        this.availData = availData;
    }

    public long getIsReporting() {
        return isReporting;
    }

    public void setIsReporting(long reporting) {
        isReporting = reporting;
    }

    public static BucketData fromJSONObject(JSONObject obj) throws JSONException {
        return new BucketData(obj);
    }

    public static List<BucketData> fromJSONArray(JSONArray jsonArray) throws JSONException {
        ArrayList<BucketData> buckets = new ArrayList<BucketData>();
        for (int i=0; i < jsonArray.length(); i++) {
            buckets.add(BucketData.fromJSONObject(jsonArray.getJSONObject(i)));
        }
        return buckets;
    }

    @Override
    public String toString() {
        return "BucketData{" +
                "name='" + name + '\'' +
                ", date=" + date +
                ", id=" + id +
                ", perfData=" + perfData +
                ", availData=" + availData +
                ", isReporting=" + isReporting +
                '}';
    }
}
