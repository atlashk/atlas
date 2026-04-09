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

{{/*
Helpers to build data-layer service hostnames from .Values.dataReleaseName
(atlas-data chart: mysql, postgres, redis, kafka, rabbitmq, elasticsearch, keycloak, minio, qdrant, smtp4dev)
*/}}
{{- define "atlas.infra.mysql" -}}
{{- printf "%s-mysql" .Values.dataReleaseName -}}
{{- end -}}

{{- define "atlas.infra.postgres" -}}
{{- printf "%s-postgres" .Values.dataReleaseName -}}
{{- end -}}

{{- define "atlas.infra.redis" -}}
{{- printf "%s-redis" .Values.dataReleaseName -}}
{{- end -}}

{{- define "atlas.infra.kafka" -}}
{{- printf "%s-kafka" .Values.dataReleaseName -}}
{{- end -}}

{{- define "atlas.infra.rabbitmq" -}}
{{- printf "%s-rabbitmq" .Values.dataReleaseName -}}
{{- end -}}

{{- define "atlas.infra.elasticsearch" -}}
{{- printf "%s-elasticsearch" .Values.dataReleaseName -}}
{{- end -}}

{{- define "atlas.infra.keycloak" -}}
{{- printf "%s-keycloak" .Values.dataReleaseName -}}
{{- end -}}

{{- define "atlas.infra.minio" -}}
{{- printf "%s-minio" .Values.dataReleaseName -}}
{{- end -}}

{{/*
Helpers to build observability service hostnames from .Values.observabilityReleaseName
(atlas-observability chart: zipkin, otel-collector, tempo, loki, prometheus, grafana)
*/}}
{{- define "atlas.infra.zipkin" -}}
{{- printf "%s-zipkin" .Values.observabilityReleaseName -}}
{{- end -}}

{{- define "atlas.infra.otelCollector" -}}
{{- printf "%s-otel-collector" .Values.observabilityReleaseName -}}
{{- end -}}
