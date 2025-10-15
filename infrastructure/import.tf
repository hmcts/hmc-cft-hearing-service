removed {
  from = module.postgresql_v15_replica[0].azurerm_postgresql_flexible_server_active_directory_administrator.pgsql_adadmin

  lifecycle {
    destroy = false
  }
}
