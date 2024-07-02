db_replicas  = ["replica01"]
sku_name     = "GP_Gen5_4"
sku_capacity = "4"

# PG Flexible Server SKU
pgsql_sku = "GP_Standard_D16s_v3"

# PG Flexible Server replica enable for this env (Prod)
enable_replica = true

# Source PG Flexible Server resource id for Prod replication
#Format: "/subscriptions/{sub-id}/resourceGroups/{rg-name}/providers/Microsoft.DBforPostgreSQL/flexibleServers/{primary-server-name}"
primary_server_id = "/subscriptions/8999dec3-0104-4a27-94ee-6588559729d1/resourceGroups/hmc-cft-hearing-service-postgres-db-v15-data-prod/providers/Microsoft.DBforPostgreSQL/flexibleServers/hmc-cft-hearing-service-postgres-db-v15-prod"
