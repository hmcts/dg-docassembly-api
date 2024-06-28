provider "azurerm" {
  features {}
}

locals {
  app_full_name     = "${var.product}-${var.component}"
  ase_name          = "core-compute-${var.env}"
  local_env         = (var.env == "preview" || var.env == "spreview") ? (var.env == "preview") ? "aat" : "saat" : var.env
  shared_vault_name = "${var.shared_product_name}-${local.local_env}"
  previewEnv        = "aat"
  nonPreviewEnv     = var.env

  local_ase    = (var.env == "preview" || var.env == "spreview") ? (var.env == "preview") ? "core-compute-aat" : "core-compute-saat" : local.ase_name
  s2s_base_uri = "http://${var.s2s_name}-${local.local_env}.service.${local.local_ase}.internal"
  tags         = var.common_tags
}

resource "azurerm_resource_group" "rg" {
  name     = "${var.product}-${var.component}-${var.env}"
  location = var.location
  tags     = local.tags
}

module "local_key_vault" {
  source                  = "git@github.com:hmcts/cnp-module-key-vault?ref=master"
  product                 = local.app_full_name
  env                     = var.env
  tenant_id               = var.tenant_id
  object_id               = var.jenkins_AAD_objectId
  resource_group_name     = azurerm_resource_group.rg.name
  product_group_object_id = "5d9cd025-a293-4b97-a0e5-6f43efce02c0"
  common_tags             = var.common_tags
  create_managed_identity = true
}

provider "vault" {
  address = "https://vault.reform.hmcts.net:6200"
}

data "azurerm_key_vault" "shared_key_vault" {
  name                = local.shared_vault_name
  resource_group_name = local.shared_vault_name
}

data "azurerm_key_vault_secret" "docmosis_access_key" {
  name         = "docmosis-access-key"
  key_vault_id = data.azurerm_key_vault.shared_key_vault.id
}

data "azurerm_key_vault_secret" "docmosis_templates_auth" {
  name         = "docmosis-templates-auth"
  key_vault_id = data.azurerm_key_vault.shared_key_vault.id
}

data "azurerm_key_vault" "s2s_vault" {
  name                = "s2s-${local.local_env}"
  resource_group_name = "rpe-service-auth-provider-${local.local_env}"
}

data "azurerm_key_vault_secret" "s2s_key" {
  name         = "microservicekey-dg-docassembly-api"
  key_vault_id = data.azurerm_key_vault.s2s_vault.id
}

data "azurerm_key_vault" "product" {
  name                = "${var.shared_product_name}-${var.env}"
  resource_group_name = "${var.shared_product_name}-${var.env}"
}

# Copy s2s key from shared to local vault
data "azurerm_key_vault" "local_key_vault" {
  name                = module.local_key_vault.key_vault_name
  resource_group_name = azurerm_resource_group.rg.name
}

resource "azurerm_key_vault_secret" "local_s2s_key" {
  name         = "microservicekey-dg-docassembly-api"
  value        = data.azurerm_key_vault_secret.s2s_key.value
  key_vault_id = data.azurerm_key_vault.local_key_vault.id
}

# Copy docmosis keys to local
resource "azurerm_key_vault_secret" "local_docmosis_access_key" {
  name         = "docmosis-access-key"
  value        = data.azurerm_key_vault_secret.docmosis_access_key.value
  key_vault_id = data.azurerm_key_vault.local_key_vault.id
}

resource "azurerm_key_vault_secret" "local_docmosis_templates_auth" {
  name         = "docmosis-templates-auth"
  value        = data.azurerm_key_vault_secret.docmosis_templates_auth.value
  key_vault_id = data.azurerm_key_vault.local_key_vault.id
}

# Load AppInsights key from rpa vault
data "azurerm_key_vault_secret" "app_insights_key" {
  name         = "EmAppInsightsInstrumentationKey"
  key_vault_id = data.azurerm_key_vault.product.id
}

resource "azurerm_key_vault_secret" "local_app_insights_key" {
  name         = "AppInsightsInstrumentationKey"
  value        = data.azurerm_key_vault_secret.app_insights_key.value
  key_vault_id = data.azurerm_key_vault.local_key_vault.id
}

data "azurerm_key_vault_secret" "app_insights_connection_string" {
  name         = "em-app-insights-connection-string"
  key_vault_id = data.azurerm_key_vault.product.id
}

resource "azurerm_key_vault_secret" "local_app_insights_connection_string" {
  name         = "app-insights-connection-string"
  value        = data.azurerm_key_vault_secret.app_insights_connection_string.value
  key_vault_id = data.azurerm_key_vault.local_key_vault.id
}

