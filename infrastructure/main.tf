provider "azurerm" {
  features {}
}

locals {
  app_full_name = "${var.product}-${var.component}"

  // Shared Resource Group
  sharedResourceGroup = "${var.raw_product}-shared-${var.env}"

  // Vault name
  vaultName = "${var.raw_product}-${var.env}"
}

data "azurerm_key_vault" "hmc_shared_key_vault" {
  name                = "${local.vaultName}"
  resource_group_name = "${local.sharedResourceGroup}"
}

module "hmc-hearing-management-db" {
  source                = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product               = var.product
  component             = var.component
  name                  = "${local.app_full_name}-postgres-db-v11"
  location              = var.location
  env                   = var.env
  subscription          = var.subscription
  postgresql_user       = var.postgresql_user
  postgresql_version    = var.postgresql_version
  database_name         = var.database_name
  sku_name              = var.sku_name
  sku_tier              = var.sku_tier
  sku_capacity          = var.sku_capacity
  ssl_enforcement       = var.ssl_enforcement
  storage_mb            = var.storage_mb
  backup_retention_days = var.backup_retention_days
  georedundant_backup   = var.georedundant_backup
  common_tags           = var.common_tags
}

////////////////////////////////
// Populate Vault with DB info
////////////////////////////////

resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  name         = "${var.component}-POSTGRES-USER"
  value        = module.hmc-hearing-management-db.user_name
  key_vault_id = data.azurerm_key_vault.hmc_shared_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  name         = "${var.component}-POSTGRES-PASS"
  value        = module.hmc-hearing-management-db.postgresql_password
  key_vault_id = data.azurerm_key_vault.hmc_shared_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-HOST" {
  name         = "${var.component}-POSTGRES-HOST"
  value        = module.hmc-hearing-management-db.host_name
  key_vault_id = data.azurerm_key_vault.hmc_shared_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-PORT" {
  name         = "${var.component}-POSTGRES-PORT"
  value        = module.hmc-hearing-management-db.postgresql_listen_port
  key_vault_id = data.azurerm_key_vault.hmc_shared_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-DATABASE" {
  name         = "${var.component}-POSTGRES-DATABASE"
  value        = module.hmc-hearing-management-db.postgresql_database
  key_vault_id = data.azurerm_key_vault.hmc_shared_key_vault.id
}
