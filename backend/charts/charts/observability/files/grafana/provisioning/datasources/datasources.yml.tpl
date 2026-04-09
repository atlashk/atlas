apiVersion: 1

datasources:
{{- if eq .Values.global.appStack.observability.logging.stack "loki" }}
  - name: Loki
    type: loki
    access: proxy
    url: http://{{ lower $.Release.Name }}-loki:3100
    uid: DS_LOKI
    isDefault: true
    editable: true
    jsonData:
      maxLines: 1000
    secureJsonData: { }
{{- end }}
{{- if eq .Values.global.appStack.observability.metrics "prometheus" }}
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://{{ lower $.Release.Name }}-prometheus:9090
    uid: DS_PROMETHEUS
    editable: true
    jsonData:
      timeInterval: 5s
      queryTimeout: 60s
      httpMethod: POST
    secureJsonData: { }
{{- end }}
{{- if eq .Values.global.appStack.observability.tracing "tempo" }}
  - name: Tempo
    type: tempo
    access: proxy
    url: http://{{ lower $.Release.Name }}-tempo:3200
    uid: DS_TEMPO
    editable: true
    jsonData:
      httpMethod: GET
    secureJsonData: { }
{{- end }}
