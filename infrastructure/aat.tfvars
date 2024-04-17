db_replicas = ["replica01"]

# PG Flexible Server SKU
pgsql_sku = "GP_Standard_D2s_v3"

# PG Flexible Server replica enable for this env (AAT)
enable_replica = true

# Source PG Flexible Server resource id for AAT replication
#Format: "/subscriptions/{sub-id}/resourceGroups/{rg-name}/providers/Microsoft.DBforPostgreSQL/flexibleServers/{primary-server-name}"
primary_server_id = "/subscriptions/1c4f0704-a29e-403d-b719-b90c34ef14c9/resourceGroups/hmc-cft-hearing-service-postgres-db-v15-data-aat/providers/Microsoft.DBforPostgreSQL/flexibleServers/hmc-cft-hearing-service-postgres-db-v15-aat"
