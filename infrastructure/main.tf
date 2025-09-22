provider "azurerm" {
  features {
    resource_group {
      prevent_deletion_if_contains_resources = false
    }
  }
}

locals {
  app_full_name = "${var.product}-${var.component}"

  // Shared Resource Group
  sharedResourceGroup = "${var.raw_product}-shared-${var.env}"

  // Vault name
  vaultName = "${var.raw_product}-${var.env}"

  db_name = "${local.app_full_name}-postgres-db-v15"
}

data "azurerm_key_vault" "hmc_shared_key_vault" {
  name                = local.vaultName
  resource_group_name = local.sharedResourceGroup
}

//////////////////////////////////////
// Postgres DB info.                //
//////////////////////////////////////

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
      value = "pg_stat_statements,pg_buffercache,hypopg"
    }
  ]
  pgsql_version              = "15"
  product                    = var.product
  name                       = local.db_name
  pgsql_sku                  = var.pgsql_sku
  pgsql_storage_mb           = var.pgsql_storage_mb
  action_group_name          = join("-", [local.db_name, var.action_group_name])
  email_address_key          = var.email_address_key
  email_address_key_vault_id = data.azurerm_key_vault.hmc_shared_key_vault.id

  force_user_permissions_trigger = "1"
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

resource "azurerm_key_vault_secret" "POSTGRES-PORT" {
  name         = "${var.component}-POSTGRES-PORT"
  value        = "5432"
  key_vault_id = data.azurerm_key_vault.hmc_shared_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-DATABASE" {
  name         = "${var.component}-POSTGRES-DATABASE"
  value        = var.database_name
  key_vault_id = data.azurerm_key_vault.hmc_shared_key_vault.id
}

////////////////////////////////////////
// DB version 15 Replication          //
////////////////////////////////////////

module "postgresql_v15_replica" {
  source = "git@github.com:hmcts/terraform-module-postgresql-flexible?ref=master"
  count  = var.enable_replica ? 1 : 0
  providers = {
    azurerm.postgres_network = azurerm.postgres_network
  }

  subnet_suffix        = "expanded"
  admin_user_object_id = var.jenkins_AAD_objectId
  business_area        = "cft"
  common_tags          = var.common_tags
  component            = var.component
  env                  = var.env
  pgsql_databases      = [{ name = var.database_name }]
  pgsql_server_configuration = [
    {
      name  = "azure.extensions"
      value = "pg_stat_statements,pg_buffercache,hypopg"
    }
  ]
  pgsql_version       = "15"
  product             = var.product
  name                = "${local.app_full_name}-postgres-db-v15-replica"
  resource_group_name = "hmc-cft-hearing-service-postgres-db-v15-data-${var.env}"
  pgsql_sku           = var.pgsql_sku
  pgsql_storage_mb    = var.pgsql_storage_mb
  create_mode         = "Replica"
  source_server_id    = var.primary_server_id
  high_availability   = false

}
