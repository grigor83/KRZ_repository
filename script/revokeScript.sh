#!/bin/bash
 cd /
 cd c/repository

openssl ca -revoke certs/$1.cert -crl_reason certificateHold -config openssl.cnf -passin pass:$2
openssl ca -gencrl -out crl.pem -config openssl.cnf -passin pass:$2
touch finish.txt
#read -p "Press any key to resume ..."