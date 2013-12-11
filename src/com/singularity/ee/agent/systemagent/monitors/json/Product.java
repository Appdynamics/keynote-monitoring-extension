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

public class Product {
    private String name;
    private String id;
    private List<Slot> slots;

    public Product() {
        slots = new ArrayList<Slot>();
    }

    private Product(JSONObject obj) throws JSONException {
        name = obj.getString("name");
        id = obj.getString("id");
        slots = Slot.fromJSONArray(obj.getJSONArray("slot"));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Slot> getSlots() {
        return slots;
    }

    public void setSlots(List<Slot> slots) {
        this.slots = slots;
    }

    public static Product fromJSONObject(JSONObject obj) throws JSONException {
        return new Product(obj);
    }

    public static List<Product> fromJSONArray(JSONArray jsonArray) throws JSONException {
        ArrayList<Product> products = new ArrayList<Product>();
        for (int i=0; i < jsonArray.length(); i++) {
            products.add(Product.fromJSONObject(jsonArray.getJSONObject(i)));
        }
        return products;
    }

}
