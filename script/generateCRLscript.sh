#!/bin/bash
 cd /
 cd c/repository
 
openssl ca -gencrl -out crl.pem -config openssl.cnf -passin pass:$1
touch finish.txt
#read -p "Press any key to resume ..."
