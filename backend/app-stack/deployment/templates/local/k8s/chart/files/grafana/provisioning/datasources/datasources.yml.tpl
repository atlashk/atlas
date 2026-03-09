apiVersion: 1

datasources:
{{- if eq .Values.appStack.observability.logging.stack "loki" }}
  - name: Loki
    type: loki
    access: proxy
    url: http://loki:3100
    uid: DS_LOKI
    isDefault: true
    editable: true
    jsonData:
      maxLines: 1000
    secureJsonData: { }
{{- end }}
{{- if eq .Values.appStack.observability.metrics "prometheus" }}
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    uid: DS_PROMETHEUS
    editable: true
    jsonData:
      timeInterval: 5s
      queryTimeout: 60s
      httpMethod: POST
    secureJsonData: { }
{{- end }}
