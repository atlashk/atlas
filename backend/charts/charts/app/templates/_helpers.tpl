{{/*
FQDN helpers for cross-namespace service resolution.
Services in the infra/security/observability namespaces are not reachable from the
app namespace using short names; a fully-qualified domain name is required.
*/}}
{{- define "mysql.hostname" -}}mysql.{{ .Values.global.namespaces.infra }}.svc.cluster.local{{- end -}}
{{- define "postgres.hostname" -}}postgres.{{ .Values.global.namespaces.infra }}.svc.cluster.local{{- end -}}
{{- define "redis.hostname" -}}redis.{{ .Values.global.namespaces.infra }}.svc.cluster.local{{- end -}}
{{- define "kafka.hostname" -}}kafka.{{ .Values.global.namespaces.infra }}.svc.cluster.local{{- end -}}
{{- define "rabbitmq.hostname" -}}rabbitmq.{{ .Values.global.namespaces.infra }}.svc.cluster.local{{- end -}}
{{- define "elasticsearch.hostname" -}}elasticsearch.{{ .Values.global.namespaces.infra }}.svc.cluster.local{{- end -}}
{{- define "minio.hostname" -}}minio.{{ .Values.global.namespaces.infra }}.svc.cluster.local{{- end -}}
{{- define "qdrant.hostname" -}}qdrant.{{ .Values.global.namespaces.infra }}.svc.cluster.local{{- end -}}
{{- define "keycloak.hostname" -}}keycloak.{{ .Values.global.namespaces.security }}.svc.cluster.local{{- end -}}
{{- define "zipkin.hostname" -}}zipkin.{{ .Values.global.namespaces.observability }}.svc.cluster.local{{- end -}}
{{- define "otelCollector.hostname" -}}otel-collector.{{ .Values.global.namespaces.observability }}.svc.cluster.local{{- end -}}

{{- define "atlas.waitFor.initContainers" -}}
{{- $deps := .deps | default list -}}
{{- if gt (len $deps) 0 -}}
initContainers:
  - name: wait-for-dependencies
    image: busybox:1.36.1
    imagePullPolicy: IfNotPresent
    command:
      - /bin/sh
      - -ec
      - |
        wait_for() {
          host="$1"
          port="$2"
          name="$3"
          echo "Waiting for ${name} (${host}:${port})"
          timeout 600 sh -c "until nc -z -w 2 ${host} ${port}; do sleep 2; done"
        }
        {{- range $dep := $deps }}
        wait_for {{ $dep.host | quote }} {{ $dep.port | quote }} {{ $dep.name | quote }}
        {{- end }}
{{- end -}}
{{- end -}}
