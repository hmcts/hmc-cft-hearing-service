provider "azurerm" {
  features {}
  skip_provider_registration = true
  alias                      = "postgres_network"
  subscription_id            = var.aks_subscription_id
}

terraform {
  backend "azurerm" {}

  required_providers {
    azuread = {
      source  = "hashicorp/azuread"
      version = "2.48.0"
    }
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.99.0" # AzureRM provider version
    }
  }
}
