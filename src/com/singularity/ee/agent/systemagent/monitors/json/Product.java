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

import java.util.ArrayList;
import java.util.List;

public class Product {

    @SuppressWarnings("UnusedDeclaration")
    private String name;
    @SuppressWarnings("UnusedDeclaration")
    private String id;
    private List<Slot> slot;

    public Product() {
        slot = new ArrayList<Slot>();
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public List<Slot> getSlots() {
        return slot;
    }

    @Override
    public String toString() {
        return String.format("{%s}", id);
    }
}
