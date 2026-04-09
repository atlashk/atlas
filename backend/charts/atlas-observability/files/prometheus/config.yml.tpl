global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'user-service-metrics'
    static_configs:
      - targets: [ '{{ .Values.appReleaseName }}-user-service:8081' ]
    metrics_path: '/actuator/prometheus'
  - job_name: 'catalog-service-metrics'
    static_configs:
      - targets: [ '{{ .Values.appReleaseName }}-catalog-service:8082' ]
    metrics_path: '/actuator/prometheus'
  - job_name: 'inventory-service-metrics'
    static_configs:
      - targets: [ '{{ .Values.appReleaseName }}-inventory-service:8083' ]
    metrics_path: '/actuator/prometheus'
  - job_name: 'order-service-metrics'
    static_configs:
      - targets: [ '{{ .Values.appReleaseName }}-order-service:8084' ]
    metrics_path: '/actuator/prometheus'
  - job_name: 'payment-service-metrics'
    static_configs:
      - targets: [ '{{ .Values.appReleaseName }}-payment-service:8085' ]
    metrics_path: '/actuator/prometheus'
  {{- if eq .Values.appStack.idp "spring" }}
  - job_name: 'authorization-server-metrics'
    static_configs:
      - targets: [ '{{ .Values.appReleaseName }}-authorization-server:8901' ]
    metrics_path: '/actuator/prometheus'
  {{- end }}
  - job_name: 'api-gateway-metrics'
    static_configs:
      - targets: [ '{{ .Values.appReleaseName }}-api-gateway:8080' ]
    metrics_path: '/actuator/prometheus'
