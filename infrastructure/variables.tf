variable "product" {
  default = "dg"
}

variable "shared_product_name" {
  default = "rpa"
}

variable "component" {
}

variable "team_name" {
  default = "evidence"
}

variable "app_language" {
  default = "java"
}

variable "location" {
  default = "UK South"
}

variable "env" {
}

variable "subscription" {
}

variable "open_id_api_base_uri" {
  default = "idam-api"
}

variable "oidc_issuer_base_uri" {
  default = "idam-api"
}

variable "tenant_id" {}

variable "jenkins_AAD_objectId" {
  description = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable "common_tags" {
  type = map(string)
}
////////////////////////////////////////////////
//Addtional Vars ///////////////////////////////
////////////////////////////////////////////////
variable "capacity" {
  default = "1"
}

variable "java_opts" {
  default = ""
}
////////////////////////////////////////////////
// Endpoints
////////////////////////////////////////////////
variable "idam_api_base_uri" {
  default = "http://betaDevBccidamAppLB.reform.hmcts.net:80"
}

variable "s2s_name" {
  default = "rpe-service-auth-provider"
}

variable "docmosis_uri" {
  default = "https://docmosis-development.platform.hmcts.net/rs/render"
}

variable "docmosis_templates_uri" {
  default = "https://docmosis-development.platform.hmcts.net"
}

////////////////////////////////////////////////
// Logging
////////////////////////////////////////////////

variable "json_console_pretty_print" {
  default = "false"
}

variable "log_output" {
  default = "single"
}

variable "root_logging_level" {
  default = "INFO"
}

variable "log_level_spring_web" {
  default = "INFO"
}

variable "log_level_dm" {
  default = "INFO"
}

variable "show_sql" {
  default = "true"
}

variable "endpoints_health_sensitive" {
  default = "true"
}

variable "endpoints_info_sensitive" {
  default = "true"
}
////////////////////////////////////////////////
// Toggle Features
////////////////////////////////////////////////
variable "enable_idam_healthcheck" {
  default = "false"
}

variable "enable_s2s_healthcheck" {
  default = "false"
}

variable "enable_template_rendition_endpoint" {
  default = "true"
}

////////////////////////////////////////////////
// Whitelists
////////////////////////////////////////////////
variable "s2s_names_whitelist" {
  default = "em_api,em_gw,ccd_gw,ccd_data,sscs,divorce_document_upload,divorce_document_generator,probate_backend,jui_webapp,pui_webapp,cmc_claim_store"
}

variable "case_worker_roles" {
  default = "caseworker-probate,caseworker-cmc,caseworker-sscs,caseworker-divorce"
}
////////////////////////////////////////////////
// Addtional
////////////////////////////////////////////////
variable "dm_store_app_url" {
  default = "dm-store"
}

variable "dg_template_management_api" {
  default = "dg-tmpl-mgmt"
}
variable "dns_server" {
}

variable "managed_identity_object_id" {
  default = ""
}
