FROM postgres:15
USER postgres

ENV TZ="UTC"
RUN date

COPY init-db.sh /docker-entrypoint-initdb.d

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD psql -c 'select 1' -d hmc_cft_hearing_service -U ${CFT_HEARING_SERVICE_DB_USERNAME}

EXPOSE 5432
