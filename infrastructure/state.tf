terraform {
  backend "azurerm" {}

  required_providers {
    azuread = {
      source  = "hashicorp/azuread"
      version = "2.22.0"
    }
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.9.0"
    }
   }
}