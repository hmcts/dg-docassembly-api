
output "idam_api_base_uri" {
  value = var.idam_api_base_uri
}

output "open_id_api_base_uri" {
  value = var.open_id_api_base_uri
}

output "oidc_issuer_base_uri" {
  value = var.oidc_issuer_base_uri
}

output "s2s_base_uri" {
  value = "http://${var.s2s_name}-${local.local_env}.service.core-compute-${local.local_env}.internal"
}

output "idam_webshow_whitelist" {
  value = "https://em-show-aat.service.core-compute-aat.internal/oauth2/callback"
}

output "enable_idam_health_check" {
  value = var.enable_idam_healthcheck
}

output "enable_idam_healthcheck" {
  value = var.enable_idam_healthcheck
}

output "enable_form_definition_endpoint" {
  value = var.enable_form_definition_endpoint
}

output "enable_template_rendition_endpoint" {
  value = var.enable_template_rendition_endpoint
}
