# Server configuration for Promtail
server:
  http_listen_port: 9080  # HTTP port for metrics and health checks
  grpc_listen_port: 0     # Disable gRPC server (not needed for this setup)

# Position file tracks the last read position in log files
# This prevents re-reading logs after Promtail restarts
positions:
  filename: /run/promtail/positions.yaml

# Loki client configuration - where to send the collected logs
clients:
  - url: http://loki:3100/loki/api/v1/push
    external_labels:
      cluster: kubernetes

limits_config:
  readline_rate: 10000
  readline_burst: 20000

scrape_configs:
  # Primary job: Collect logs from Kubernetes pods using service discovery
  - job_name: kubernetes-pods
    # Use Kubernetes service discovery to find pods automatically
    kubernetes_sd_configs:
      - role: pod  # Discover all pods in the cluster
    # Relabel configs transform Kubernetes metadata into Promtail labels
    relabel_configs:
      # namespace
      - source_labels:
          - __meta_kubernetes_namespace
        target_label: namespace
      # pod
      - source_labels:
          - __meta_kubernetes_pod_name
        target_label: pod
      # container
      - source_labels:
          - __meta_kubernetes_pod_container_name
        target_label: container
      # node
      - source_labels:
          - __meta_kubernetes_pod_node_name
        target_label: node
      # app label
      - source_labels:
          - __meta_kubernetes_pod_label_app
        target_label: app
        regex: (.+)
        replacement: $1
      # fallback to controller name
      - source_labels:
          - __meta_kubernetes_pod_controller_name
        target_label: app
        regex: (.+)
        replacement: $1
        action: replace
      # log path
      - replacement: /var/log/pods/*$1/*.log
        separator: /
        source_labels:
          - __meta_kubernetes_pod_uid
          - __meta_kubernetes_pod_container_name
        target_label: __path__
    pipeline_stages:
      # Parse CRI container logs
      - cri: {}
      # Multiline for Java stacktraces
      - multiline:
          firstline: '^\d{4}-\d{2}-\d{2}'
      # Drop health check logs
      - drop:
          expression: ".*actuator/health.*"
      # Extract log level
      - regex:
          expression: '^\S+\s+(?P<level>TRACE|DEBUG|INFO|WARN|ERROR)'
      # Add level label
      - labels:
          level:
      # Final log output
      - output:
          source: log

  # fallback job
  - job_name: kubernetes-pods-fallback
    static_configs:
      - targets:
          - localhost
        labels:
          job: containerlogs
          __path__: /var/log/pods/*/*/*.log
    pipeline_stages:
      - cri: {}
      - regex:
          source: filename
          expression: \/var\/log\/pods\/(?P<namespace>[^_]+)_(?P<pod>[^_]+)_[^\/]+\/(?P<container>[^\/]+)\/.*
      - labels:
          namespace:
          pod:
          container: