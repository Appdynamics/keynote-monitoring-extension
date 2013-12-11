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

public class Slot {
    private String url;
    private int slotId;
    private String slotAlias;
    private String transType;

    public Slot() {
    }

    private Slot(JSONObject obj) throws JSONException {
        url = obj.getString("url");
        slotId = obj.getInt("slot_id");
        slotAlias = obj.getString("slot_alias");
        transType = obj.getString("trans_type");
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    public String getSlotAlias() {
        return slotAlias;
    }

    public void setSlotAlias(String slotAlias) {
        this.slotAlias = slotAlias;
    }

    public String getTransType() {
        return transType;
    }

    public void setTransType(String transType) {
        this.transType = transType;
    }

    public static Slot fromJSONObject(JSONObject jsonObject) throws JSONException {
        return new Slot(jsonObject);
    }

    public static List<Slot> fromJSONArray(JSONArray jsonArray) throws JSONException {
        ArrayList<Slot> slots = new ArrayList<Slot>();
        for (int i=0; i < jsonArray.length(); i++) {
            slots.add(Slot.fromJSONObject(jsonArray.getJSONObject(i)));
        }
        return slots;
    }
}
