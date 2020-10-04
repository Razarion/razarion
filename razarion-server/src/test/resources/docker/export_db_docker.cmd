docker-compose exec db mariadb-dump --user="root" --password="1234" --skip-add-drop-table --skip-add-locks --skip-comments --skip-disable-keys --skip-set-charset --add-drop-database --hex-blob --tab=/tmp razarion

docker cp db:/tmp ../sql/dev_local
