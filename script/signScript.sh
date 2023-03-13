#!/bin/bash
 cd /
 cd c/repository

printf 'y\ny\n' | openssl ca -in requests/$1.req -out certs/$1.cert -config openssl.cnf -passin pass:sigurnost
touch finish.txt

#read -p "Press any key to resume ..."