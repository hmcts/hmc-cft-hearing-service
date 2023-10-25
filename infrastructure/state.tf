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
      version = "1.6.0"
     }
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.76.0"       # AzureRM provider version
    }
  }
}
