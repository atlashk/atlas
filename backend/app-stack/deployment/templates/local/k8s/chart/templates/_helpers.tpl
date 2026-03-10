{{- define "hasGrafana" -}}
{{- $observability := .Values.appStack.observability | default dict -}}
{{- if or 
      (eq ($observability.metrics | default "") "prometheus")
      (eq (($observability.logging | default dict).stack | default "") "loki")
      (eq ($observability.tracing | default "") "zipkin")
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
