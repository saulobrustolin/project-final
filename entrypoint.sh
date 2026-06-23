#!/bin/bash

echo "Iniciando o notification consumer..."
java -jar consumer.jar &

echo "Iniciando a API..."
exec java -jar api.jar