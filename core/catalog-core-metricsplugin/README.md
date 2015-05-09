#Importing Kibana Dashboards

*Instructions pre-[Kibana 4.1](https://github.com/elastic/kibana/issues/1552)*

Install [elasticdump](https://github.com/taskrabbit/elasticsearch-dump) using npm.

```
npm install elasticdump -g
```

Import dashboards and visualizations to Elasticsearch used by Kibana 4.

```
elasticdump --input=src/main/resources/kibana.json \
    --output=http://localhost:9200/.kibana \
    --type=data
```

See [Kibana 4 - Import and Export Visualizations and Dashboards with Elasticdump](http://air.ghost.io/kibana-4-export-and-import-visualizations-and-dashboards/) for more details.