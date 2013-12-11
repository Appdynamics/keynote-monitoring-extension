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

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Measurement {
    private int id;
    private String alias;
    private List<BucketData> buckets;

    public Measurement() {
        buckets = new ArrayList<BucketData>();
    }

    private Measurement(JSONObject obj) throws JSONException {
        id = obj.getInt("id");
        alias = obj.getString("alias");
        buckets = BucketData.fromJSONArray(obj.getJSONArray("bucket_data"));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public List<BucketData> getBuckets() {
        return buckets;
    }

    public void setBuckets(List<BucketData> buckets) {
        this.buckets = buckets;
    }

    public static Measurement fromJSONObject(JSONObject obj) throws JSONException {
        return new Measurement(obj);
    }

    public static List<Measurement> fromJSONArray(JSONArray arr) throws JSONException {
        List<Measurement> measurements = new ArrayList<Measurement>();
        for (int i=0; i < arr.length(); i++) {
            measurements.add(Measurement.fromJSONObject(arr.getJSONObject(i)));
        }
        return measurements;
    }
}
