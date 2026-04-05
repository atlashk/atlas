server:
  http_listen_port: {{ .Values.tempo.service.ports.http }}
  grpc_listen_port: {{ .Values.tempo.service.ports.grpc }}

distributor:
  receivers:
    otlp:
      protocols:
        grpc:
          endpoint: 0.0.0.0:{{ .Values.tempo.service.ports.otlpGrpc }}
        http:
          endpoint: 0.0.0.0:{{ .Values.tempo.service.ports.otlpHttp }}

storage:
  trace:
    backend: local
    local:
      path: /var/tempo/blocks
    wal:
      path: /var/tempo/wal
