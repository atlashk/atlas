receivers:
  otlp:
    protocols:
      grpc:
        endpoint: 0.0.0.0:{{ .Values.global.otelCollector.service.ports.otlpGrpc }}
      http:
        endpoint: 0.0.0.0:{{ .Values.global.otelCollector.service.ports.otlpHttp }}
processors:
  batch:
exporters:
  otlp:
    endpoint: tempo:{{ .Values.global.tempo.service.ports.otlpGrpc }}
    tls:
      insecure: true
  debug:
    verbosity: basic
extensions:
  health_check:
service:
  extensions: [health_check]
  pipelines:
    traces:
      receivers: [otlp]
      processors: [batch]
      exporters: [otlp, debug]
