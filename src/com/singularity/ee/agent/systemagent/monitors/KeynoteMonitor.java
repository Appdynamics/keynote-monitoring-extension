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

package com.singularity.ee.agent.systemagent.monitors;

import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import com.singularity.ee.agent.systemagent.monitors.json.BucketData;
import com.singularity.ee.agent.systemagent.monitors.json.Measurement;
import com.singularity.ee.agent.systemagent.monitors.json.Product;
import com.singularity.ee.agent.systemagent.monitors.json.Slot;

import org.apache.commons.lang.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.http.client.utils.URIBuilder;

import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.Resty;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class KeynoteMonitor extends AManagedMonitor {

    private Resty restClient = new Resty();
    private static final String BASE_URL = "http://api.keynote.com/keynote/api/";
    private String apiKey = "b79ca429-92ac-31f5-8fcf-5ec24e4f4fc2";
    private int bucketSize = 60;
    private List<String> excludeSlotNames = new ArrayList<String>();
    private static final Log logger = LogFactory.getLog(KeynoteMonitor.class);

    public KeynoteMonitor() {
    }

    public URIBuilder getURIBuilder(String verb) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(BASE_URL + verb);
        uriBuilder.addParameter("api_key", apiKey);
        uriBuilder.addParameter("format", "json");
        return uriBuilder;
    }

    public URI getSlotMetadatURI() throws URISyntaxException {
        return getURIBuilder("getslotmetadata").build();
    }

    public URI getGraphDataURI(List<Integer> slotIdList) throws URISyntaxException {
        URIBuilder uriBuilder = getURIBuilder("getgraphdata");
        uriBuilder.addParameter("slotidlist", StringUtils.join(slotIdList, ","));
        uriBuilder.addParameter("bucket", Integer.toString(bucketSize));
        uriBuilder.addParameter("timemode", "relative");
        uriBuilder.addParameter("relativehours", "14400");
        uriBuilder.addParameter("timezone", "UCT");
        return uriBuilder.build();
    }

    public boolean isExcludedSlot(String slotName) {
        for (String excludeName : excludeSlotNames) {
            if (slotName.contains(excludeName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public TaskOutput execute(Map<String, String> stringStringMap, TaskExecutionContext taskExecutionContext) throws TaskExecutionException {
        if (stringStringMap.containsKey("api_key")) {
            apiKey = stringStringMap.get("api_key");
        }
        if (stringStringMap.containsKey("exclude_slots")) {
            String[] slotNames = stringStringMap.get("exclude_slots").split("\\s*,\\s*");
            // TODO: elegantly convert String[] to List<String>
        }

        try {
            List<Integer> slotIdList = new ArrayList<Integer>();

            logger.info("Getting slot metadata");

            URI uri = getSlotMetadatURI();
            JSONObject responseData = restClient.json(uri).object();
            List<Product> products = Product.fromJSONArray(responseData.getJSONArray("product"));
            for (Product product : products) {
                if (product.getId().equals("ApP")) {
                    for (Slot slot : product.getSlots()) {
                        if (slot.getTransType().equals("FAnalyze")) {
                            String url = slot.getUrl();
                            logger.info("Found measurement slot: " + slot.getSlotAlias());

                            if (isExcludedSlot(slot.getSlotAlias())) {
                                    logger.info("Excluding slot " + slot.getSlotAlias() + " based on configuration");
                            } else {
                                logger.info("Adding slot " + slot.getSlotAlias() + " to retrieve list");
                                slotIdList.add(slot.getSlotId());
                            }
                        }
                    }
                }
            }

            logger.info("Getting graph data for " + Integer.toString(slotIdList.size()) + " slots");

            if (slotIdList.size() > 0) {

                uri = getGraphDataURI(slotIdList);
                responseData = restClient.json(uri).object();
                List<Measurement> measurements = Measurement.fromJSONArray((responseData.getJSONArray("measurement")));

                for (Measurement measurement : measurements) {
                    String[] names = measurement.getAlias().split("\\(");
                    String path = "Custom Metrics|Keynote|" + names[0];
                    BucketData whichBucket = null;
                    for (int bucketId = measurement.getBuckets().size() - 1; bucketId >= 0; bucketId--) {
                        if (measurement.getBuckets().get(bucketId).getIsReporting() == 1) {
                            whichBucket = measurement.getBuckets().get(bucketId);
                            break;
                        }
                    }

                    logger.info(names[0] + ": " + whichBucket.toString());

                    getMetricWriter(path + "|Performance",
                            MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
                            MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
                            MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE).printMetric(Long.toString(whichBucket.getPerfData()));
                    getMetricWriter(path + "|Availability",
                            MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
                            MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
                            MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE).printMetric(Long.toString(whichBucket.getAvailData()));
                }
            }

        } catch (URISyntaxException e) {
            logger.error("Error building Keynote API url", e);
        } catch (IOException e) {
            logger.error("IO exception fetching data from Keynote API", e);
        } catch (JSONException e) {
            logger.error("Error parsing JSON data from Keynote", e);
        }

        return new TaskOutput("Success");
    }

    public static void main(String[] argv) throws Exception {
        Map<String, String> executeParams = new HashMap<String, String>();
        executeParams.put("api_key", "b79ca429-92ac-31f5-8fcf-5ec24e4f4fc2");
        new KeynoteMonitor().execute(executeParams, null);
    }
}
