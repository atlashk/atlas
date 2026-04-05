{{- define "hasGrafana" -}}
{{- $observability := .Values.appStack.observability | default dict -}}
{{- if or 
      (eq ($observability.metrics | default "") "prometheus")
      (eq (($observability.logging | default dict).stack | default "") "loki")
      (eq ($observability.tracing | default "") "tempo")
}}
true
{{- end -}}
{{- end -}}

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
nodeSelector for the application node group.
Used by all microservice Deployments.
*/}}
{{- define "atlas.nodeSelector.application" -}}
nodeSelector:
  role: application
{{- end -}}

{{/*
nodeSelector + toleration for the infrastructure node group.
Used by all stateful service StatefulSets/Deployments.
The toleration matches the taint: dedicated=infrastructure:NoSchedule.
*/}}
{{- define "atlas.nodeSelector.infrastructure" -}}
nodeSelector:
  role: infrastructure
tolerations:
  - key: dedicated
    operator: Equal
    value: infrastructure
    effect: NoSchedule
{{- end -}}

{{/*
Toleration-only for the infrastructure node group.
Used by DaemonSets (e.g. Promtail) that must run on ALL nodes,
including infrastructure nodes which carry the NoSchedule taint.
*/}}
{{- define "atlas.tolerations.infrastructure" -}}
tolerations:
  - key: dedicated
    operator: Equal
    value: infrastructure
    effect: NoSchedule
{{- end -}}
