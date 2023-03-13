#!/bin/bash
 cd /
 cd c/repository

#openssl genrsa -out USERS/$1/$1.key -aes128 -passout pass:$2 2048
#printf '\n\n\n'$1'\n'$2'\n\n' | openssl req -new -key USERS/$1/$1.key -out requests/$1.req -config openssl.cnf -passin pass:$2

# druga varijanta je bez lozinke za korisnicki kljuc
openssl genrsa -out USERS/$1/$1.key 2048
printf '\n\n\n'$1'\n'$2'\n\n' | openssl req -new -key USERS/$1/$1.key -out requests/$1.req -config openssl.cnf
touch finish.txt

#read -p "Press any key to resume ..."