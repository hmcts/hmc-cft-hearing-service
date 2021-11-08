provider "azurerm" {
  features {}
}

terraform {
  required_version = ">= 0.15"  # Terraform client version

  backend "azurerm" {}

  required_providers {
    azuread = {
      source  = "hashicorp/azuread"
      version = "1.6.0"
     }
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 2.57.0"       # AzureRM provider version
    }
  }
}
