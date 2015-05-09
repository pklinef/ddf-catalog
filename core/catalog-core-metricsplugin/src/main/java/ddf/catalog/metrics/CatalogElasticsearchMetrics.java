/**
 * Copyright (c) Codice Foundation
 * 
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 * 
 **/
package ddf.catalog.metrics;

import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Shape;
import ddf.catalog.filter.FilterAdapter;
import ddf.catalog.operation.CreateResponse;
import ddf.catalog.operation.DeleteResponse;
import ddf.catalog.operation.ProcessingDetails;
import ddf.catalog.operation.QueryRequest;
import ddf.catalog.operation.QueryResponse;
import ddf.catalog.operation.ResourceResponse;
import ddf.catalog.operation.UpdateResponse;
import ddf.catalog.plugin.PluginExecutionException;
import ddf.catalog.plugin.PostIngestPlugin;
import ddf.catalog.plugin.PostQueryPlugin;
import ddf.catalog.plugin.PostResourcePlugin;
import ddf.catalog.plugin.PreQueryPlugin;
import ddf.catalog.plugin.StopProcessingException;
import ddf.catalog.source.UnsupportedQueryException;
import ddf.security.SubjectUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.codice.ddf.configuration.ConfigurationManager;
import org.codice.ddf.configuration.ConfigurationWatcher;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.geotools.filter.text.cql2.CQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Catalog plug-in to capture metrics on catalog operations.
 * 
 */
public final class CatalogElasticsearchMetrics implements PreQueryPlugin, PostQueryPlugin, PostIngestPlugin,
        PostResourcePlugin, ConfigurationWatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogElasticsearchMetrics.class);

    public static final String CATALOG_METRICS = "catalog-metrics";

    private static final String UNKNOWN_USER = "UNKNOWN";

    private final SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private final SimpleDateFormat indexDateFormat = new SimpleDateFormat("yyyy-MM");

    private Client client;

    private FilterAdapter filterAdapter;

    private String localSourceId;

    public CatalogElasticsearchMetrics(FilterAdapter filterAdapter) throws URISyntaxException, IOException {
        this.filterAdapter = filterAdapter;

        TimeZone tz = TimeZone.getTimeZone("UTC");
        timestampFormat.setTimeZone(tz);
        indexDateFormat.setTimeZone(tz);

        LOGGER.info("Connecting to ES");
        client = new TransportClient().addTransportAddress(new InetSocketTransportAddress("localhost", 9300));

        LOGGER.info("Reading catalog metrics mapping");
        String catalogMetricsMapping = IOUtils.toString(CatalogElasticsearchMetrics.class.getResourceAsStream("/catalog_metrics_mapping.json"));

        LOGGER.info("Updating catalog metrics template");
        client.admin().indices().putTemplate(new PutIndexTemplateRequest(CATALOG_METRICS + "-template")
                .template(CATALOG_METRICS + "*")
                .mapping("_default_", catalogMetricsMapping)).actionGet();
    }

    // PostQuery
    @Override
    public QueryResponse process(QueryResponse input) throws PluginExecutionException,
        StopProcessingException {

        recordSourceQueryExceptions(input);

        QueryRequest request = input.getRequest();
        boolean cacheQuery = "cache".equals(input.getRequest().getProperties().get("mode"));
        if (!cacheQuery) {
            boolean federated = isFederated(request);

            QueryTypeFilterDelegate queryType = new QueryTypeFilterDelegate();
            try {
                filterAdapter.adapt(request.getQuery(), queryType);

                List<Shape> shapes = queryType.getShapes();

                List<String> features = getFeatures(queryType);
                if (federated) {
                    features.add("federated");
                }

                String cql = CQL.toCQL(request.getQuery());

                String user = SubjectUtils.getName(SecurityUtils.getSubject(), UNKNOWN_USER);

                // TODO get site name from platform settings

                LOGGER.info("Adding metrics to ES");
                Date date = new Date();
                client.prepareIndex(getIndexName(date), "query").setSource(
                        jsonBuilder().startObject()
                                .field("cql", cql)
                                .field("location", getGeoPoints(shapes))
                                .field("features", features)
                                .field("property_names", queryType.getProperties())
                                .field("sources", request.getSourceIds())
                                .field("terms", queryType.getTerms())
                                .field("user", user)
                                .field("hits_long", input.getHits())
                                .field("get_hits_bool", request.getQuery().requestsTotalResultsCount())
                                .field("timestamp_date", timestampFormat.format(date))
                                .field("federated_bool", federated)
                                .field("comparison_bool", queryType.isComparison())
                                .field("spatial_bool", queryType.isSpatial())
                                .field("fuzzy_bool", queryType.isFuzzy())
                                .field("xpath_bool", queryType.isXpath())
                                .field("logical_bool", queryType.isLogical())
                                .field("case_sensitive_bool", queryType.isCaseSensitive())
                                .field("temporal_bool", queryType.isTemporal())
                                .endObject()
                ).execute().actionGet();
            } catch (UnsupportedQueryException | IOException e) {
                // ignore filters not supported by the QueryTypeFilterDelegate
            }
        }

        return input;
    }

    private String getIndexName(Date date) {
        return CATALOG_METRICS + "-" + indexDateFormat.format(date);
    }

    private List<String> getFeatures(QueryTypeFilterDelegate queryType) {
        List<String> features = new ArrayList<>();

        if (queryType.isComparison()) {
            features.add("comparison");
        }
        if (queryType.isSpatial()) {
            features.add("spatial");
        }
        if (queryType.isFuzzy()) {
            features.add("fuzzy");
        }
        if (queryType.isXpath()) {
            features.add("xpath");
        }
        if (queryType.isLogical()) {
            features.add("logical");
        }
        if (queryType.isCaseSensitive()) {
            features.add("casesensitive");
        }
        if (queryType.isTemporal()) {
            features.add("temporal");
        }
        return features;
    }

    // PreQuery
    @Override
    public QueryRequest process(QueryRequest input) throws PluginExecutionException,
        StopProcessingException {

        return input;
    }

    private List<String> getGeoPoints(List<Shape> shapes) {
        List<String> points = new ArrayList<>();

        for (Shape shape : shapes) {
            Point center = shape.getCenter();
            points.add(center.getY() + "," + center.getX());
        }

        return points;
    }

    // PostCreate
    @Override
    public CreateResponse process(CreateResponse input) throws PluginExecutionException {
        // TODO
        return input;
    }

    // PostUpdate
    @Override
    public UpdateResponse process(UpdateResponse input) throws PluginExecutionException {
        // TODO
        return input;
    }

    // PostDelete
    @Override
    public DeleteResponse process(DeleteResponse input) throws PluginExecutionException {
        // TODO
        return input;
    }

    // PostResource
    @Override
    public ResourceResponse process(ResourceResponse input) throws PluginExecutionException,
        StopProcessingException {
        // TODO
        return input;
    }

    private void recordSourceQueryExceptions(QueryResponse response) {
        Set<ProcessingDetails> processingDetails = (Set<ProcessingDetails>) response
                .getProcessingDetails();

        if (processingDetails == null) {
            return;
        }

        for (ProcessingDetails next : processingDetails) {
            if (next != null && next.getException() != null) {
                Exception ex = next.getException();

                String name = ex.getClass().getName();
                String reason = ex.getMessage();
                // TODO
            }
        }
    }

    private boolean isFederated(QueryRequest queryRequest) {
        Set<String> sourceIds = queryRequest.getSourceIds();

        if (queryRequest.isEnterprise()) {
            return true;
        } else if (sourceIds == null) {
            return false;
        } else {
            return (sourceIds.size() > 1)
                    || (sourceIds.size() == 1 && !sourceIds.contains("")
                            && !sourceIds.contains(null) && !sourceIds.contains(localSourceId));
        }
    }

    @Override
    public void configurationUpdateCallback(Map<String, String> configuration) {
        if (configuration != null && !configuration.isEmpty()) {
            String siteName = configuration.get(ConfigurationManager.SITE_NAME);
            if (StringUtils.isNotBlank(siteName)) {
                localSourceId = siteName;
            }
        }
    }

    public void destroy() {
        if (client != null) {
            LOGGER.info("Disconnecting from ES");
            client.close();
        }
    }

}
