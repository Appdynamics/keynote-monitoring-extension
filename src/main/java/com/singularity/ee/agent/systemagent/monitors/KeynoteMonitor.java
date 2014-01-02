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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import com.singularity.ee.agent.systemagent.monitors.json.*;

import org.apache.commons.lang.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeynoteMonitor extends AManagedMonitor {

    private String apiKey = "c05f56b6-2ca8-3765-afc6-92745cb9709b";
    private int bucketSize = 60;
    private List<Pattern> excludePatterns = new ArrayList<Pattern>();
    private HttpClient client = new DefaultHttpClient();
    private Gson gson;

    private static final String BASE_URL = "http://api.keynote.com/keynote/api/";
    private static final Log logger = LogFactory.getLog(KeynoteMonitor.class);

    public KeynoteMonitor() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat("yyyy-MM-dd hh:mm:ss");
        gson = gsonBuilder.create();
        client = new DefaultHttpClient();
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
        for (Pattern pattern : excludePatterns) {
            Matcher matcher = pattern.matcher(slotName);
            if (matcher.find()) {
                return true;
            }
        }
        return false;
    }

    private <T extends KeynoteResponse> T fetchJson(URI uri, Class<T> clazz) throws IOException {

        HttpGet method = new HttpGet(uri);
        HttpResponse response = client.execute(method);

        if (response.getStatusLine().getStatusCode() >= 400) {
            throw new IOException("HTTP call failed: " + response.getStatusLine().getReasonPhrase());
        }

        InputStream in = response.getEntity().getContent();
        InputStreamReader reader = new InputStreamReader(in, "UTF-8");

        KeynoteResponse obj = gson.fromJson(reader, clazz);
        if (obj.isError()) {
            throw new IOException("Keynote API error: " + obj.getMessage());
        }
        return (T)obj;
    }

    @Override
    public TaskOutput execute(Map<String, String> stringStringMap, TaskExecutionContext taskExecutionContext) throws TaskExecutionException {

        if (stringStringMap.containsKey("api_key")) {
            apiKey = stringStringMap.get("api_key");
        }

        if (StringUtils.isNotEmpty(stringStringMap.get("exclude_slots"))) {
            String[] patterns = stringStringMap.get("exclude_slots").split("\\s*,\\s*");
            for (String pattern : patterns) {
                this.excludePatterns.add(Pattern.compile(pattern));
            }
        }

        try {

            List<Integer> slotIdList = new ArrayList<Integer>();

            logger.info("Getting slot metadata");

            SlotMetadata slotMetadata = fetchJson(getSlotMetadatURI(), SlotMetadata.class);
            for (Product product : slotMetadata.getProducts()) {
                for (Slot slot : product.getSlots()) {
                    logger.debug("Found measurement slot: " + slot.getSlotAlias());

                    if (isExcludedSlot(slot.getSlotAlias())) {
                            logger.info("Excluding slot " + slot.getSlotAlias() + " based on configuration");
                    } else {
                        logger.debug("Adding slot " + slot.getSlotAlias() + " to retrieve list");
                        slotIdList.add(slot.getSlotId());
                    }
                }
            }

            logger.info("Getting graph data for " + Integer.toString(slotIdList.size()) + " slots");

            if (slotIdList.size() > 0) {

                GraphData graphData = fetchJson(getGraphDataURI(slotIdList), GraphData.class);
                for (Measurement measurement : graphData.getMeasurements()) {
                    String[] names = measurement.getAlias().split(" \\(");
                    String name = names[0];
                    String path = "Custom Metrics|Keynote|" + name;
                    BucketData whichBucket = null;
                    for (int bucketId = measurement.getBucketData().size() - 1; bucketId >= 0; bucketId--) {
                        if (measurement.getBucketData().get(bucketId).getIsReporting() == 1) {
                            whichBucket = measurement.getBucketData().get(bucketId);
                            break;
                        }
                    }

                    if (whichBucket != null) {

                        logger.debug(name + ": " + whichBucket.toString());

                        long perfData = Math.round(Double.valueOf(whichBucket.getPerfData().getValue()) * 1000.0);
                        getMetricWriter(path + "|Performance",
                                MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
                                MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
                                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE).printMetric(Long.toString(perfData));

                        long availData = Math.round(Double.valueOf(whichBucket.getAvailData().getValue()));
                        getMetricWriter(path + "|Availability",
                                MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
                                MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
                                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE).printMetric(Long.toString(availData));

                    } else {
                        logger.warn("Couldn't find a bucket for slot " + name + " that had valid measurements");
                    }
                }
            }

            logger.info("Success");
            return new TaskOutput("Success");

        } catch (URISyntaxException e) {
            logger.error("Error building Keynote API url", e);
            throw new TaskExecutionException("Keynote task execution failed", e);
        } catch (IOException e) {
            logger.error("IO exception fetching data from Keynote API", e);
            throw new TaskExecutionException("Keynote task execution failed", e);
        }
    }

    public static void main(String[] argv) throws Exception {
        Map<String, String> executeParams = new HashMap<String, String>();
        executeParams.put("api_key", "c05f56b6-2ca8-3765-afc6-92745cb9709b");
        executeParams.put("exclude_slots", "");
        new KeynoteMonitor().execute(executeParams, null);
    }
}
