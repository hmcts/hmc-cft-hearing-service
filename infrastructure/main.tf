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
  name                = local.vaultName
  resource_group_name = local.sharedResourceGroup
}

module "hmc-hearing-management-db" {
  source                = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product               = var.product
  component             = var.component
  name                  = "${local.app_full_name}-postgres-db"
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
  replicas              = var.db_replicas
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

module "postgresql_v15" {
  source = "git@github.com:hmcts/terraform-module-postgresql-flexible?ref=master"
  providers = {
    azurerm.postgres_network = azurerm.postgres_network
  }

  subnet_suffix = "expanded"

  admin_user_object_id = var.jenkins_AAD_objectId
  business_area        = "cft"
  common_tags          = var.common_tags
  component            = var.component
  env                  = var.env
  pgsql_databases = [
    {
      name = var.database_name
    }
  ]
  pgsql_server_configuration = [
    {
      name  = "azure.extensions"
      value = "plpgsql,pg_stat_statements,pg_buffercache,hypopg"
    }
  ]
  pgsql_version    = "15"
  product          = var.product
  name             = "${local.app_full_name}-postgres-db-v15"
  pgsql_sku        = var.pgsql_sku
  pgsql_storage_mb = var.pgsql_storage_mb
}

resource "azurerm_key_vault_secret" "POSTGRES-USER-V15" {
  name         = "${var.component}-POSTGRES-USER-V15"
  value        = module.postgresql_v15.username
  key_vault_id = data.azurerm_key_vault.hmc_shared_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS-V15" {
  name         = "${var.component}-POSTGRES-PASS-V15"
  value        = module.postgresql_v15.password
  key_vault_id = data.azurerm_key_vault.hmc_shared_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-HOST-V15" {
  name         = "${var.component}-POSTGRES-HOST-V15"
  value        = module.postgresql_v15.fqdn
  key_vault_id = data.azurerm_key_vault.hmc_shared_key_vault.id
}
