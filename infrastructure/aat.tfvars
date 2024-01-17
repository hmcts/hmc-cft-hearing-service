db_replicas = [ "replica01" ]


# PG Flexible Server SKU
pgsql_sku = "GP_Standard_D2s_v3"

# PG Flexible Server replica enable for this env (AAT)
enable_replica = true

# Source PG Flexible Server FQDN for AAT replication
primary_server_fqdn = "hmc-cft-hearing-service-postgres-db-v15-aat.postgres.database.azure.com"