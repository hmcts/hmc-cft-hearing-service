FROM postgres:11.8

COPY init-db.sh /docker-entrypoint-initdb.d

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD psql -c 'select 1' -d hmc_hearing_service -U ${HMC_HEARING_MANAGEMENT_DB_USERNAME}

EXPOSE 5432
