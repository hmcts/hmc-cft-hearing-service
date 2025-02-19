// Infrastructural variables
variable "product" {
}

variable "raw_product" {
  default = "hmc"
}

variable "component" {
}

variable "env" {
}

variable "location" {
  default = "UK South"
}

variable "common_tags" {
  type = map(string)
}

variable "tenant_id" {
  description = "(Required) The Azure Active Directory tenant ID that should be used for authenticating requests to the key vault. This is usually sourced from environment variables and not normally required to be specified."
}

variable "jenkins_AAD_objectId" {
  description = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable "subscription" {
}

////////////////////////////////
// Database
////////////////////////////////

variable "sku_tier" {
  default = "GeneralPurpose"
}

variable "storage_mb" {
  default = "51200"
}

variable "sku_capacity" {
  default = "2"
}

variable "ssl_enforcement" {
  default = "Enabled"
}

variable "backup_retention_days" {
  default = "35"
}

variable "georedundant_backup" {
  default = "Enabled"
}

variable "database_name" {
  default = "hmc_cft_hearing_service"
}

variable "db_replicas" {
  type    = list(string)
  default = []
}

variable "pgsql_sku" {
  description = "The PGSql flexible server instance sku"
  default     = "GP_Standard_D2s_v3"
}

variable "aks_subscription_id" {}

variable "pgsql_storage_mb" {
  description = "Max storage allowed for the PGSql Flexibile instance"
  type        = number
  default     = 65536
}
variable "enable_replica" {
  description = "Flag to enable the creation of a PostgreSQL Flexible server replica"
  type        = bool
  default     = false
}
variable "primary_server_id" {
  description = "Azure resource ID of the primary PostgreSQL server"
  type        = string
  default     = "not_applicable" // Dummy Value for none replica environments
}

variable "action_group_name" {
  description = "The name of the Action Group to create."
  type        = string
  default     = "hmc-support"
}

variable "email_address_key" {
  description = "Email address key in azure Key Vault."
  type        = string
  default     = "db-alert-monitoring-email-address"
}
