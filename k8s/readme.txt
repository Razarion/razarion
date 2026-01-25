gcloud container clusters get-credentials razarion-cluster --zone=us-central1

kubectl create configmap mariadb-custom-config --from-file=my-custom.cnf

# Secrets (in Lens erstellen):
#
# 1. mariadb-secrets
#    - root-password: <MariaDB Root Passwort>
#    - user-password: <MariaDB User Passwort für raz_user>
#
# 2. jwt-secrets
#    - keystore-password: <JWT Keystore Passwort>
#    - key-password: <JWT Key Passwort>
#
# 3. keystore-secret (bereits vorhanden)
#    - jwt-keystore.p12: <Keystore Datei>
#
# 4. grafana-loki-secrets (bereits vorhanden)
#    - grafana-loki-url
#    - grafana-loki-username
#    - grafana-loki-password

kubectl exec -it razarion-mariadb-0 -- mysql -u root -p<ROOT_PASSWORD> razarion -e "SET FOREIGN_KEY_CHECKS = 0; SELECT GROUP_CONCAT(table_name) INTO @tables FROM information_schema.tables WHERE table_schema = 'razarion' AND table_type = 'BASE TABLE'; SET @tables = CONCAT('DROP TABLE ', @tables); PREPARE stmt FROM @tables; EXECUTE stmt; DEALLOCATE PREPARE stmt; SET FOREIGN_KEY_CHECKS = 1;"

kubectl cp db_2025_11_23.sql razarion-mariadb-0:/tmp/import.sql

kubectl exec -it razarion-mariadb-0 -- bash -c "mysql -u root -p<ROOT_PASSWORD> razarion < /tmp/import.sql"