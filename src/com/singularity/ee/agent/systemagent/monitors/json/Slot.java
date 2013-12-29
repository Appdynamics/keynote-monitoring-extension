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

import java.util.Date;


public class Slot {
    private String url;
    private int slot_id;
    private String slot_alias;
    private String trans_type;
    private String pages;
    private int shared_script_id;
    private int agent_id;
    private String agent_name;
    private int target_id;
    private Date start_date;
    private Date end_date;
    private int target_or_group;
    private String target_type;
    private int index_id;

    public Slot() { }

    public String getUrl() {
        return url;
    }

    public int getSlotId() {
        return slot_id;
    }

    public String getSlotAlias() {
        return slot_alias;
    }

    public String getTransType() {
        return trans_type;
    }

    public String getPages() {
        return pages;
    }

    public int getSharedScriptId() {
        return shared_script_id;
    }

    public int getAgentId() {
        return agent_id;
    }

    public String getAgentName() {
        return agent_name;
    }

    public int getTargetId() {
        return target_id;
    }

    public Date getStartDate() {
        return start_date;
    }

    public Date getEndDate() {
        return end_date;
    }

    public int getTargetOrGroup() {
        return target_or_group;
    }

    public String getTargetType() {
        return target_type;
    }

    public int getIndexId() {
        return index_id;
    }

    @Override
    public String toString() {
        return String.format("{%d %s}", slot_id, slot_alias);
    }
}
