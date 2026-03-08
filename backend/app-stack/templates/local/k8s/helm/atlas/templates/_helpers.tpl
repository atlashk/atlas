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
