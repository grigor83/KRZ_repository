#!/bin/bash
 cd /
 cd c/repository
 
# Naredba za generisanje rsa kljuca CA tijela
openssl genrsa -out private/ca-key.pem  4096
printf '\n\n\nKRZ_CA\n'$1'\n' | openssl req -new -x509 -out ca-cert.pem -passout pass:$1 -config openssl.cnf
# Odmah kreiram i fajl-crl listu povucenih sertifikata
openssl ca -gencrl -out crl.pem -config openssl.cnf -passin pass:$1
touch finish.txt

#read -p "Press any key to resume ..."